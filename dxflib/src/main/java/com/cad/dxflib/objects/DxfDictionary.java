package com.cad.dxflib.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DxfDictionary {
    private String handle; // code 5
    private String ownerHandle; // code 330
    private boolean isHardOwner; // code 281 (0 = soft, 1 = hard)
    private int cloningFlag; // code 280 (0 = not clonable, 1 = clonable)
    private Map<String, String> entries; // code 3 for name, code 350/360/340 for object handle

    public DxfDictionary() {
        this.entries = new HashMap<>();
        // Default cloning flag is often 1 (clonable)
        this.cloningFlag = 1;
        // Default ownership is often soft (0) unless specified
        this.isHardOwner = false;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getOwnerHandle() {
        return ownerHandle;
    }

    public void setOwnerHandle(String ownerHandle) {
        this.ownerHandle = ownerHandle;
    }

    public boolean isHardOwner() {
        return isHardOwner;
    }

    public void setHardOwner(boolean hardOwner) {
        isHardOwner = hardOwner;
    }

    public int getCloningFlag() {
        return cloningFlag;
    }

    public void setCloningFlag(int cloningFlag) {
        this.cloningFlag = cloningFlag;
    }

    public Map<String, String> getEntries() {
        return entries;
    }

    public void addEntry(String name, String objectHandle) {
        if (name != null && !name.isEmpty() && objectHandle != null && !objectHandle.isEmpty()) {
            this.entries.put(name, objectHandle);
        }
    }

    public String getObjectHandle(String name) {
        return this.entries.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfDictionary that = (DxfDictionary) o;
        return Objects.equals(handle, that.handle); // Dictionaries are often unique by handle
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return "DxfDictionary{" +
                "handle='" + handle + '\'' +
                ", ownerHandle='" + ownerHandle + '\'' +
                ", isHardOwner=" + isHardOwner +
                ", cloningFlag=" + cloningFlag +
                ", entries.size=" + entries.size() +
                '}';
    }
}
