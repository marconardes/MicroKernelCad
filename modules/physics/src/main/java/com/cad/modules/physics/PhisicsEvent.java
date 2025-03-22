package com.cad.modules.physics;

import com.cad.core.api.ModuleInterface;

public class PhisicsEvent implements ModuleInterface {

    @Override
    public void init() {
        System.out.println("PhisicsEvent init");
    }

    @Override
    public void start() {
        System.out.println("PhisicsEvent start");
    }

    @Override
    public void stop() {
        System.out.println("PhisicsEvent stop");
    }

    @Override
    public void destroy() {
        System.out.println("PhisicsEvent destroy");
    }
}
