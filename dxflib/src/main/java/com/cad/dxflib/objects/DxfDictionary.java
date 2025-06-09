package com.cad.dxflib.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a DXF DICTIONARY object.
 * Dictionaries are used to store and manage other DXF objects (and entities in some cases),
 * mapping string names to object handles. They are a key part of the DXF object hierarchy.
 * Example: The Named Object Dictionary (NOD) is the root dictionary.
 */
public class DxfDictionary {
    private String handle; // Handle of this dictionary object (code 5)
    private String ownerHandle; // Handle of the owner object (code 330), often another dictionary or a root object.
    private boolean isHardOwner; // Hard-ownership flag (code 281): 0 = soft, 1 = hard.
    private int cloningFlag; // Duplicate record cloning flag (code 280): 0 = Not clonable, 1 = Clonable.
    private final Map<String, String> entries; // Map of entry names (code 3) to entry object handles (code 350, 360, or 340).

    /**
     * Constructs a new DxfDictionary.
     * Initializes entries map, sets default cloning flag to clonable (1),
     * and default ownership to soft (false).
     */
    public DxfDictionary() {
        this.entries = new HashMap<>();
        this.cloningFlag = 1; // Default: Clonable
        this.isHardOwner = false; // Default: Soft ownership
    }

    /**
     * Gets the handle of this dictionary object.
     * @return The handle string.
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Sets the handle of this dictionary object.
     * @param handle The handle string.
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Gets the handle of the owner of this dictionary.
     * @return The owner's handle string, or null if not set.
     */
    public String getOwnerHandle() {
        return ownerHandle;
    }

    /**
     * Sets the handle of the owner of this dictionary.
     * @param ownerHandle The owner's handle string.
     */
    public void setOwnerHandle(String ownerHandle) {
        this.ownerHandle = ownerHandle;
    }

    /**
     * Checks if this dictionary has hard ownership of its entries.
     * Hard ownership means that if the dictionary is deleted, its entries are also deleted.
     * @return true if hard ownership, false for soft ownership.
     */
    public boolean isHardOwner() {
        return isHardOwner;
    }

    /**
     * Sets the hard-ownership flag.
     * @param hardOwner true for hard ownership, false for soft ownership.
     */
    public void setHardOwner(boolean hardOwner) {
        isHardOwner = hardOwner;
    }

    /**
     * Gets the duplicate record cloning flag.
     * (0 = Not clonable, 1 = Clonable, 2 = XREF-dependent name, etc.)
     * @return The cloning flag.
     */
    public int getCloningFlag() {
        return cloningFlag;
    }

    /**
     * Sets the duplicate record cloning flag.
     * @param cloningFlag The cloning flag.
     */
    public void setCloningFlag(int cloningFlag) {
        this.cloningFlag = cloningFlag;
    }

    /**
     * Gets an unmodifiable map of entries in this dictionary.
     * Keys are entry names (strings), values are handles (strings) of the referenced objects.
     * @return An unmodifiable map of dictionary entries.
     */
    public Map<String, String> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * Adds an entry to the dictionary.
     * An entry consists of a name and the handle of the object associated with that name.
     * @param name The name of the entry (code 3).
     * @param objectHandle The handle of the object (code 350, 360, or 340).
     */
    public void addEntry(String name, String objectHandle) {
        if (name != null && !name.isEmpty() && objectHandle != null && !objectHandle.isEmpty()) {
            // DXF dictionary entry names are case-preserved but lookups are often case-insensitive.
            // Storing as-is, lookup in DxfDocument can handle case.
            this.entries.put(name, objectHandle);
        }
    }

    /**
     * Retrieves the handle of an object associated with a given name in this dictionary.
     * Note: DXF dictionary names are generally case-insensitive in practice,
     * but this method performs a case-sensitive lookup on the stored names.
     * For case-insensitive lookup, iterate through entries or pre-process keys.
     * @param name The name of the entry.
     * @return The object handle string, or null if the name is not found.
     */
    public String getObjectHandle(String name) {
        return this.entries.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfDictionary that = (DxfDictionary) o;
        // Dictionaries are often unique by their handle.
        return Objects.equals(handle, that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return "DxfDictionary{" +
                "handle='" + handle + '\'' +
                (ownerHandle != null ? ", ownerHandle='" + ownerHandle + '\'' : "") +
                ", isHardOwner=" + isHardOwner +
                ", cloningFlag=" + cloningFlag +
                ", entries.size=" + entries.size() +
                '}';
    }
}
