package at.favre.app.blurbenchmark.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class BenchmarkUtil {
    private static final DecimalFormat format = new DecimalFormat("#.0");
    private static final String fileSeperator = ";";

    static {
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        format.setRoundingMode(RoundingMode.HALF_UP);
    }

    @TargetApi(17)
    public static long elapsedRealTimeNanos() {
        if (Build.VERSION.SDK_INT >= 17) {
            return SystemClock.elapsedRealtimeNanos();
        }
        return SystemClock.elapsedRealtime() * 1000000l;
    }

    public static String formatNum(double number) {
        return format.format(number);
    }

    public static String formatNum(double number, String formatString) {
        final DecimalFormat format = new DecimalFormat(formatString);
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(number);
    }

    public static String saveFiles(List<File> files) {
        StringJoiner joiner = new StringJoiner(fileSeperator);
        for (File file : files) {
            joiner.add(file.getAbsolutePath());
        }
        return joiner.toString();
    }

    public static List<File> getAsFiles(String filestring) {
        String[] files = filestring.split(fileSeperator);
        List<File> fileArrayList = new ArrayList<File>();
        for (String absPath : files) {
            File f = new File(absPath);
            if (f.isFile() && !f.getAbsolutePath().isEmpty()) {
                fileArrayList.add(f);
            }
        }
        return fileArrayList;
    }

    public static String getScalingUnitByteSize(int byteSize) {
        double scaledByteSize = (double) byteSize;
        String unit = "byte";

        if (scaledByteSize < 1024) {
            return formatNum(scaledByteSize, "0.##") + unit;
        } else {
            unit = "KiB";
            scaledByteSize /= 1024d;

            if (scaledByteSize < 1024) {
                return formatNum(scaledByteSize, "0.##") + unit;
            } else {
                unit = "MiB";
                scaledByteSize /= 1024d;
                if (scaledByteSize < 1024) {
                    return formatNum(scaledByteSize, "0.##") + unit;
                } else {
                    unit = "GiB";
                    scaledByteSize /= 1024d;
                    return formatNum(scaledByteSize, "0.##") + unit;
                }
            }
        }

    }
}
