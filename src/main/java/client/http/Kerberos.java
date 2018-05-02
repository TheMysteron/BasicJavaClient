package client.http;

import client.Config;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Kerberos {

    private static String url;
    private static String principal;
    private static String keyTabLocation;
    private static Configuration config;
    private static Subject sub;

    /**
     * Sets the common variables and calls the sendGet or sendPost depending on the config received
     *
     * @param conf - Allows this to determine which request type to use, and set the variables
     * @return - Returns the HTTP Response
     * @throws Exception if http call fails or config is set wrong
     */
    public HttpResponse run(Config conf) throws Exception {
        url = conf.getUrl();
        principal = conf.getPrincipal();
        keyTabLocation = conf.getKeytab();

        config = new Configuration() {
            @SuppressWarnings("serial")
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[]{new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>() {
                    {
                        put("useTicketCache", "false");
                        put("useKeyTab", "true");
                        put("keyTab", keyTabLocation);
                        //Krb5 in GSS API needs to be refreshed so it does not throw the error
                        //Specified version of key is not available
                        put("refreshKrb5Config", "true");
                        put("principal", principal);
                        put("storeKey", "true");
                        put("doNotPrompt", "true");
                        put("isInitiator", "true");
                        put("debug", "true");
                    }
                })};
            }
        };
        Set<Principal> princ = new HashSet<>(1);
        princ.add(new KerberosPrincipal(principal));
        sub = new Subject(false, princ, new HashSet<>(), new HashSet<>());
        if (conf.getRequestType().equalsIgnoreCase(Config.GET)) {
            return sendGet();
        } else if (conf.getRequestType().equalsIgnoreCase(Config.POST)) {
            return sendPost();
        } else {
            throw new Exception("Currently only GET and POST request types are supported, amend the properties file");
        }
    }

    /**
     * Sends a HTTP Get request to the URL using Kerberos Auth
     *
     * @return - Returns the HTTP Response
     * @throws Exception if http call fails
     */
    private static HttpResponse sendGet() throws Exception {
        LoginContext lc = new LoginContext("", sub, null, config);
        lc.login();
        Subject serviceSubject = lc.getSubject();

        System.out.println(serviceSubject.getPrivateCredentials().iterator().next().toString());
        return Subject.doAs(serviceSubject, new PrivilegedAction<HttpResponse>() {
            @Override
            public HttpResponse run() {
                HttpUriRequest request = new HttpGet(url);
                HttpClient spnegoHttpClient = buildSpengoHttpClient();
                try {
                    return spnegoHttpClient.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * Sends a HTTP Post request to the URL using Kerberos Auth
     *
     * @return - Returns the HTTP Response
     * @throws Exception if http call fails
     */
    private static HttpResponse sendPost() throws Exception {
        LoginContext lc = new LoginContext("", sub, null, config);
        lc.login();
        Subject serviceSubject = lc.getSubject();
        return Subject.doAs(serviceSubject, new PrivilegedAction<HttpResponse>() {
            @Override
            public HttpResponse run() {
                HttpUriRequest request = new HttpPost(url);
                HttpClient spnegoHttpClient = buildSpengoHttpClient();
                try {
                    return spnegoHttpClient.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * Builds the SPENGO client used in the Kerberos Auth.
     *
     * @return - returns the HTTP Client that uses SPENGO
     */
    private static HttpClient buildSpengoHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().
                register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
        builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(null, -1, null), new Credentials() {
            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }
        });
        return builder.setDefaultCredentialsProvider(credentialsProvider).build();
    }
}