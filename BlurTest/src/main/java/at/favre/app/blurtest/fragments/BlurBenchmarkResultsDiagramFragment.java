package at.favre.app.blurtest.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.activities.MainActivity;
import at.favre.app.blurtest.blur.EBlurAlgorithm;
import at.favre.app.blurtest.models.BenchmarkResultDatabase;
import at.favre.app.blurtest.models.ResultTableModel;
import at.favre.app.blurtest.util.BenchmarkUtil;
import at.favre.app.blurtest.util.JsonUtil;
import at.favre.app.blurtest.util.TranslucentLayoutUtil;

/**
 * Created by PatrickF on 22.04.2014.
 */
public class BlurBenchmarkResultsDiagramFragment extends Fragment {
	private static final String TAG = BlurBenchmarkResultsDiagramFragment.class.getSimpleName();

	private static final String DATATYPE_KEY ="DATATYPE_KEY";
	private static final String RADIUS_KEY ="RADIUS_KEY";

	private List<ResultTableModel.DataType> dataTypeList = Arrays.asList(ResultTableModel.DataType.values());
	private List<Integer> radiusList;
	private BenchmarkResultDatabase db;
	private Spinner radiusSpinner, dataTypeSpinner;

	private int radius=16;
	private ResultTableModel.DataType dataType = ResultTableModel.DataType.AVG;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			radius = savedInstanceState.getInt(RADIUS_KEY);
			dataType = ResultTableModel.DataType.valueOf(savedInstanceState.getString(DATATYPE_KEY));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_benchmarkdiagram,container,false);
		radiusList = new ArrayList<Integer>(loadResultsDB().getAllBlurRadii());
		radiusSpinner = (Spinner)  v.findViewById(R.id.spinner_radius);
		radiusSpinner.setAdapter(new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_dropdown_item,radiusList));
		radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				radius = ((Integer)adapterView.getAdapter().getItem(i));
				updateGraph();
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		radiusSpinner.setSelection(radiusList.indexOf(radius));

		dataTypeSpinner = (Spinner)  v.findViewById(R.id.spinner_datatypes);
		dataTypeSpinner.setAdapter(new ArrayAdapter<ResultTableModel.DataType>(getActivity(),android.R.layout.simple_spinner_dropdown_item,dataTypeList));
		dataTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				dataType = ((ResultTableModel.DataType)adapterView.getAdapter().getItem(i));
				updateGraph();
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		dataTypeSpinner.setSelection(dataTypeList.indexOf(dataType));

		TranslucentLayoutUtil.setTranslucentThemeInsets(getActivity(), v.findViewById(R.id.contentWrapper));
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateGraph();
	}

	private void updateGraph() {
		FrameLayout layout = (FrameLayout) getView().findViewById(R.id.graph);
		layout.removeAllViews();
		layout.addView(createGraph(loadResultsDB(), dataType,radius));
	}

	private GraphView createGraph(BenchmarkResultDatabase database, final ResultTableModel.DataType dataType, int blurRadius) {
		Resources res = getResources();
		int lineThicknessPx = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics()));

		Map<EBlurAlgorithm,List<GraphView.GraphViewData>> dataMap = new HashMap<EBlurAlgorithm, List<GraphView.GraphViewData>>();
		final List<String> imageSizes = new ArrayList<String>(database.getAllImageSizes());
		for (EBlurAlgorithm eBlurAlgorithm : EBlurAlgorithm.getAllAlgorithms()) {
			dataMap.put(eBlurAlgorithm,new ArrayList<GraphView.GraphViewData>());
			int i = 0;
			for (String imageSize : imageSizes) {
				dataMap.get(eBlurAlgorithm).add(i,new GraphView.GraphViewData(i,ResultTableModel.getValueForType(BenchmarkResultDatabase.getRecentWrapper(database.getByImageSizeAndRadiusAndAlgorithm(imageSize, blurRadius, eBlurAlgorithm)),dataType).getValue()));
				i++;
			}
		}

		LineGraphView graphView = new LineGraphView(getActivity() , "");


		for (EBlurAlgorithm eBlurAlgorithm : dataMap.keySet()) {
			GraphViewSeries.GraphViewSeriesStyle seriesStyle = new GraphViewSeries.GraphViewSeriesStyle(res.getColor(eBlurAlgorithm.getColorResId()),lineThicknessPx);
			graphView.addSeries(new GraphViewSeries(eBlurAlgorithm.toString(), seriesStyle,dataMap.get(eBlurAlgorithm).toArray(new GraphView.GraphViewData[dataMap.get(eBlurAlgorithm).size()])));
		}

		graphView.setScrollable(true);
		graphView.setScalable(true);
		graphView.setDrawBackground(false);
		graphView.setShowLegend(true);
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (!isValueX) {
					return BenchmarkUtil.formatNum(value,"0.0") + dataType.getUnit();
				} else {
					return imageSizes.get((int) Math.round(value));
				}
			}
		});
		graphView.getGraphViewStyle().setHorizontalLabelsColor(res.getColor(R.color.optionsTextColor));
		graphView.getGraphViewStyle().setNumHorizontalLabels(6);
		graphView.getGraphViewStyle().setVerticalLabelsColor(res.getColor(R.color.optionsTextColor));
		graphView.getGraphViewStyle().setNumVerticalLabels(6);
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Paint.Align.CENTER);
		graphView.getGraphViewStyle().setVerticalLabelsWidth( (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, res.getDisplayMetrics()));
		graphView.getGraphViewStyle().setTextSize((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9, res.getDisplayMetrics())));
		graphView.getGraphViewStyle().setLegendWidth((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115, res.getDisplayMetrics())));
		return graphView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DATATYPE_KEY,dataType.toString());
		outState.putInt(RADIUS_KEY, radius);
	}

	private BenchmarkResultDatabase loadResultsDB() {
		if (db == null) {
			Log.d(TAG, "start load db");
			SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
			String resultsString = settings.getString(MainActivity.PREF_RESULTS, null);
			if (resultsString != null) {
				db = JsonUtil.fromJsonString(resultsString, BenchmarkResultDatabase.class);
				Log.d(TAG, "done load db");
			} else {
				Log.d(TAG, "done load db");
			}
		}
		return db;
	}
}
