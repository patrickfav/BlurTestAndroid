package at.favre.app.blurtest.fragments;

import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.models.BenchmarkWrapper;
import at.favre.app.blurtest.util.JsonUtil;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class BenchmarkDetailsDialog extends DialogFragment {
	private static final String WRAPPER_KEY = "wrapperKey";


	private BenchmarkWrapper wrapper;
	private DecimalFormat format;

	public static BenchmarkDetailsDialog createInstance(BenchmarkWrapper wrapper) {
		BenchmarkDetailsDialog dialog = new BenchmarkDetailsDialog();
		dialog.setWrapper(wrapper);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			wrapper = JsonUtil.fromJsonString(savedInstanceState.getString(WRAPPER_KEY),BenchmarkWrapper.class);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_benchmark_details,container,false);
		Picasso.with(getActivity()).load(wrapper.getBitmapAsFile()).into((android.widget.ImageView) v.findViewById(R.id.image));

		FrameLayout layout = (FrameLayout) v.findViewById(R.id.graph);
		layout.addView(createGraph(wrapper));
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		return v;
	}


	private GraphView createGraph(BenchmarkWrapper wrapper) {
		Resources res = getResources();
		int lineThicknessPx = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics()));

		format = new DecimalFormat("#.0");
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		format.setRoundingMode(RoundingMode.HALF_UP);

		GraphView.GraphViewData[] data  = new GraphView.GraphViewData[wrapper.getStatInfo().getBenchmarkData().size()];
		for (int j = 0; j < wrapper.getStatInfo().getBenchmarkData().size(); j++) {
			data[j] = new GraphView.GraphViewData(j+1,wrapper.getStatInfo().getBenchmarkData().get(j));
		}

		LineGraphView graphView = new LineGraphView(getActivity() , "");
		GraphViewSeries.GraphViewSeriesStyle seriesStyle = new GraphViewSeries.GraphViewSeriesStyle(res.getColor(R.color.graphBgGreen),lineThicknessPx);
//		seriesStyle.setValueDependentColor(new ValueDependentColor() {
//			@Override
//			public int get(GraphViewDataInterface data) {
//
//				if (data.getY() > 24) {
//					return getResources().getColor(R.color.graphBgRed);
//				} else if (data.getY() > 16) {
//					return getResources().getColor(R.color.graphBgYellow);
//				} else {
//					return getResources().getColor(R.color.graphBgGreen);
//				}
//			}
//		});
		if(wrapper.getStatInfo().getAsAvg().getMin() <= 16) {
			graphView.addSeries(getStraightLine(16, wrapper.getStatInfo().getBenchmarkData().size(), "16ms", new GraphViewSeries.GraphViewSeriesStyle(res.getColor(R.color.graphBgRed), lineThicknessPx)));
		}
		graphView.addSeries(getStraightLine((int) wrapper.getStatInfo().getAsAvg().getAvg(), wrapper.getStatInfo().getBenchmarkData().size(), "Avg", new GraphViewSeries.GraphViewSeriesStyle(res.getColor(R.color.graphBlue), lineThicknessPx)));
		graphView.addSeries(new GraphViewSeries("Blur", seriesStyle, data));
		graphView.setScrollable(true);
		graphView.setScalable(true);
		graphView.setManualYAxis(true);
		graphView.getGraphViewStyle().setGridColor(res.getColor(R.color.transparent));
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (!isValueX) {
					return Math.round(value) + "ms";
				} else {
					return null;
				}
			}
		});
		graphView.setManualYAxisBounds(wrapper.getStatInfo().getAsAvg().getMax(), Math.max(0, wrapper.getStatInfo().getAsAvg().getMin() - 3l));
		graphView.setDrawBackground(false);
		graphView.setShowLegend(true);

		graphView.getGraphViewStyle().setHorizontalLabelsColor(res.getColor(R.color.transparent));
		graphView.getGraphViewStyle().setNumHorizontalLabels(0);
		graphView.getGraphViewStyle().setVerticalLabelsColor(res.getColor(R.color.optionsTextColor));
		graphView.getGraphViewStyle().setNumVerticalLabels(4);
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Paint.Align.CENTER);
		graphView.getGraphViewStyle().setVerticalLabelsWidth( (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, res.getDisplayMetrics()));

		return graphView;
	}


	private GraphViewSeries getStraightLine(int heightY,int maxX,String name,GraphViewSeries.GraphViewSeriesStyle seriesStyle) {
		GraphView.GraphViewData[] data  = new GraphView.GraphViewData[2];
		data[0] = new GraphView.GraphViewData(0,heightY);
		data[1] = new GraphView.GraphViewData(maxX,heightY);
		return new GraphViewSeries(name,seriesStyle,data);
	}


	public void setWrapper(BenchmarkWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(WRAPPER_KEY, JsonUtil.toJsonString(wrapper));
	}
}
