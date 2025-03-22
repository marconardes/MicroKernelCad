package com.cad.modules.geometry;

import com.cad.core.api.ModuleInterface;

public class GeometryEvent implements ModuleInterface {

    @Override
    public void init() {
        System.out.println("GeometryEvent init");
    }

    @Override
    public void start() {
        System.out.println("GeometryEvent start");
    }

    @Override
    public void stop() {
        System.out.println("GeometryEvent stop");
    }

    @Override
    public void destroy() {
        System.out.println("GeometryEvent destroy");
    }
}
