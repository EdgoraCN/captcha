package com.edgora.ms.captcha.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    
	private static Properties properties =null;
	private static Logger LOG =LoggerFactory.getLogger(Config.class);
	private static final String REG_EL = "\\$\\{(.+?)\\}"; //
	private static final Pattern PATTERN = Pattern.compile(REG_EL);
	private static final String[] locations = {"file:/app/application.properties","file:${user.home}/.app/application.properties","classPath:/application.properties","classPath:application.properties"};
    private static String filePath=null;
    private static Long lastModified=0L;
    /**
     * not allowed to new
     */
    private Config(){

    }
    /**
     * load files
     */
	public static void load(){
		Properties p = System.getProperties();
		for(Object key:p.keySet()){
			LOG.debug("Property:"+key+"="+p.getProperty(key.toString()));
		}
		File[] configFiles = new File[locations.length];
		int i =0 ;
		for(String location:locations) {
			location = apply(location);
			String path = getPath(location);
			File configFile = new File(path);
			if(configFile.exists()) {
				LOG.debug("config file found:"+path);
				filePath = path;
				lastModified = configFile.lastModified();
				configFiles[i]=configFile;
			}else {
				configFiles[i]=null;
				LOG.debug("config file not exists:"+path);
			}
			i++;
		}
		properties = new Properties();
		for(int j=locations.length-1;j>-1;j--) {
			FileInputStream is = null;
			try {
				File configFile =configFiles[j];
				if(configFile==null){
					LOG.warn("skip config file:");
					continue;
				}
				is = new FileInputStream(configFile);
				properties.load(is);
				is.close();
				LOG.info("Config file loaded:"+configFile.getAbsolutePath());
			} catch (FileNotFoundException e) {
				LOG.error("config file not found "+e.getMessage(), e);
			} catch (IOException e) {
				LOG.error("read config file failed: " +e.getMessage(), e);
			}
		}
		if(properties.isEmpty()) {
			InputStream inputStream =null;
			try {
				inputStream =Config.class.getClassLoader().getResourceAsStream("application.properties");
				properties.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException e) {
				LOG.error("Load config file failed:", e);
			} catch (IOException e) {
				LOG.error("Load config file failed:", e);
			}
		}
    }
    static{
        load();
    }
    /**
     * get list value
     */
	public static List<String> getList(String key,String defaultValue){
		String value = Config.getValue(key, defaultValue);
		List<String> values = new ArrayList<String>();
		if(isBlank(value)){
			return values;
		}else{
			String[] vs = value.trim().split(",");
			if(vs!=null){
				for(String v:vs){
					values.add(v);
				}
			}
			return values;
		}
    }
    private static String snakeCaseFormat(String name) {
        final StringBuilder result = new StringBuilder();
    
        boolean lastUppercase = false;
    
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            char lastEntry = i == 0 ? 'X' : result.charAt(result.length() - 1);
            if (ch == ' ' || ch == '_' || ch == '-' || ch == '.') {
                lastUppercase = false;
    
                if (lastEntry == '_') {
                    continue;
                } else {
                    ch = '_';
                }
            } else if (Character.isUpperCase(ch)) {
                ch = Character.toLowerCase(ch);
                // is start?
                if (i > 0) {
                    if (lastUppercase) {
                        // test if end of acronym
                        if (i + 1 < name.length()) {
                            char next = name.charAt(i + 1);
                            if (!Character.isUpperCase(next) && Character.isAlphabetic(next)) {
                                // end of acronym
                                if (lastEntry != '_') {
                                    result.append('_');
                                }
                            }
                        }
                    } else {
                        // last was lowercase, insert _
                        if (lastEntry != '_') {
                            result.append('_');
                        }
                    }
                }
                lastUppercase = true;
            } else {
                lastUppercase = false;
            }
    
            result.append(ch);
        }
        return result.toString().toUpperCase();
    }

    public static String getValueLikeSpringBoot(String key,String defaultValue){
        String value = System.getProperty(key);
        if(value==null){
            value = System.getenv(snakeCaseFormat(key));
            if(value==null&&properties!=null){
                value = properties.getProperty(key,defaultValue);
            }
        }
        return value;
        
    }
	/**
	 * get string value
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getValue(String key,String defaultValue) {
		String value = getValueLikeSpringBoot(key, defaultValue);
		if(!isBlank(value)&&value.indexOf('$')>-1) {
			value = apply(value);
		}
		return value;
    }
    /**
     * get value without default value
     */
	public static String getValue(String key) {
		return getValue(key,"");
    }
    /**
     * parse path with default value
     * @param key key
     * @param defaultValue default value
     * @return final value
     */
	public static String getPath(String key,String defaultValue) {
		String value = getValue(key,defaultValue);
		return getPath(value);
    }
    /**
     *  get path
     * @param value original value
     * @return the parsed path
     */
	public static String getPath(String value) {
		String path = "";
		if(value!=null&&value.startsWith("classPath:")) {
			path = value.replace("classPath:", "");
			URL  url =Config.class.getResource(path);
			if(url!=null){
				path = url.getPath();
			}
		}else if(value!=null&&value.startsWith("file:")) {
			path = value.replace("file:", "");
		}else {
			path = value;
		}
		try {
			path = java.net.URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("decode url failed", e);
		}
		return path;
    }
    /**
     * get profile Properties
     * @param profile name
     * @return Properties
     */
	public static Properties getProfile(String profile){
		Set<Object> keys = Config.getConfig().keySet();
		Properties properties = new Properties();
		for(Object key:keys){
			String keyStr = (String)key;
			if(keyStr.startsWith(profile+".")){
				properties.setProperty(keyStr.replaceFirst(profile+".",""),Config.getValue(keyStr,""));
			}
		}
		return properties;
	}
	/**
     * apply text with expression
	 * @param text template
	 * @return the parsed text
	 */
	public static String apply(String text) {
		Matcher m = PATTERN.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String el = m.group(1);
			Object v = null;
			v= getValue(el);
			String value = "";
			if (v == null) {
				value = "";
			} else {
				value = v.toString().replace("\\", "\\\\");
				value = value.replaceAll("\\$", "\\\\\\$");
			}
			LOG.debug("el=" + el);
			LOG.debug("v=" + value);
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	/**
	 apply text with expression
	 * @param text template
     * @param map variables
	 * @return the parsed text
	 */
	public static String apply(String text,Map<String,String> map) {
		Matcher m = PATTERN.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String el = m.group(1);
			Object v = null;
			v = map.get(el);
			if(v==null) {
				v = getValue(el,map.get(el));
			}
			String value = "";
			if (v == null) {
				value = "";
			} else {
				value = v.toString().replace("\\", "\\\\");
				value = value.replaceAll("\\$", "\\\\\\$");
			}
			LOG.debug("el=" + el);
			LOG.debug("v=" + value);
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb.toString();
    }
    /**
     * get value with key and default value
     * @param key key
     * @param defaultValue defaultValue
     * @return the value
     */
	public static boolean getValue(String key,boolean defaultValue) {
		String value = getValue(key);
		 if(isBlank(value)) {
			 return defaultValue;
		 }else {
			 return "true".equalsIgnoreCase(value.trim());
		 }
	}
	public static boolean isBlank(String value){
		if(value==null){
			return true;
		}else {
			return "".equals(value.trim());
		}
    }
    /**
	 * get Long value
	 * @param key
	 * @param defaultValue
	 * @return int value
	 */
	public static Long getValue(String key,Long defaultValue) {
        String value = getValue(key);
		Long v = defaultValue;
		if(!isBlank(value)) {
			try {
				v = Long.parseLong(value);
			}catch(NumberFormatException e){
				LOG.error("parse config item failed:"+"key="+key+" stringValue:"+v);
			}
		}
		return v;
    }
	/**
	 * get int value
	 * @param key
	 * @param defaultValue
	 * @return int value
	 */
	public static int getValue(String key,int defaultValue) {
		String value = getValue(key);
		int v = defaultValue;
		if(!isBlank(value)) {
			try {
				v = Integer.parseInt(value);
			}catch(NumberFormatException e){
				LOG.error("parse config item failed:"+"key="+key+" stringValue:"+v);
			}
		}
		return v;
    }
    /**
     * get config properties
     * @return properties
     */
	public static Properties getConfig() {
		if(properties==null){
			load();
		}
		return (Properties)properties.clone();
	}
}
