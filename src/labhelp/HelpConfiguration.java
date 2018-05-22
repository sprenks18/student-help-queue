/**
 * 
 */
package labhelp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Read in the properties from a file
 * 
 * @author sprenkle
 * 
 */
public class HelpConfiguration {

	static Properties helpProp = new Properties();
	static Properties hostMap = new Properties();

	public static final String SERVER_NAME;
	public static final int SERVER_PORT;

	static {
		try {
			helpProp.load(HelpConfiguration.class
					.getResourceAsStream("configuration.prop"));
			hostMap.load(HelpConfiguration.class.getResourceAsStream("hostmap.prop"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SERVER_NAME = helpProp.getProperty("server.host");
		SERVER_PORT = Integer.parseInt(helpProp.getProperty("server.port"));

	}

}
