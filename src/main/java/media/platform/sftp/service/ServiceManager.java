package media.platform.sftp.service;

import media.platform.sftp.config.SftpConfig;
import media.platform.sftp.sftp.SftpManager;
import media.platform.sftp.util.PasswdDecryptor;
import media.platform.sftp.util.StringUtil;
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

    public void process(ServiceDefine mode) {
        startService(mode);
        stopService();
    }

    /**
     * Start Service
     */
    private void startService(ServiceDefine mode) {
        log.info("Start Service ({} mode)", mode.getStr());
        SftpManager sftpManager = SftpManager.getInstance();
        sftpManager.init(instance.getConfig());
        sftpManager.process(mode);
    }

    /**
     * Finalizes all the resources
     */
    private void stopService() {
        log.info("Stop Service");
    }

    public static void generateKey(String userPw) {
        if (StringUtil.isNull(userPw)) {
            System.err.println("SFTP Key Generator Arguments Error");
            System.err.println("Please Enter User Password.");
            return;
        }

        System.out.println("SFTP Key Generator Start... ");
        PasswdDecryptor decryptor = new PasswdDecryptor(SftpManager.KEY, SftpManager.ALGORITHM);
        String encryptedPw = decryptor.encrypt(userPw);
        System.out.println("SFTP Key => " + encryptedPw);
    }
}
