package at.favre.app.blurbenchmark.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.inqbarna.tablefixheaders.TableFixHeaders;

import at.favre.app.blurbenchmark.BenchmarkStorage;
import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.adapter.ResultTableAdapter;
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase;
import at.favre.app.blurbenchmark.models.ResultTableModel;
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil;

/**
 * A table view that shows misc. statistics for the benchmark categories (size& blur radius)
 *
 * @author pfavre
 */
public class ResultsBrowserFragment extends Fragment {
    private static final String TAG = ResultsBrowserFragment.class.getSimpleName();

    private TableFixHeaders table;
    private ResultTableModel.DataType dataType = ResultTableModel.DataType.AVG;
    private BenchmarkResultDatabase db;

    public ResultsBrowserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_resultbrowser, container, false);
        table = v.findViewById(R.id.table);
        table.setVisibility(View.GONE);

        v.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        new BenchmarkStorage.AsyncLoadResults() {
            @Override
            protected void onPostExecute(BenchmarkResultDatabase benchmarkResultDatabase) {
                if (isAdded() && isVisible()) {
                    v.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (benchmarkResultDatabase == null) {
                        table.setVisibility(View.GONE);
                        v.findViewById(R.id.tv_noresults).setVisibility(View.VISIBLE);
                    } else {
                        table.setVisibility(View.VISIBLE);
                        table.setAdapter(new ResultTableAdapter(getActivity(), benchmarkResultDatabase, dataType));
                        TranslucentLayoutUtil.setTranslucentThemeInsets(getActivity(), v.findViewById(R.id.tableWrapper));
                    }
                }
            }
        }.execute(getActivity());

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.results_menu, menu);
        Spinner spinner = (Spinner) menu.findItem(R.id.action_select_datatype).getActionView();
        ArrayAdapter<ResultTableModel.DataType> adapter = new ArrayAdapter<>(((AppCompatActivity) getActivity()).getSupportActionBar().getThemedContext(), R.layout.inc_spinner_light, ResultTableModel.DataType.values());
        spinner.setAdapter(adapter);
        if (Build.VERSION.SDK_INT >= 16) {
            spinner.setPopupBackgroundDrawable(getResources().getDrawable(R.drawable.spinner_popup_dark));
        } else if (Build.VERSION.SDK_INT >= 21) {
            spinner.setPopupBackgroundDrawable(getResources().getDrawable(R.drawable.spinner_popup_dark, getActivity().getTheme()));
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setNewDataType((ResultTableModel.DataType) adapterView.getAdapter().getItem(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setGravity(Gravity.RIGHT);
    }

    private void setNewDataType(ResultTableModel.DataType type) {
        dataType = type;
        table.setAdapter(new ResultTableAdapter(getActivity(), BenchmarkStorage.getInstance(getActivity()).loadResultsDB(), dataType));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteData() {
        BenchmarkStorage.getInstance(getActivity()).deleteData();
        table.setAdapter(new ResultTableAdapter(getActivity(), BenchmarkStorage.getInstance(getActivity()).loadResultsDB(), dataType));
    }
}
