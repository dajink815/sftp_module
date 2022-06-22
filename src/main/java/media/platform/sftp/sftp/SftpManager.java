package media.platform.sftp.sftp;

import media.platform.sftp.config.SftpConfig;
import media.platform.sftp.util.PasswdDecryptor;
import media.platform.sftp.util.SFTPUtil;
import media.platform.sftp.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author dajin kim
 */
public class SftpManager {
    static final Logger log = LoggerFactory.getLogger(SftpManager.class);
    private static SftpManager sftpManager = null;
    private SFTPUtil sftpUtil;
    private SftpConfig config;

    public static final String KEY = "SFTP";
    public static final String ALGORITHM = "PBEWITHMD5ANDDES";

    private SftpManager() {
        // nothing
    }

    public static SftpManager getInstance() {
        if (sftpManager == null)
            sftpManager = new SftpManager();
        return sftpManager;
    }

    public void init(SftpConfig config) {
        if (config == null) return;
        this.config = config;

        PasswdDecryptor decryptor = new PasswdDecryptor(KEY, ALGORITHM);
        String decPass = "";

        if (StringUtil.notNull(config.getPass())) {
            try {
                decPass = decryptor.decrypt0(config.getPass());
            } catch (Exception e) {
                log.error("SftpManager Password is not available [{}]", config.getPass());
                decPass = null;
            }
        }

        sftpUtil = new SFTPUtil(config.getHost(), config.getUser(), decPass, config.getPort(), config.getPrivateKey());
        sftpUtil.init();

        if (sftpUtil.isConnected())
            log.info("SftpManager.init Success");
        else
            log.error("SftpManager.init Fail");
    }

    public void process() {
        log.info("SftpManager.process Start");

        // SFTPUtil 초기화 체크
        if (sftpUtil == null || !sftpUtil.isConnected()) {
            log.error("Need to Initialize SFTPUtil");
            return;
        }

        // srcDirPath 하위 파일들 -> uploadPath 로 이동
        String srcDirPath = config.getSrcDir();
        String uploadPath = config.getUploadDir();
        log.info("Local [{}] --> Target [{}@{}:{}]", srcDirPath, config.getUser(), config.getHost(), uploadPath);

        // srcDirPath Directory 파일 이름 리스트
        String[] fileNames = getDirFileList(srcDirPath, uploadPath);
        if (fileNames.length <= 0) {
            return;
        }
        log.info("Local directory has [{}] files.", fileNames.length);

        // 업로드 된 총 파일 개수
        int uploadFileCnt = 0;

        int index = 1;
        for (String targetFile : fileNames) {
            log.debug("[{}] {}", index, targetFile);

            String space = getSpace(index);
            index++;

            // 파일 필터링
            if (!checkValid(targetFile)) {
                log.debug("{}Check Valid Fail : {}", space, targetFile);
                continue;
            }

            File uploadFile = new File(srcDirPath + File.separator + targetFile);
            if (uploadFile.isFile()) {
                // 업로드 결과
                boolean result = sftpUtil.upload(uploadPath, uploadFile);
                log.info("{}SFTPManager Upload Result [{}] : {}", space, targetFile, result);
                if (result) uploadFileCnt++;
            } else {
                log.debug("{}SFTPManager [{}] is Directory, Cannot Upload", space, targetFile);
            }
        }

        log.info(">>  SFTPManager Total [{}] Files Uploaded", uploadFileCnt);

        // 연결 해제
        sftpUtil.disconnection();
    }

    private boolean checkValid(String fileName) {
        try {
            String suffix = config.getTargetFileSuffix();
            int suffixLength = suffix.length();
            int index = fileName.length() - suffixLength;
            if (index < 0) return false;

            String fileSuffix = fileName.substring(index);
            return suffix.equalsIgnoreCase(fileSuffix);
        } catch (Exception e) {
            log.error("SftpManager.checkValid.Exception ", e);
        }
        return false;
    }

    public SFTPUtil getSftpUtil() {
        return sftpUtil;
    }

    private String getSpace(int index) {
        String space;
        if (index < 10)
            space = "    ";
        else if (index < 100)
            space = "     ";
        else
            space = "      ";
        return space;
    }

    /**
     * srcDirPath 의 로컬 파일 리스트 조회
     *
     * @param srcDirPath 로컬 파일 경로
     * @param uploadPath 업로드 경로
     */
    private String[] getDirFileList(String srcDirPath, String uploadPath) {
        // srcDirPath 체크
        File srcDir = new File(srcDirPath);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            log.error("Check Local Directory Path [{}]", srcDirPath);
            return new String[0];
        }

        // uploadPath 체크
        if (!sftpUtil.exists(uploadPath)) {
            log.error("Check Remote Directory Path [{}@{}:{}]", config.getUser(), config.getHost(), uploadPath);
            return new String[0];
        }

        // srcDirPath 에 존재 하는 파일 이름 리스트
        String[] fileNames = srcDir.list();
        if (fileNames == null || fileNames.length <= 0) {
            log.warn("{} is Empty", srcDirPath);
            return new String[0];
        }

        return fileNames;
    }
}
