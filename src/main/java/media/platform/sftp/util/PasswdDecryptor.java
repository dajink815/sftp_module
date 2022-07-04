/*
 * Copyright (C) 2019. Uangel Corp. All rights reserved.
 *
 */

package media.platform.sftp.util;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PasswdDecryptor
 *
 * @file PasswdDecryptor.java
 * @author Tony Lim
 */
public class PasswdDecryptor {
    static final Logger log = LoggerFactory.getLogger(PasswdDecryptor.class);

    StandardPBEStringEncryptor crypto;

    public PasswdDecryptor(String key, String alg) {
        crypto = new StandardPBEStringEncryptor();
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setPassword(key);
        config.setAlgorithm(alg);
        crypto.setConfig(config);
    }

    public static void main(String[] args) {
        PasswdDecryptor decryptor = new PasswdDecryptor("skt_acs", "PBEWITHMD5ANDDES");
        String encryptedPw = decryptor.encrypt("a2s.123");
        log.debug("Encrypt : {}", encryptedPw);

        String decryptPwStr = "nZpSdjeATsJhil4WCANIKkT+6bX8XXva";
        String decryptedPw = decryptor.decrypt0(decryptPwStr);
        log.debug("DecryptPw String : {}", decryptPwStr);
        log.debug("Decrypted : {}", decryptedPw);
    }

    public String decrypt0(String encrypted) {
        return crypto.decrypt(encrypted);
    }

    public String encrypt(String pass) {
        return crypto.encrypt(pass);
    }

    public String decrypt(String f) {
        Pattern p = Pattern.compile("(ENC\\((.+?)\\))");
        Matcher m = p.matcher(f);
        String g = f;
        while (m.find()) {
            String enc = m.group(1);
            String encryptedPass = m.group(2);
            String pass = decrypt0(encryptedPass);
            g = StringUtils.replace(g, enc, pass);
        }

        return g;
    }
}
