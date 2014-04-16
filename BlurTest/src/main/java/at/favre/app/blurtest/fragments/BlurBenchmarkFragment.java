package at.favre.app.blurtest.fragments;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.favre.app.blurtest.BenchmarkListAdapter;
import at.favre.app.blurtest.BlurBenchmarkTask;
import at.favre.app.blurtest.R;
import at.favre.app.blurtest.SettingsController;
import at.favre.app.blurtest.activities.MainActivity;
import at.favre.app.blurtest.util.BitmapUtil;
import at.favre.app.blurtest.util.JsonUtil;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BlurBenchmarkFragment extends Fragment implements IFragmentWithBlurSettings{
	private static final String TAG = BlurBenchmarkFragment.class.getSimpleName();

	private static final String BENCHMARK_LIST_KEY = "benchmark_list";

	private static final int MAX_RADIUS = 16;
	private static final int START_RADIUS = 2;
	private static final int BENCHMARK_ROUNDS = 100;
	private static final int[] TEST_SUBJECT_RESID_LIST = {R.drawable.test_100x100_2,R.drawable.test_200x200_2,/*R.drawable.test_300x300_2,R.drawable.test_400x400_2*/};
	private SettingsController settingsController;

	private BenchmarkResultList benchmarkResultList = new BenchmarkResultList();
//	private

	private ListAdapter adapter;

	private ListView listView;
	private View btn;
	private View headerView;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			benchmarkResultList = JsonUtil.fromJsonString(savedInstanceState.getString(BENCHMARK_LIST_KEY), BenchmarkResultList.class);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_benchmark,container,false);
		headerView = inflater.inflate(R.layout.list_benchmark_header,null);

		settingsController = new SettingsController(v,null,null,null,new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				benchmark();
				btn = view;
				btn.setEnabled(false);
				settingsController.switchShow();
			}
		});
		settingsController.setVisibility(false, false, false, true);
		settingsController.setBtnText("Start Benchmark");

		listView = (ListView) v.findViewById(R.id.listview);
		setUpListView();

		settingsController.switchShow();

		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(TEST_SUBJECT_RESID_LIST.length * 4);
		progressDialog.setMessage("Benchmark");

		return v;
	}


	@Override
	public void onResume() {
		super.onResume();
		setBackground();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BENCHMARK_LIST_KEY, JsonUtil.toJsonString(benchmarkResultList));
	}

	private void benchmark() {
		Log.d(TAG,"start benchmark");
		lockOrientation();
		BitmapUtil.clearCacheDir(new File(BitmapUtil.getCacheDir(getActivity())));
		progressDialog.setProgress(0);
		progressDialog.setCancelable(false);
		progressDialog.show();
		benchmarkResultList = new BenchmarkResultList();
		nextTest(0,START_RADIUS);

	}

	private void lockOrientation() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
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
						progressDialog.setProgress(progressDialog.getProgress()+1);
						benchmarkResultList.getBenchmarkWrappers().add(wrapper);
						Log.d(TAG, "next test");
						nextTest(photoIndex, radius * 2);
					}
				}.execute();
			}
		}
	}

	private void testDone() {
		Log.d(TAG,"done benchmark");
		progressDialog.setProgress(progressDialog.getMax());
		progressDialog.hide();
		setUpListView();

		if(btn != null) {
			btn.setEnabled(true);
		}

		setBackground();
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

	private void setBackground() {
		if(!benchmarkResultList.getBenchmarkWrappers().isEmpty()) {
			getView().findViewById(R.id.innerRoot).setBackgroundColor(getResources().getColor(R.color.halftransparent));

			new AsyncTask<Void,Void,Bitmap>() {
				@Override
				protected Bitmap doInBackground(Void... voids) {
					try {
						return Picasso.with(getActivity()).load(benchmarkResultList.getBenchmarkWrappers().get(benchmarkResultList.getBenchmarkWrappers().size() - 1).getBitmapAsFile()).get();
					} catch (IOException e) {
						Log.w(TAG, "Could not set background", e);
						return null;
					}
				}
				@Override
				protected void onPostExecute(Bitmap bitmap) {
					if(getView() != null) {
						getView().findViewById(R.id.root).setBackgroundDrawable(new BitmapDrawable(getActivity().getResources(), bitmap));
					}
				}
			}.execute();
		}
	}

	private void setUpListView() {
		if(!benchmarkResultList.getBenchmarkWrappers().isEmpty()) {
			((TextView) headerView.findViewById(R.id.tv_header)).setText(settingsController.getAlgorithm().toString());
            listView.removeHeaderView(headerView);
			listView.addHeaderView(headerView);
			adapter = new BenchmarkListAdapter(getActivity(), R.id.list_item, benchmarkResultList.getBenchmarkWrappers());
			listView.setAdapter(adapter);
		}
	}

	@Override
	public void switchShowSettings() {
		settingsController.switchShow();
	}

	public static class BenchmarkResultList {
		private List<BlurBenchmarkTask.BenchmarkWrapper> benchmarkWrappers = new ArrayList<BlurBenchmarkTask.BenchmarkWrapper>();

		public BenchmarkResultList() {
		}

		public List<BlurBenchmarkTask.BenchmarkWrapper> getBenchmarkWrappers() {
			return benchmarkWrappers;
		}

		public void setBenchmarkWrappers(List<BlurBenchmarkTask.BenchmarkWrapper> benchmarkWrappers) {
			this.benchmarkWrappers = benchmarkWrappers;
		}
	}
}
