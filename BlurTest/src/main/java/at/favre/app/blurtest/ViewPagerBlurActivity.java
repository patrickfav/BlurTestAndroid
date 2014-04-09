package at.favre.app.blurtest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by PatrickF on 08.04.2014.
 */
public class ViewPagerBlurActivity extends FragmentActivity {
	private static final String TAG = ViewPagerBlurActivity.class.getSimpleName();


	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */


	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;
	private ColorDrawable imageBackgroundDrawable;
	private View canvasView;
	private View canvasView2;

	private RenderScript rs;
	private Bitmap dest;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rs = RenderScript.create(this);
		setContentView(R.layout.activity_viewpagerblur);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter();
		mPager.setAdapter(mPagerAdapter);

		canvasView =  findViewById(R.id.stripe);
		canvasView2 = findViewById(R.id.stripe2);

		imageBackgroundDrawable = new ColorDrawable(getResources().getColor(R.color.darkgrey));
		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				updateBlurView();
				//Log.d(TAG,"scroll "+positionOffsetPixels );
			}

			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				//Log.d(TAG, "Touch event");
				//updateBlurView();
				return false;
			}
		});


		mPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				updateBlurView();
			}
		});

	}

	private void updateBlurView() {
		if(canvasView.getWidth() != 0 && canvasView.getHeight() != 0) {
			//			new AsyncTask<Void,Void,Bitmap>() {
//				@Override
//				protected Bitmap doInBackground(Void... voids) {
//					return drawViewToBitmap(dest, findViewById(R.id.wrapper), 6, imageBackgroundDrawable);
//				}
//
//				@Override
//				protected void onPostExecute(Bitmap b) {
//					canvasView.setBackground(new BitmapDrawable(getResources(), b));
//				}
//			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			Bitmap b = drawViewToBitmap(dest, findViewById(R.id.wrapper), 6, imageBackgroundDrawable);
			canvasView.setBackground(new BitmapDrawable(getResources(), BlurUtil.blur(rs,crop(b,canvasView,6),12,BlurUtil.Algorithm.RENDERSCRIPT)));
			canvasView2.setBackground(new BitmapDrawable(getResources(), BlurUtil.blur(rs,crop(b,canvasView2,6),12,BlurUtil.Algorithm.RENDERSCRIPT)));
		}
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

	private Bitmap drawViewToBitmap(Bitmap dest, View view, int downSampling, Drawable background) {
		float scale = 1f / downSampling;
		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		int bmpWidth = Math.round(viewWidth * scale);
		int bmpHeight = Math.round(viewHeight * scale);

		//Log.d(TAG, "viewheight:"+viewHeight+" viewWidth:"+viewWidth+" bmpHeight:"+bmpHeight+" bmpWidth:"+bmpWidth);
		if (dest == null || dest.getWidth() != bmpWidth || dest.getHeight() != bmpHeight) {
			dest = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		}
		Canvas c = new Canvas(dest);

		background.setBounds(new Rect(0, 0, viewWidth, viewHeight));
		background.draw(c);
		if (downSampling > 1) {
			c.scale(scale, scale);
		}

		view.draw(c);

		//view.layout(0, 0, viewWidth, viewHeight);
		//return BlurUtil.blur(rs,dest,12,BlurUtil.Algorithm.RENDERSCRIPT);
		//return crop(dest,canvasView,downSampling);
		return dest;
	}

	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 0) {
			// If the user is currently looking at the first step, allow the system to handle the
			// Back button. This calls finish() on this activity and pops the back stack.
			super.onBackPressed();
		} else {
			// Otherwise, select the previous step.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
		}
	}


	private class ScreenSlidePagerAdapter extends PagerAdapter
	{

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
			FrameLayout frameLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.inc_image_page, mPager,false);
			((ImageView) frameLayout.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(drawableResId));
			return frameLayout;
		}
	}


}
