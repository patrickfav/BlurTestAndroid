package at.favre.lib.dhali.blur;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 20.04.2014.
 */
public enum EBlurAlgorithm {
    RS_GAUSS_FAST, RS_BOX_5x5,
	RS_GAUSS_5x5, RS_STACKBLUR,STACKBLUR,
	GAUSS_FAST, BOX_BLUR, NONE;


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
