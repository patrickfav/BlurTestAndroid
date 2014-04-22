package at.favre.app.blurtest.blur;

import android.graphics.Bitmap;

/**
 * Created by PatrickF on 20.04.2014.
 */
public interface IBlur {
    public Bitmap blur(int radius, Bitmap original);
}
