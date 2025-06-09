package com.cad.dxflib.structure;

public class DxfBlockRecord {

    private String name; // code 2
    private String handle; // code 5
    private String ownerDictionaryHandle; // code 330
    private String layoutHandle; // code 340
    private String xrefPathName; // code 1

    public DxfBlockRecord(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getOwnerDictionaryHandle() {
        return ownerDictionaryHandle;
    }

    public void setOwnerDictionaryHandle(String ownerDictionaryHandle) {
        this.ownerDictionaryHandle = ownerDictionaryHandle;
    }

    public String getLayoutHandle() {
        return layoutHandle;
    }

    public void setLayoutHandle(String layoutHandle) {
        this.layoutHandle = layoutHandle;
    }

    public String getXrefPathName() {
        return xrefPathName;
    }

    public void setXrefPathName(String xrefPathName) {
        this.xrefPathName = xrefPathName;
    }
}
