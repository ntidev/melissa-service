package com.nettechinternational.melissa.utils;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class HttpUtils {

    public static String getToken(String queryParams) {

        if (queryParams == null || queryParams.isEmpty()) {
            return null;
        }

        String params[] = queryParams.split("&");

        for (String p : params) {
            String kv[] = p.split("=");
            if (kv.length >= 2 && "token".equals(kv[0])) {
                return kv[1];
            }

        }
        return null;
    }

    public static JsonObject params(String queryParams) {

        if (queryParams == null || queryParams.isEmpty()) {
            return null;
        }
        JsonObject param = new JsonObject();
        String params[] = queryParams.split("&");

        for (String p : params) {
            String kv[] = p.split("=");
            if (kv.length >= 2) {
                param.put(kv[0], kv[1]);
            }

        }
        return param;
    }
}
