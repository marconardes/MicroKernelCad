package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Line2DTest {

    @Test
    void testLine2DCreationAndGetters() {
        Point2D start = new Point2D(1.0, 2.0);
        Point2D end = new Point2D(3.0, 4.0);
        Line2D line = new Line2D(start, end);

        assertEquals(start, line.getStartPoint(), "Start point should match constructor argument.");
        assertEquals(end, line.getEndPoint(), "End point should match constructor argument.");
    }

    @Test
    void testLine2DEqualsAndHashCode() {
        Point2D p1 = new Point2D(1, 1);
        Point2D p2 = new Point2D(2, 2);
        Point2D p3 = new Point2D(3, 3);

        Line2D line1 = new Line2D(p1, p2);
        Line2D line2 = new Line2D(p1, p2); // Same as line1
        Line2D line3 = new Line2D(p2, p1); // Different from line1 (order matters for directed segment)
        Line2D line4 = new Line2D(p1, p3); // Different from line1

        assertEquals(line1, line2, "Lines with same start and end points should be equal.");
        assertEquals(line1.hashCode(), line2.hashCode(), "Hash codes for equal lines should be the same.");

        assertNotEquals(line1, line3, "Lines with swapped start and end points should not be equal.");
        assertNotEquals(line1, line4, "Lines with different end points should not be equal.");
    }

    @Test
    void testLine2DNullPoints() {
        Point2D p1 = new Point2D(1,1);
        assertThrows(NullPointerException.class, () -> new Line2D(null, p1), "Constructor should throw NullPointerException for null start point.");
        assertThrows(NullPointerException.class, () -> new Line2D(p1, null), "Constructor should throw NullPointerException for null end point.");
        assertThrows(NullPointerException.class, () -> new Line2D(null, null), "Constructor should throw NullPointerException for two null points.");
    }
}
