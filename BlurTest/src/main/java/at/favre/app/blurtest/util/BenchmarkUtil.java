package at.favre.app.blurtest.util;

import android.os.Build;
import android.os.SystemClock;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class BenchmarkUtil {
	public static long elapsedRealTimeNanos() {
		if(Build.VERSION.SDK_INT >= 17) {
			return SystemClock.elapsedRealtimeNanos();
		}
		return SystemClock.elapsedRealtime()*1000000l;
	}
}
