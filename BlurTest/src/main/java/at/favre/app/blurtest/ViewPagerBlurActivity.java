package at.favre.app.blurtest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.renderscript.RenderScript;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by PatrickF on 08.04.2014.
 */
public class ViewPagerBlurActivity extends FragmentActivity {
	private static final String TAG = ViewPagerBlurActivity.class.getSimpleName();

	private final static int IN_SAMPLE_SIZE = 6;
	private final static int BLUR_RADIUS = 10;

	private ViewPager mPager;

	private PagerAdapter mPagerAdapter;
	private View topBlurView;
	private View bottomBlurView;

	private RenderScript rs;
	private Bitmap dest;

	private AtomicBoolean isWorking = new AtomicBoolean(false);

	private SeekBar seekRadius;
	private SeekBar seekInSampleSize;
	private TextView tvRadius;
	private TextView tvInSample;
	private Spinner algorithmSpinner;

	private TextView tvPerformance;

	private int radius;
	private int inSampleSize;
	private BlurUtil.Algorithm algorithm = BlurUtil.Algorithm.RENDERSCRIPT;
	private List<BlurUtil.Algorithm> algorithmList = new ArrayList<BlurUtil.Algorithm>(Arrays.asList(BlurUtil.Algorithm.values()));

	private LruCache<String, FrameLayout> mMemoryCache;

	private long max=0;
	private long min=9999;
	private double avgSum =0;
	private long avgCount =0;
	private long last=0;

	private boolean optionsShown =false;
	private ViewTreeObserver.OnGlobalLayoutListener ogl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rs = RenderScript.create(this);
		setContentView(R.layout.activity_viewpagerblur);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter();
		mPager.setAdapter(mPagerAdapter);

		topBlurView = findViewById(R.id.topCanvas);
		bottomBlurView = findViewById(R.id.bottomCanvas);

		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				updateBlurView();
			}

			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		ogl = new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if(updateBlurView()) {
					disableLayoutListener();
				}
			}
		};
		mPager.getViewTreeObserver().addOnGlobalLayoutListener(ogl);

		tvPerformance = (TextView) findViewById(R.id.tv_performance);
		seekInSampleSize = (SeekBar) findViewById(R.id.seek_insample);
		seekRadius = (SeekBar) findViewById(R.id.seek_radius);

		inSampleSize = seekInSampleSize.getProgress()+1;
		radius= seekRadius.getProgress()+1;

		tvInSample = (TextView) findViewById(R.id.tv_insample_value);
		tvRadius = (TextView) findViewById(R.id.tv_radius_value);

		tvInSample.setText("1/"+inSampleSize*inSampleSize);
		tvRadius.setText(radius+"px");

		seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				radius=i+1;
				tvRadius.setText(radius+"px");
				updateBlurView();
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
				tvInSample.setText("1/"+inSampleSize*inSampleSize);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				updateBlurView();
			}
		});

		ArrayAdapter<BlurUtil.Algorithm> alogrithmArrayAdapter = new ArrayAdapter<BlurUtil.Algorithm>(this,R.layout.inc_spinner_textview, algorithmList);
		alogrithmArrayAdapter.setDropDownViewResource(R.layout.inc_spinner_item);
		algorithmSpinner = (Spinner) findViewById(R.id.spinner_algorithm);
		algorithmSpinner.setAdapter(alogrithmArrayAdapter);
		algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				algorithm = algorithmList.get(i);
				updateBlurView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

//		findViewById(R.id.options).setVisibility(View.VISIBLE);
//		final Animation anim = AnimationUtils.loadAnimation(ViewPagerBlurActivity.this, R.animator.slide_in_top);
//		anim.setFillAfter(true);
//		anim.setDuration(0);
//		findViewById(R.id.options).startAnimation(anim);

		changeOptionsView(false);

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 2;

		mMemoryCache = new LruCache<String, FrameLayout>(cacheSize) {
			@Override
			protected int sizeOf(String key, FrameLayout bitmap) {
				return ((BitmapDrawable) ((ImageView) bitmap.findViewById(R.id.imageView)).getDrawable()).getBitmap().getByteCount() / 1024;
			}
		};
	}

	private void disableLayoutListener() {
		mPager.getViewTreeObserver().removeOnGlobalLayoutListener(ogl);
	}

	public void addBitmapToMemoryCache(String key, FrameLayout bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public FrameLayout getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	private boolean updateBlurView() {
		if (!isWorking.get() && topBlurView.getWidth() != 0 && topBlurView.getHeight() != 0) {
			isWorking.compareAndSet(false, true);
			long start = SystemClock.elapsedRealtime();
			dest = drawViewToBitmap(dest, findViewById(R.id.wrapper), inSampleSize);
			topBlurView.setBackground(new BitmapDrawable(getResources(), BlurUtil.blur(rs, crop(dest, topBlurView, inSampleSize), radius, algorithm)));
			bottomBlurView.setBackground(new BitmapDrawable(getResources(), BlurUtil.blur(rs, crop(dest, bottomBlurView, inSampleSize), radius, algorithm)));
			checkAndSetPerformanceTextView(SystemClock.elapsedRealtime()-start);
			isWorking.compareAndSet(true, false);
			return true;
		} else {
			Log.v(TAG, "skip blur frame");
			return false;
		}
	}

	private void checkAndSetPerformanceTextView(long currentRunMs) {
		if(max < currentRunMs) {
			max = currentRunMs;
		}
		if(min > currentRunMs) {
			min = currentRunMs;
		}
		avgCount++;
		avgSum += currentRunMs;
		last = currentRunMs;
		tvPerformance.setText("last: "+last+"ms / avg: "+Math.round(avgSum/avgCount)+"ms / min:"+min+"ms / max:"+max+"ms");
	}

	private Bitmap drawViewToBitmap(Bitmap dest, View view, int downSampling) {
		float scale = 1f / downSampling;
		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		int bmpWidth = Math.round(viewWidth * scale);
		int bmpHeight = Math.round(viewHeight * scale);

		if (dest == null || dest.getWidth() != bmpWidth || dest.getHeight() != bmpHeight) {
			dest = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		}

		Canvas c = new Canvas(dest);
		if (downSampling > 1) {
			c.scale(scale, scale);
		}

		view.draw(c);
		return dest;
	}

	private Bitmap crop(Bitmap srcBmp, View canvasView, int downsampling) {
		float scale = 1f / downsampling;
		return Bitmap.createBitmap(
				srcBmp,
				Math.round(canvasView.getX()*scale),
				Math.round(canvasView.getY()*scale),
				Math.round(canvasView.getWidth()*scale),
				Math.round(canvasView.getHeight()*scale)
		);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_pager, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void changeOptionsView(boolean show) {
		if(show) {
//			final Animation anim = AnimationUtils.loadAnimation(ViewPagerBlurActivity.this, R.animator.slide_in_top);
//			anim.setFillAfter(true);
//			anim.setAnimationListener(new Animation.AnimationListener() {
//				@Override
//				public void onAnimationStart(Animation animation) {
//					findViewById(R.id.options).setAlpha(1.0f);
//					findViewById(R.id.options).setVisibility(View.VISIBLE);
//				}
//
//				@Override
//				public void onAnimationEnd(Animation animation) {
//
//				}
//
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//
//				}
//			});
//			findViewById(R.id.options).startAnimation(anim);
			findViewById(R.id.options).setVisibility(View.VISIBLE);
		} else {
//			final Animation anim = AnimationUtils.loadAnimation(ViewPagerBlurActivity.this, R.animator.slide_out_top);
//			anim.setFillAfter(true);
//			anim.setAnimationListener(new Animation.AnimationListener() {
//				@Override
//				public void onAnimationStart(Animation animation) {
//
//				}
//
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					findViewById(R.id.options).setVisibility(View.GONE);
//				}
//
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//
//				}
//			});
//			findViewById(R.id.options).startAnimation(anim);
			findViewById(R.id.options).setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_settings:
				optionsShown = !optionsShown;
				changeOptionsView(optionsShown);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class ScreenSlidePagerAdapter extends PagerAdapter {

		public View getView(int position, ViewPager pager) {
			switch (position) {
				case 0:
					return createImageView(R.drawable.photo3_med);
				case 1:
					return createImageView(R.drawable.photo2_med);
				case 2:
					return createImageView(R.drawable.photo4_med);
				case 3:
					return createImageView(R.drawable.photo1_med);
				default:
					return createImageView(R.drawable.photo1_med);
			}
		}


		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewPager pager = (ViewPager) container;
			View view = getView(position, pager);

			pager.addView(view);

			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			((ViewPager) container).removeView((View) view);
		}

		public View createImageView(int drawableResId) {
			FrameLayout frameLayout = getBitmapFromMemCache(String.valueOf(drawableResId));
			if(frameLayout == null) {
				Log.d(TAG, "Not found in cache - createing view for viewpager");
				frameLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.inc_image_page, mPager, false);
				((ImageView) frameLayout.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(drawableResId));
				addBitmapToMemoryCache(String.valueOf(drawableResId),frameLayout);
			} else {

			}
			return frameLayout;
		}
	}


}
