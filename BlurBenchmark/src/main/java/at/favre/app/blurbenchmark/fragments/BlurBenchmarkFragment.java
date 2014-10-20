package at.favre.app.blurbenchmark.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.favre.app.blurbenchmark.BenchmarkStorage;
import at.favre.app.blurbenchmark.BlurBenchmarkTask;
import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.activities.BenchmarkResultActivity;
import at.favre.app.blurbenchmark.activities.MainActivity;
import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;
import at.favre.app.blurbenchmark.models.BenchmarkImage;
import at.favre.app.blurbenchmark.models.BenchmarkResultList;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;
import at.favre.app.blurbenchmark.util.JsonUtil;
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil;

/**
 * The main view, where you may start a benchmark with custom settings
 *
 * @author pfavre
 */
public class BlurBenchmarkFragment extends Fragment {
	private static final String TAG = BlurBenchmarkFragment.class.getSimpleName();
	private static final int IMAGE_PICK = 43762;
	private static List<EBlurAlgorithm> algorithmList = new ArrayList<EBlurAlgorithm>(Arrays.asList(EBlurAlgorithm.values()));
	private static Rounds[] roundArray = new Rounds[] {new Rounds(3),new Rounds(10),new Rounds(25),new Rounds(50),new Rounds(100),new Rounds(250),new Rounds(500),new Rounds(1000)};

	private static final String ROUNDS_KEY = "ROUNDS_KEY";
	private static final String CUSTOM_IMAGES = "CUSTOM_IMAGES";

	private int rounds;
	private boolean run =false;
	private BenchmarkResultList benchmarkResultList = new BenchmarkResultList();
	private BlurBenchmarkTask task;
	private List<File> customPicturePaths = new ArrayList<File>();

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

    private ViewGroup algorithmGroup;

	private ProgressDialog progressDialog;

	public BlurBenchmarkFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

		rounds = getResources().getBoolean(R.bool.debugBuild) ? 3 : 100;

		if(savedInstanceState !=  null) {
			rounds = savedInstanceState.getInt(ROUNDS_KEY);
			customPicturePaths = BenchmarkUtil.getAsFiles(savedInstanceState.getString(CUSTOM_IMAGES));
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

        algorithmGroup = (ViewGroup) v.findViewById(R.id.algorithm_wrapper);
        for (EBlurAlgorithm algorithm1 : algorithmList) {
            algorithmGroup.addView(createAlgorithmCheckbox(algorithm1));
        }
        ((CheckBox) algorithmGroup.getChildAt(0)).setChecked(true);

		v.findViewById(R.id.btn_addpic).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, IMAGE_PICK);
			}
		});

		TranslucentLayoutUtil.setTranslucentThemeInsets(getActivity(), v.findViewById(R.id.rootScrollView));
        return v;
	}

    private CheckBox createAlgorithmCheckbox(EBlurAlgorithm algorithm) {
        CheckBox cb = new CheckBox(getActivity());
        cb.setText(algorithm.toString());
        cb.setTag(algorithm);
        cb.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        cb.setTextColor(getResources().getColor(R.color.optionsTextColor));
        cb.setPadding(0,getResources().getDimensionPixelSize(R.dimen.form_element_std_padding),0,getResources().getDimensionPixelSize(R.dimen.form_element_std_padding));
        return cb;
    }

	@Override
	public void onResume() {
		super.onResume();
		updateCustomPictures();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ROUNDS_KEY, rounds);
		outState.putString(CUSTOM_IMAGES, BenchmarkUtil.saveFiles(customPicturePaths));
	}

	private void benchmark() {
		Log.d(TAG,"start benchmark");
		run=true;
		List<Integer> radius = getRadiusSizesFromSettings();
		List<BenchmarkImage> images = getImagesFromSettings();
		images.addAll(getCustomImages());
        List<EBlurAlgorithm> algorithms = getAllSelectedAlgorithms();
        int benchmarkCount = radius.size()*images.size()  * algorithms.size();
		if(benchmarkCount <= 0) {
			Toast.makeText(getActivity(),"Choose at least one radius and image size or custom image",Toast.LENGTH_SHORT).show();
			return;
		}
		showProgressDialog(benchmarkCount);
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		benchmarkResultList = new BenchmarkResultList();
		nextTest(0,0,0,images,radius, algorithms);
	}

	private List<BenchmarkImage> getImagesFromSettings() {
		List<BenchmarkImage> images = new ArrayList<BenchmarkImage>();
		if (cbSize100.isChecked()) {
			images.add(new BenchmarkImage(R.drawable.test_100x100_2));
		}
		if (cbSize200.isChecked()) {
			images.add(new BenchmarkImage(R.drawable.test_200x200_2));
		}
		if (cbSize300.isChecked()) {
			images.add(new BenchmarkImage(R.drawable.test_300x300_2));
		}
		if (cbSize400.isChecked()) {
			images.add(new BenchmarkImage(R.drawable.test_400x400_2));
		}
		if (cbSize500.isChecked()) {
			images.add(new BenchmarkImage(R.drawable.test_500x500_2));
		}
		if (cbSize600.isChecked()) {
			images.add(new BenchmarkImage(R.drawable.test_600x600_2));
		}
		return images;
	}

	private List<BenchmarkImage> getCustomImages() {
		List<BenchmarkImage> images = new ArrayList<BenchmarkImage>();
		for (File customPicturePath : customPicturePaths) {
			images.add(new BenchmarkImage(customPicturePath.getAbsolutePath()));
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

	private List<EBlurAlgorithm> getAllSelectedAlgorithms() {
		List<EBlurAlgorithm> algorithms = new ArrayList<EBlurAlgorithm>();
		for (int i = 0; i <algorithmGroup.getChildCount(); i++) {
			CheckBox cb = (CheckBox) algorithmGroup.getChildAt(i);
			if(cb.isChecked()) {
				algorithms.add((EBlurAlgorithm) algorithmGroup.getChildAt(i).getTag());
			}
		}
		return algorithms;
	}

	private void showProgressDialog(int max) {
		lockOrientation();
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Benchmark in progress");
		progressDialog.setMax(max);
		progressDialog.setProgress(0);
		progressDialog.setCancelable(true);
		progressDialog.show();
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				cancelTests();
			}
		});
	}

	private void lockOrientation() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}

	private void nextTest(final int photoIndex, final int radiusIndex,final int algoIndex, final List<BenchmarkImage> imageList , final List<Integer> radiusList, final List<EBlurAlgorithm> algorithmList) {
        if(run) {
			if (radiusIndex >= radiusList.size()) {
				nextTest(photoIndex + 1, 0, algoIndex, imageList, radiusList, algorithmList);
			} else {
				if (photoIndex >= imageList.size()) {
					nextTest(0, 0, algoIndex + 1, imageList, radiusList, algorithmList);
				} else {
					if (algoIndex >= algorithmList.size()) {
						testDone();
					} else {
						task = new BlurBenchmarkTask(imageList.get(photoIndex), rounds, radiusList.get(radiusIndex), algorithmList.get(algoIndex), ((MainActivity) getActivity()).getRs(), getActivity().getApplicationContext()) {
							@Override
							protected void onPostExecute(BenchmarkWrapper wrapper) {
								progressDialog.setProgress(progressDialog.getProgress() + 1);
								benchmarkResultList.getBenchmarkWrappers().add(wrapper);
								Log.d(TAG, "next test");
								nextTest(photoIndex, radiusIndex + 1, algoIndex, imageList, radiusList, algorithmList);
							}
						};
						task.execute();
					}
				}
			}
		} else {
			Log.d(TAG,"ignore next test, was canceled");
		}
	}

	private void testDone() {
		if(run) {
			run=false;
			if(isAdded() && isVisible()) {
				Log.d(TAG, "done benchmark");
				progressDialog.setProgress(progressDialog.getMax());
				progressDialog.dismiss();
				getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				saveTest();

				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

				Intent i = new Intent(getActivity(), BenchmarkResultActivity.class);
				i.putExtra(BenchmarkResultActivity.BENCHMARK_LIST_KEY, JsonUtil.toJsonString(benchmarkResultList));
				startActivity(i);
			}
		}
    }

	private void saveTest() {
		BenchmarkStorage.getInstance(getActivity()).saveTest(benchmarkResultList.getBenchmarkWrappers());
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onPause() {
		super.onPause();
		cancelTests();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK && null != data) {
			try {
				Uri selectedImage = data.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};
				Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				if(picturePath != null && !picturePath.isEmpty()) {
					customPicturePaths.add(new File(picturePath));
					updateCustomPictures();
				}
			}catch (Exception e) {
				Log.e(TAG,"Could not get requested picture",e);
			}
		}
	}

	private void updateCustomPictures() {
		LinearLayout tvCustomViews = (LinearLayout) getView().findViewById(R.id.tv_additionalPics);
		tvCustomViews.removeAllViews();
		for (final File customPicturePath : customPicturePaths) {
			if(customPicturePath != null && customPicturePath.isFile() && !customPicturePath.getAbsolutePath().isEmpty()) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.inc_custom_img, tvCustomViews, false);

				TextView tv = (TextView) vg.findViewById(R.id.tv_pic_name);
				tv.setText(customPicturePath.getName());

				vg.findViewById(R.id.btn_remove).setTag(customPicturePath);
				vg.findViewById(R.id.btn_remove).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						customPicturePaths.remove(view.getTag());
						LinearLayout tvCustomViews = (LinearLayout) getView().findViewById(R.id.tv_additionalPics);
						for (int i = 0; i < tvCustomViews.getChildCount(); i++) {
							if (view.getTag().equals(tvCustomViews.getChildAt(i).findViewById(R.id.btn_remove).getTag())) {
								tvCustomViews.removeViewAt(i);
								break;
							}
						}
						checkIfCustomImgBtnShouldBeShown();
					}
				});
				tvCustomViews.addView(vg);
			}
		}
		checkIfCustomImgBtnShouldBeShown();
	}

	private void checkIfCustomImgBtnShouldBeShown() {

		if(customPicturePaths.size() > 5) {
			getView().findViewById(R.id.btn_addpic).setVisibility(View.GONE);
		} else {
			getView().findViewById(R.id.btn_addpic).setVisibility(View.VISIBLE);
		}
	}

	private void cancelTests() {
		Log.d(TAG, "cancel benchmark");
		run=false;
		if(task != null) {
			task.cancelBenchmark();
		}
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
