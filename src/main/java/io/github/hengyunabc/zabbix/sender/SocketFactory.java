package io.github.hengyunabc.zabbix.sender;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

public class SocketFactory {

    private static String keyAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    private static String trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

    public static Socket createSocket() {
        return new Socket();
    }

    public static Socket createSSLSocket(CertificateStorage certificateStorage) throws IOException, GeneralSecurityException {
        if (certificateStorage == null) {
            return SSLSocketFactory.getDefault().createSocket();
        }
        String jksStorage = certificateStorage.getKeyStore();
        String keyStorePassword = certificateStorage.getKeyStorePassword();
        if (jksStorage == null || keyStorePassword == null) {
            throw new GeneralSecurityException("Invalid CertificateStorage. KeyStore and keyStorePassword must not be null");
        }
        String trustStore = certificateStorage.getTrustStore();
        String trustStorePassword = certificateStorage.getTrustStorePassword();

        if (trustStore != null && trustStorePassword != null) {
            overwriteSystemProps(jksStorage, keyStorePassword, trustStore, trustStorePassword);
            return SSLSocketFactory.getDefault().createSocket();
        }

        return createCustomSSLSocket(jksStorage, keyStorePassword);
    }

    private static Socket createCustomSSLSocket(String jksStorage, String keyStorePassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        FileInputStream stream = null;
        Socket socket;
        try {
            stream = new FileInputStream(jksStorage);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(stream, keyStorePassword.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(getKeyAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(getTrustAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            socket = sslSocketFactory.createSocket();

        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return socket;
    }

    private static void overwriteSystemProps(String jksStorage, String keyStorePassword, String trustStore, String trustStorePassword) {
        System.setProperty("javax.net.ssl.keyStore", jksStorage);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    public static String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public static void setKeyAlgorithm(String keyAlgorithm) {
        SocketFactory.keyAlgorithm = keyAlgorithm;
    }

    public static String getTrustAlgorithm() {
        return trustAlgorithm;
    }

    public static void setTrustAlgorithm(String trustAlgorithm) {
        SocketFactory.trustAlgorithm = trustAlgorithm;
    }
}
