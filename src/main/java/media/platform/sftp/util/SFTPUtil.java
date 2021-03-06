package media.platform.sftp.util;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dajin kim
 */
public class SFTPUtil {
    static final Logger log = LoggerFactory.getLogger(SFTPUtil.class);

    private static final String CHANNEL_TYPE = "sftp";
    private Session session = null;
    private ChannelSftp channelSftp = null;

    private final String host;
    private final String userName;
    private final String password;
    private final int port;
    private final String privateKey;

    /**
     * 서버 연결에 필요한 값들 세팅
     *
     * @param host 서버 주소
     * @param userName 아이디
     * @param password 패스워드
     * @param port 포트번호
     * @param privateKey 개인키
     */
    public SFTPUtil(String host, String userName, String password, int port, String privateKey) {
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.port = port;
        this.privateKey = privateKey;
    }

    /**
     * 서버와 연결 초기화
     */
    public void init() {
        log.info("SFTPUtil.init [Host:{}, User:{}, Port:{}]", host, userName, port);

        // JSch 호출
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

            // 세션과 관련된 정보 설정
            java.util.Properties config = new java.util.Properties();
            // 호스트 정보 검사 하지 않음
            config.put("StrictHostKeyChecking", "no"); // 접속 시 HostKeyChecking 여부
            session.setConfig(config);

            // 세션 연결
            session.connect();

            // 채널 방식 설정 (sftp 채널 오픈) -> 채널을 이용해 Upload & Download
            Channel channel = session.openChannel(CHANNEL_TYPE);
            channel.connect();

            channelSftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            log.error("SFTPUtil.init.Exception ", e);
        }
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
        ArrayList<ChannelSftp.LsEntry> list = null;
        try {
            list = new ArrayList<>(channelSftp.ls(path));
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return list != null && !list.isEmpty();
    }

    /**
     * @param path : ls 명령어를 입력하려고 하는 path 저장소
     * @return
     */
    public List<ChannelSftp.LsEntry> getFileList(String path) {
        List<ChannelSftp.LsEntry> list = null;
        try {
            channelSftp.cd(path);
            list = new ArrayList<>(channelSftp.ls("."));

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
    public void disconnect() {
        // ChannelSftp 가 Channel 을 감싸고 있으므로 channelSftp 만 종료
        // -> 내부적으로 Channel.disconnect() 도 호출됨
        //    (ChannelSftp quit == exit == disconnect)
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.quit();
                log.info("SFTPUtil.disconnect - ChannelSftp (Status:{})", channelSftp.isConnected());
            }
        } finally {
            channelSftp = null;
        }

        // session.disconnect 를 안하면 프로세스가 안 죽는다.
        try {
            if (session != null && session.isConnected()) {
                session.disconnect();
                log.info("SFTPUtil.disconnect - Session (Status:{})", session.isConnected());
            }
        } finally {
            session = null;
        }

        log.info("SFTPUtil Disconnected...");
    }

    public boolean isConnected() {
        return channelSftp != null && channelSftp.isConnected();
    }
}
