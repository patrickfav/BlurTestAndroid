package at.favre.app.blurtest.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import at.favre.app.blurtest.util.BenchmarkUtil;
import at.favre.app.blurtest.util.BlurUtil;

/**
 * Created by PatrickF on 18.04.2014.
 */
public class ResultTableModel {
	public static final String TAG = ResultTableModel.class.getSimpleName();
    public static final String MISSING = "?";
    public static final String NUMBER_FORMAT = "0.00";
    public static final String NUMBER_FORMAT_SORT = "00000000000.0000";

    public enum DataType {AVG, MIN_MAX}
    public enum RelativeType {BEST, WORST, AVG}

    Map<String,Map<String,BenchmarkResultDatabase.BenchmarkEntry>> tableModel;
    List<String> rows;
    List<String> columns;

    public ResultTableModel(BenchmarkResultDatabase db) {
        setUpModel(db);
    }

    private void setUpModel(BenchmarkResultDatabase db) {
        columns = new ArrayList<String>();
        for (BlurUtil.Algorithm algorithm : BlurUtil.Algorithm.values()) {
            columns.add(algorithm.toString());
        }
        Collections.sort(columns);

        TreeSet<String> rowHeaders = new TreeSet<String>();
        for (BenchmarkResultDatabase.BenchmarkEntry benchmarkEntry : db.getEntryList()) {
            rowHeaders.add(benchmarkEntry.getCategory());
        }
        rows = new ArrayList<String>(rowHeaders);

        tableModel = new HashMap<String, Map<String, BenchmarkResultDatabase.BenchmarkEntry>>();
        for (String column : columns) {
            tableModel.put(column, new HashMap<String, BenchmarkResultDatabase.BenchmarkEntry>());
            for (String row : rows) {
                tableModel.get(column).put(row, db.getByCategoryAndAlgorithm(row, BlurUtil.Algorithm.valueOf(column)));
            }
        }
    }

    public BenchmarkResultDatabase.BenchmarkEntry getCell(int row,int column) {
        return tableModel.get(columns.get(column)).get(rows.get(row));
    }

    private BenchmarkWrapper getRecentWrapper(int row, int column) {
        BenchmarkResultDatabase.BenchmarkEntry entry = getCell(row,column);
        if(entry != null && !entry.getWrapper().isEmpty()) {
            Collections.sort(entry.getWrapper());
            return entry.getWrapper().get(0);
        } else {
            return null;
        }
    }

    public String getValue(int row, int column, DataType type) {
		try {
			BenchmarkWrapper wrapper = getRecentWrapper(row, column);
			if (wrapper != null) {
				switch (type) {
					case AVG:
						return BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg(),NUMBER_FORMAT) + "ms";
					case MIN_MAX:
						return BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMin(),NUMBER_FORMAT) + "/" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMax(),NUMBER_FORMAT) + "ms";
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Error while getting data",e);
		}
        return MISSING;
    }

    public RelativeType getRelativeType(int row,int column, DataType type) {
        if(row < 0 || column < 0) {
            return RelativeType.AVG;
        }
        List<String> columns = new ArrayList<String>();
        BenchmarkResultDatabase.BenchmarkEntry entry;
        BenchmarkWrapper wrapper=null;
        for (int i = 0; i < this.columns.size(); i++) {
            entry = getCell(row,i);

            if(entry != null && !entry.getWrapper().isEmpty()) {
                Collections.sort(entry.getWrapper());
                wrapper = entry.getWrapper().get(0);
            }

            if(wrapper != null) {
                switch (type) {
                    case AVG:
                        columns.add(BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg(), NUMBER_FORMAT_SORT));
                        break;
                    case MIN_MAX:
                        columns.add(BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMin(), NUMBER_FORMAT_SORT) + "/" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMax(), NUMBER_FORMAT_SORT));
                        break;
                }
            } else {
                columns.add(MISSING);
            }
        }
        List<String> sortedColumns = new ArrayList<String>(columns);
        Collections.sort(sortedColumns);

        String columnVal = columns.get(column);

        if(columnVal.equals(MISSING)) {
            return RelativeType.AVG;
        }

        int order = sortedColumns.indexOf(columnVal);
        if(order == 0) {
            return RelativeType.BEST;
        } else if(order == (columns.size()-1)) {
            return RelativeType.WORST;
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
}
