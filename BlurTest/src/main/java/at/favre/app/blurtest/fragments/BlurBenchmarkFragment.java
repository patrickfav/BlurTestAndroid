package at.favre.app.blurtest.fragments;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import at.favre.app.blurtest.BenchmarkListAdapter;
import at.favre.app.blurtest.BlurBenchmarkTask;
import at.favre.app.blurtest.R;
import at.favre.app.blurtest.SettingsController;
import at.favre.app.blurtest.activities.MainActivity;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BlurBenchmarkFragment extends Fragment implements IFragmentWithBlurSettings{
	private static final String TAG = BlurBenchmarkFragment.class.getSimpleName();

	private static final int MAX_RADIUS = 16;
	private static final int START_RADIUS = 2;
	private static final int BENCHMARK_ROUNDS = 100;
	private static final int[] TEST_SUBJECT_RESID_LIST = {R.drawable.test_100x100,R.drawable.test_200x200,R.drawable.test_400x400};
	private SettingsController settingsController;

	private List<BlurBenchmarkTask.BenchmarkWrapper> benchmarkWrappers;

	private ProgressBar progressBar;
	private ListView listView;
	private View btn;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_benchmark,container,false);

		settingsController = new SettingsController(v,null,null,null,new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				benchmark();
				btn = view;
				btn.setEnabled(false);
				settingsController.switchShow();
			}
		});
		settingsController.setVisibility(false,false,false,true);
		settingsController.setBtnText("Start Benchmark");
		progressBar = (ProgressBar) v.findViewById(R.id.progress);
		progressBar.setIndeterminate(false);
		progressBar.setMax(TEST_SUBJECT_RESID_LIST.length * 4);

		listView = (ListView) v.findViewById(R.id.listview);

		settingsController.switchShow();
		return v;
	}

	private void benchmark() {
		Log.d(TAG,"start benchmark");
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);
		benchmarkWrappers = new ArrayList<BlurBenchmarkTask.BenchmarkWrapper>();
		nextTest(0,START_RADIUS);
	}

	private void nextTest(final int photoIndex, final int radius) {
		if(radius > MAX_RADIUS) {
			nextTest(photoIndex+1,START_RADIUS);
		} else {
			if(photoIndex >= TEST_SUBJECT_RESID_LIST.length) {
				testDone();
			} else {
				new BlurBenchmarkTask(TEST_SUBJECT_RESID_LIST[photoIndex], BENCHMARK_ROUNDS, radius, settingsController.getAlgorithm(), ((MainActivity) getActivity()).getRs(), getActivity()) {
					@Override
					protected void onPostExecute(BenchmarkWrapper wrapper) {
						progressBar.setProgress(progressBar.getProgress()+1);
						benchmarkWrappers.add(wrapper);
						Log.d(TAG,"next test");
						nextTest(photoIndex, radius * 2);
					}
				}.execute();
			}
		}
	}

	private void testDone() {
		Log.d(TAG,"done benchmark");
		progressBar.setProgress(progressBar.getMax());
		progressBar.setVisibility(View.GONE);
		listView.setAdapter(new BenchmarkListAdapter(getActivity(),R.id.list_item,benchmarkWrappers));

		if(btn != null) {
			btn.setEnabled(true);
		}

		getView().findViewById(R.id.innerRoot).setBackgroundColor(getResources().getColor(R.color.halftransparent));
		getView().findViewById(R.id.root).setBackgroundDrawable(new BitmapDrawable(getResources(),benchmarkWrappers.get(benchmarkWrappers.size()-1).getResultBitmap()));
	}

	@Override
	public void switchShowSettings() {
		settingsController.switchShow();
	}
}
