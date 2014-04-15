package at.favre.app.blurtest.util;

import android.content.Context;
import android.graphics.Bitmap;
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

	public static File saveAndRecycleBitmap(Bitmap bitmap, String filename, String path) {
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
			bitmap.recycle();
		}
		return null;
	}

	public static String getCacheDir(Context ctx) {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||!Environment.isExternalStorageRemovable() ?
				ctx.getExternalCacheDir().getPath() : ctx.getCacheDir().getPath();
	}
}
