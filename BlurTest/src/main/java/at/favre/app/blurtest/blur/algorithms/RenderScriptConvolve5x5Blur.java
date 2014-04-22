package at.favre.app.blurtest.blur.algorithms;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicConvolve5x5;

import at.favre.app.blurtest.blur.IBlur;

/**
 * Created by PatrickF on 20.04.2014.
 */
public class RenderScriptConvolve5x5Blur implements IBlur {
    private RenderScript rs;

    public RenderScriptConvolve5x5Blur(RenderScript rs) {
        this.rs = rs;
    }

    @Override
    public Bitmap blur(int radius, Bitmap bitmapOriginal) {
        Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal, Allocation.MipmapControl.MIPMAP_NONE,Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicConvolve5x5 script = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs));
        script.setCoefficients(new float[] {0.04f,0.04f,0.04f,0.04f,0.04f,  0.04f,0.04f,0.04f,0.04f,0.04f,  0.04f,0.0425f,0.05f,0.0425f,0.04f,  0.04f,0.04f,0.04f,0.04f,0.04f,  0.04f,0.04f,0.04f,0.04f,0.04f,});
        for (int i = 0; i < radius; i++) {
            script.setInput(input);
            script.forEach(output);
            input = output;
        }
        output.copyTo(bitmapOriginal);
        return bitmapOriginal;
    }
}
