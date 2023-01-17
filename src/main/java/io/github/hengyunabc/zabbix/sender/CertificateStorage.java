package io.github.hengyunabc.zabbix.sender;

public class CertificateStorage {

    private final String keyStore;
    private final String keyStorePassword;
    private String trustStore;
    private String trustStorePassword;

    public CertificateStorage(String keyStore, String keyStorePassword) {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
    }

    public CertificateStorage(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword) {
        this(keyStore, keyStorePassword);
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }
}
