package com.cad.dxflib.structure;

import java.util.Objects;

/**
 * Represents a DXF BLOCK_RECORD table entry.
 * Block records hold information about blocks, such as their name, associated layout,
 * and XREF path if applicable.
 */
public class DxfBlockRecord {

    private String name; // Block record name (code 2), same as the BLOCK name it refers to.
    private String handle; // Handle of the BLOCK_RECORD object itself (code 5).
    private String ownerDictionaryHandle; // Handle of owner dictionary (code 330), typically the BLOCK_RECORD table.
    private String layoutHandle; // Handle of the LAYOUT object associated with this block record (code 340).
                                 // For *Model_Space and *Paper_Space, this links to their respective LAYOUT objects.
    private String xrefPathName; // Xref path name (code 1), if this block is an external reference.

    /**
     * Constructs a new DxfBlockRecord with the given name.
     * @param name The name of the block record. Must not be null or empty.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    public DxfBlockRecord(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Block record name cannot be null or empty.");
        }
        this.name = name;
    }

    /**
     * Gets the name of the block record. This typically matches the name of the corresponding BLOCK.
     * @return The block record name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the block record.
     * @param name The new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the handle of this block record object.
     * @return The handle string.
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Sets the handle of this block record object.
     * @param handle The handle string.
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Gets the handle of the owner dictionary, typically the BLOCK_RECORD table dictionary.
     * @return The owner dictionary handle string.
     */
    public String getOwnerDictionaryHandle() {
        return ownerDictionaryHandle;
    }

    /**
     * Sets the handle of the owner dictionary.
     * @param ownerDictionaryHandle The owner dictionary handle string.
     */
    public void setOwnerDictionaryHandle(String ownerDictionaryHandle) {
        this.ownerDictionaryHandle = ownerDictionaryHandle;
    }

    /**
     * Gets the handle of the LAYOUT object associated with this block record.
     * This is particularly relevant for `*Model_Space` and `*Paper_Space` block records.
     * @return The layout handle string, or null if not set.
     */
    public String getLayoutHandle() {
        return layoutHandle;
    }

    /**
     * Sets the handle of the LAYOUT object associated with this block record.
     * @param layoutHandle The layout handle string.
     */
    public void setLayoutHandle(String layoutHandle) {
        this.layoutHandle = layoutHandle;
    }

    /**
     * Gets the path name if this block record represents an external reference (XREF).
     * @return The XREF path name, or null if not an XREF or not set.
     */
    public String getXrefPathName() {
        return xrefPathName;
    }

    /**
     * Sets the path name for an XREF block record.
     * @param xrefPathName The XREF path name.
     */
    public void setXrefPathName(String xrefPathName) {
        this.xrefPathName = xrefPathName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DxfBlockRecord that = (DxfBlockRecord) o;
        // Block record names are case-insensitive in DXF and should be the primary identifier.
        // Assuming names are stored consistently (e.g., uppercase) by DxfDocument.
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        // Assuming names are stored consistently (e.g., uppercase).
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DxfBlockRecord{" +
                "name='" + name + '\'' +
                (handle != null ? ", handle='" + handle + '\'' : "") +
                (layoutHandle != null ? ", layoutHandle='" + layoutHandle + '\'' : "") +
                (xrefPathName != null ? ", xrefPathName='" + xrefPathName + '\'' : "") +
                '}';
    }
}
