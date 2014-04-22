package at.favre.app.blurtest.blur;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 20.04.2014.
 */
public enum EBlurAlgorithm {
    RS_GAUSSIAN, RS_SIMPLEBLUR_3x3, RS_SIMPLEBLUR_5x5, STACKBLUR, GAUSSIAN_BLUR_FAST, BOX_BLUR, NONE;

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
