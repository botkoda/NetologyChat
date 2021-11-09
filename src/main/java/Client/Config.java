package Client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String PROPERTIES_FILE = "src/main/resources/config.properties";
    public static String HOST;
    public static int PORT;


    static {
        Properties properties = new Properties();
        FileInputStream propertiesFile = null;
        try {
            propertiesFile = new FileInputStream(PROPERTIES_FILE);
            properties.load(propertiesFile);
            HOST = properties.getProperty("HOST");
            PORT = Integer.parseInt(properties.getProperty("PORT"));
        } catch (FileNotFoundException e) {
            System.err.println("Файл настроек не найден");
        } catch (IOException e) {
            System.err.println("Файл не читается");
        } finally {
            try {
                propertiesFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
