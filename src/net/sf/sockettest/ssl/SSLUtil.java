package net.sf.sockettest.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SSLUtil {
    /*
     * enabledProtocol:TLSv1
     * Provider:SunJSSE version 1.7
     * enabledProtocol:SSLv2Hello
     * enabledProtocol:SSLv3
     * enabledProtocol:TLSv1
     * enabledProtocol:TLSv1.1
     * enabledProtocol:TLSv1.2
     */
    public static String sslType = "TLSv1";

    public static SSLServerSocket createServerSocket(KeyManagerFactory kmf, int port, int backlog, InetAddress bindAddr)
            throws Exception {

        SSLContext context = SSLContext.getInstance(sslType);

        /**
         * KeyManager[] 第一个参数是授权的密钥管理器，用来授权验证。TrustManager[]第二个是被授权的证书管理器，
         * 用来验证服务器端的证书。第三个参数是一个随机数值，可以填写null。如果只是服务器传输数据给客户端来验证，就传入第一个参数就可以，
         * 客户端构建环境就传入第二个参数。双向认证的话，就同时使用两个管理器。
         */
        context.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory ssf = context.getServerSocketFactory();
        return (SSLServerSocket) ssf.createServerSocket(port, backlog, bindAddr);
    }

    public static KeyManagerFactory buildKeyManagerFactory(InputStream input, String passwd)
            throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance("jks");
        //InputStream input = new FileInputStream(filename);
        ks.load(input, passwd.toCharArray());
        //input.close();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passwd.toCharArray());

        return kmf;
    }

}
