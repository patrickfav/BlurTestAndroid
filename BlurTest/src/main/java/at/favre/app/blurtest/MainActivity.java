package at.favre.app.blurtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private ImageView imageViewBlur;
	private ImageView imageViewNormal;
	private SeekBar seekRadius;
	private SeekBar seekInSampleSize;
	private TextView tvRadius;
	private TextView tvInSample;
	private Spinner algorithmSpinner;
	private long start;

	private int radius;
	private int inSampleSize;
	private BlurUtil.Algorithm algorithm = BlurUtil.Algorithm.RENDERSCRIPT;
	private List<BlurUtil.Algorithm> algorithmList = new ArrayList<BlurUtil.Algorithm>(Arrays.asList(BlurUtil.Algorithm.values()));
	private boolean showCrossfade = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		imageViewNormal= (ImageView) findViewById(R.id.normal_image);
		imageViewBlur = (ImageView) findViewById(R.id.blur_image);

		seekInSampleSize = (SeekBar) findViewById(R.id.seek_insample);
		seekRadius = (SeekBar) findViewById(R.id.seek_radius);

		inSampleSize = seekInSampleSize.getProgress()+1;
		radius= seekRadius.getProgress()+1;

		tvInSample = (TextView) findViewById(R.id.tv_insample_value);
		tvRadius = (TextView) findViewById(R.id.tv_radius_value);

		tvInSample.setText(inSampleSize+"");
		tvRadius.setText(radius+"px");

		seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				radius=i+1;
				tvRadius.setText(radius+"px");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		seekInSampleSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				inSampleSize = i+1;
				tvInSample.setText(inSampleSize+"");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		findViewById(R.id.btn_redraw).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startBlur();
			}
		});

		algorithmSpinner = (Spinner) findViewById(R.id.spinner_algorithm);
		algorithmSpinner.setAdapter(new ArrayAdapter<BlurUtil.Algorithm>(this, R.layout.inc_spinner_item, algorithmList));
		algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				algorithm = algorithmList.get(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		Bitmap normalBitmap = ((BitmapDrawable)imageViewNormal.getDrawable()).getBitmap();
		((TextView) findViewById(R.id.tv_resolution_normal)).setText("Original: "+normalBitmap.getWidth()+"x"+normalBitmap.getHeight()+" / "+(BlurUtil.sizeOf(normalBitmap)/1024)+"kB");

		((CheckBox) findViewById(R.id.cb_crossfade)).setChecked(showCrossfade);
		((CheckBox) findViewById(R.id.cb_crossfade)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				showCrossfade = b;
			}
		});

		findViewById(R.id.options).setVisibility(View.VISIBLE);
		final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.slide_in_top);
		anim.setFillAfter(true);
		anim.setDuration(0);
		findViewById(R.id.options).startAnimation(anim);

		startBlur();
    }

	private void startBlur() {

		new BlurTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_settings:
				if(findViewById(R.id.options).getVisibility() == View.VISIBLE) {
					findViewById(R.id.options).setVisibility(View.INVISIBLE);
					final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.slide_out_top);
					anim.setFillAfter(true);
					findViewById(R.id.options).startAnimation(anim);
				} else {
					findViewById(R.id.options).setVisibility(View.VISIBLE);
					final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.slide_in_top);
					anim.setFillAfter(true);
					findViewById(R.id.options).startAnimation(anim);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public class BlurTask extends AsyncTask<Void, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
			start = SystemClock.elapsedRealtime();
			imageViewNormal.setAlpha(1f);
			imageViewBlur.setAlpha(1f);
		}

		@Override
		protected Bitmap doInBackground(Void... voids) {
			Log.d(TAG,"Load Bitmap");
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = inSampleSize;
			Bitmap loadedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo1, options);
			Log.d(TAG,"Start blur algorithm");
			Bitmap blurredBitmap = BlurUtil.blur(MainActivity.this,loadedBitmap, radius, algorithm);
			Log.d(TAG,"Done blur algorithm");
			return  blurredBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			Log.d(TAG,"Set image to imageView");
			imageViewBlur.setImageBitmap(bitmap);
			long duration = (SystemClock.elapsedRealtime()-start);
			Log.d(TAG, "Bluring duration "+(SystemClock.elapsedRealtime()-start)+"ms");
			Toast.makeText(MainActivity.this,algorithm+ "/  sample "+inSampleSize+" / radius "+radius+"px / "+duration+"ms"+" / "+ (BlurUtil.sizeOf(bitmap)/1024)+"kB",Toast.LENGTH_SHORT).show() ;

			if(showCrossfade) {
				final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.alpha_fadeout);
				anim.setFillAfter(true);
				imageViewNormal.startAnimation(anim);
				final Animation anim2 = AnimationUtils.loadAnimation(MainActivity.this, R.animator.alpha_fadein);
				anim2.setFillAfter(true);
				imageViewBlur.startAnimation(anim2);
			} else {
				imageViewBlur.setAlpha(1.0f);
				imageViewNormal.setAlpha(0.0f);
			}

			Bitmap blurBitmap = ((BitmapDrawable)imageViewBlur.getDrawable()).getBitmap();
			((TextView) findViewById(R.id.tv_resolution_blur)).setText(blurBitmap.getWidth()+"x"+blurBitmap.getHeight()+" / "+(BlurUtil.sizeOf(blurBitmap)/1024)+"kB / " +duration +"ms");

		}
	}
}
