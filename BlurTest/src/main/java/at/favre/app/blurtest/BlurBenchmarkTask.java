package at.favre.app.blurtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.renderscript.RenderScript;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import at.favre.app.blurtest.util.Average;
import at.favre.app.blurtest.util.BitmapUtil;
import at.favre.app.blurtest.util.BlurUtil;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BlurBenchmarkTask extends AsyncTask<Void, Void, BlurBenchmarkTask.BenchmarkWrapper> {
	private static final String TAG = BlurBenchmarkTask.class.getSimpleName();

	private StatInfo statInfo;

	private long startWholeProcess;

	private int bitmapDrawableResId;

	private Bitmap master;
	private int benchmarkRounds;
	private int radius;
	private BlurUtil.Algorithm algorithm;
	private Context ctx;
	private RenderScript rs;

	public BlurBenchmarkTask(int bitmapDrawableResId, int benchmarkRounds, int radius, BlurUtil.Algorithm algorithm, RenderScript rs, Context ctx) {
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
		startWholeProcess = SystemClock.elapsedRealtime();
	}

	@Override
	protected BenchmarkWrapper doInBackground(Void... voids) {
		try {
			long startReadBitmap = SystemClock.elapsedRealtime();
			final BitmapFactory.Options options = new BitmapFactory.Options();
			master = BitmapFactory.decodeResource(ctx.getResources(), bitmapDrawableResId, options);
			long readBitmapDuration = SystemClock.elapsedRealtime() - startReadBitmap;

			statInfo = new StatInfo(master.getHeight(), master.getWidth(),radius,algorithm);
			statInfo.setLoadBitmap(readBitmapDuration);

			Bitmap blurredBitmap = null;

			for (int i = 0; i < benchmarkRounds; i++) {
				long startBlur = SystemClock.elapsedRealtime();
				blurredBitmap = BlurUtil.blur(rs, master, radius, algorithm);
				statInfo.getAvgBlur().add(SystemClock.elapsedRealtime() - startBlur);
			}

			statInfo.setBenchmarkDuration(SystemClock.elapsedRealtime() - startWholeProcess);
			return new BenchmarkWrapper(BitmapUtil.saveAndRecycleBitmap(blurredBitmap, UUID.randomUUID().toString().substring(0, 6) + "" + radius + "px_" + algorithm + ".png", BitmapUtil.getCacheDir(ctx)), statInfo);
		} catch (Exception e) {
			return new BenchmarkWrapper(null, new StatInfo(e.getMessage()));
		}
	}

	@Override
	protected void onPostExecute(BenchmarkWrapper bitmap) {
		master.recycle();
		master = null;
		Log.d(TAG,"test done");
	}



	public static class BenchmarkWrapper {
		private String bitmapPath;
		private StatInfo statInfo;
		private boolean additionalInfoVisibility = false;

		public BenchmarkWrapper() {
		}

		public BenchmarkWrapper(File bitmapFile, StatInfo statInfo) {
			this.bitmapPath = bitmapFile.getAbsolutePath();
			this.statInfo = statInfo;
			if(bitmapPath == null) {
				statInfo.setError(true);
			}

		}

		public StatInfo getStatInfo() {
			return statInfo;
		}

		public String getBitmapPath() {
			return bitmapPath;
		}

		public void setBitmapPath(String bitmapPath) {
			this.bitmapPath = bitmapPath;
		}

		public void setStatInfo(StatInfo statInfo) {
			this.statInfo = statInfo;
		}

		public boolean isAdditionalInfoVisibility() {
			return additionalInfoVisibility;
		}

		public void setAdditionalInfoVisibility(boolean additionalInfoVisibility) {
			this.additionalInfoVisibility = additionalInfoVisibility;
		}

		@JsonIgnore
		public File getBitmapAsFile() {return new File(bitmapPath);}
	}

	public static class StatInfo {
		private List<Long> avgBlur;
		private long benchmarkDuration;
		private long loadBitmap;
		private int bitmapHeight;
		private int bitmapWidth;
		private int blurRadius;
		private BlurUtil.Algorithm algorithm;
		private boolean error=false;
		private String errorDescription;

		private Average<Long> avg;

		public StatInfo() {
		}

		public StatInfo(int bitmapHeight, int bitmapWidth,int blurRadius, BlurUtil.Algorithm algorithm) {
			this.bitmapHeight = bitmapHeight;
			this.bitmapWidth = bitmapWidth;
			this.blurRadius = blurRadius;
			this.algorithm = algorithm;
			avgBlur = new ArrayList<Long>();
		}

		public StatInfo(String errorDescription) {
			this.errorDescription = errorDescription;
			this.error = true;
		}

		public long getLoadBitmap() {
			return loadBitmap;
		}

		public void setLoadBitmap(long loadBitmap) {
			this.loadBitmap = loadBitmap;
		}

		public List<Long> getAvgBlur() {
			avg = null;
			return avgBlur;
		}

		public void setAvgBlur(List<Long> avgBlur) {
			avg = null;
			this.avgBlur = avgBlur;
		}

		public int getBitmapHeight() {
			return bitmapHeight;
		}

		public int getBitmapWidth() {
			return bitmapWidth;
		}

		public long getBenchmarkDuration() {
			return benchmarkDuration;
		}

		public void setBenchmarkDuration(long benchmarkDuration) {
			this.benchmarkDuration = benchmarkDuration;
		}

		public boolean isError() {
			return error;
		}

		public void setError(boolean error) {
			this.error = error;
		}

		public String getErrorDescription() {
			return errorDescription;
		}

		public void setErrorDescription(String errorDescription) {
			this.errorDescription = errorDescription;
		}

		public int getBlurRadius() {
			return blurRadius;
		}

		public void setBlurRadius(int blurRadius) {
			this.blurRadius = blurRadius;
		}

		public BlurUtil.Algorithm getAlgorithm() {
			return algorithm;
		}

		public void setAlgorithm(BlurUtil.Algorithm algorithm) {
			this.algorithm = algorithm;
		}

		public void setBitmapHeight(int bitmapHeight) {
			this.bitmapHeight = bitmapHeight;
		}

		public void setBitmapWidth(int bitmapWidth) {
			this.bitmapWidth = bitmapWidth;
		}

		@JsonIgnore
		public String getBitmapKBSize() {
			return String.valueOf((double)Math.round((double) (bitmapHeight * bitmapWidth) / 1024d * 100d) / 100d)+"kB";
		}

		@JsonIgnore
		public String getMegaPixels() {
			return String.valueOf((double)Math.round((double) (bitmapHeight * bitmapWidth) / 1000000d * 100d) / 100d)+"MP";
		}
		@JsonIgnore
		public Average<Long> getAsAvg() {
			if (avg == null) {
				avg = new Average<Long>((List<Long>) avgBlur);
			}
			return avg;
		}
	}
}
