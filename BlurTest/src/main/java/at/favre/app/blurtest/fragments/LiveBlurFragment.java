package at.favre.app.blurtest.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.SettingsController;
import at.favre.app.blurtest.activities.MainActivity;
import at.favre.app.blurtest.util.BlurUtil;

/**
 * Created by PatrickF on 08.04.2014.
 */
public class LiveBlurFragment extends Fragment implements IFragmentWithBlurSettings{
	private static final String TAG = LiveBlurFragment.class.getSimpleName();

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private View topBlurView;
	private View bottomBlurView;
	private TextView tvPerformance;

	private AtomicBoolean isWorking = new AtomicBoolean(false);

	private LruCache<String, FrameLayout> mMemoryCache;
	private Bitmap dest;

	private long max=0;
	private long min=9999;
	private double avgSum =0;
	private long avgCount =0;
	private long last=0;

	private ViewTreeObserver.OnGlobalLayoutListener ogl;
	private SettingsController settingsController;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_liveblur,container,false);


		mPager = (ViewPager) v.findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter();
		mPager.setAdapter(mPagerAdapter);

		topBlurView = v.findViewById(R.id.topCanvas);
		bottomBlurView = v.findViewById(R.id.bottomCanvas);


		tvPerformance = (TextView) v.findViewById(R.id.tv_performance);

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


		settingsController = new SettingsController(v,new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {updateBlurView();}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		},new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				updateBlurView();
			}
		},new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				updateBlurView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {}
		},null,true);

		createLRUCache();

		return v;
	}

	private void createLRUCache() {
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
		if (getView() != null && !isWorking.get() && topBlurView.getWidth() != 0 && topBlurView.getHeight() != 0) {
			isWorking.compareAndSet(false, true);
			long start = SystemClock.elapsedRealtime();
			dest = drawViewToBitmap(dest, getView().findViewById(R.id.wrapper), settingsController.getInSampleSize());
			topBlurView.setBackground(new BitmapDrawable(getResources(), BlurUtil.blur(((MainActivity)getActivity()).getRs(), crop(dest, topBlurView, settingsController.getInSampleSize()), settingsController.getRadius(), settingsController.getAlgorithm())));
			bottomBlurView.setBackground(new BitmapDrawable(getResources(), BlurUtil.blur(((MainActivity)getActivity()).getRs(), crop(dest, bottomBlurView, settingsController.getInSampleSize()), settingsController.getRadius(), settingsController.getAlgorithm())));
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
	public void switchShowSettings() {
		settingsController.switchShow();
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
				frameLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.inc_image_page, mPager, false);
				((ImageView) frameLayout.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(drawableResId));
				addBitmapToMemoryCache(String.valueOf(drawableResId),frameLayout);
			} else {

			}
			return frameLayout;
		}
	}
}
