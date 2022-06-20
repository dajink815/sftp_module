/*
 * Copyright (C) 2018. Uangel Corp. All rights reserved.
 *
 */

package media.platform.sftp.util;

/**
 * @author dajin kim
 */
public class StringUtil {
    private static final String STR_OK = "OK";
    private static final String STR_FAIL = "FAIL";
    private static final String TRUE = "TRUE";

    private StringUtil() {
        // Do Nothing
    }

    public static String getOkFail(boolean result) {
        return (result ? STR_OK : STR_FAIL);
    }

    public static boolean checkTrue(String str) {
        return TRUE.equalsIgnoreCase(str);
    }


    public static String blankIfNull(String str) {
        return str == null ? "" : str;
    }

    public static String removeLine(String str) {
        return str.replaceAll("(\r\n|\r|\n|\n\r)", "").trim();
    }

    public static boolean isNull(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean notNull(String str) {
        return str != null && !str.isEmpty();
    }


}
