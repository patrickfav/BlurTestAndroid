package at.favre.app.blurbenchmark.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;
import at.favre.app.blurbenchmark.util.Average;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;

/**
 * Wrapper for all statistic info on a benchmark
 *
 * @author pfavre
 */
public class StatInfo {
    private List<Double> benchmarkData;
    private long benchmarkDuration;
    private long loadBitmap;
    private int bitmapHeight;
    private int bitmapWidth;
    private int blurRadius;
    private int rounds;
    private EBlurAlgorithm algorithm;
    private boolean error = false;
    private String errorDescription;
    private long date;
    private Integer byteAllocation;

    private Average<Double> avg;

    public StatInfo() {
    }

    public StatInfo(int bitmapHeight, int bitmapWidth, int blurRadius, EBlurAlgorithm algorithm, int rounds, Integer byteAllocation) {
        this.bitmapHeight = bitmapHeight;
        this.bitmapWidth = bitmapWidth;
        this.blurRadius = blurRadius;
        this.algorithm = algorithm;
        this.rounds = rounds;
        benchmarkData = new ArrayList<Double>();
        date = new Date().getTime();
        this.byteAllocation = byteAllocation;
    }

    public long getLoadBitmap() {
        return loadBitmap;
    }

    public void setLoadBitmap(long loadBitmap) {
        this.loadBitmap = loadBitmap;
    }

    public List<Double> getBenchmarkData() {
        avg = null;
        return benchmarkData;
    }

    public void setBenchmarkData(List<Double> benchmarkData) {
        avg = null;
        this.benchmarkData = benchmarkData;
    }

    public int getBitmapHeight() {
        return bitmapHeight;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public long getBenchmarkDuration() {
        return benchmarkDuration;
    }

    public void setBenchmarkDuration(long benchmarkDuration) {
        this.benchmarkDuration = benchmarkDuration;
    }

    public void setException(Throwable throwable) {
        setError(true);
        setErrorDescription(throwable.toString());
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
    }

    public EBlurAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(EBlurAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void setBitmapHeight(int bitmapHeight) {
        this.bitmapHeight = bitmapHeight;
    }

    public void setBitmapWidth(int bitmapWidth) {
        this.bitmapWidth = bitmapWidth;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getByteAllocation() {
        if (byteAllocation == null) {
            return byteAllocation = bitmapWidth * bitmapHeight;
        }
        return byteAllocation;
    }

    public void setByteAllocation(int byteAllocation) {
        this.byteAllocation = byteAllocation;
    }

    @JsonIgnore
    public Average<Double> getAvg() {
        return avg;
    }

    @JsonIgnore
    public void setAvg(Average<Double> avg) {
        this.avg = avg;
    }

    @JsonIgnore
    public double getThroughputMPixelsPerSec() {
        return (double) bitmapWidth * (double) bitmapHeight / getAsAvg().getAvg() * 1000d / 1000000d;
    }

    @JsonIgnore
    public String getKeyString() {
        return bitmapHeight + "x" + bitmapWidth + "_" + algorithm + "_" + String.format("%02d", blurRadius) + "px";
    }

    @JsonIgnore
    public String getCategoryString() {
        return getImageSizeCategoryString() + " / " + BenchmarkUtil.formatNum(blurRadius, "00") + "px";
    }

    @JsonIgnore
    public String getImageSizeCategoryString() {
        return bitmapHeight + "x" + bitmapWidth;
    }

    @JsonIgnore
    public String getBitmapByteSize() {
        if (byteAllocation == null) {
            return BenchmarkUtil.getScalingUnitByteSize(bitmapHeight * bitmapWidth);
        } else {
            return BenchmarkUtil.getScalingUnitByteSize(byteAllocation);
        }
    }

    @JsonIgnore
    public String getMegaPixels() {
        return String.valueOf((double) Math.round((double) (bitmapHeight * bitmapWidth) / 1000000d * 100d) / 100d) + "MP";
    }

    @JsonIgnore
    public Average<Double> getAsAvg() {
        if (avg == null) {
            avg = new Average<Double>(benchmarkData);
        }
        return avg;
    }
}
