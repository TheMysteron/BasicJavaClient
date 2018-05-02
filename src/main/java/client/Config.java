package client;

import javax.xml.bind.DatatypeConverter;
import java.util.Properties;

public class Config {

    public static final String KERBEROS = "KERBEROS";
    public static final String BASIC = "BASIC";
    public static final String POST = "POST";
    public static final String GET = "GET";

    private String url;
    private String username;
    private String password;
    private String requestType;
    private String authType;
    private String keytab;
    private String principal;

    Config(Properties properties) {
        // Assign the config Variables
        authType = properties.getProperty("authentication");
        requestType = properties.getProperty("http.request");

        if (authType.equalsIgnoreCase(KERBEROS)) {
            keytab = properties.getProperty("kerberos.keytab");
            principal = properties.getProperty("kerberos.principal");
        } else if (authType.equalsIgnoreCase(BASIC)) {
            username = properties.getProperty("basic.username");
            byte[] decoded = DatatypeConverter.parseBase64Binary(properties.getProperty("basic.password"));
            password = new String(decoded);
        }

        if (properties.getProperty("http.ssl", "no").equalsIgnoreCase("yes")) {
            byte[] decodedTrustStorePassword = DatatypeConverter
                    .parseBase64Binary(properties.getProperty("javax.net.ssl.trustStorePassword"));
            String trustStorePassword = new String(decodedTrustStorePassword);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            System.setProperty("javax.net.ssl.trustStoreType", properties.getProperty("javax.net.ssl.trustStoreType"));
            System.setProperty("javax.net.ssl.trustStore", properties.getProperty("javax.net.ssl.trustStore"));

            url = String.format("https://%s:%s%s", properties.getProperty("url.ip"), properties.getProperty("url.port"),
                    properties.getProperty("url.uri"));
        } else {
            url = String.format("http://%s:%s%s", properties.getProperty("url.ip"), properties.getProperty("url.port"),
                    properties.getProperty("url.uri"));
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getAuthType() {
        return authType;
    }

    public String getKeytab() {
        return keytab;
    }

    public String getPrincipal() {
        return principal;
    }
}