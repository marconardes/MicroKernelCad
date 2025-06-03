package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Polyline2DTest {

    @Test
    void testPolyline2DCreationAndGetters() {
        Point2D p1 = new Point2D(0, 0);
        Point2D p2 = new Point2D(1, 1);
        List<Point2D> vertices = Arrays.asList(p1, p2);
        Polyline2D polyline = new Polyline2D(new ArrayList<>(vertices), false);

        assertEquals(2, polyline.getNumberOfVertices(), "Number of vertices should be 2.");
        assertEquals(vertices, polyline.getVertices(), "Vertices list should match.");
        assertFalse(polyline.isClosed(), "Polyline should not be closed by default.");

        Polyline2D closedPolyline = new Polyline2D(new ArrayList<>(vertices), true);
        assertTrue(closedPolyline.isClosed(), "Polyline should be closed.");
    }

    @Test
    void testPolyline2DEqualsAndHashCode() {
        Point2D p1 = new Point2D(0,0);
        Point2D p2 = new Point2D(1,1);
        Point2D p3 = new Point2D(2,2);

        Polyline2D poly1 = new Polyline2D(Arrays.asList(p1, p2), false);
        Polyline2D poly2 = new Polyline2D(Arrays.asList(p1, p2), false); // Same
        Polyline2D poly3 = new Polyline2D(Arrays.asList(p1, p2), true);  // Different closed flag
        Polyline2D poly4 = new Polyline2D(Arrays.asList(p1, p3), false); // Different vertices

        assertEquals(poly1, poly2, "Polylines with same vertices and closed status should be equal.");
        assertEquals(poly1.hashCode(), poly2.hashCode(), "Hash codes for equal polylines should be the same.");

        assertNotEquals(poly1, poly3);
        assertNotEquals(poly1, poly4);
    }

    @Test
    void testPolyline2DInvalidConstruction() {
        assertThrows(NullPointerException.class, () -> new Polyline2D(null, false), "Constructor should throw for null vertices list.");
        assertThrows(IllegalArgumentException.class, () -> new Polyline2D(new ArrayList<>(), false), "Constructor should throw for empty vertices list.");

        List<Point2D> listWithNull = new ArrayList<>();
        listWithNull.add(new Point2D(0,0));
        listWithNull.add(null);
        assertThrows(NullPointerException.class, () -> new Polyline2D(listWithNull, false), "Constructor should throw for list containing null vertex.");
    }

    @Test
    void testPolyline2DUnmodifiableVertices() {
        Point2D p1 = new Point2D(0,0);
        List<Point2D> originalVertices = new ArrayList<>();
        originalVertices.add(p1);
        Polyline2D polyline = new Polyline2D(originalVertices, false);

        List<Point2D> retrievedVertices = polyline.getVertices();
        assertThrows(UnsupportedOperationException.class, () -> retrievedVertices.add(new Point2D(1,1)), "Getter for vertices should return an unmodifiable list.");
    }
}
