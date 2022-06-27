package media.platform.sftp;

import media.platform.sftp.service.AppInstance;
import media.platform.sftp.service.ServiceDefine;
import media.platform.sftp.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dajin kim
 */
public class SftpMain {
    static final Logger log = LoggerFactory.getLogger(SftpMain.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("SFTP Module Arguments Error");
            return;
        }

        if (ServiceDefine.MODE_KEY.getStr().equalsIgnoreCase(args[0])) {
            String pass = args.length > 1? args[1] : "";
            ServiceManager.generateKey(pass);
            return;
        }

        log.info("SFTP Process Start {} ", args[0]);

        AppInstance instance = AppInstance.getInstance();
        instance.setConfigPath(args[0]);

        String strMode = args.length > 1? args[1] : "upload";
        ServiceDefine mode = ServiceDefine.getTypeEnum(strMode);

        ServiceManager serviceManager = ServiceManager.getInstance();
        serviceManager.process(mode);
    }
}
