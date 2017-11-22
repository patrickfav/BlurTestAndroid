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
    //    NDK_STACKBLUR(R.color.graphBgOrange),
//    NDK_NE10_BOX_BLUR(R.color.graphBgSkyBlue),
    STACKBLUR(R.color.graphBgYellow),
    GAUSS_FAST(R.color.graphBgRed), BOX_BLUR(R.color.graphBgTurquoise),
    SUPER_FAST_BLUR(R.color.graphBgSandyBrown), NONE(R.color.graphBgBlack);

    /**
     * Color used in graphs
     */
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
            if (!algorithm.equals(NONE)) {
                algorithms.add(algorithm);
            }
        }
        return algorithms;
    }
}
