package com.cad.dxflib.parser;

public class DxfParserException extends Exception {
    public DxfParserException(String message) {
        super(message);
    }

    public DxfParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
