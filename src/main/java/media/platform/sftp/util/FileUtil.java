package media.platform.sftp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

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

    public static boolean checkFile(String filePathName) {
        File file = new File(filePathName);
        return file.exists() && file.isFile();
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
     * @fn checkExtensions
     * @brief 파일 확장자 체크
     * @param fileName: 파일 명
     * @param extensionList: 확장자 명 리스트
     * */
    public static boolean checkExtensions(String fileName, List<String> extensionList) {
        for (String extension : extensionList) {
            if (checkExtension(fileName, extension)) {
                log.info("[{}] File Extension : {}", fileName, extension);
                return true;
            }
        }
        return false;
    }

    /**
     * @fn checkExtension
     * @brief 파일 확장자 체크
     * @param fileName: 파일 명
     * @param extension: 확장자 명
     * */
    public static boolean checkExtension(String fileName, String extension) {
        if (fileName.length() < extension.length()) return false;
        String fileSuffix = fileName.substring(fileName.length() - extension.length());
        return extension.equals(fileSuffix);
    }

    /**
     * @fn getFilenameFilter
     * @brief FilenameFilter 객체 생성
     * @param filterData: 필터링 할 데이터, 해당 값을 포함 하면 통과
     * */
    public static FilenameFilter getFilenameFilter(String filterData) {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(filterData);
            }
        };
    }

}
