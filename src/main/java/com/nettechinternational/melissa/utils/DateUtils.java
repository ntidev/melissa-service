package com.nettechinternational.melissa.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class DateUtils {

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static Date toDate(String strDate) {

        Date date;
        try {
            date = DATE_FORMAT.parse(strDate);
        } catch (ParseException ex) {
            date = new Date();
        }

        return date;
    }

}
