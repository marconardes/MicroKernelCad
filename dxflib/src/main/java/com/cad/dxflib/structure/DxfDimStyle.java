package com.cad.dxflib.structure;

public class DxfDimStyle {
    private String name;
    // Outros campos do DIMSTYLE podem ser adicionados aqui no futuro (ex: DIMTXT, DIMASZ, etc.)

    public DxfDimStyle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DxfDimStyle{name='" + name + "'}";
    }
}
