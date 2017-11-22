package at.favre.app.blurbenchmark.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Helper class for calculating some simple statistics
 * data. It uses a cache for better performance, but sacrificeing
 * a little memory efficiency.
 *
 * @author pfavre
 */
public class Average<T extends Number> {

    private Double avg;
    private Double normalizedAvg;
    private Double variance;
    private Double mean;
    private Map<Double, ConfidenceIntervall> cache;
    private TreeSet<T> data;

    public Average(Collection<T> data) {
        this();
        this.data = new TreeSet<T>(data);
    }

    public Average() {
        data = new TreeSet<T>();
        reset();
    }

    public void add(T elem) {
        data.add(elem);
        reset();
    }

    public void addAll(Collection<T> data) {
        this.data.addAll(data);
        reset();
    }

    private void reset() {
        cache = new HashMap<Double, ConfidenceIntervall>();
        avg = variance = mean = null;
    }

    public double getAvg() {
        if (avg == null) {
            double sum = 0;
            for (T t : data) {
                sum += t.doubleValue();
            }
            avg = sum / (double) data.size();
        }
        return avg;
    }

    public double getMedian() {
        if (mean == null) {
            List<T> array = new ArrayList<T>(data);
            int middle = array.size() / 2;
            if (array.size() % 2 == 0) {
                T left = array.get(middle - 1);
                T right = array.get(middle);
                mean = (left.doubleValue() + right.doubleValue()) / 2d;
            } else {
                mean = array.get(middle).doubleValue();
            }
        }
        return mean;
    }

    public ConfidenceIntervall get80PercentConfidenceIntervall() {
        return getConfidenceIntervall(1.28d);
    }

    public ConfidenceIntervall get90PercentConfidenceIntervall() {
        return getConfidenceIntervall(1.645d);
    }

    public ConfidenceIntervall get95PercentConfidenceIntervall() {
        return getConfidenceIntervall(1.96d);
    }

    public ConfidenceIntervall get99PercentConfidenceIntervall() {
        return getConfidenceIntervall(2.58d);
    }

    private ConfidenceIntervall getConfidenceIntervall(double confidenceLevel) {
        if (!cache.containsKey(confidenceLevel)) {
            double stddev = Math.sqrt(getVariance());
            double stdErr = confidenceLevel * stddev;
            cache.put(confidenceLevel, new ConfidenceIntervall(getAvg(), stdErr));
        }
        return cache.get(confidenceLevel);
    }

    public double getVariance() {
        if (variance == null) {
            double xxbar = 0.0d;
            for (T t : data) {
                xxbar += Math.pow(t.doubleValue() - getAvg(), 2);
            }

            variance = xxbar / (data.size() - 1);
        }
        return variance;
    }

    public T getMax() {
        return data.last();
    }

    public T getMin() {
        return data.first();
    }

    public List<T> getValuesGreaterThanGiven(double lowerLimit) {
        List<T> overList = new ArrayList<T>();
        for (T t : data) {
            if (lowerLimit < t.doubleValue()) {
                overList.add(t);
            }
        }
        return overList;
    }

    public double getPercentageOverGivenValue(double lowerLimit) {
        double overCount = getValuesGreaterThanGiven(lowerLimit).size();
        double wholeCount = data.size();

        return (overCount * 100) / wholeCount;
    }

    public static class ConfidenceIntervall {
        private final double avg;
        private final double stdError;

        public ConfidenceIntervall(double avg, double stdError) {
            this.avg = avg;
            this.stdError = stdError;
        }

        public double getAvg() {
            return avg;
        }

        public double getStdError() {
            return stdError;
        }
    }
}
