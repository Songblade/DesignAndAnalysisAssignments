package testing;

import edu.yu.da.FindMinyan.PriorityQueue;
import edu.yu.introtoalgs.BigOIt;
import edu.yu.introtoalgs.BigOMeasurable;
import org.junit.jupiter.api.Test;

public class EfficiencyTest {
    // this will contain efficiency tests for both Priority Queue and Find Minyan (if I make for the latter)
    private final BigOIt bigOMeter;

    public EfficiencyTest() {
        bigOMeter = new BigOIt();
    }

    @Test
    public void insertBigOTest() {
        double ratio = bigOMeter.doublingRatio("testing.EfficiencyTest$InsertMeasurable", 10000);
        System.out.println("Insert ratio is " + ratio);
    }

    public static class InsertMeasurable extends BigOMeasurable{

        private PriorityQueue<Integer> queue;

        @Override
        public void setup(int n) {
            super.setup(n);
            queue = new PriorityQueue<>(n + 5);
            for (int i = n; i > 0; i--) {
                queue.insert(i);
            }
        }

        @Override
        public void execute() {
            queue.insert(0);
        }
    }

    @Test
    public void deleteBigOTest() {
        double ratio = bigOMeter.doublingRatio("testing.EfficiencyTest$DeleteMeasurable", 10000);
        System.out.println("Delete ratio is " + ratio);
    }

    public static class DeleteMeasurable extends BigOMeasurable{

        private PriorityQueue<Integer> queue;

        @Override
        public void setup(int n) {
            super.setup(n);
            queue = new PriorityQueue<>(n);
            for (int i = 0; i < n; i++) {
                queue.insert(i);
            }
        }

        @Override
        public void execute() {
            queue.remove();
        }
    }

    @Test
    public void reHeapifyBigOTest() {
        double ratio = bigOMeter.doublingRatio("testing.EfficiencyTest$ReHeapifyMeasurable", 10000);
        System.out.println("reheapify ratio is " + ratio);
    }

    public static class ReHeapifyMeasurable extends BigOMeasurable{

        private PriorityQueue<TestClass> queue;
        private TestClass element;

        @Override
        public void setup(int n) {
            super.setup(n);
            queue = new PriorityQueue<>(n);
            element = new TestClass(0);
            queue.insert(element);
            for (int i = 1; i < n; i++) {
                queue.insert(new TestClass(i));
            }
        }

        @Override
        public void execute() {
            element.id = n + 5;
            queue.reHeapify(element);
        }

        private static class TestClass implements Comparable<TestClass>{
            private int id;

            private TestClass(int id) {
                this.id = id;
            }

            // equals is deliberately not overriden, so that changing the id does not change its equals identity
            // so reheapify can still find it

            @Override
            public int compareTo(TestClass o) {
                if (o == null) {
                    throw new NullPointerException("Test object is null? Seriously? How is this even possible?");
                }
                return this.id - o.id;
            }

            @Override
            public String toString() {
                return "TestClass{" +
                        "id=" + id +
                        '}';
            }
        }

    }

}
