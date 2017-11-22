package at.favre.app.blurbenchmark.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;
import at.favre.app.blurbenchmark.blur.IBlur;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;

/**
 * Helper class that is used to gather the data
 * needed for the table adapter
 *
 * @author pfavre
 */
public class ResultTableModel {
    public static final String TAG = ResultTableModel.class.getSimpleName();
    public static final double BEST_WORST_THRESHOLD_PERCENTAGE = 5;
    public static final String MISSING = "?";

    public static final String NUMBER_FORMAT = "0.00";

    public enum DataType {
        AVG(true, "ms"), MIN(true, "ms"), MAX(true, "ms"), MEDIAN(true, "ms"), CONF_95(true, "ms"), OVER_16_MS(true, "%"), MPIXEL_PER_S(false, "MPix/s");
        private final boolean minIsBest;
        private final String unit;

        DataType(boolean minIsBest, String unit) {
            this.minIsBest = minIsBest;
            this.unit = unit;
        }

        public boolean isMinIsBest() {
            return minIsBest;
        }

        public String getUnit() {
            return unit;
        }
    }

    public enum RelativeType {BEST, WORST, AVG}

    Map<String, Map<String, BenchmarkResultDatabase.BenchmarkEntry>> tableModel;
    List<String> rows;
    List<String> columns;

    public ResultTableModel(BenchmarkResultDatabase db) {
        setUpModel(db);
    }

    private void setUpModel(BenchmarkResultDatabase db) {
        columns = new ArrayList<String>();
        for (EBlurAlgorithm algorithm : EBlurAlgorithm.getAllAlgorithms()) {
            columns.add(algorithm.toString());
        }
        Collections.sort(columns);

        TreeSet<BenchmarkResultDatabase.Category> rowHeaders = new TreeSet<BenchmarkResultDatabase.Category>();
        if (db != null) {
            for (BenchmarkResultDatabase.BenchmarkEntry benchmarkEntry : db.getEntryList()) {
                rowHeaders.add(benchmarkEntry.getCategoryObj());
            }
        }

        rows = new ArrayList<String>();
        for (BenchmarkResultDatabase.Category rowHeader : rowHeaders) {
            rows.add(rowHeader.getCategory());
        }

        tableModel = new HashMap<String, Map<String, BenchmarkResultDatabase.BenchmarkEntry>>();
        for (String column : columns) {
            tableModel.put(column, new HashMap<String, BenchmarkResultDatabase.BenchmarkEntry>());
            for (String row : rows) {
                tableModel.get(column).put(row, db.getByCategoryAndAlgorithm(row, EBlurAlgorithm.valueOf(column)));
            }
        }
    }

    public BenchmarkResultDatabase.BenchmarkEntry getCell(int row, int column) {
        return tableModel.get(columns.get(column)).get(rows.get(row));
    }

    private BenchmarkWrapper getRecentWrapper(int row, int column) {
        BenchmarkResultDatabase.BenchmarkEntry entry = getCell(row, column);
        return BenchmarkResultDatabase.getRecentWrapper(entry);
    }

    public String getValue(int row, int column, DataType type) {
        try {
            BenchmarkWrapper wrapper = getRecentWrapper(row, column);
            if (wrapper != null) {
                return getValueForType(wrapper, type).getRepresentation();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error while getting data", e);
        }
        return MISSING;
    }

    public RelativeType getRelativeType(int row, int column, DataType type, boolean minIsBest) {
        if (row < 0 || column < 0) {
            return RelativeType.AVG;
        }
        List<Double> columns = new ArrayList<Double>();
        BenchmarkResultDatabase.BenchmarkEntry entry;
        BenchmarkWrapper wrapper = null;
        for (int i = 0; i < this.columns.size(); i++) {
            entry = getCell(row, i);

            if (entry != null && !entry.getWrapper().isEmpty()) {
                Collections.sort(entry.getWrapper());
                wrapper = entry.getWrapper().get(0);
            }

            if (wrapper != null) {
                columns.add(getValueForType(wrapper, type).getValue());
            } else {
                columns.add(Double.NEGATIVE_INFINITY);
            }
        }
        List<Double> sortedColumns = new ArrayList<Double>(columns);
        Collections.sort(sortedColumns);

        Double columnVal = columns.get(column);

        if (columnVal.equals(Double.NEGATIVE_INFINITY)) {
            return RelativeType.AVG;
        }

        double minThreshold = sortedColumns.get(0) + (sortedColumns.get(0) * BEST_WORST_THRESHOLD_PERCENTAGE / 100);
        double maxThreshold = sortedColumns.get(columns.size() - 1) - (sortedColumns.get(columns.size() - 1) * BEST_WORST_THRESHOLD_PERCENTAGE / 100);
        if (columnVal >= maxThreshold && columnVal <= sortedColumns.get(columns.size() - 1)) {
            if (minIsBest) {
                return RelativeType.WORST;
            } else {
                return RelativeType.BEST;
            }
        } else if (columnVal <= minThreshold && columnVal >= sortedColumns.get(0)) {
            if (minIsBest) {
                return RelativeType.BEST;
            } else {
                return RelativeType.WORST;
            }
        } else {
            return RelativeType.AVG;
        }
    }

    public List<String> getRows() {
        return rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public static StatValue getValueForType(BenchmarkWrapper wrapper, DataType type) {
        if (wrapper != null) {
            switch (type) {
                case AVG:
                    return new StatValue(wrapper.getStatInfo().getAsAvg().getAvg(),
                            BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg(), NUMBER_FORMAT) + "ms");
                case MIN:
                    return new StatValue(wrapper.getStatInfo().getAsAvg().getMin(),
                            BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMin(), "0.#") + "ms");
                case MAX:
                    return new StatValue(wrapper.getStatInfo().getAsAvg().getMax(),
                            BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMax(), "0.#") + "ms");
                case MEDIAN:
                    return new StatValue(wrapper.getStatInfo().getAsAvg().getMedian(),
                            BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMedian(), "0.#") + "ms");
                case CONF_95:
                    return new StatValue(wrapper.getStatInfo().getAsAvg().get95PercentConfidenceIntervall().getStdError() + wrapper.getStatInfo().getAsAvg().get95PercentConfidenceIntervall().getAvg(),
                            BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().get95PercentConfidenceIntervall().getAvg(), "0.#") + "ms +/-" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().get95PercentConfidenceIntervall().getStdError(), "0.#"));
                case OVER_16_MS:
                    return new StatValue(wrapper.getStatInfo().getAsAvg().getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH),
                            BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH), NUMBER_FORMAT) + "%");
                case MPIXEL_PER_S:
                    return new StatValue(wrapper.getStatInfo().getThroughputMPixelsPerSec(), BenchmarkUtil.formatNum(wrapper.getStatInfo().getThroughputMPixelsPerSec(), NUMBER_FORMAT) + "MP/s");
            }
        }
        return new StatValue();
    }

    public static class StatValue {
        public static final String MISSING = "?";

        private final Double value;
        private final String representation;
        private final boolean noValue;

        public StatValue(Double value, String representation) {
            this.value = value;
            this.representation = representation;
            noValue = false;
        }

        public StatValue() {
            value = Double.NEGATIVE_INFINITY;
            representation = MISSING;
            noValue = true;
        }

        public Double getValue() {
            return value;
        }

        public String getRepresentation() {
            return representation;
        }

        public boolean isNoValue() {
            return noValue;
        }
    }
}
