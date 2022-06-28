package media.platform.sftp.config;

import media.platform.sftp.util.CalUtil;
import media.platform.sftp.util.StringUtil;

import java.util.List;

/**
 * @author dajin kim
 */
public class SftpConfigInfo {
    // COMMON
    protected String host;
    protected String user;
    protected int port;
    protected String pass;
    protected String privateKey;

    protected String srcDir;
    protected String uploadDir;

    protected String filterValue;
    protected List<String> filterExtsList;

    protected SftpConfigInfo() {
        // nothing
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public int getPort() {
        return port;
    }

    public String getPass() {
        return pass;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public String getFilterValue() {
        if (StringUtil.isNull(filterValue)) return CalUtil.calDate(-1);
        return filterValue;
    }

    public List<String> getFilterExtsList() {
        return filterExtsList;
    }

    @Override
    public String toString() {
        return "SftpConfigInfo{" +
                "host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", port=" + port +
                ", pass='" + pass + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", srcDir='" + srcDir + '\'' +
                ", uploadDir='" + uploadDir + '\'' +
                ", filterValue='" + filterValue + '\'' +
                ", filterExtsList=" + filterExtsList +
                '}';
    }
}
