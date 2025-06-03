package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Circle2DTest {

    @Test
    void testCircle2DCreationAndGetters() {
        Point2D center = new Point2D(5.0, 5.0);
        double radius = 10.0;
        Circle2D circle = new Circle2D(center, radius);

        assertEquals(center, circle.getCenter(), "Center point should match constructor argument.");
        assertEquals(radius, circle.getRadius(), 0.001, "Radius should match constructor argument.");
    }

    @Test
    void testCircle2DEqualsAndHashCode() {
        Point2D c1 = new Point2D(1, 1);
        Point2D c2 = new Point2D(2, 2);

        Circle2D circle1 = new Circle2D(c1, 5.0);
        Circle2D circle2 = new Circle2D(c1, 5.0); // Same as circle1
        Circle2D circle3 = new Circle2D(c2, 5.0); // Different center
        Circle2D circle4 = new Circle2D(c1, 10.0); // Different radius

        assertEquals(circle1, circle2, "Circles with same center and radius should be equal.");
        assertEquals(circle1.hashCode(), circle2.hashCode(), "Hash codes for equal circles should be the same.");

        assertNotEquals(circle1, circle3, "Circles with different centers should not be equal.");
        assertNotEquals(circle1, circle4, "Circles with different radii should not be equal.");
    }

    @Test
    void testCircle2DInvalidRadius() {
        Point2D center = new Point2D(0, 0);
        assertThrows(IllegalArgumentException.class, () -> new Circle2D(center, 0), "Constructor should throw IllegalArgumentException for zero radius.");
        assertThrows(IllegalArgumentException.class, () -> new Circle2D(center, -5.0), "Constructor should throw IllegalArgumentException for negative radius.");
    }
     @Test
    void testCircle2DNullCenter() {
        assertThrows(NullPointerException.class, () -> new Circle2D(null, 10.0), "Constructor should throw NullPointerException for null center point.");
    }
}
