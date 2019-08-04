package exceptions;

public class MyXMLException extends Exception{

    public MyXMLException(eErrorCodesXML code, String additionalData) {
        super(String.format(code.getErrorMessage(),additionalData));
    }
}
