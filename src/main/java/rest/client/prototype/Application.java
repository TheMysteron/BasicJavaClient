package rest.client.prototype;

import rest.client.prototype.business.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Application {

    private final static String CONFIG = "config_file";
    private final static String SEPARATOR = System.getProperty("file.separator");

    private static Service service = new Service();

    public static void main(String[] args) {
        try {
            service.getResponse(new Config(getProperties()));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Gets the properties from the properties file
     *
     * @return - returns the Properties object that was read in from the properties file
     * @throws Exception if the file cannot be found or read.
     */
    private static Properties getProperties() throws Exception {
        File file;
        String configParam = System.getProperty(CONFIG);

        // Set the properties file to be used
        if (configParam == null) {
            file = new File("config" + SEPARATOR + "application.properties");
        } else {
            file = new File(configParam);
        }

        // Attempt to read the file from the directory
        FileInputStream fileInput = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        return properties;
    }
}