package io.github.hengyunabc.zabbix.sender;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class DefaultCertificateStorage implements CertificateStorage {

    private final KeyStorage keyStorage;

    private final TrustStorage trustStorage;

    public DefaultCertificateStorage(String keyStore, String keyStorePassword) {
        this(new KeyStorage(keyStore, keyStorePassword));
    }

    public DefaultCertificateStorage(KeyStorage keyStorage) throws CertificateStorageError {
        this(keyStorage, new TrustStorage(keyStorage.getStorePath(), keyStorage.getStorePassword(), keyStorage.getAlgorithm()));
    }

    public DefaultCertificateStorage(KeyStorage keyStorage, TrustStorage trustStorage) throws CertificateStorageError {
        DefaultStorage.checkNonNull(keyStorage, DefaultCertificateStorage.name(), KeyStorage.name());
        DefaultStorage.checkNonNull(trustStorage, DefaultCertificateStorage.name(), TrustStorage.name());
        this.keyStorage = keyStorage;
        this.trustStorage = trustStorage;
    }

    public static String name() {
        return DefaultCertificateStorage.class.getSimpleName();
    }

    @Override
    public Storage getKeyStorage() {
        return keyStorage;
    }

    @Override
    public Storage getTrustStorage() {
        return trustStorage;
    }

    public final static class KeyStorage extends DefaultStorage {
        public KeyStorage(String store, String storePassword) {
            this(store, storePassword, KeyManagerFactory.getDefaultAlgorithm());
        }

        public KeyStorage(String store, String storePassword, String algorithm) {
            super(store, storePassword, algorithm);
        }

        @Override
        public String getName() {
            return name();
        }

        public static String name() {
            return KeyStorage.class.getSimpleName();
        }
    }

    public final static class TrustStorage extends DefaultStorage {
        public TrustStorage(String store, String storePassword) {
            this(store, storePassword, TrustManagerFactory.getDefaultAlgorithm());
        }

        public TrustStorage(String store, String storePassword, String algorithm) {
            super(store, storePassword, algorithm);
        }

        @Override
        public String getName() {
            return name();
        }

        public static String name() {
            return TrustStorage.class.getSimpleName();
        }
    }

    public abstract static class DefaultStorage implements Storage {

        private final String storePath;
        private final String storePassword;

        private final String algorithm;

        DefaultStorage(String storePath, String storePassword, String algorithm) {
            this.storePath = storePath;
            this.storePassword = storePassword;
            this.algorithm = algorithm;
            checkStorage(this, this.getName());
        }

        @Override
        public String getStorePath() {
            return storePath;
        }

        @Override
        public String getStorePassword() {
            return storePassword;
        }

        @Override
        public String getAlgorithm() {
            return algorithm;
        }

        static void checkNonNull(Storage storage, String storageName) throws CertificateStorageError {
            checkNonNull(storage, storageName, storageName);
        }

        static void checkNonNull(Storage storage, String invalid, String emptyPart) throws CertificateStorageError {
            if (storage == null) {
                throw CertificateStorageError.emptyDetected(invalid, emptyPart);
            }
        }

        static void checkStorage(Storage storage, String storageName) throws CertificateStorageError {
            checkNonNull(storage, storageName);
            if (storage.getStorePath() == null || storage.getStorePath().isBlank()) {
                throw CertificateStorageError.emptyDetected(name(), storageName + " store", " or empty");
            }
            if (storage.getStorePassword() == null) {
                throw CertificateStorageError.emptyDetected(name(), storageName + " store password");
            }
            if (storage.getAlgorithm() == null || storage.getAlgorithm().isBlank()) {
                throw CertificateStorageError.emptyDetected(name(), storageName + " store algorithm", " or empty");
            }
        }
    }

}
