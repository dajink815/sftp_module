package media.platform.sftp.util;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * @author dajin kim
 */
public class SFTPUtil {
    static final Logger log = LoggerFactory.getLogger(SFTPUtil.class);

    private static final String CHANNEL_TYPE = "sftp";
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;

    private final String host;
    private final String userName;
    private final String password;
    private final int port;
    private final String privateKey;

    public SFTPUtil(String host, String userName, String password, int port, String privateKey) {
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.port = port;
        this.privateKey = privateKey;
    }

    public void init() {
        this.init(host, userName, password, port, privateKey);
    }

    /**
     * 서버와 연결에 필요한 값들을 가져와 초기화 시킴
     *
     * @param host 서버 주소
     * @param userName 아이디
     * @param password 패스워드
     * @param port 포트번호
     * @param privateKey 개인키
     */
    public void init(String host, String userName, String password, int port, String privateKey) {
        log.info("SFTPUtil.init [Host:{}, User:{}, Port:{}]", host, userName, port);

        // JSch 라이브러리 호출
        JSch jSch = new JSch();

        try {
            if (StringUtil.isNull(password) && StringUtil.isNull(privateKey)) {
                log.error("SFTPUtil.init.Exception - Need Correct User Password or Private Key");
                return;
            }

            // 개인키 존재
            if(StringUtil.notNull(privateKey)) {
                jSch.addIdentity(privateKey);
            }

            // 세션 호출
            session = jSch.getSession(userName, host, port);

            // 개인키 없다면 PW 접속
            if(StringUtil.isNull(privateKey) && StringUtil.notNull(password)) {
                session.setPassword(password);
            }

            // 프로퍼티 설정 (세션 키 없이 호출?
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no"); // 접속 시 hostkeychecking 여부
            session.setConfig(config);

            // 세션 연결
            session.connect();

            // 채널 방식 설정 (sftp 채널 오픈) -> 채널을 이용해 Upload & Download
            channel = session.openChannel(CHANNEL_TYPE);
            channel.connect();
        } catch (JSchException e) {
            log.error("SFTPUtil.init.Exception ", e);
        }

        channelSftp = (ChannelSftp) channel;
    }

    /**
     * 디렉토리 생성
     *
     * @param dir 이동할 주소
     * @param mkdirName 생성할 디렉토리명
     */
    public void mkdir(String dir, String mkdirName) {
        if (!this.exists(dir + "/" + mkdirName)) {
            try {
                channelSftp.cd(dir);
                channelSftp.mkdir(mkdirName);
            } catch (SftpException e) {
                log.error("SFTPUtil.mkdir.Exception ", e);
            }
        }
    }

    /**
     * 디렉토리(파일) 존재 여부
     * @param path 디렉토리 (파일)
     * @return
     */
    public boolean exists(String path) {
        Vector res = null;
        try {
            res = channelSftp.ls(path);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return res != null && !res.isEmpty();
    }

    /**
     * @param path : ls 명령어를 입력하려고 하는 path 저장소
     * @return
     */
    public Vector<ChannelSftp.LsEntry> getFileList(String path) {
        Vector<ChannelSftp.LsEntry> list = null;
        try {
            channelSftp.cd(path);
            list = channelSftp.ls(".");

        } catch (Exception e) {
            log.error("SFTPUtil.getFileList.Exception ", e);
        }
        return list;
    }

    /**
     * 파일 업로드
     *
     * @param dir 저장할 디렉토리
     * @param file 저장할 파일
     * @return 업로드 여부
     */
    public boolean upload(String dir, File file) {
        boolean isUpload = false;
        try (FileInputStream in = new FileInputStream(file)) {
            channelSftp.cd(dir);
            channelSftp.put(in, file.getName());

            // 업로드했는지 확인
            if (this.exists(dir + "/" + file.getName())) {
                isUpload = true;
            }
        } catch (SftpException | IOException e) {
            log.error("SFTPUtil.upload.Exception ", e);
        }
        return isUpload;
    }

    /**
     * 파일 다운로드
     *
     * @param dir 다운로드 할 디렉토리
     * @param downloadFileName 다운로드 할 파일
     * @param path 다운로드 후 로컬에 저장될 경로(파일명)
     */
    public void download(String dir, String downloadFileName, String path) {
        InputStream in;
        try {
            channelSftp.cd(dir);
            in = channelSftp.get(downloadFileName);
        } catch (SftpException e) {
            log.error("SFTPUtil.download.Exception ", e);
            return;
        }

        try (FileOutputStream out = new FileOutputStream(path)) {
            int i;

            while ((i = in.read()) != -1) {
                out.write(i);
            }
        } catch (IOException e) {
            log.error("SFTPUtil.download.Exception ", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("SFTPUtil.download.close.Exception ", e);
            }
        }
    }

    /**
     * 연결 종료
     */
    public void disconnection() {
        if (session.isConnected()) {
            log.info("SFTPUtil Disconnecting...");
            channelSftp.quit();
            channel.disconnect();
            session.disconnect();
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }
}
