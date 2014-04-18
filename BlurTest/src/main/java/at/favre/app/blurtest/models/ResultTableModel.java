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

    public enum DataType {AVG, MIN_MAX}

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
        return tableModel.get(columns.get(row)).get(rows.get(column));
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
						return BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg(),"0.##") + "ms";
					case MIN_MAX:
						return BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMin(),"0.##") + "/" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMax(),"0.##") + "ms";
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Error while getting data",e);
		}
        return "?";
    }

    public List<String> getRows() {
        return rows;
    }

    public List<String> getColumns() {
        return columns;
    }
}
