package testing;

import edu.yu.da.DamConstruction;
import org.junit.jupiter.api.Test;
import tools.BigOIt;
import tools.BigOMeasurable;

import java.util.Random;

public class MeasurementTest {
    // will measure order of growth here

    @Test
    public void solveMeasurementTest() {
        BigOIt ti84 = new BigOIt();
        double ratio = ti84.doublingRatio("testing.MeasurementTest$SolveMeasurement", 100000);
        System.out.println("solve()'s doubling ratio is " + ratio);
    }

    public static class SolveMeasurement extends BigOMeasurable {

        private DamConstruction damRegulator;

        @Override
        public void setup(int n) {
            super.setup(n);
            int[] dams = new int[n];
            Random pablo = new Random();
            dams[0] = pablo.nextInt(5) + 1;
            for (int i = 1; i < n; i++) {
                dams[i] = dams[i - 1] + pablo.nextInt(5) + 1;
            }
            damRegulator = new DamConstruction(dams, n * 5);
        }

        @Override
        public void execute() {
            damRegulator.solve();
        }
    }

    @Test
    public void costMeasurementTest() {
        BigOIt ti84 = new BigOIt();
        double ratio = ti84.doublingRatio("testing.MeasurementTest$CostMeasurement", 10000);
        System.out.println("cost()'s doubling ratio is " + ratio);
    }

    public static class CostMeasurement extends BigOMeasurable {

        private DamConstruction damRegulator;
        private int[] dams;

        @Override
        public void setup(int n) {
            super.setup(n);
            dams = new int[n];
            Random pablo = new Random();
            dams[0] = pablo.nextInt(5) + 1;
            for (int i = 1; i < n; i++) {
                dams[i] = dams[i - 1] + pablo.nextInt(5) + 1;
            }
            damRegulator = new DamConstruction(dams, n * 5);
        }

        @Override
        public void execute() {
            damRegulator.cost(dams);
        }
    }

}
