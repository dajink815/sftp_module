package media.platform.sftp.service;

/**
 * @author dajin kim
 */
public enum ServiceDefine {

    MODE_KEY("key"), MODE_UPLOAD("upload"), MODE_LIST("list");

    private final String str;

    ServiceDefine(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public static ServiceDefine getTypeEnum(String str) {
        switch (str.toLowerCase()) {
            case "key" :
                return MODE_KEY;
            case "list" :
                return MODE_LIST;
            case "upload" :
            default:
                return MODE_UPLOAD;
        }
    }
}
