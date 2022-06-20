package media.platform.sftp.service;

import media.platform.sftp.config.SftpConfig;

/**
 * @author dajin kim
 */
public class AppInstance {
    private static AppInstance instance = null;

    private SftpConfig config = null;
    private String configPath = null;

    private AppInstance() {
        // nothing
    }

    public static AppInstance getInstance() {
        if (instance == null) {
            instance = new AppInstance();
        }

        return instance;
    }

    public SftpConfig getConfig() {
        return config;
    }
    public void setConfig(SftpConfig config) {
        this.config = config;
    }

    public String getConfigPath() {
        return configPath;
    }
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
