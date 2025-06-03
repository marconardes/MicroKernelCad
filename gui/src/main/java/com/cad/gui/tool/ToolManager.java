package com.cad.gui.tool;

public class ToolManager {
    private ActiveTool currentTool = ActiveTool.NONE;

    public void setActiveTool(ActiveTool tool) {
        this.currentTool = tool;
        System.out.println("Tool changed to: " + this.currentTool); // Feedback básico
    }

    public ActiveTool getActiveTool() {
        return currentTool;
    }
}
