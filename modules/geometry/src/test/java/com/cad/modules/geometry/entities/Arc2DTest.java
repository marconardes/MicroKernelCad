package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Arc2DTest {

    @Test
    void testArc2DCreationAndGetters() {
        Point2D center = new Point2D(1.0, 2.0);
        double radius = 5.0;
        double startAngle = 0.0;
        double endAngle = 90.0;
        Arc2D arc = new Arc2D(center, radius, startAngle, endAngle);

        assertEquals(center, arc.getCenter(), "Center should match.");
        assertEquals(radius, arc.getRadius(), 0.001, "Radius should match.");
        assertEquals(startAngle, arc.getStartAngle(), 0.001, "Start angle should match.");
        assertEquals(endAngle, arc.getEndAngle(), 0.001, "End angle should match.");
    }

    @Test
    void testArc2DEqualsAndHashCode() {
        Point2D c1 = new Point2D(0,0);
        Arc2D arc1 = new Arc2D(c1, 10, 0, 90);
        Arc2D arc2 = new Arc2D(c1, 10, 0, 90); // Same
        Arc2D arc3 = new Arc2D(new Point2D(1,1), 10, 0, 90); // Different center
        Arc2D arc4 = new Arc2D(c1, 12, 0, 90); // Different radius
        Arc2D arc5 = new Arc2D(c1, 10, 10, 90); // Different start angle
        Arc2D arc6 = new Arc2D(c1, 10, 0, 80); // Different end angle

        assertEquals(arc1, arc2, "Arcs with same properties should be equal.");
        assertEquals(arc1.hashCode(), arc2.hashCode(), "Hash codes for equal arcs should be the same.");

        assertNotEquals(arc1, arc3);
        assertNotEquals(arc1, arc4);
        assertNotEquals(arc1, arc5);
        assertNotEquals(arc1, arc6);
    }

    @Test
    void testArc2DInvalidRadius() {
        Point2D center = new Point2D(0, 0);
        assertThrows(IllegalArgumentException.class, () -> new Arc2D(center, 0, 0, 90), "Constructor should throw for zero radius.");
        assertThrows(IllegalArgumentException.class, () -> new Arc2D(center, -5, 0, 90), "Constructor should throw for negative radius.");
    }

    @Test
    void testArc2DNullCenter() {
        assertThrows(NullPointerException.class, () -> new Arc2D(null, 10.0, 0, 90), "Constructor should throw NullPointerException for null center point.");
    }
}
