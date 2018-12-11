package com.nettechinternational.melissa.utils;

import java.util.UUID;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class TokenGenerator {

    public static String get() {
        String toString = UUID.randomUUID().toString();
        return toString.replaceAll("-", "");
    }

}
