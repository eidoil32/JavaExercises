package exceptions;

public class MyXMLException extends Exception{

    private String additionalData;
    private eErrorCodesXML code;

    public MyXMLException(eErrorCodesXML code, String additionalData) {
        super(String.format(code.getErrorMessage(),additionalData));
        this.additionalData = additionalData;
        this.code = code;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public eErrorCodesXML getCode() {
        return code;
    }
}
