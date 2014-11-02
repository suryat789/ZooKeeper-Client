package zk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

	private static Properties PROPS;
	
	public static String getPropertyValue(final String key){
		if(PROPS == null || PROPS.isEmpty()){
			loadProperties();
		}
		return (String)PROPS.get(key);
	}

	private static void loadProperties() {
		PROPS = new Properties();
		InputStream inputStream = null;
		File f = null;
		try {
			f = new File(Constants.CONF_FILE_PATH);
			inputStream = new FileInputStream(f);
			PROPS.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
