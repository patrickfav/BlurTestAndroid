package at.favre.app.blurtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
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
				new BlurTask().execute();
			}
		});

		findViewById(R.id.options_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(findViewById(R.id.options).getVisibility() == View.VISIBLE) {
					findViewById(R.id.options).setVisibility(View.INVISIBLE);
					findViewById(R.id.options_button_wrapper).setBackgroundColor(getResources().getColor(R.color.transparent));
				} else {
					findViewById(R.id.options).setVisibility(View.VISIBLE);
					findViewById(R.id.options_button_wrapper).setBackgroundColor(getResources().getColor(R.color.halftransparent));
				}

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

		new BlurTask().execute();
    }

	public class BlurTask extends AsyncTask<Void, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
			start = SystemClock.elapsedRealtime();
			imageViewNormal.setVisibility(View.VISIBLE);
			imageViewBlur.setVisibility(View.INVISIBLE);
		}

		@Override
		protected Bitmap doInBackground(Void... voids) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = inSampleSize;
			return BlurUtil.fastblur(MainActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.photo1, options), radius, algorithm);
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			imageViewBlur.setImageBitmap(bitmap);
			long duration = (SystemClock.elapsedRealtime()-start);
			Log.d("BlurUtil", "Bluring duration "+(SystemClock.elapsedRealtime()-start)+"ms");
			Toast.makeText(MainActivity.this,algorithm+ "/  sample "+inSampleSize+" / radius "+radius+"px / "+duration+"ms"+" / "+ (BlurUtil.sizeOf(bitmap)/1024)+"kB",Toast.LENGTH_LONG).show() ;

			final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.alpha_fadeout);
			anim.setFillAfter(true);
			imageViewNormal.startAnimation(anim);
			final Animation anim2 = AnimationUtils.loadAnimation(MainActivity.this, R.animator.alpha_fadein);
			anim2.setFillAfter(true);
			imageViewBlur.startAnimation(anim2);

			Bitmap blurBitmap = ((BitmapDrawable)imageViewBlur.getDrawable()).getBitmap();
			((TextView) findViewById(R.id.tv_resolution_blur)).setText(blurBitmap.getWidth()+"x"+blurBitmap.getHeight()+" / "+(BlurUtil.sizeOf(blurBitmap)/1024)+"kB / " +duration +"ms");

		}
	}
}
