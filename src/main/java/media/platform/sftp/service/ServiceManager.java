package media.platform.sftp.service;

import media.platform.sftp.config.SftpConfig;
import media.platform.sftp.sftp.SftpManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dajin kim
 */
public class ServiceManager {
    static final Logger log = LoggerFactory.getLogger(ServiceManager.class);
    private static ServiceManager serviceManager = null;
    private final AppInstance instance = AppInstance.getInstance();

    /**
     * Reads a config file in the constructor
     */
    private ServiceManager() {
        instance.setConfig(new SftpConfig(instance.getConfigPath()));
    }

    public static ServiceManager getInstance() {
        if (serviceManager == null) {
            serviceManager = new ServiceManager();
        }

        return serviceManager;
    }

    public void process() {
        this.startService();
        stopService();
    }

    /**
     * Start Service
     */
    public void startService() {
        log.info("Start Service");
        SftpManager sftpManager = SftpManager.getInstance();
        sftpManager.init(instance.getConfig());
        sftpManager.process();
    }

    /**
     * Finalizes all the resources
     */
    private void stopService() {
        log.info("Stop Service");
    }
}
