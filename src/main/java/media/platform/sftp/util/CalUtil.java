package media.platform.sftp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author dajin kim
 */
public class CalUtil {

    private CalUtil() {
        // nothing
    }

    /**
     * 날짜 계산
     * @param addDay 더하거나 빼려는 일의 수
     * */
    public static String calDate(int addDay) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, addDay);
        return new SimpleDateFormat("yyyyMMdd").format(c.getTime());
    }

    public static int getMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public static int getSecond() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    public static int getMilli() {
        return Calendar.getInstance().get(Calendar.MILLISECOND);
    }

}
