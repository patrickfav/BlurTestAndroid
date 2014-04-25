package at.favre.app.blurbenchmark.blur;

import android.graphics.Bitmap;

/**
 * Created by PatrickF on 20.04.2014.
 */
public interface IBlur {
	public static final int MS_THRESHOLD_FOR_SMOOTH = 16;

	public Bitmap blur(int radius, Bitmap original);
}
