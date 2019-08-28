package exceptions;

public class RepositoryException extends Exception {
    private eErrorCodes code;
    public RepositoryException(eErrorCodes code) {
        super(code.getMessage());
        this.code = code;
    }

    public RepositoryException(eErrorCodes code, String additionalData) {
        super(String.format(code.getMessage(),additionalData));
        this.code = code;
    }

    public eErrorCodes getCode() {
        return code;
    }
}