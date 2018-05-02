package client.http;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import client.Config;

import java.nio.charset.StandardCharsets;

public class Basic {

    private static String url;
    private static UsernamePasswordCredentials creds;
    private static HttpClient httpClient;

    /**
     * Sets the common variables and calls the sendGet or sendPost depending on the config received
     *
     * @param conf - Allows this to determine which request type to use, and set the variables
     * @return - Returns the HTTP Response
     * @throws Exception if http call fails or config is set wrong
     */
    public HttpResponse run(Config conf) throws Exception {
        url = conf.getUrl();
        // Set the Credentials
        creds = new UsernamePasswordCredentials(conf.getUsername(), conf.getPassword());
        // Create the HTTP client that will be used
        httpClient = HttpClientBuilder.create().build();

        if (conf.getRequestType().equalsIgnoreCase(Config.GET)) {
            return sendGet();
        } else if (conf.getRequestType().equalsIgnoreCase(Config.POST)) {
            return sendPost();
        } else {
            throw new Exception("Currently only GET and POST request types are supported, amend the properties file");
        }
    }

    /**
     * Sends a HTTP Get request to the URL using Basic Auth
     *
     * @return - Returns the HTTP Response
     * @throws Exception if http call fails
     */
    private static HttpResponse sendGet() throws Exception {
        // Create the Call using the URL
        HttpGet http = new HttpGet(url);
        // Set the credentials set earlier into the headers
        Header header = new BasicScheme(StandardCharsets.UTF_8).authenticate(creds, http, null);
        // Set the header into the HTTP request
        http.addHeader(header);
        // Print the response
        return httpClient.execute(http);
    }

    /**
     * Sends a HTTP Post request to the URL using Basic Auth
     *
     * @return - Returns the HTTP Response
     * @throws Exception if http call fails
     */
    private static HttpResponse sendPost() throws Exception {
        // Create the Call using the URL
        HttpPost http = new HttpPost(url);
        // Set the credentials set earlier into the headers
        Header header = new BasicScheme(StandardCharsets.UTF_8).authenticate(creds, http, null);
        // Set the header into the HTTP request
        http.addHeader(header);
        // Print the response
        return httpClient.execute(http);
    }
}