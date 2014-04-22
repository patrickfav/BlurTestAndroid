package at.favre.app.blurtest.blur;

import java.util.ArrayList;
import java.util.List;

import at.favre.app.blurtest.R;

/**
 * Created by PatrickF on 20.04.2014.
 */
public enum EBlurAlgorithm {
    RS_GAUSSIAN(R.color.graphBgGreen), RS_SIMPLEBLUR_3x3(R.color.graphBgOrange), RS_SIMPLEBLUR_5x5(R.color.graphBlue), STACKBLUR(R.color.graphBgYellow), GAUSSIAN_BLUR_FAST(R.color.graphBgRed), BOX_BLUR(R.color.graphBgTurquoise), NONE(R.color.graphBgBlack);

	private final int colorResId;

	EBlurAlgorithm(int colorResId) {
		this.colorResId = colorResId;
	}

	public int getColorResId() {
		return colorResId;
	}

	public static List<EBlurAlgorithm> getAllAlgorithms() {
        List<EBlurAlgorithm> algorithms = new ArrayList<EBlurAlgorithm>();
        for (EBlurAlgorithm algorithm : values()) {
            if(!algorithm.equals(NONE)) {
                algorithms.add(algorithm);
            }
        }
        return algorithms;
    }
}
