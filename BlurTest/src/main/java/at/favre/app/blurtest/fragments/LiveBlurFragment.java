package at.favre.app.blurtest.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.SettingsController;
import at.favre.app.blurtest.activities.MainActivity;
import at.favre.app.blurtest.util.BlurUtil;
import at.favre.app.blurtest.util.TranslucentLayoutUtil;
import at.favre.app.blurtest.view.ObservableScrollView;

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
	private TextView tvImageSizes;

	private AtomicBoolean isWorking = new AtomicBoolean(false);

	private Bitmap dest;

	private long max=0;
	private long min=9999;
	private double avgSum =0;
	private long avgCount =0;
	private long last=0;

	private ViewTreeObserver.OnGlobalLayoutListener ogl;
	private SettingsController settingsController;

	private Bitmap prevFrame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		dest=null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_liveblur,container,false);


		mPager = (ViewPager) v.findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter();
		mPager.setAdapter(mPagerAdapter);
		topBlurView = v.findViewById(R.id.topCanvas);
		bottomBlurView = v.findViewById(R.id.bottomCanvas);

		tvPerformance = (TextView) v.findViewById(R.id.tv_performance);
		tvImageSizes = (TextView) v.findViewById(R.id.tv_imagesizes);

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
		//mPager.getViewTreeObserver().addOnGlobalLayoutListener(ogl);


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
				prevFrame=null;
				updateBlurView();
			}
		},new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				updateBlurView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {}
		},null);
		settingsController.setVisibility(true,true,false,false);
		TranslucentLayoutUtil.setTranslucentThemeInsets(getActivity(), v.findViewById(R.id.contentWrapper));
		TranslucentLayoutUtil.setTranslucentThemeInsetsWithoutActionbarHeight(getActivity(),v.findViewById(R.id.topCanvasWrapper));
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main_menu, menu);
	}

	private void disableLayoutListener() {
		mPager.getViewTreeObserver().removeGlobalOnLayoutListener(ogl);
	}

	private boolean updateBlurView() {
		if (getView() != null && !isWorking.get() && topBlurView.getWidth() != 0 && topBlurView.getHeight() != 0) {
			isWorking.compareAndSet(false, true);
			long start = SystemClock.elapsedRealtime();
			dest = drawViewToBitmap(dest, getView().findViewById(R.id.wrapper), settingsController.getInSampleSize());
			topBlurView.setBackgroundDrawable(new BitmapDrawable(getResources(), BlurUtil.blur(((MainActivity)getActivity()).getRs(), crop(dest.copy(dest.getConfig(), true), topBlurView, settingsController.getInSampleSize()), settingsController.getRadius(), settingsController.getAlgorithm())));
			bottomBlurView.setBackgroundDrawable(new BitmapDrawable(getResources(), BlurUtil.blur(((MainActivity)getActivity()).getRs(),crop(dest.copy(dest.getConfig(),true), bottomBlurView, settingsController.getInSampleSize()) , settingsController.getRadius(), settingsController.getAlgorithm())));
			checkAndSetPerformanceTextView(SystemClock.elapsedRealtime()-start);
			tvImageSizes.setText(((BitmapDrawable)topBlurView.getBackground()).getBitmap().getWidth()+"x"+((BitmapDrawable)topBlurView.getBackground()).getBitmap().getHeight()+" / "+((BitmapDrawable)bottomBlurView.getBackground()).getBitmap().getWidth()+"x"+((BitmapDrawable)bottomBlurView.getBackground()).getBitmap().getHeight());
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
				(int) Math.floor((canvasView.getX())*scale),
				(int) Math.floor((canvasView.getY())*scale),
				(int) Math.floor((canvasView.getWidth())*scale),
				(int) Math.floor((canvasView.getHeight())*scale)
		);
	}



	@Override
	public void switchShowSettings() {
		settingsController.switchShow();
	}

	private class ScreenSlidePagerAdapter extends PagerAdapter {

		private FrameLayout scrollViewLayout;
		private FrameLayout listViewLayout;

		public View getView(int position, ViewPager pager) {
			switch (position) {
				case 0:
					return createImageView(R.drawable.photo3_med);
				case 1:
					return createImageView(R.drawable.photo2_med);
				case 2:
					return createImageView(R.drawable.photo4_med);
				case 3:
					return createScrollView();
				case 4:
					return createListView();

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
			return 5;
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
			ImageView imageView = new ImageView(getActivity());
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT));
			Picasso.with(getActivity()).load(drawableResId).into(imageView);
			return imageView;
		}

		public View createScrollView() {
			if(scrollViewLayout == null) {
				scrollViewLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.inc_scrollview, mPager, false);
				((ObservableScrollView) scrollViewLayout.findViewById(R.id.scrollview)).setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
					@Override
					public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
						updateBlurView();
					}
				});
				Picasso.with(getActivity()).load(R.drawable.photo1_med).into((ImageView) scrollViewLayout.findViewById(R.id.photo1));
				Picasso.with(getActivity()).load(R.drawable.photo2_med).into((ImageView) scrollViewLayout.findViewById(R.id.photo2));
			}
			return scrollViewLayout;
		}
		public View createListView() {
			if(listViewLayout == null) {
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < 20; i++) {
					list.add("This is a long line of text and so on "+i);
				}
				listViewLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.inc_listview, mPager, false);
				((ListView) listViewLayout.findViewById(R.id.listview)).setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list));
				((ListView) listViewLayout.findViewById(R.id.listview)).setOnScrollListener(new AbsListView.OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsListView absListView, int i) {
					}

					@Override
					public void onScroll(AbsListView absListView, int i, int i2, int i3) {
						updateBlurView();
					}
				});
			}
			return listViewLayout;
		}
	}
}
