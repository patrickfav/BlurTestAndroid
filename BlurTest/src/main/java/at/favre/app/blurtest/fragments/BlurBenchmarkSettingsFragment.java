package at.favre.app.blurtest.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import at.favre.app.blurtest.models.BenchmarkResultDatabase;
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
        setHasOptionsMenu(true);
		if(savedInstanceState !=  null) {
			rounds = savedInstanceState.getInt(ROUNDS_KEY);
			algorithm = BlurUtil.Algorithm.valueOf(savedInstanceState.getString(ALGO_KEY));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bechmark_settings,container,false);

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

    private void benchmarkAll() {
        List<Integer> radius = Arrays.asList(new Integer[]{4,8,16,24});
        List<Integer> images = Arrays.asList(new Integer[]{R.drawable.test_100x100_2,R.drawable.test_200x200_2,R.drawable.test_300x300_2,R.drawable.test_400x400_2,R.drawable.test_500x500_2,R.drawable.test_600x600_2});
        rounds = 10;
        showProgressDialog(radius.size()*images.size()*BlurUtil.Algorithm.values().length);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        nextTest(0,0,0,images,radius,Arrays.asList(BlurUtil.Algorithm.values()));
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
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		benchmarkResultList = new BenchmarkResultList();
		nextTest(0,0,0,images,radius, Arrays.asList(algorithm));
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

	private void nextTest(final int photoIndex, final int radiusIndex,final int algoIndex, final List<Integer> imageList , final List<Integer> radiusList, final List<BlurUtil.Algorithm> algorithmList) {
        if(radiusIndex >= radiusList.size()) {
			nextTest(photoIndex+1,0,algoIndex,imageList,radiusList, algorithmList);
		} else {
			if(photoIndex >= imageList.size()) {
                nextTest(0, 0, algoIndex+1, imageList, radiusList, algorithmList);
			} else {
                if(algoIndex >= algorithmList.size()) {
                    testDone();
                } else {
                    new BlurBenchmarkTask(imageList.get(photoIndex), rounds, radiusList.get(radiusIndex), algorithmList.get(algoIndex), ((MainActivity) getActivity()).getRs(), getActivity()) {
                        @Override
                        protected void onPostExecute(BenchmarkWrapper wrapper) {
                            progressDialog.setProgress(progressDialog.getProgress() + 1);
                            benchmarkResultList.getBenchmarkWrappers().add(wrapper);
                            Log.d(TAG, "next test");
                            nextTest(photoIndex, radiusIndex + 1, algoIndex, imageList, radiusList, algorithmList);
                        }
                    }.execute();
                }
			}
		}
	}



	private void testDone() {
		Log.d(TAG, "done benchmark");
		progressDialog.setProgress(progressDialog.getMax());
		progressDialog.dismiss();
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		saveTest();

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		Intent i = new Intent(getActivity(), BenchmarkResultActivity.class);
		i.putExtra(BenchmarkResultActivity.BENCHMARK_LIST_KEY,JsonUtil.toJsonString(benchmarkResultList));
		startActivity(i);
    }

	private void saveTest() {
		// Restore preferences
		SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
		String resultsString = settings.getString(MainActivity.PREF_RESULTS,null);
		BenchmarkResultDatabase db;

		if(resultsString == null) {
			db = new BenchmarkResultDatabase();
		} else {
			db = JsonUtil.fromJsonString(resultsString,BenchmarkResultDatabase.class);
		}


		for (BenchmarkWrapper benchmarkWrapper : benchmarkResultList.getBenchmarkWrappers()) {
			if(!benchmarkWrapper.getStatInfo().isError()) {
				BenchmarkResultDatabase.BenchmarkEntry template = new BenchmarkResultDatabase.BenchmarkEntry(benchmarkWrapper.getStatInfo().getKeyString(), benchmarkWrapper.getStatInfo().getCategoryString(), new ArrayList<BenchmarkWrapper>());
				if(db.getEntryList().contains(template)) {
					db.getEntryList().get(db.getEntryList().indexOf(template)).getWrapper().add(benchmarkWrapper);
				} else {
					template.getWrapper().add(benchmarkWrapper);
					db.getEntryList().add(template);
				}
			}
		}

		settings.edit().putString(MainActivity.PREF_RESULTS,JsonUtil.toJsonString(db)).commit();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.benchmark_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_benchmark:
                benchmark();
                return true;
            case R.id.action_benchmark_all:
                benchmarkAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
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
