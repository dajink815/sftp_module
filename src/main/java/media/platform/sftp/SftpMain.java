package media.platform.sftp;

import media.platform.sftp.service.AppInstance;
import media.platform.sftp.service.ServiceManager;
import media.platform.sftp.sftp.SftpManager;
import media.platform.sftp.util.PasswdDecryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dajin kim
 */
public class SftpMain {
    static final Logger log = LoggerFactory.getLogger(SftpMain.class);

    private static final String MODE_KEY = "key";

    public static void main(String[] args) {
        if (MODE_KEY.equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                System.err.println("SFTP Key Generator Arguments Error");
                return;
            }

            System.out.println("SFTP Key Generator Start... ");
            PasswdDecryptor decryptor = new PasswdDecryptor(SftpManager.KEY, SftpManager.ALGORITHM);
            String pw = decryptor.encrypt(args[1]);
            System.out.println("SFTP Key => " + pw);
            return;
        }

        log.info("SFTP Process Start {} ", args[0]);

        AppInstance instance = AppInstance.getInstance();
        instance.setConfigPath(args[0]);

        ServiceManager serviceManager = ServiceManager.getInstance();
        serviceManager.process();
    }
}
