package testing;

import edu.yu.da.DetectTerrorist;
import edu.yu.introtoalgs.BigOIt;
import edu.yu.introtoalgs.BigOMeasurable;
import org.junit.jupiter.api.Test;

public class MeasurementTest {

    @Test
    public void getDoublingRatio() {
        BigOIt calc = new BigOIt();
        double ratio = calc.doublingRatio("testing.MeasurementTest$DetectorMeasurement", 10000);
        System.out.println("Ratio is " + ratio);
    }

    public static class DetectorMeasurement extends BigOMeasurable {

        private int[] passengers;

        @Override
        public void setup(int n) {
            super.setup(n);
            passengers = new int[n];
            passengers[0] = -1;
        }

        @Override
        public void execute() {
            DetectTerrorist mossad = new DetectTerrorist(passengers);
        }
    }

}
