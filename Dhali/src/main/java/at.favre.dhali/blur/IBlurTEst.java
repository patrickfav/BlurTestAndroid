package at.favre.lib.dhali.blur;

import android.graphics.Bitmap;

/**
 * Created by PatrickF on 20.04.2014.
 */
public interface IBlurTEst {
	public static final int MS_THRESHOLD_FOR_SMOOTH = 16;

	public Bitmap blur(int radius, Bitmap original);
}
