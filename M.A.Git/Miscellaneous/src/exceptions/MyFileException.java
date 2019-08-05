package exceptions;

public class MyFileException extends Exception {
    private eErrorCodes code;
    private String filename;

    public MyFileException(eErrorCodes code, String filename) {
        super(String.format(code.getMessage(),filename));
        this.code = code;
        this.filename = filename;
    }

    public eErrorCodes getCode() {
        return code;
    }

    public String getFilename() {
        return filename;
    }
}
