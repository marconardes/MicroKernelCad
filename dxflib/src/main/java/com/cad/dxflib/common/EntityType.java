package com.cad.dxflib.common;

public enum EntityType {
    LINE,
    CIRCLE,
    ARC,
    LWPOLYLINE,
    TEXT,
    MTEXT, // Included for future, though not in initial scope
    INSERT,
    POINT, // Included for completeness, though not in initial scope
    SOLID, // Included for completeness
    DIMENSION, // Included for future
    HATCH, // Included for future
    SPLINE, // Included for future
    ELLIPSE, // Included for future
    UNKNOWN // For entities not yet supported
}
