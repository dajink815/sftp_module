package media.platform.sftp.config;

/**
 * @author dajin kim
 */
public class SftpConfigInfo {
    // SFTP
    protected String host;
    protected String user;
    protected int port;
    protected String pass;
    protected String privateKey;

    protected String srcDir;
    protected String uploadDir;

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
                '}';
    }
}
