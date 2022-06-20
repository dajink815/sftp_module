import com.jcraft.jsch.ChannelSftp;
import media.platform.sftp.config.SftpConfig;
import media.platform.sftp.sftp.SftpManager;
import media.platform.sftp.util.SFTPUtil;
import org.junit.Test;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * @author dajin kim
 */
public class SftpTest {
    final static String A2S_HOST = "192.168.7.34";
    final static String A2S_USER_NAME = "a2s";
    final static String A2S_PW = "a2s.123";
    final static int PORT = 22;
    final static String UPLOAD_PATH = "/APP/a2s";
    final static String LOCAL_DIR = "/Users/kimdajin/C-ACS/sftp_module/src/test/resources/upload/";
    final static String LOCAL_FILE = "/sftp_upload.B01";
    final static String TARGET_FILE_SUFFIX = ".B01";

    @Test
    public void test() {
        String file  = "sftp_upload.B01.INFO";
        String file2 = "sftp_upload.B01";
        String dir = "upload";

        System.out.println("Result : " + checkValid(file));
        System.out.println("Result : " + checkValid(file2));
        System.out.println("Result : " + checkValid(dir));
    }

    public static boolean checkValid(String fileName) {
        int suffixLength = TARGET_FILE_SUFFIX.length();
        int index = fileName.length() - suffixLength;

        String suffix = fileName.substring(index);
        System.out.println(fileName + " suffix : " + suffix);
        return TARGET_FILE_SUFFIX.equalsIgnoreCase(suffix);
    }

    public static String parseSuffix(String fileName) {
        int suffixLength = TARGET_FILE_SUFFIX.length();

        int index = fileName.length() - suffixLength;
        return fileName.substring(index);
    }

    @Test
    public void example() {

        final String host = "접속할 서버 아이피";
        final String userName = "접속할 아이디";
        final int port = 22;
        final String uploadPath = "업로드경로";
        final String downloadPath = "다운로드경로";
        final String privateKey = "개인키경로/파일명";

        final SFTPUtil sftpUtil = new SFTPUtil(host, userName, null, port, privateKey);

        // 업로드 시 업로드 폴더 아래에
        // 현재 날짜 년월일을 생성하고 그 아래 올리기 위한 날짜 변수
        final Date today = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        // 접속
        sftpUtil.init();

        // 업로드 테스트
        File uploadfile = new File("업로드할 경로+파일명"); // 파일 객체 생성

        String mkdirPath = sdf.format(today); //현재날짜 년월일
        sftpUtil.mkdir(uploadPath, mkdirPath); // 업로드경로에 현재날짜 년월일 폴더 생성
        boolean isUpload = sftpUtil.upload(uploadPath+mkdirPath, uploadfile); //업로드
        System.out.println("isUpload -" + isUpload); // 업로드 여부 확인

        // 다운로드 테스트
        sftpUtil.download(downloadPath, "다운로드파일명", "로컬에저장할경로+파일명");
        File downloadFile = new File("로컬에저장할경로+파일명");
        if (downloadFile.exists()) {
            System.out.println("다운로드 완료");
            System.out.println(downloadFile.getPath());
            System.out.println(downloadFile.getName());
        }
    }

    @Test
    public void uploadTest() {
        /*
        String data = FileUtil.readFile(fileName);
        System.out.println(data);*/

        // 접속
        final SFTPUtil sftpUtil = new SFTPUtil(A2S_HOST, A2S_USER_NAME, A2S_PW, PORT, null);
        sftpUtil.init();

        // 업로드 테스트
        File uploadFile = new File(LOCAL_DIR + LOCAL_FILE); // 파일 객체 생성
        boolean isUpload = sftpUtil.upload(UPLOAD_PATH, uploadFile); //업로드
        System.out.println(LOCAL_FILE + " Upload Result - " + isUpload); // 업로드 여부 확인

        // 연결 해제
        sftpUtil.disconnection();
    }

    @Test
    public void uploadDirTest() {
        // 접속
        final SFTPUtil sftpUtil = new SFTPUtil(A2S_HOST, A2S_USER_NAME, A2S_PW, PORT, null);
        sftpUtil.init();

/*        // 업로드 테스트
        File uploadFile = new File(LOCAL_DIR); // 파일 객체 생성
        boolean isUpload = sftpUtil.upload(UPLOAD_PATH, uploadFile); //업로드
        System.out.println(LOCAL_FILE + " Upload Result - " + isUpload); // 업로드 여부 확인*/

        File dir = new File(LOCAL_DIR);
        if (dir.isDirectory()) {
            System.out.println(LOCAL_DIR + " is Directory");
        }
/*        File files[] = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            System.out.println("file: " + files[i]);
        }*/

        String[] filenames = dir.list();
        for (int i = 0; i < filenames.length; i++) {
            System.out.println("Print File List : " + filenames[i]);
        }

        System.out.println();

        int uploadFileCnt = 0;

        for (String targetFile : filenames) {
/*            int index = targetFile.lastIndexOf('.');
            if (index < 0) {
                System.out.println(">> " + targetFile + " index : " + index);
                continue;
            }

            String suffix = targetFile.substring(index);
            System.out.println("L subString result : " + suffix);

            if (!suffix.equalsIgnoreCase(".B01")) {
                System.out.println("[X] " + targetFile + " : wrong suffix");
                continue;
            }*/

            if (!checkValid(targetFile)) {
                System.out.println("[X] " + targetFile + " : wrong suffix");
                continue;
            }

            File uploadFile = new File(LOCAL_DIR + targetFile);
            if (uploadFile.isFile()) {
                boolean isUpload = sftpUtil.upload(UPLOAD_PATH, uploadFile); //업로드
                System.out.println("[O] " + targetFile + " Upload Result - " + isUpload); // 업로드 여부 확인*/
                if (isUpload) uploadFileCnt++;
            } else {
                System.out.println("[X] " + targetFile + " is Directory, cannot upload ");
            }
        }

        System.out.println();
        System.out.println("Total [" + uploadFileCnt + "] Files Uploaded");

        // 연결 해제
        sftpUtil.disconnection();
    }

    @Test
    public void printFileList() {
        // 접속
        final SFTPUtil sftpUtil = new SFTPUtil(A2S_HOST, A2S_USER_NAME, A2S_PW, PORT, null);
        sftpUtil.init();

        Vector<ChannelSftp.LsEntry> files = sftpUtil.getFileList(UPLOAD_PATH);

        int i = 1;
        for (ChannelSftp.LsEntry file : files) {
            System.out.println(i + " : " + file.getFilename());
            i++;
        }

        // 연결 해제
        sftpUtil.disconnection();
    }

    @Test
    public void sftpManagerTest() {
        String configPath = "/Users/kimdajin/C-ACS/sftp_module/src/test/resources/config";
        SftpConfig config = new SftpConfig(configPath);

        SftpManager sftpManager = SftpManager.getInstance();
        sftpManager.init(config);
        sftpManager.process();
    }
}
