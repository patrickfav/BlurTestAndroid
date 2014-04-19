package at.favre.app.blurtest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by PatrickF on 14.04.2014.
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
			double sum=0;
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
			int middle = array.size()  / 2;
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

	private ConfidenceIntervall getConfidenceIntervall(double stdDeviations) {
		if(!cache.containsKey(stdDeviations)) {
			double stddev = Math.sqrt(getVariance());
			double lo = getAvg() - stdDeviations * stddev;
			double hi = getAvg() + stdDeviations * stddev;
			cache.put(stdDeviations, new ConfidenceIntervall(lo, hi, getAvg()));
		}
		return cache.get(stdDeviations);
	}

	public double getVariance() {
		if(variance == null) {
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
            if(lowerLimit < t.doubleValue()) {
                overList.add(t);
            }
        }
        return overList;
    }

    public double getPercentageOverGivenValue(double lowerLimit) {
        double overCount = getValuesGreaterThanGiven(lowerLimit).size();
        double wholeCount = data.size();

        return wholeCount * overCount /100;
    }

	public static class ConfidenceIntervall {
		private final double high;
		private final double low;
		private final double avg;

		public ConfidenceIntervall(double low, double high, double avg) {
			this.high = high;
			this.low = low;
			this.avg = avg;
		}

		public double getHigh() {
			return high;
		}

		public double getLow() {
			return low;
		}

		public double getAvg() {
			return avg;
		}

		public double getDeviationsInPercent() {
			return (high-avg + avg-low)/2d;
		}
	}
}
