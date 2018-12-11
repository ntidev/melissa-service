package com.nettechinternational.melissa.utils;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class ConfigUtils {
    
    public static String getEnv(String key, String def) {
        String val = System.getenv(key);
        return val != null ? val : def;
    }
}
