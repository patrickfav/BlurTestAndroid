package ru0xdc.ne10;

import android.graphics.Bitmap;


/**
 * Blur using the box filter of the NE10 image processing module
 * https://github.com/projectNe10/Ne10
 */
public class Blur {

    static {
        System.loadLibrary("ne10blur");
    }

    private static native void functionToBlur(Bitmap bitmapOut, int radius);

    public Bitmap blur(int radius, Bitmap bitmap) {
        functionToBlur(bitmap, radius);
        return bitmap;
    }
}
