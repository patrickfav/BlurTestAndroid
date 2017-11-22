package at.favre.app.blurbenchmark.fragments;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
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

import at.favre.app.blurbenchmark.BenchmarkStorage;
import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;
import at.favre.app.blurbenchmark.blur.IBlur;
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase;
import at.favre.app.blurbenchmark.models.ResultTableModel;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;
import at.favre.app.blurbenchmark.util.GraphUtil;
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil;

/**
 * Shows the results of past benchmarks in a Diagram, where misc. statistics can be
 * choosen.
 *
 * @author pfavre
 */
public class ResultsDiagramFragment extends Fragment {
    private static final String TAG = ResultsDiagramFragment.class.getSimpleName();

    private static final String DATATYPE_KEY = "DATATYPE_KEY";
    private static final String RADIUS_KEY = "RADIUS_KEY";

    private List<ResultTableModel.DataType> dataTypeList = Arrays.asList(ResultTableModel.DataType.values());
    private List<Integer> radiusList;
    private Spinner radiusSpinner, dataTypeSpinner;

    private int radius = 16;
    private ResultTableModel.DataType dataType = ResultTableModel.DataType.AVG;

    public ResultsDiagramFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            radius = savedInstanceState.getInt(RADIUS_KEY);
            dataType = ResultTableModel.DataType.valueOf(savedInstanceState.getString(DATATYPE_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_resultsdiagram, container, false);
        v.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        v.findViewById(R.id.contentWrapper).setVisibility(View.INVISIBLE);

        new BenchmarkStorage.AsyncLoadResults() {
            @Override
            protected void onPostExecute(BenchmarkResultDatabase db) {
                if (isAdded() && isVisible()) {
                    v.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (db == null) {
                        v.findViewById(R.id.contentWrapper).setVisibility(View.GONE);
                        v.findViewById(R.id.tv_noresults).setVisibility(View.VISIBLE);
                    } else {
                        createUI(v, db);
                    }
                }
            }
        }.execute(getActivity());

        TranslucentLayoutUtil.setTranslucentThemeInsets(getActivity(), v.findViewById(R.id.contentWrapper));
        return v;
    }

    private void createUI(View v, BenchmarkResultDatabase db) {
        v.findViewById(R.id.contentWrapper).setVisibility(View.VISIBLE);

        radiusList = new ArrayList<Integer>(db.getAllBlurRadii());
        radiusSpinner = v.findViewById(R.id.spinner_radius);
        radiusSpinner.setAdapter(new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_dropdown_item, radiusList));
        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                radius = ((Integer) adapterView.getAdapter().getItem(i));
                updateGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        radiusSpinner.setSelection(radiusList.indexOf(radius));

        dataTypeSpinner = v.findViewById(R.id.spinner_datatypes);
        dataTypeSpinner.setAdapter(new ArrayAdapter<ResultTableModel.DataType>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dataTypeList));
        dataTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dataType = ((ResultTableModel.DataType) adapterView.getAdapter().getItem(i));
                updateGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        dataTypeSpinner.setSelection(dataTypeList.indexOf(dataType));
    }

    private void updateGraph() {
        if (BenchmarkStorage.getInstance(getActivity()).loadResultsDB() != null) {
            FrameLayout layout = getView().findViewById(R.id.graph);
            layout.removeAllViews();
            layout.addView(createGraph(BenchmarkStorage.getInstance(getActivity()).loadResultsDB(), dataType, radius));
        }
    }

    private GraphView createGraph(BenchmarkResultDatabase database, final ResultTableModel.DataType dataType, int blurRadius) {
        Resources res = getResources();
        int lineThicknessPx = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics()));

        Map<EBlurAlgorithm, List<GraphView.GraphViewData>> dataMap = new HashMap<EBlurAlgorithm, List<GraphView.GraphViewData>>();
        final List<BenchmarkResultDatabase.ImageSize> imageSizes = new ArrayList<BenchmarkResultDatabase.ImageSize>(database.getAllImageSizes());
        for (EBlurAlgorithm eBlurAlgorithm : EBlurAlgorithm.getAllAlgorithms()) {
            dataMap.put(eBlurAlgorithm, new ArrayList<GraphView.GraphViewData>());
            int i = 0;
            for (BenchmarkResultDatabase.ImageSize imageSize : imageSizes) {
                ResultTableModel.StatValue val = ResultTableModel.getValueForType(BenchmarkResultDatabase.getRecentWrapper(database.getByImageSizeAndRadiusAndAlgorithm(imageSize.getImageSizeString(), blurRadius, eBlurAlgorithm)), dataType);
                if (val.getValue() != Double.NEGATIVE_INFINITY) {
                    dataMap.get(eBlurAlgorithm).add(i, new GraphView.GraphViewData(i, val.getValue()));
                    i++;
                } else {
                    break;
                }
            }
        }

        LineGraphView graphView = new LineGraphView(getActivity(), "");

        for (EBlurAlgorithm eBlurAlgorithm : dataMap.keySet()) {
            GraphViewSeries.GraphViewSeriesStyle seriesStyle = new GraphViewSeries.GraphViewSeriesStyle(res.getColor(eBlurAlgorithm.getColorResId()), lineThicknessPx);
            graphView.addSeries(new GraphViewSeries(eBlurAlgorithm.toString(), seriesStyle, dataMap.get(eBlurAlgorithm).toArray(new GraphView.GraphViewData[dataMap.get(eBlurAlgorithm).size()])));
        }

        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.setDrawBackground(false);
        graphView.setShowLegend(true);
        graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (!isValueX) {
                    return BenchmarkUtil.formatNum(value, "0.0") + dataType.getUnit();
                } else {
                    if (!imageSizes.isEmpty()) {
                        return imageSizes.get((int) Math.round(value)).getImageSizeString();
                    }
                    return "";
                }
            }
        });
        if (dataType.getUnit().equalsIgnoreCase("ms") && dataType.isMinIsBest() && !imageSizes.isEmpty()) {
            graphView.addSeries(GraphUtil.getStraightLine(IBlur.MS_THRESHOLD_FOR_SMOOTH, imageSizes.size() - 1, "16ms", new GraphViewSeries.GraphViewSeriesStyle(res.getColor(R.color.graphMidnightBlue), lineThicknessPx)));
        }
        graphView.getGraphViewStyle().setHorizontalLabelsColor(res.getColor(R.color.optionsTextColor));
        graphView.getGraphViewStyle().setNumHorizontalLabels(6);
        graphView.getGraphViewStyle().setVerticalLabelsColor(res.getColor(R.color.optionsTextColor));
        graphView.getGraphViewStyle().setNumVerticalLabels(6);
        graphView.getGraphViewStyle().setVerticalLabelsAlign(Paint.Align.CENTER);
        graphView.getGraphViewStyle().setVerticalLabelsWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, res.getDisplayMetrics()));
        graphView.getGraphViewStyle().setTextSize((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9, res.getDisplayMetrics())));
        graphView.getGraphViewStyle().setLegendWidth((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115, res.getDisplayMetrics())));
        return graphView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DATATYPE_KEY, dataType.toString());
        outState.putInt(RADIUS_KEY, radius);
    }
}
