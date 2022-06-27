package media.platform.sftp.sftp;

import com.jcraft.jsch.ChannelSftp;
import media.platform.sftp.config.SftpConfig;
import media.platform.sftp.service.ServiceDefine;
import media.platform.sftp.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

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
    private static final int START_IDX = 6;
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String INFO_SUFFIX = ".INFO";

    private String cdrFileName;
    private String infoFileName;
    private int uploadFileCnt = 0;

    private SftpManager() {
        // nothing
    }

    public static SftpManager getInstance() {
        if (sftpManager == null)
            sftpManager = new SftpManager();
        return sftpManager;
    }

    public SFTPUtil getSftpUtil() {
        return sftpUtil;
    }

    /**
     * Initialize - set SFTPUtil
     * */
    public void init(SftpConfig config) {
        if (config == null) return;
        this.config = config;

        // Password 해독
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

    /**
     * SftpManager Main Process
     * */
    public void process(ServiceDefine mode) {
        log.info("SftpManager.process Start");

        // Config, SFTPUtil 초기화 체크
        if (config == null || sftpUtil == null || !sftpUtil.isConnected()) {
            log.error("SftpManager - Need to Initialize");
            return;
        }

        // srcDirPath -> uploadPath 로 이동
        String srcDirPath = config.getSrcDir();
        String uploadPath = config.getUploadDir();
        log.info("Local [{}] --> Target [{}@{}:{}]", srcDirPath, config.getUser(), config.getHost(), uploadPath);

        // uploadPath 체크
        if (!sftpUtil.exists(uploadPath)) {
            log.error("Check Remote Directory Path [{}@{}:{}]", config.getUser(), config.getHost(), uploadPath);
            return;
        }

        if (ServiceDefine.MODE_LIST.equals(mode)) {
            listProcess(uploadPath);
        } else {
            uploadProcess(srcDirPath, uploadPath);
        }

        // 연결 해제
        sftpUtil.disconnection();
    }

    private void listProcess(String uploadPath) {
        log.info("Check Remote Directory File List");

        List<ChannelSftp.LsEntry> fileList = sftpUtil.getFileList(uploadPath);

        // . , .. , 숨김 파일 제외 하고 출력
        int index = 1;
        for (ChannelSftp.LsEntry file : fileList) {
            if (!file.getFilename().equals(".") && !file.getFilename().equals("..")
                    && !file.getFilename().startsWith(".")) {
                log.info("[{}] {}", index, file.getFilename());
                index++;
            }
        }
    }

    private void uploadProcess(String srcDirPath, String uploadPath) {
        // srcDirPath 체크 및 내림 차순 으로 정렬된 파일 이름 리스트
        List<String> fileList = getDirFileList(srcDirPath);

        // srcDirectory 비어 있으면 return
        if (fileList.isEmpty()) {
            log.warn("{} is Empty", srcDirPath);
            return;
        }
        log.info(">>  Local directory has [{}] files.", fileList.size());


        // 파일 조회
        int index = 1;
        for (String targetFile : fileList) {
            // 날짜로 파일 필터링
            if (checkValid(targetFile, index)) {
                // CDR, INFO 파일 이름 변수에 세팅
                if (isInfoFile(targetFile)) {
                    infoFileName = targetFile;
                    log.info("=>>  Found INFO File : {}", infoFileName);
                } else {
                    cdrFileName = targetFile;
                    log.info("=>>  Found CDR File : {}", cdrFileName);
                }
            }
            index++;

            // CDR, INFO 모두 찾으면 조회 종료
            if (cdrFileName != null && infoFileName != null) {
                break;
            }
        }

        // CDR, INFO 파일 전송
        uploadFiles(srcDirPath, uploadPath);
        log.info(">>  SFTPManager Total [{}] Files Uploaded", uploadFileCnt);
    }

    /**
     * srcDirPath 의 로컬 파일 리스트 조회
     *
     * @param srcDirPath 로컬 파일 경로
     */
    private List<String> getDirFileList(String srcDirPath) {
        // srcDirPath 체크
        File srcDir = new File(srcDirPath);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            log.error("Check Local Directory Path [{}]", srcDirPath);
            return new ArrayList<>();
        }

        // srcDirPath 에 존재 하는 파일 이름 리스트 내림차순 정렬
        List<String> fileList = Arrays.asList(Objects.requireNonNull(srcDir.list()));
        fileList.sort(Collections.reverseOrder());
        return fileList;
    }

    /**
     * 어제 날짜의 파일 인지 확인
     *
     * @param fileName 파일 이름
     * @param index 파일 순서
     */
    private boolean checkValid(String fileName, int index) {
        if (fileName.length() < START_IDX + DATE_FORMAT.length()) {
            log.debug("[{}] {} : checkValid FAIL", index, fileName);
            return false;
        }

        // 어제 날짜와 파일의 날짜 비교
        String yesterday = CalUtil.calDate(-1);
        String fileDate = fileName.substring(START_IDX, START_IDX + DATE_FORMAT.length());

        boolean result = yesterday.equals(fileDate);
        log.debug("[{}] {} : checkValid {}", index, fileName, StringUtil.getOkFail(result));
        log.debug("{}Check File Date({})", getSpace(index), fileDate);
        return result;
    }

    /**
     * CDR, INFO 파일 전송
     *
     * @param srcDirPath 로컬 파일 경로
     * @param uploadPath 업로드 경로
     */
    private void uploadFiles(String srcDirPath, String uploadPath) {
        // CDR, INFO 파일 모두 존재할 때만 업로드
        if (cdrFileName != null && infoFileName != null) {
            String cdrPath = srcDirPath + File.separator + cdrFileName;
            String infoPath = srcDirPath + File.separator + infoFileName;

            if (FileUtil.checkFile(cdrPath) && FileUtil.checkFile(infoPath)) {
                uploadFile(cdrPath, uploadPath);
                uploadFile(infoPath, uploadPath);
                return;
            }
        }

        String yesterday = CalUtil.calDate(-1);
        log.warn("Check [{}] CDR or INFO File", yesterday);
    }

    /**
     * 파일 전송
     *
     * @param filePath 로컬 파일 경로/이름
     * @param uploadPath 업로드 경로
     */
    private void uploadFile(String filePath, String uploadPath) {
        if (sftpUtil == null) {
            log.warn("SftpManager.uploadFile SftpUtil is Null");
            return;
        }

        File uploadFile = new File(filePath);
        boolean result = sftpUtil.upload(uploadPath, uploadFile);
        if (result) uploadFileCnt ++;
        log.info("SFTPManager Upload Result [{}] : {}", filePath, result);
    }

    /**
     * CDR INFO 파일 이름 확인
     *
     * @param fileName 파일 이름
     */
    private boolean isInfoFile(String fileName) {
        return fileName.contains(INFO_SUFFIX);
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
}
