package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when file storage operations fail
 */
public class FileStorageException extends BusinessException {

    private static final String ERROR_CODE = "FILE_STORAGE_ERROR";

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
