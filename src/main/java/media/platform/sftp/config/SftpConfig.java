package media.platform.sftp.config;

import media.platform.sftp.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
public class SftpConfig extends SftpConfigInfo {
    static final Logger log = LoggerFactory.getLogger(SftpConfig.class);
    private static final String USER_CONFIG = "sftp_user.config";
    private ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> userBuilder;

    // SECTION
    private static final String COMMON = "COMMON";

    // KEY
    private static final String SFTP_HOST = "HOST";
    private static final String SFTP_USER = "USER";
    private static final String SFTP_PORT = "PORT";
    private static final String SFTP_PASS = "PASS";
    private static final String PRIVATE_KEY = "PRIVATE_KEY";

    private static final String SRC_DIR = "SRC_DIR";
    private static final String UPLOAD_DIR = "UPLOAD_DIR";

    private static final String FILTER_VALUE = "FILTER_VALUE";
    private static final String FILTER_EXTS = "FILTER_EXTS";

    public SftpConfig(String configPath) {
        loadConfigFile(configPath);
        loadConfig();
        log.debug("{}", this);
    }

    /**
     * @fn loadConfigFile
     * @brief Config 파일 로드 및 변경 이벤트 추가
     *        (a2s_user.config/.a2s_dev.config load & 변경 될 시 알림 이벤트)
     * @param configPath: config 공통 경로
     */
    private void loadConfigFile(String configPath) {
        try {
            userBuilder = createConfigBuilder(configPath + "/" + USER_CONFIG);
        } catch (Exception e) {
            log.error("A2sConfig.loadConfigFile", e);
        }
    }

    private void loadConfig() {
        loadCommonConfig();
    }

    // COMMON Section
    private void loadCommonConfig() {
        // config null error, IP validate
        super.host = getStrValue(COMMON, SFTP_HOST, "172.27.17.126");
        super.user = getStrValue(COMMON, SFTP_USER, "coreacs");
        super.port = getIntValue(COMMON, SFTP_PORT, 30022);
        super.pass = getStrValue(COMMON, SFTP_PASS, "nZpSdjeATsJhil4WCANIKkT+6bX8XXva");
        super.privateKey = getStrValue(COMMON, PRIVATE_KEY, "");

        super.srcDir = getStrValue(COMMON, SRC_DIR, "/a2s_cdr/backup/");
        super.uploadDir = getStrValue(COMMON, UPLOAD_DIR, "/upload");

        super.filterValue = getStrValue(COMMON, FILTER_VALUE, "");
        super.filterExtsList = getListValue(COMMON, FILTER_EXTS, Arrays.asList("INFO", "B01"));

    }


    /**
     * config String Value load
     *
     * @param section
     * @param key
     * @param defaultValue
     * @return
     */
    public String getStrValue(String section, String key, String defaultValue) {
        String mkey = section + "." + key;

        if (section == null) {
            return defaultValue;
        }

        String value = null;
        try {
            value = userBuilder.getConfiguration().getString(mkey, defaultValue);

        } catch (Exception e) {
            log.error("A2sConfig.getStrValue", e);
        }

        return value;
    }

    public List<String> getListValue(String section, String key, List<String> defaultList) {
        String mkey = section + "." + key;

        if (section == null) {
            return defaultList;
        }
        try {
            String value = userBuilder.getConfiguration().getString(mkey, "");
            if (StringUtil.isNull(value)) return defaultList;

            // 쉼표 split, 공백 제거
            String[] arr = value.split("\\s*,\\s*");
            List<String> strList = new ArrayList<>(Arrays.asList(arr));

            if (!strList.isEmpty()) return strList;
        } catch (Exception e) {
            log.error("A2sConfig.getStrValue", e);
        }
        return defaultList;
    }

    /**
     * config Integer Value load
     *
     * @param section
     * @param key
     * @param defaultValue
     * @return
     */
    public int getIntValue(String section, String key, int defaultValue) {

        String mkey = section + "." + key;
        int rvalue = 0;

        if (section == null) {
            return defaultValue;
        }

        try {
            rvalue = userBuilder.getConfiguration().getInt(mkey, defaultValue);

        } catch (Exception e) {
            log.error("A2sConfig.getIntValue", e);
        }

        return rvalue;
    }

    /**
     * @fn createConfigBuilder
     * @brief Config Builder 생성 및 변경 이벤트 추가
     * @param configPath: config 파일 경로
     *
     * */
    private ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> createConfigBuilder(String configPath) {
        try {
            // Create
            Parameters params = new Parameters();
            File configFile = new File(configPath);
            ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(INIConfiguration.class)
                    .configure(params.fileBased().setFile(configFile));

            // Trigger
            PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(builder.getReloadingController(), null, 1, TimeUnit.SECONDS);
            trigger.start();
            return builder;
        } catch (Exception e) {
            log.error("createConfigBuilder.Exception ", e);
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
