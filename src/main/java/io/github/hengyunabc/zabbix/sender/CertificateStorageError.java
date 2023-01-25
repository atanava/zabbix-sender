package io.github.hengyunabc.zabbix.sender;

public class CertificateStorageError extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Invalid %s.";

    public static CertificateStorageError emptyDetected(String invalid, String emptyPart, String endOfMessage) {
        return new CertificateStorageError(String.format(
                MESSAGE_TEMPLATE + " %s must not be null%s.",
                invalid == null ? "" : invalid,
                emptyPart == null ? "This" : emptyPart,
                endOfMessage == null ? "" : endOfMessage
        ));
    }

    public static CertificateStorageError emptyDetected(String invalid, String emptyPart) {
        return emptyDetected(invalid, emptyPart, null);
    }

    public static CertificateStorageError emptyDetected(String invalid) {
        return emptyDetected(invalid, null);
    }

    public static CertificateStorageError storageIsNull() {
        return emptyDetected(DefaultCertificateStorage.name());
    }

    public CertificateStorageError() {
        this(String.format(MESSAGE_TEMPLATE, DefaultCertificateStorage.name()));
    }

    public CertificateStorageError(String message) {
        super(message);
    }

    public CertificateStorageError(Throwable cause) {
        super(cause);
    }
}
