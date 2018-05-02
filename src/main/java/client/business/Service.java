package client.business;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import client.Config;
import client.http.Basic;
import client.http.Kerberos;

public class Service {

    private Kerberos kerberos = new Kerberos();
    private Basic basic = new Basic();

    /**
     * Run the HTTP calls from this central location
     *
     * @param config - the HTTP Response to be printed
     * @throws Exception if http call fails or if config is set wrong
     */
    public void getResponse(Config config) throws Exception {
        if (config.getAuthType().equalsIgnoreCase(Config.KERBEROS)) {
            printHttpResponse(kerberos.run(config));
        } else if (config.getAuthType().equalsIgnoreCase(Config.BASIC)) {
            printHttpResponse(basic.run(config));
        } else {
            throw new Exception("Currently only BASIC, KERBEROS and KERBEROSADVANCED authentication types are supported");
        }
    }

    /**
     * Prints a HTTP response into the logs
     *
     * @param response - the HTTP response to be printed
     * @throws Exception if the response is not set appropriately
     */
    private void printHttpResponse(HttpResponse response) throws Exception {
        String content = new String(IOUtils.toByteArray(response.getEntity().getContent()));
        System.out.println(content + response.getStatusLine().getStatusCode());
    }
}