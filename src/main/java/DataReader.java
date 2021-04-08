import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class DataReader {
    public static Properties readPropertiesFile() throws IOException {
        FileInputStream fis = null;
        Properties prop = null;
        //Change this to Data.properties and enter your API Key on properties
        String fileName = "Test.properties";
        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            fis.close();
        }
        return prop;
    }
    public static String getProperty(String name) throws IOException {
        Properties prop = readPropertiesFile();
        return prop.getProperty(name).trim();
    }
}
