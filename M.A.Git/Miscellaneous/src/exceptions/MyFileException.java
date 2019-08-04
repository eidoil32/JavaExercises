package exceptions;

public class MyFileException extends Exception {
    private eErrorCodes code;
    private String filename;

    public MyFileException(eErrorCodes code, String filename) {
        super();
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
