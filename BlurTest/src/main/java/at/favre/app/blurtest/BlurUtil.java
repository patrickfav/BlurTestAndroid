package at.favre.app.blurtest;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by PatrickF on 07.04.2014.
 */
public class BlurUtil {
	private static final String TAG = BlurUtil.class.getSimpleName();

	public enum Algorithm {RENDERSCRIPT, STACKBLUR, GAUSSIAN_BLUR_FAST, BOX_BLUR}

	public static Bitmap blur(Context context, Bitmap bitmap, int radius, Algorithm algorithm) {
		Log.i(TAG,"Using "+algorithm);
		switch (algorithm) {
			case RENDERSCRIPT:
				return blurRenderScript(context,bitmap,radius);
			case STACKBLUR:
				return blurStackBlur(bitmap,radius);
			case GAUSSIAN_BLUR_FAST:
				return gaussianBlurFast(bitmap,radius);
			case BOX_BLUR:
				return boxBlur(bitmap,radius);
			default:
				return bitmap;
		}
	}

	private static Bitmap blurRenderScript(Context context, Bitmap bitmap, int radius) {
		if (Build.VERSION.SDK_INT > 17) {
			Log.d(TAG,"Using renderscript");

			final RenderScript rs = RenderScript.create(context);
			final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
					Allocation.USAGE_SCRIPT);
			final Allocation output = Allocation.createTyped(rs, input.getType());
			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			script.setRadius(radius /* e.g. 3.f */);
			script.setInput(input);
			script.forEach(output);
			output.copyTo(bitmap);
			return bitmap;
		} else {
			Toast.makeText(context, "Renderscript needs sdk >= 17", Toast.LENGTH_LONG).show();
			return bitmap;
		}
	}

	/**
	 * http://stackoverflow.com/questions/8218438
	 * @param bmp
	 * @param range
	 * @return
	 */
	public static Bitmap boxBlur(Bitmap bmp, int range) {
		assert (range & 1) == 0 : "Range must be odd.";

		Bitmap blurred = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(blurred);

		int w = bmp.getWidth();
		int h = bmp.getHeight();

		int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
		bmp.getPixels(pixels, 0, w, 0, 0, w, h);

		boxBlurHorizontal(pixels, w, h, range / 2);
		boxBlurVertical(pixels, w, h, range / 2);

		c.drawBitmap(pixels, 0, w, 0.0F, 0.0F, w, h, true, null);

		return blurred;
	}

	private static void boxBlurHorizontal(int[] pixels, int w, int h,
										  int halfRange) {
		int index = 0;
		int[] newColors = new int[w];

		for (int y = 0; y < h; y++) {
			int hits = 0;
			long r = 0;
			long g = 0;
			long b = 0;
			for (int x = -halfRange; x < w; x++) {
				int oldPixel = x - halfRange - 1;
				if (oldPixel >= 0) {
					int color = pixels[index + oldPixel];
					if (color != 0) {
						r -= Color.red(color);
						g -= Color.green(color);
						b -= Color.blue(color);
					}
					hits--;
				}

				int newPixel = x + halfRange;
				if (newPixel < w) {
					int color = pixels[index + newPixel];
					if (color != 0) {
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}
					hits++;
				}

				if (x >= 0) {
					newColors[x] = Color.argb(0xFF, (int) (r / hits), (int) (g / hits), (int) (b / hits));
				}
			}

			for (int x = 0; x < w; x++) {
				pixels[index + x] = newColors[x];
			}

			index += w;
		}
	}

	private static void boxBlurVertical(int[] pixels, int w, int h,
										int halfRange) {

		int[] newColors = new int[h];
		int oldPixelOffset = -(halfRange + 1) * w;
		int newPixelOffset = (halfRange) * w;

		for (int x = 0; x < w; x++) {
			int hits = 0;
			long r = 0;
			long g = 0;
			long b = 0;
			int index = -halfRange * w + x;
			for (int y = -halfRange; y < h; y++) {
				int oldPixel = y - halfRange - 1;
				if (oldPixel >= 0) {
					int color = pixels[index + oldPixelOffset];
					if (color != 0) {
						r -= Color.red(color);
						g -= Color.green(color);
						b -= Color.blue(color);
					}
					hits--;
				}

				int newPixel = y + halfRange;
				if (newPixel < h) {
					int color = pixels[index + newPixelOffset];
					if (color != 0) {
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}
					hits++;
				}

				if (y >= 0) {
					newColors[y] = Color.argb(0xFF, (int) (r / hits), (int) (g / hits), (int) (b / hits));
				}

				index += w;
			}

			for (int y = 0; y < h; y++) {
				pixels[y * w + x] = newColors[y];
			}
		}
	}

	/**
	 * http://stackoverflow.com/a/13436737/774398
	 * @param bmp
	 * @param radius
	 */
	static Bitmap gaussianBlurFast (Bitmap bmp, int radius) {
		Bitmap copy = bmp.copy(bmp.getConfig(), true);
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		int[] pix = new int[w * h];
		bmp.getPixels(pix, 0, w, 0, 0, w, h);

		for(int r = radius; r >= 1; r /= 2) {
			for(int i = r; i < h - r; i++) {
				for(int j = r; j < w - r; j++) {
					int tl = pix[(i - r) * w + j - r];
					int tr = pix[(i - r) * w + j + r];
					int tc = pix[(i - r) * w + j];
					int bl = pix[(i + r) * w + j - r];
					int br = pix[(i + r) * w + j + r];
					int bc = pix[(i + r) * w + j];
					int cl = pix[i * w + j - r];
					int cr = pix[i * w + j + r];

					pix[(i * w) + j] = 0xFF000000 |
							(((tl & 0xFF) + (tr & 0xFF) + (tc & 0xFF) + (bl & 0xFF) + (br & 0xFF) + (bc & 0xFF) + (cl & 0xFF) + (cr & 0xFF)) >> 3) & 0xFF |
							(((tl & 0xFF00) + (tr & 0xFF00) + (tc & 0xFF00) + (bl & 0xFF00) + (br & 0xFF00) + (bc & 0xFF00) + (cl & 0xFF00) + (cr & 0xFF00)) >> 3) & 0xFF00 |
							(((tl & 0xFF0000) + (tr & 0xFF0000) + (tc & 0xFF0000) + (bl & 0xFF0000) + (br & 0xFF0000) + (bc & 0xFF0000) + (cl & 0xFF0000) + (cr & 0xFF0000)) >> 3) & 0xFF0000;
				}
			}
		}
		copy.setPixels(pix, 0, w, 0, 0, w, h);
		return copy;
	}

	/** Stack BlurUtil v1.0 from
	http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html

	Java Author: Mario Klingemann <mario at quasimondo.com>
	http://incubator.quasimondo.com
	created Feburary 29, 2004
	Android port : Yahel Bouaziz <yahel at kayenko.com>
	http://www.kayenko.com
	ported april 5th, 2012

	This is a compromise between Gaussian BlurUtil and Box blur
	It creates much better looking blurs than Box BlurUtil, but is
	7x faster than my Gaussian BlurUtil implementation.

	I called it Stack BlurUtil because this describes best how this
	filter works internally: it creates a kind of moving stack
	of colors whilst scanning through the image. Thereby it
	just has to add one new block of color to the right side
	of the stack and remove the leftmost color. The remaining
	colors on the topmost layer of the stack are either added on
	or reduced by one, depending on if they are on the right or
	on the left side of the stack.

	If you are using this algorithm in your code please add
	the following line:

	Stack BlurUtil Algorithm by Mario Klingemann <mario@quasimondo.com> */

	private static Bitmap blurStackBlur(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public static int sizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else {
			return data.getByteCount();
		}
	}
}