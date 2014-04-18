package at.favre.app.blurtest.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by PatrickF on 15.04.2014.
 */
public class BitmapUtil {
	private static final String TAG = BitmapUtil.class.getSimpleName();

	public static void clearCacheDir(File cacheDir) {
		File[] files = cacheDir.listFiles();

		if (files != null) {
			for (File file : files)
				file.delete();
		}
	}

	public static File saveBitmap(Bitmap bitmap, String filename, String path, boolean recycle) {
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
			Log.e(TAG, "Could not save bitmap", e);
		} finally {
			try{
				out.close();
			} catch(Throwable ignore) {}
			if(recycle) {
				bitmap.recycle();
			}
		}
		return null;
	}

	public static String getCacheDir(Context ctx) {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||!Environment.isExternalStorageRemovable() ?
				ctx.getExternalCacheDir().getPath() : ctx.getCacheDir().getPath();
	}

	public static Bitmap flip(Bitmap src) {
		Matrix m = new Matrix();
		m.preScale(-1, 1);
		return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
	}
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static int sizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return data.getByteCount();
		} else {
			return data.getAllocationByteCount();
		}
	}
}
