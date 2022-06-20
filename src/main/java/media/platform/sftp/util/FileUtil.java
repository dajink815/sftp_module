package media.platform.sftp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author dajin kim
 */
public class FileUtil {
    static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
        // Nothing
    }

    public static boolean isDirExist(String dirPath) {
        File dir = new File(dirPath);
        return dir.exists();
    }

    public static boolean isFileExist(String filePathName) {
        File file = new File(filePathName);
        return file.exists();
    }

    public static boolean isDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.isDirectory();
    }

    public static boolean isFile(String filePathName) {
        File file = new File(filePathName);
        return file.isFile();
    }

    public static void createDir(String dirPath) {
        createDir(new File(dirPath));
    }

    public static void createDir(File newDir) {
        if (!newDir.exists()) {
            try {
                if (newDir.mkdir()) {
                    log.debug("Created New Directory [{}]", newDir.getName());
                }
            } catch (Exception e) {
                log.error("FileUtil.createDir Error [{}] ", newDir.getName(), e);
            }
        }
    }

    public static File createFile(String dirPath, String fileName) {
        return createFile(new File(dirPath, fileName));
    }

    public static File createFile(File newFile) {
        if (!newFile.exists()) {
            try {
                if (newFile.createNewFile()) {
                    log.debug("Created New File [{}]", newFile.getName());
                }
            } catch (Exception e) {
                log.error("FileUtil.createFile Error [{}] ", newFile.getName(), e);
            }
        }

        return newFile;
    }

    public static void writeFile(String fileName, String data, boolean append) {
        File file = new File(fileName);
        try(FileWriter fw = new FileWriter(file, append)) {
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(data);
            writer.close();
        } catch (Exception e) {
            log.error("FileUtil.writeFile Error", e);
        }
    }

    public static String readFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                sb.append(strLine);
                sb.append("\r\n");
            }
        } catch (Exception e) {
            log.error("FileUtil.readFile Error", e);
        }
        return sb.toString();
    }

    /**
     * @fn moveFile
     * @brief 파일을 이동 시키는 함수
     * @param srcFileName: 파일 원위치
     * @param destFileName: 파일 이동 위치
     * */
    public static void moveFile(String srcFileName, String destFileName) {
        try {
            String cmd = "mv " + srcFileName + " " + destFileName;
            log.info("System Command Exec [{}]", cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            log.error("FileUtil.moveFile Error [{} -> {}] ", srcFileName, destFileName, e);
        }
    }

}
