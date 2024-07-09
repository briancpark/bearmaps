package bearmaps.utils.ps;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class KDTreeTest {


    /**
     * Lecture Example
     * https://docs.google.com/presentation/d/1DNunK22t-4OU_9c-OBgKkMAdly9aZQkWuv_tBkDg1G4/edit#slide=id.g54b6045cf5_150_128'
     * https://docs.google.com/presentation/d/1WW56RnFa3g6UJEquuIBymMcu9k2nqLrOE1ZlnTYFebg/edit#slide=id.g54b6045b73_0_374
     */
    @Test
    public void specTest() {
        Point p1 = new Point(2, 3); // constructs a Point with x = 1.1, y = 2.2
        Point p2 = new Point(4, 2);
        Point p3 = new Point(4, 2);
        Point p4 = new Point(4, 5); //figure out how to handle duplicates later
        Point p5 = new Point(3, 3);
        Point p6 = new Point(1, 5);
        Point p7 = new Point(4, 4);

        NaivePointSet nn = new NaivePointSet(List.of(p1, p2, p3, p5, p6, p7));

        KDTree kdtree = new KDTree(List.of(p1, p2, p3, p5, p6, p7));

        Point ret = nn.nearest(0, 7);
        Point kdret = kdtree.nearest(0, 7);


        assertTrue(ret.getX() == kdret.getX());
        assertTrue(ret.getY() == kdret.getY());

    }

    @Test
    public void randomTest() {
        Random random = new Random();

        List<Point> l = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Point p = new Point(random.nextDouble(), random.nextDouble());
            l.add(p);
        }

        NaivePointSet nn = new NaivePointSet(l);
        KDTree kdtree = new KDTree(l);

        for (int i = 0; i < 100; i++) {
            double x = 1000 * random.nextDouble();
            double y = 1000 * random.nextDouble();


            Point ret = nn.nearest(x, y);
            Point kdret = kdtree.nearest(x, y);


            assertTrue(ret.getX() == kdret.getX());
            assertTrue(ret.getY() == kdret.getY());
        }
    }

    @Test
    public void testAGC100() {
        /**
         * Test Failed!
         * You must get all nearest queries right to pass this test. Error was as follows:
         *
         * On a KdTree with points
         *
         * [Point x: 188.3541835438, y: -730.3409314820,
         * Point x: -36.8399405401, y: -815.8883292022,
         * Point x: 818.3340334168, y: 699.3291524571],
         *
         * your code thought the right answer is
         * Point x: 188.3541835438, y: -730.3409314820,
         *
         * but it is actually
         * Point x: 818.3340334168, y: 699.3291524571.
         *     at AGKDTreeTest.testTinyKDTree:593 (AGKDTreeTest.java)
         *
         *     Testing that your code works correctly if the KD Tree has only 3 points.
         */

        Random random = new Random();

        Point p1 = new Point(188.3541835438, -730.3409314820);
        Point p2 = new Point(-36.8399405401, -815.8883292022);
        Point p3 = new Point(818.3340334168, 699.3291524571);

        NaivePointSet nn = new NaivePointSet(List.of(p1, p2, p3));
        KDTree kdtree = new KDTree(List.of(p1, p2, p3));


        for (int i = 0; i < 100; i++) {
            double x = 1000 * random.nextDouble();
            double y = 1000 * random.nextDouble();


            Point ret = nn.nearest(x, y);
            Point kdret = kdtree.nearest(x, y);


            assertTrue(ret.getX() == kdret.getX());
            assertTrue(ret.getY() == kdret.getY());
        }


        double x = 145.8499322305641;
        double y = 471.0202459286887;
        Point ret = nn.nearest(x, y);
        Point kdret = kdtree.nearest(x, y);
        assertTrue(ret.getX() == kdret.getX());
        assertTrue(ret.getY() == kdret.getY());
    }
}
