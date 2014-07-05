package at.favre.app.blurbenchmark.blur;

import java.util.ArrayList;
import java.util.List;

import at.favre.app.blurbenchmark.R;

/**
 * Enum of all supported algorithms
 *
 * @author pfavre
 */
public enum EBlurAlgorithm {
    RS_GAUSS_FAST(R.color.graphBgGreen), RS_BOX_5x5(R.color.graphBlue),
	RS_GAUSS_5x5(R.color.graphBgWhite), RS_STACKBLUR(R.color.graphBgViolet),
    NDK_STACKBLUR(R.color.graphBgOrange), STACKBLUR(R.color.graphBgYellow),
	GAUSS_FAST(R.color.graphBgRed), BOX_BLUR(R.color.graphBgTurquoise), NONE(R.color.graphBgBlack)
    ;

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
