package tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class BigOIt {

    private Constructor<BigOMeasurable> constructor;
    private Stopwatch stopwatch;

    /**
     * Given the name of a class that implements the BigOMeasurable API, creates
     * and executes instances of the class, such that by measuring the resulting
     * performance, can return the "doubling ratio" for that algorithm's
     * performance.
     * <p>
     * See extended discussion in Sedgewick, Chapter 1.4, on the topic of
     * doubling ratio experiments.
     *
     * @param bigOMeasurable name of the class for which we want to compute a
     *                       doubling ratio.  The client guarantees that the corresponding class
     *                       implements the BigOMeasurable API, and can be constructed with a
     *                       no-argument constructor.  This method is therefore able to first construct
     *                       instances of this class, invoke "setup(n)" for whatever values of "n" are
     *                       desired, and then invoke "execute()" to measure the performance of a
     *                       single invocation of the algorithm.  The client is responsible for
     *                       ensuring that invocation of setup(n) produces a suitably populated
     *                       (perhaps randomized) set of state scaled as a function of n.
     * @param timeOutInMs    number of milliseconds allowed for the computation.  If
     *                       the implementation has not computed an answer by this time, it should
     *                       return NaN.
     * @return the doubling ratio for the specified algorithm if one can be
     * calculated, NaN otherwise.
     * @throws IllegalArgumentException if bigOMeasurable parameter doesn't
     *                                  fulfil the contract specified above or if some characteristic of the
     *                                  algorithm is at odds with the doubling ratio assumptions.
     */
    public double doublingRatio(String bigOMeasurable, long timeOutInMs, int startingValue) {
        checkForIllegalInputs(bigOMeasurable, timeOutInMs);
        double ratio1 = applyDoublingRatio(bigOMeasurable, timeOutInMs / 2, startingValue);
        double ratio2 = applyDoublingRatio(bigOMeasurable, timeOutInMs / 2, startingValue);
        if (Double.toString(ratio1).equals("NaN")) {
            return ratio2;
        } else if (Double.toString(ratio1).equals("NaN")) {
            return ratio1;
        }
        return (ratio1 + ratio2) / 2;
    }

    public double doublingRatio(String bigOMeasurable, long timeOutInMs) {
        return doublingRatio(bigOMeasurable, timeOutInMs, 100);
    }

    private double applyDoublingRatio(String bigOMeasurable, long timeOutInMs, int startValue) {
        stopwatch = new Stopwatch(timeOutInMs);

        setConstructor(bigOMeasurable);
        // now I will warm up the JVM, hoping that this actually does something and isn't just
        // being silly
        boolean canContinue = warmUpJVM();
        if (!canContinue) {
            return Double.NaN;
        }
        // now I need to do the actual doubling ratio experiment
        // I am going to have to tear this apart later when I want to add in the time limit, but for now,
        // I will ignore that
        // So, let's figure out how to actually run it
        // Let's run the test for each level, say, 25 times, starting at 1000
        // we will get the average of the times, which will be considered the time for that level
        // in a loop:
        // we will then do the same thing for the next level
        // if the average ratio is within .2 of the average times of the previous level:
        // we return the average ratio
        // otherwise, we iterate again
        // which makes this test essentially O(n^2), which is not a good sign

        long previousTime = getAverageTimeInNLevel(50, startValue);
        RunningAverage ratioAvg = new RunningAverage(); // this keeps track of the average ratio
        for (int i = 1; !stopwatch.runningOutOfTime(); i++) {
            if (100 * (int) Math.pow(2, i) < 0) { // if this gives us an overflow, we have a problem
                System.out.println("Too big");
                return Double.NaN;
                // if we still haven't figured it out, it probably isn't consistent
            }
            long newTime = getAverageTimeInNLevel(50, startValue * (int) Math.pow(2, i));

            // here we decide what to do if we need to terminate
            if (newTime == 0) {
                if (ratioAvg.ratioCloseToAverage(Integer.MIN_VALUE, true)) {
                    return ratioAvg.currentRecentAverage();
                } else {
                    return Double.NaN;
                }
            }

            // we get the new time of the next double
            double ratio = (double) newTime / (double) previousTime;
            // this will probably result in a loss of precision, but we are looking for the general
            // ratio, so if anything, that is a plus
            double avg = ratioAvg.getRecentAverageWithNewRatio(ratio);
            if (ratioAvg.ratioCloseToAverage(ratio, false) && i > 5) { // if they are very close, we should be there
                // i > 5 is included in order that it doesn't return with bad precision
                return avg;
            }
            previousTime = newTime; // so that the next ratio will be by this
        }

        if (ratioAvg.ratioCloseToAverage(Integer.MIN_VALUE, true)) {
            return ratioAvg.currentRecentAverage();
        } else {
            return Double.NaN;
        }
    }

    /**
     * Checks for some obvious illegal inputs
     * @param bigOMeasurable same as method
     * @param timeOutInMs same as method
     */
    private void checkForIllegalInputs(String bigOMeasurable, long timeOutInMs) {
        if (bigOMeasurable == null) {
            throw new IllegalArgumentException("String is null");
        }
        if (bigOMeasurable.isBlank()) {
            throw new IllegalArgumentException("You did not give a class to test");
        }
        if (!Character.isLetter(bigOMeasurable.charAt(0))) {
            // all class/package names start with a letter
            throw new IllegalArgumentException(bigOMeasurable + " is not a valid class name");
        }
        if (timeOutInMs < 1) {
            throw new IllegalArgumentException("Timeout " + timeOutInMs + " is not valid");
        }
    }

    /**
     * Sets the constructor of the class to
     * @param className the name of the class whose constructor we are trying to get
     * @throws IllegalArgumentException if the class does not exist, does not extend BigOMeasurable, or does
     * not have a no-argument constructor
     */
    private void setConstructor(String className) {
        // this part is dealing with extracting the class
        // Once I figure out what it is supposed to be returning, I will probably make it a separate method
        Class<BigOMeasurable> measuredClass;
        try {
            measuredClass = (Class<BigOMeasurable>) Class.forName(className);
            // I don't like this unchecked cast, but since it is generics, I can't figure out how to check it
            // So I am just trying it and hoping nothing breaks
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("The class " + className + " cannot be found", e);
            // that's what we are supposed to do if we have problems
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The class " + className + " does not extend BigOMeasurable", e);
        }

        // now that we have successfully extracted the class, let's extract the constructor
        try {
            constructor = measuredClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The class " + className + " does not have a no-argument constructor", e);
        }
    }

    /**
     * Runs the test x times, and returns the average runtime
     * @param x number of times to run the test
     * @param n size of the input within each test
     * @return the average runtime
     */
    private long getAverageTimeInNLevel(int x, int n) {
        long totalTime = 0; // I don't have to worry about a memory overflow, because if the total elapsed time
            // eclipsed the max carrying capacity, this program has been running for hundreds of millions of years
        for (int i = 0; i < x; i++) {
            BigOMeasurable testObject;
            try {
                testObject = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("I don't know what's going on, but you have a constructor problem", e);
            }
            testObject.setup(n);
            long initialTime = System.nanoTime();
            testObject.execute();
            totalTime += System.nanoTime() - initialTime; // we add the difference to the total amount of
                // time we have spent on execution
            if (stopwatch.runningOutOfTime()) {
                return 0; // we are ending early, so that we stay within the time limit
            }
        }
        return totalTime / x; // we return the average time
    }

    /**
     * The point of this method is to get the JVM used to running things, so that things should
     * go faster and more consistently during the actual testing
     * It is possible that this will cause problems with slow applications, but I kept n at 100
     * and just repeated it a bunch, so that shouldn't be a problem
     * @return true if the program can continue, false if the duration is so short, we are already dead
     */
    private boolean warmUpJVM() {
        for (int i = 0; i < 100; i++) {
            long warning = getAverageTimeInNLevel(100, 100);
            if (warning == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * This static class lets you add ratios to it, and returns the current average of the ratios
     */
    private static class RunningAverage {
        private double ratioSum; // the total of all the ratios added together
        private int numSums; // the total number of ratios that have been added
        private final List<Double> recentRatios;

        private RunningAverage() {
            ratioSum = 0;
            numSums = 0;
            recentRatios = new LinkedList<>();
        }

        /**
         * To only be called internally, adds the ratio to the system, and might shuffle through
         * the recent ratios
         * @param ratio we are adding to the average object
         */
        private void addRatio(double ratio) {
            ratioSum += ratio;
            numSums++;
            recentRatios.add(ratio);
            if (recentRatios.size() == 6) {
                recentRatios.remove(0);
                // if we now have 6, remove the oldest ratio from recent ratios
            }
        }

        private double getRecentAverageWithNewRatio(double ratio) {
            addRatio(ratio);
            return currentRecentAverage();
        }

        /**
         * @return recent average without adding a new ratio
         */
        private double currentRecentAverage() {
            double recentSum = recentRatios.stream().mapToDouble(Double::doubleValue).sum();
            // It felt stupid to loop through this to get a sum, so I am using functions
            return numSums < 5 ? recentSum / numSums : recentSum / 5;
        }

        // delete the below once you replace it
        /**
         * Says if the ratio is good enough to return
         * @param ratio that we are evaluating as being close enough
         * @param outOfTime if we are out of time and need an answer now
         * @return true if we should end the program, false if we should continue or return NaN
         */
        private boolean ratioCloseToAverage(double ratio, boolean outOfTime) {
            if (ratio == Integer.MIN_VALUE) {
                ratio = recentRatios.size() > 4? recentRatios.get(4) : recentRatios.get(recentRatios.size() - 1);
            }
            double currentRatioDeviation = Math.abs(ratio - currentRecentAverage());
            double devianceThreshold = outOfTime? currentRecentAverage() / 3
                    : currentRecentAverage() / 20;
            // that is the amount of deviance I am allowing right now
            // I am currently setting it up that for O(n), it should be about 0.1 under normal
                // circumstances, and close to 0.3 if pressed for time

            return currentRatioDeviation < devianceThreshold;
        }
    }

    private static class Stopwatch {

        private final long startTime;
        private final long timeOut;

        /**
         * Sets up a stopwatch
         * @param timeOut duration when the program must terminate in milliseconds
         */
        private Stopwatch(long timeOut) {
            this.startTime = System.currentTimeMillis();
            this.timeOut = timeOut;
        }

        /**
         * Gets the duration from the current time, measured in milliseconds
         * @return time since the stopwatch was set up, in milliseconds
         */
        private long getDuration() {
            return System.currentTimeMillis() - startTime;
        }

        private boolean runningOutOfTime() {
            return getDuration() > timeOut * 9/10;
        }

    }
}
