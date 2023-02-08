package io.github.hengyunabc.zabbix.sender;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class SocketFactorySSL extends SSLSocketFactory {

    private CertificateStorage certificateStorage;

    public SocketFactorySSL() {
    }

    public SocketFactorySSL(CertificateStorage certificateStorage) {
        this.certificateStorage = certificateStorage;
    }

    @Override
    public Socket createSocket() throws IOException {
        return certificateStorage == null
                ? SSLSocketFactory.getDefault().createSocket()
                : this.createSocket(certificateStorage);
    }

    public Socket createSocket(CertificateStorage certificateStorage) throws IOException {
        if (certificateStorage == null) {
            throw CertificateStorageError.storageIsNull();
        }
        try {
            return createCustomSocket(certificateStorage);
        } catch (GeneralSecurityException e) {
            throw new CertificateStorageError(e);
        }
    }

    private Socket createCustomSocket(CertificateStorage certificateStorage) throws GeneralSecurityException, IOException {
        var keyStorage = certificateStorage.getKeyStorage();
        var trustStorage = certificateStorage.getTrustStorage();
        try (FileInputStream keyStream = new FileInputStream(keyStorage.getStorePath());
             FileInputStream trustStream = new FileInputStream(trustStorage.getStorePath())) {

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyStorage.getAlgorithm());
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStream, keyStorage.getStorePassword().toCharArray());

            keyManagerFactory.init(keyStore, keyStorage.getStorePassword().toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustStorage.getAlgorithm());
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(trustStream, trustStorage.getStorePassword().toCharArray());

            trustManagerFactory.init(trustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return sslSocketFactory.createSocket();
        }
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return SSLSocketFactory.getDefault().createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
        return SSLSocketFactory.getDefault().createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return SSLSocketFactory.getDefault().createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return SSLSocketFactory.getDefault().createSocket(inetAddress, i, inetAddress1, i1);
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

}
