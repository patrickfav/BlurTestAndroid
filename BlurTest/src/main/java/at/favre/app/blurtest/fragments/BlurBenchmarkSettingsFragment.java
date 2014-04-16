package at.favre.app.blurtest.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.favre.app.blurtest.BlurBenchmarkTask;
import at.favre.app.blurtest.R;
import at.favre.app.blurtest.activities.BenchmarkResultActivity;
import at.favre.app.blurtest.activities.MainActivity;
import at.favre.app.blurtest.models.BenchmarkResultList;
import at.favre.app.blurtest.models.BenchmarkWrapper;
import at.favre.app.blurtest.util.BitmapUtil;
import at.favre.app.blurtest.util.BlurUtil;
import at.favre.app.blurtest.util.JsonUtil;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BlurBenchmarkSettingsFragment extends Fragment {
	private static final String TAG = BlurBenchmarkSettingsFragment.class.getSimpleName();

	private static List<BlurUtil.Algorithm> algorithmList = new ArrayList<BlurUtil.Algorithm>(Arrays.asList(BlurUtil.Algorithm.values()));
	private static Rounds[] roundArray = new Rounds[] {new Rounds(10),new Rounds(25),new Rounds(50),new Rounds(100),new Rounds(250),new Rounds(500),new Rounds(1000)};

	private static final String ROUNDS_KEY = "ROUNDS_KEY";
	private static final String ALGO_KEY = "ALGO_KEY";

	private BlurUtil.Algorithm algorithm = BlurUtil.Algorithm.RS_GAUSSIAN;
	private int rounds=100;
	private BenchmarkResultList benchmarkResultList = new BenchmarkResultList();

	private Button btn;
	private Spinner algorithmSpinner;
	private Spinner roundsSpinner;

	private CheckBox cBradius4px;
	private CheckBox cBradius8px;
	private CheckBox cBradius16px;
	private CheckBox cBradius24px;

	private CheckBox cbSize100;
	private CheckBox cbSize200;
	private CheckBox cbSize300;
	private CheckBox cbSize400;
	private CheckBox cbSize500;
	private CheckBox cbSize600;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState !=  null) {
			rounds = savedInstanceState.getInt(ROUNDS_KEY);
			algorithm = BlurUtil.Algorithm.valueOf(savedInstanceState.getString(ALGO_KEY));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bechmark_settings,container,false);

		btn = (Button) v.findViewById(R.id.btn_start);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				benchmark();
			}
		});
		btn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				roundsSpinner.setSelection(0);
				cbSize100.setChecked(true);cbSize200.setChecked(true);cbSize300.setChecked(true);
				cbSize400.setChecked(false);cbSize500.setChecked(false);cbSize600.setChecked(false);
				cBradius4px.setChecked(true);cBradius8px.setChecked(true);cBradius16px.setChecked(true);cBradius24px.setChecked(false);
				return true;
			}
		});

		cBradius4px = (CheckBox) v.findViewById(R.id.cb_r_4px);
		cBradius8px = (CheckBox) v.findViewById(R.id.cb_r_8px);
		cBradius16px = (CheckBox) v.findViewById(R.id.cb_r_16px);
		cBradius24px = (CheckBox) v.findViewById(R.id.cb_r_24px);

		cbSize100 = (CheckBox) v.findViewById(R.id.cb_s_100);
		cbSize200 = (CheckBox) v.findViewById(R.id.cb_s_200);
		cbSize300 = (CheckBox) v.findViewById(R.id.cb_s_300);
		cbSize400 = (CheckBox) v.findViewById(R.id.cb_s_400);
		cbSize500 = (CheckBox) v.findViewById(R.id.cb_s_500);
		cbSize600 = (CheckBox) v.findViewById(R.id.cb_s_600);

		algorithmSpinner = (Spinner)  v.findViewById(R.id.spinner_algorithm);
		algorithmSpinner.setAdapter(new ArrayAdapter<BlurUtil.Algorithm>(getActivity(),android.R.layout.simple_spinner_dropdown_item, algorithmList));
		algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				algorithm = algorithmList.get(i);
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		algorithmSpinner.setSelection(algorithmList.indexOf(algorithm));

		roundsSpinner = (Spinner)  v.findViewById(R.id.spinner_rounds);
		roundsSpinner.setAdapter(new ArrayAdapter<Rounds>(getActivity(),android.R.layout.simple_spinner_dropdown_item,roundArray));
		roundsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				rounds = ((Rounds)adapterView.getAdapter().getItem(i)).getRounds();
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		roundsSpinner.setSelection(Arrays.asList(roundArray).indexOf(new Rounds(rounds)));

		return v;
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ROUNDS_KEY,rounds);
		outState.putString(ALGO_KEY,algorithm.toString());
	}

	private List<Integer> getImagesFromSettings() {
		List<Integer> images = new ArrayList<Integer>();
		if(cbSize100.isChecked()) {
			images.add(R.drawable.test_100x100_2);
		}
		if(cbSize200.isChecked()) {
			images.add(R.drawable.test_200x200_2);
		}
		if(cbSize300.isChecked()) {
			images.add(R.drawable.test_300x300_2);
		}
		if(cbSize400.isChecked()) {
			images.add(R.drawable.test_400x400_2);
		}
		if(cbSize500.isChecked()) {
			images.add(R.drawable.test_500x500_2);
		}
		if(cbSize600.isChecked()) {
			images.add(R.drawable.test_600x600_2);
		}
		return images;
	}

	private List<Integer> getRadiusSizesFromSettings() {
		List<Integer> radius = new ArrayList<Integer>();
		if(cBradius4px.isChecked()) {
			radius.add(4);
		}
		if(cBradius8px.isChecked()) {
			radius.add(8);
		}
		if(cBradius16px.isChecked()) {
			radius.add(16);
		}
		if(cBradius24px.isChecked()) {
			radius.add(24);
		}
		return radius;
	}

	private void benchmark() {
		Log.d(TAG,"start benchmark");

		List<Integer> radius = getRadiusSizesFromSettings();
		List<Integer> images = getImagesFromSettings();

		if(radius.isEmpty() || images.isEmpty()) {
			Toast.makeText(getActivity(),"Choose at least one radius and image size",Toast.LENGTH_SHORT).show();
			return;
		}
		BitmapUtil.clearCacheDir(new File(BitmapUtil.getCacheDir(getActivity())));
		showProgressDialog(radius.size()*images.size());
		benchmarkResultList = new BenchmarkResultList();
		nextTest(0,0,images,radius);
	}

	private void showProgressDialog(int max) {
		lockOrientation();
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Benchmark in progress");
		progressDialog.setMax(max);
		progressDialog.setProgress(0);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	private void lockOrientation() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}

	private void nextTest(final int photoIndex, final int radiusIndex, final List<Integer> imageList , final List<Integer> radiusList) {
		if(radiusIndex >= radiusList.size()) {
			nextTest(photoIndex+1,0,imageList,radiusList);
		} else {
			if(photoIndex >= imageList.size()) {
				testDone();
			} else {
				new BlurBenchmarkTask(imageList.get(photoIndex), rounds, radiusList.get(radiusIndex), algorithm, ((MainActivity) getActivity()).getRs(), getActivity()) {
					@Override
					protected void onPostExecute(BenchmarkWrapper wrapper) {
						progressDialog.setProgress(progressDialog.getProgress()+1);
						benchmarkResultList.getBenchmarkWrappers().add(wrapper);
						Log.d(TAG, "next test");
						nextTest(photoIndex, radiusIndex+1,imageList,radiusList);
					}
				}.execute();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	private void testDone() {
		Log.d(TAG, "done benchmark");
		progressDialog.setProgress(progressDialog.getMax());
		progressDialog.dismiss();

		if(btn != null) {
			btn.setEnabled(true);
		}
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		Intent i = new Intent(getActivity(), BenchmarkResultActivity.class);
		i.putExtra(BenchmarkResultActivity.BENCHMARK_LIST_KEY,JsonUtil.toJsonString(benchmarkResultList));
		startActivity(i);
    }

	public static class Rounds {
		private int rounds;

		public Rounds(int rounds) {
			this.rounds = rounds;
		}

		public int getRounds() {
			return rounds;
		}

		@Override
		public String toString() {
			return rounds+" Rounds per Benchmark";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Rounds rounds1 = (Rounds) o;

			if (rounds != rounds1.rounds) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return rounds;
		}
	}

}
