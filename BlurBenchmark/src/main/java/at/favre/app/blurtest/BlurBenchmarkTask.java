package at.favre.app.blurtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import at.favre.app.blurtest.blur.EBlurAlgorithm;
import at.favre.app.blurtest.models.BenchmarkWrapper;
import at.favre.app.blurtest.models.StatInfo;
import at.favre.app.blurtest.util.BenchmarkUtil;
import at.favre.app.blurtest.util.BitmapUtil;
import at.favre.app.blurtest.util.BlurUtil;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BlurBenchmarkTask extends AsyncTask<Void, Void, BenchmarkWrapper> {
	private static final String TAG = BlurBenchmarkTask.class.getSimpleName();

	private StatInfo statInfo;

	private long startWholeProcess;

	private int bitmapDrawableResId;

	private Bitmap master;
	private int benchmarkRounds;
	private int radius;
	private EBlurAlgorithm algorithm;
	private Context ctx;
	private RenderScript rs;

	public BlurBenchmarkTask(int bitmapDrawableResId, int benchmarkRounds, int radius, EBlurAlgorithm algorithm, RenderScript rs, Context ctx) {
		this.bitmapDrawableResId = bitmapDrawableResId;
		this.benchmarkRounds = benchmarkRounds;
		this.radius = radius;
		this.algorithm = algorithm;
		this.rs = rs;
		this.ctx = ctx;
	}


	@Override
	protected void onPreExecute() {
		Log.d(TAG,"Start test with "+radius+"px radius, "+benchmarkRounds+"rounds in "+algorithm);
		startWholeProcess = BenchmarkUtil.elapsedRealTimeNanos();
	}

	@Override
	protected BenchmarkWrapper doInBackground(Void... voids) {
		try {
			long startReadBitmap = BenchmarkUtil.elapsedRealTimeNanos();
			final BitmapFactory.Options options = new BitmapFactory.Options();
			master = BitmapFactory.decodeResource(ctx.getResources(), bitmapDrawableResId, options);
			long readBitmapDuration = (BenchmarkUtil.elapsedRealTimeNanos() - startReadBitmap)/1000000l;

			statInfo = new StatInfo(master.getHeight(), master.getWidth(),radius,algorithm,benchmarkRounds);
			statInfo.setLoadBitmap(readBitmapDuration);

			Bitmap blurredBitmap = null;



			for (int i = 0; i < benchmarkRounds; i++) {
				long startBlur = BenchmarkUtil.elapsedRealTimeNanos();
				blurredBitmap = master.copy(master.getConfig(), true);
				blurredBitmap = BlurUtil.blur(rs, blurredBitmap, radius, algorithm);
				statInfo.getBenchmarkData().add((BenchmarkUtil.elapsedRealTimeNanos() - startBlur)/1000000d);
			}

			statInfo.setBenchmarkDuration((BenchmarkUtil.elapsedRealTimeNanos() - startWholeProcess)/1000000l);

			String fileName = master.getWidth()+"x"+master.getHeight()+"_" + radius + "px_" + algorithm + ".png";
			return new BenchmarkWrapper(BitmapUtil.saveBitmap(blurredBitmap, fileName, BitmapUtil.getCacheDir(ctx), false),
					BitmapUtil.saveBitmap(BitmapUtil.flip(blurredBitmap),"mirror_"+fileName,BitmapUtil.getCacheDir(ctx),true),
					statInfo);
		} catch (Throwable e) {
            Log.e(TAG,"Could not complete benchmark",e);
			return new BenchmarkWrapper(null,null, new StatInfo(e.toString(),algorithm));
		}
	}

	@Override
	protected void onPostExecute(BenchmarkWrapper bitmap) {
		master.recycle();
		master = null;
		Log.d(TAG,"test done");
	}


}
