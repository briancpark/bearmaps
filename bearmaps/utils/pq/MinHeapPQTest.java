package bearmaps.utils.pq;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class MinHeapPQTest {

    @Test
    public void testAG3() {
        MinHeapPQ<Character> m = new MinHeapPQ<>();
        m.insert('a', 1);
        assertEquals(m.size(), 1);
        m.poll();
        assertEquals(m.size(), 0);
    }

    @Test
    public void testAGPriority() {
        MinHeapPQ<Character> m = new MinHeapPQ<>();
        m.insert('b', 5);
        m.insert('a', 7);
        m.insert('c', 3);
        m.poll(); // remove c
        m.insert('d', 4);
        m.insert('g', 8);
        m.poll(); // remove d
        m.insert('h', 6);
        m.insert('i', 2);
        m.changePriority('a', 3);
        m.changePriority('b', 1);
        System.out.println(m.toString());
    }

    @Test
    public void minHeapPQTest() {
        MinHeapPQ<Double> m = new MinHeapPQ<>();

        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            m.insert(rand.nextDouble(), rand.nextDouble());
        }
    }

    @Test
    public void naiveTest() {
        NaiveMinPQ<Integer> n = new NaiveMinPQ<>();

        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            n.insert(rand.nextInt(), rand.nextInt());
        }
    }

    @Test
    public void test() {
        MinHeapPQ<Integer> m = new MinHeapPQ<>();
        NaiveMinPQ<Integer> n = new NaiveMinPQ<>();

        Random rand = new Random();

        for (int i = 0; i < 10000; i++) {
            int x = rand.nextInt();
            int y = rand.nextInt();

            n.insert(x, y);
            m.insert(x, y);
        }

        for (int i = -100000; i < 10000; i++) {
            assertEquals(n.contains(i), m.contains(i));
        }

        for (int i = 0; i < 10000; i++) {
            assertEquals(n.poll(), m.poll());
        }
    }
}