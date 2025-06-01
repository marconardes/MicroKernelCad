package com.cad.dxflib.parser;

public class DxfGroupCode {
    public final int code;
    public final String value;

    public DxfGroupCode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + code + ", \"" + value + "\")";
    }
}
