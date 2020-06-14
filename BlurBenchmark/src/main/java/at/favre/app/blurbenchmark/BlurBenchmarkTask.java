package at.favre.app.blurbenchmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.renderscript.RenderScript;

import java.io.IOException;

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;
import at.favre.app.blurbenchmark.models.BenchmarkImage;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;
import at.favre.app.blurbenchmark.models.StatInfo;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;
import at.favre.app.blurbenchmark.util.BitmapUtil;
import at.favre.app.blurbenchmark.util.BlurUtil;

/**
 * This is the the task for completing a single Benchmark with
 * the given image, blur radius, algorithm and rounds.
 * <p>
 * It uses warmup rounds to warmup the VM. After the benchmark
 * the statistics and downscaled versions of the blurred images
 * are store to disk.
 *
 * @author pfavre
 * @since 2014/04/14
 */
public class BlurBenchmarkTask extends AsyncTask<Void, Void, BenchmarkWrapper> {
    private static final String TAG = BlurBenchmarkTask.class.getSimpleName();
    private static final int WARMUP_ROUNDS = 5;

    private StatInfo statInfo;

    private long startWholeProcess;

    private int bitmapDrawableResId;
    private String absolutePath;
    private Bitmap master;
    private int benchmarkRounds;
    private int radius;
    private EBlurAlgorithm algorithm;
    private Context ctx;
    private RenderScript rs;
    private boolean run = false;
    private boolean isCustomPic = false;

    public BlurBenchmarkTask(BenchmarkImage image, int benchmarkRounds, int radius, EBlurAlgorithm algorithm, RenderScript rs, Context ctx) {
        if (image.isResId()) {
            this.bitmapDrawableResId = image.getResId();
        } else {
            this.absolutePath = image.getAbsolutePath();
            this.isCustomPic = true;
        }
        this.benchmarkRounds = benchmarkRounds;
        this.radius = radius;
        this.algorithm = algorithm;
        this.rs = rs;
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Start test with " + radius + "px radius, " + benchmarkRounds + "rounds in " + algorithm);
        startWholeProcess = BenchmarkUtil.elapsedRealTimeNanos();
    }

    @Override
    protected BenchmarkWrapper doInBackground(Void... voids) {
        try {
            run = true;
            long startReadBitmap = BenchmarkUtil.elapsedRealTimeNanos();
            master = loadBitmap();

            if (master == null) {
                throw new IOException("Could not load bitmap");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                master.setHasMipMap(false);
            long readBitmapDuration = (BenchmarkUtil.elapsedRealTimeNanos() - startReadBitmap) / 1000000l;

            statInfo = new StatInfo(master.getHeight(), master.getWidth(), radius, algorithm, benchmarkRounds, BitmapUtil.sizeOf(master));
            statInfo.setLoadBitmap(readBitmapDuration);

            Bitmap blurredBitmap = null;

            //if just quick round, skip warmup
            if (benchmarkRounds > WARMUP_ROUNDS) {
                Log.d(TAG, "Warmup");
                for (int i = 0; i < WARMUP_ROUNDS; i++) {
                    if (!run) {
                        break;
                    }

                    BenchmarkUtil.elapsedRealTimeNanos();
                    blurredBitmap = master.copy(master.getConfig(), true);
                    blurredBitmap = BlurUtil.blur(rs, ctx, blurredBitmap, radius, algorithm);
                }
            } else {
                Log.d(TAG, "Skip warmup");
            }

            Log.d(TAG, "Start benchmark");
            for (int i = 0; i < benchmarkRounds; i++) {
                if (!run) {
                    break;
                }

                long startBlur = BenchmarkUtil.elapsedRealTimeNanos();
                blurredBitmap = master.copy(master.getConfig(), true);
                blurredBitmap = BlurUtil.blur(rs, ctx, blurredBitmap, radius, algorithm);
                statInfo.getBenchmarkData().add((BenchmarkUtil.elapsedRealTimeNanos() - startBlur) / 1000000d);
            }

            if (!run) {
                return null;
            }

            statInfo.setBenchmarkDuration((BenchmarkUtil.elapsedRealTimeNanos() - startWholeProcess) / 1000000l);

            String fileName = master.getWidth() + "x" + master.getHeight() + "_" + radius + "px_" + algorithm + ".png";
            return new BenchmarkWrapper(BitmapUtil.saveBitmapDownscaled(blurredBitmap, fileName, BitmapUtil.getCacheDir(ctx), false, 800, 800),
                    BitmapUtil.saveBitmapDownscaled(BitmapUtil.flip(blurredBitmap), "mirror_" + fileName, BitmapUtil.getCacheDir(ctx), true, 300, 300),
                    statInfo, isCustomPic);
        } catch (Throwable e) {
            Log.e(TAG, "Could not complete benchmark", e);
            statInfo.setException(e);
            return new BenchmarkWrapper(null, null, statInfo, isCustomPic);
        }
    }

    private Bitmap loadBitmap() {
        if (isCustomPic && absolutePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inMutable = true;
            return BitmapFactory.decodeFile(absolutePath, options);
        } else {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            return BitmapFactory.decodeResource(ctx.getResources(), bitmapDrawableResId, options);
        }
    }

    public void cancelBenchmark() {
        run = false;
        Log.d(TAG, "canceled");
    }

    @Override
    protected void onPostExecute(BenchmarkWrapper bitmap) {
        master.recycle();
        master = null;
        Log.d(TAG, "test done");
    }

}
