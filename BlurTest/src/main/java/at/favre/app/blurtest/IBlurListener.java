package at.favre.app.blurtest;

import android.graphics.Bitmap;

/**
 * Created by PatrickF on 09.04.2014.
 */
public interface IBlurListener {
	public void update(Bitmap top, Bitmap bottom);
}
