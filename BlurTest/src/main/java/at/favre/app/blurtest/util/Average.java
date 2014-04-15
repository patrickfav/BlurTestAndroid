package at.favre.app.blurtest.util;

import java.util.Collection;
import java.util.HashMap;
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

	public double getNormalizedAvg() {
		if (normalizedAvg == null) {
			double sum=0;
			for (T t : data) {
				if(!t.equals(getMax()) && !t.equals(getMin())) {
					sum += t.doubleValue();
				}
			}
			normalizedAvg = sum / (double) data.size()-2;
		}
		return normalizedAvg;
	}

	public double getMedian() {
		if (mean == null) {
			T[] array = (T[]) data.toArray();
			int middle = array.length / 2;
			if (array.length % 2 == 0) {
				T left = array[middle - 1];
				T right = array[middle];
				mean = (left.doubleValue() + right.doubleValue()) / 2d;
			} else {
				mean = array[middle].doubleValue();
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
			double lo = getNormalizedAvg() - stdDeviations * stddev;
			double hi = getNormalizedAvg() + stdDeviations * stddev;
			cache.put(stdDeviations, new ConfidenceIntervall(lo, hi, getNormalizedAvg()));
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
