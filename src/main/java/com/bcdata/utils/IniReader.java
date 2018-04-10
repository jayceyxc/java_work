package com.bcdata.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James Fancy
 */
public class IniReader {
	private static final String CONFIG_FILE_PATH = "conf";
	private static final String CONFIG_FILE_NAME = "config.properties";
	public static final String CONFIG_FILE = System.getProperty("user.dir") + System.getProperty("file.separator")
			+ CONFIG_FILE_PATH + System.getProperty("file.separator") + CONFIG_FILE_NAME;
	private static final Log log = LogFactory.getLog(IniReader.class);

	protected HashMap<String, Properties> sections = new HashMap<String, Properties>();
	private transient String currentSecion = null;
	private transient Properties current = null;

	private static final Pattern sectionPattern = Pattern.compile("\\[.*\\]");
	private static final Pattern propertyPattern = Pattern.compile(".*=.*");

	private static HashMap<String, IniReader> readerMap = new HashMap<String, IniReader>();

	public static final String ES_SECTION = "es";
    public static final String MYSQL_SECTION = "mysql";

    public static final String ES_HOST_PROP = "es_host";
    public static final String ES_PORT_PROP = "es_port";
    public static final String ES_USER_PROP = "es_user";
    public static final String ES_PASSWORD_PROP = "es_password";

    public static final String MYSQL_HOST_PROP = "mysql_host";
    public static final String MYSQL_PORT_PROP = "mysql_port";
    public static final String MYSQL_USER_PROP = "mysql_user";
    public static final String MYSQL_PASSWORD_PROP = "mysql_password";
    public static final String MYSQL_RMC_DB_PROP = "mysql_rmc_db";
    public static final String MYSQL_AD_DB_PROP = "mysql_ad_db";


	private IniReader (String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		read(reader);
		reader.close();
	}

	public static IniReader getInstance(String filename) {
		if (readerMap.containsKey(filename)) {
			return readerMap.get(filename);
		}
		try {
			IniReader reader = new IniReader(filename);
			readerMap.put(filename, reader);
			return reader;
		} catch (IOException ioe) {
			log.error("read the config file  " + filename + " failed.", ioe);
		}
		
		return null;
	}

	protected void read(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			parseLine(line);
		}
		if (currentSecion != null && current != null) {
			sections.put(currentSecion, current);
		}
	}

	protected void parseLine(String line) {
		line = line.trim();
		if (line.startsWith("#") || line.startsWith(";")) {
			// comment line
			return;
		}
		if (sectionPattern.matcher(line).matches()) {
			if (current != null) {
				sections.put(currentSecion, current);
			}
			currentSecion = line.replaceFirst("\\[(.*)\\]", "$1");
			current = new Properties();
		} else if (propertyPattern.matcher(line).matches()) {
			int i = line.indexOf('=');
			String name = line.substring(0, i);
			String value = line.substring(i + 1);
			current.setProperty(name, value);
		}
	}

	public String getValue(String section, String name) {
		Properties p = (Properties) sections.get(section);
		if (p == null) {
			return null;
		}
		String value = p.getProperty(name);
		return value;
	}
	
	public int getIntValue(String section, String name) {
		Properties p = (Properties) sections.get(section);
		if (p == null) {
			return -1;
		}
		String value = p.getProperty(name);
		return Integer.parseInt(value);
	}
	
	public double getDoubleValue(String section, String name) {
		Properties p = (Properties) sections.get(section);
		if (p == null) {
			return -1;
		}
		String value = p.getProperty(name);
		return Double.parseDouble(value);
	}

	public static void main(String[] args) {
		String line = "[system]";
		Matcher matcher = sectionPattern.matcher(line);
		if (matcher.matches()) {
			String section = matcher.group().trim().substring(1, line.length() - 1);
			System.out.println(section);
			System.out.println(line.replaceFirst("\\[(.*)\\]", "$1"));
		}

		IniReader reader = IniReader.getInstance(CONFIG_FILE);
		for (String section : reader.sections.keySet()) {
			System.out.println("section name: " + section);
			Properties prop = reader.sections.get(section);
			prop.list(System.out);
		}
		System.out.println(reader.getValue("system", "expire_time"));
		System.out.println(reader.getValue("bid", "pushid"));
		System.out.println(reader.getValue("bid", "adid"));
	}
}
