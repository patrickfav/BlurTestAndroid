package at.favre.app.blurtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.renderscript.RenderScript;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import at.favre.app.blurtest.util.Average;
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

			return new BenchmarkWrapper(saveAndRecycleBitmap(blurredBitmap, UUID.randomUUID().toString().substring(0,6)+""+radius+"px_"+algorithm+".png", getCacheDir()), statInfo);
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

    private File saveAndRecycleBitmap(Bitmap bitmap, String filename, String path) {
        FileOutputStream out=null;
        try {
            File f = new File(path,filename);
            if(!f.exists()) {
                f.createNewFile();
            }
            out = new FileOutputStream(f);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                return f;
            }
        } catch (Exception e) {
            Log.e(TAG,"Could not save bitmap",e);
        } finally {
            try{
                out.close();
            } catch(Throwable ignore) {}
            bitmap.recycle();
        }
        return null;
    }

    private String getCacheDir() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||!Environment.isExternalStorageRemovable() ?
                ctx.getExternalCacheDir().getPath() : ctx.getCacheDir().getPath();
    }

	public static class BenchmarkWrapper {
		private final File resultBitmap;
		private final StatInfo statInfo;

		public BenchmarkWrapper(File bitmapFile, StatInfo statInfo) {
			this.resultBitmap = bitmapFile;
			this.statInfo = statInfo;
		}

		public StatInfo getStatInfo() {
			return statInfo;
		}

		public File getResultBitmap() {
			return resultBitmap;
		}
	}

	public static class StatInfo {
		private Average<Long> avgBlur;
		private long benchmarkDuration;
		private long loadBitmap;
		private int bitmapHeight;
		private int bitmapWidth;
		private int blurRadius;
		private BlurUtil.Algorithm algorithm;
		private boolean error=false;
		private String errorDescription;

		public StatInfo(int bitmapHeight, int bitmapWidth,int blurRadius, BlurUtil.Algorithm algorithm) {
			this.bitmapHeight = bitmapHeight;
			this.bitmapWidth = bitmapWidth;
			this.blurRadius = blurRadius;
			this.algorithm = algorithm;
			avgBlur = new Average<Long>();
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

		public Average<Long> getAvgBlur() {
			return avgBlur;
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

		public String getBitmapKBSize() {
			return String.valueOf((double)Math.round((double) (bitmapHeight * bitmapWidth) / 1024d * 100d) / 100d)+"kB";
		}

		public String getMegaPixels() {
			return String.valueOf((double)Math.round((double) (bitmapHeight * bitmapWidth) / 1000000d * 100d) / 100d)+"MP";
		}
	}
}
