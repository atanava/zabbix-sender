package io.github.hengyunabc.zabbix.sender;

public interface CertificateStorage {
    Storage getKeyStorage();

    Storage getTrustStorage();

    interface Storage {
        String getStorePath();

        String getStorePassword();

        String getAlgorithm();

        String getName();
    }
}
