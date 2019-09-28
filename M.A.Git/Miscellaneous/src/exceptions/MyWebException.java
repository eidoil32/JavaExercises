package exceptions;

public class MyWebException extends Exception {
    private eErrorCodes code;
    private String additionalData;

    public MyWebException(eErrorCodes code, String additionalData) {
        this.code = code;
        this.additionalData = additionalData;
    }

    @Override
    public String getMessage() {
        return String.format(code.getMessage(), additionalData);
    }
}
