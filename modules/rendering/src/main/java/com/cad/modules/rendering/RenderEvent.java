package com.cad.modules.rendering;

import com.cad.core.api.ModuleInterface;


public class RenderEvent implements ModuleInterface{

    @Override
    public void init() {
        System.out.println("RenderEvent init");
    }

    @Override
    public void start() {
        System.out.println("RenderEvent start");
    }

    @Override
    public void stop() {
        System.out.println("RenderEvent stop");
    }

    @Override
    public void destroy() {
        System.out.println("RenderEvent destroy");
    }

}
