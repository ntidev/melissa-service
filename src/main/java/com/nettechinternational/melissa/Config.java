package com.nettechinternational.melissa;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class Config {

    
    public static final String SSL_CERT_FILE_NAME = "cert.pem";
    public static final String SSL_KEY_FILE_NAME = "key.pem";
    
    // Default config env variables.
    public static final int DEFAULT_HTTP_PORT = 8095;
    public static final String DEFAULT_CONFIG_DIR = "etc";
    public static final String DEFAULT_DATABASE_URL = "jdbc:mysql://mysql/melissadb?characterEncoding=UTF-8&useSSL=false";
    public static final String DEFAULT_DATABASE_USER = "melissa";
    public static final String DEFAULT_DATABASE_PASSWORD = "melissa123";

    // config file 'config.json' keys.
    public static final String CONFIG_DATABASE_URL = "database.url";
    public static final String CONFIG_DATABASE_USER = "database.user";
    public static final String CONFIG_DATABASE_PASSWORD = "database.password";
    public static final String CONFIG_API_ROOT_TOKEN= "api.rootToken";

    // Path where all config file are store, logger, cluster.
    public static final String ENV_CONFIG_DIR = "CONFIG_DIR";
    public static final String ENV_DATABASE_USER = "DATABASE_USER";
    public static final String ENV_DATABASE_PASSWORD = "DATABASE_PASSWORD";
    public static final String ENV_DATABASE_URL = "DATABASE_URL";
    public static final String ENV_ROOT_TOKEN = "ROOT_TOKEN";

}
