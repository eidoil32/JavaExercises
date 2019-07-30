package com.magit.exceptions;

public class RepositoryException extends Exception {
    private eErrorCodes code;
    public RepositoryException(eErrorCodes code) {
        super();
        this.code = code;
    }

    public eErrorCodes getCode() {
        return code;
    }
}
