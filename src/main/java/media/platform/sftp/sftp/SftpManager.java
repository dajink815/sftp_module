package media.platform.sftp.sftp;

import com.jcraft.jsch.ChannelSftp;
import media.platform.sftp.config.SftpConfig;
import media.platform.sftp.service.ServiceDefine;
import media.platform.sftp.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

        // 1. Config, SFTPUtil 초기화 체크
        if (config == null || sftpUtil == null || !sftpUtil.isConnected()) {
            log.error("SftpManager - Need to Initialize");
            return;
        }

        // 2. 로컬 디렉토리 경로, 업로드 경로
        String srcDirPath = config.getSrcDir();
        String uploadPath = config.getUploadDir();
        // srcDirPath -> uploadPath 로 이동
        log.info("Local [{}] --> Target [{}@{}:{}]", srcDirPath, config.getUser(), config.getHost(), uploadPath);

        // 3. uploadPath 체크
        if (!sftpUtil.exists(uploadPath)) {
            log.error("Check Remote Directory Path [{}@{}:{}]", config.getUser(), config.getHost(), uploadPath);
            return;
        }

        // 4. 모드 별 동작
        if (ServiceDefine.MODE_LIST.equals(mode)) {
            listProcess(uploadPath);
        } else {
            uploadProcess(srcDirPath, uploadPath);
        }

        // 5. 연결 해제
        sftpUtil.disconnection();
    }

    /**
     * LIST MODE - Print Remote Directory File List
     * */
    private void listProcess(String uploadPath) {
        log.info("Check Remote Directory File List");

        List<ChannelSftp.LsEntry> fileList = sftpUtil.getFileList(uploadPath);

        // '.' , '..' , 숨김 파일 제외 하고 출력
        int index = 1;
        for (ChannelSftp.LsEntry file : fileList) {
            if (!file.getFilename().equals(".") && !file.getFilename().equals("..")
                    && !file.getFilename().startsWith(".")) {
                log.info("[{}] {}", index, file.getFilename());
                index++;
            }
        }
    }

    /**
     * UPLOAD MODE - Upload Filtered File
     * */
    private void uploadProcess(String srcDirPath, String uploadPath) {
        // 1. srcDirPath 체크 및 필터링 된 파일 이름 리스트
        String filterValue = config.getFilterValue();
        List<String> fileList = getDirFileList(srcDirPath, filterValue);

        // 2. srcDirectory 비어 있으면 return
        if (fileList.isEmpty()) {
            log.warn(">> {} [{}] File Empty", srcDirPath, filterValue);
            return;
        }

        log.info(">>  Local directory has [{}] files.", getDirFileNum(srcDirPath));
        log.info(">>  [{}] file num : {}", filterValue, fileList.size());
        log.debug("Filtered File List: {}", fileList);

        // 3. 확장자 명으로 필터링
        List<String> extsList = config.getFilterExtsList();
        log.debug("Extension List: {}", extsList);

        List<String> foundFile = fileList.stream()
                .filter(fileName -> FileUtil.checkExtensions(fileName, extsList))
                .collect(Collectors.toList());
        log.debug("FoundFile : {}", foundFile);

        // 4. 파일 전송
        for (String file : foundFile) {
            String filePath = srcDirPath + File.separator + file;
            if (FileUtil.checkFile(filePath)) {
                uploadFile(filePath, uploadPath);
            } else {
                log.info("File Doesnt Exist, Cannot Send [{}]", filePath);
            }
        }

        // 5. 파일 전송 결과 출력
        log.info(">>  SFTPManager Upload Total [{}] Files.", uploadFileCnt);
    }

    /**
     * 디렉토리의 파일 리스트 필터링 해서 조회
     * @param dirPath 디렉토리 경로
     * @param filterValue 파일 필터링 값
     */
    private List<String> getDirFileList(String dirPath, String filterValue) {
        // dirPath 체크
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            log.error("Check Local Directory Path [{}]", dirPath);
            return new ArrayList<>();
        }

        // dirPath 에 존재 하는 파일 중 filterValue 포함 하는 파일만 get
        String[] arrFileList = dir.list(FileUtil.getFilenameFilter(filterValue));
        return Arrays.asList(Objects.requireNonNull(arrFileList));
    }

    /**
     * 파일 전송
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
     * 디렉토리의 파일 개수
     * @param dirPath 디렉토리 경로
     */
    private int getDirFileNum(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        List<String> fileList = Arrays.asList(Objects.requireNonNull(dir.list()));
        return fileList.size();
    }
}
