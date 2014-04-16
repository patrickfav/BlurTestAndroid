package at.favre.app.blurtest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

import at.favre.app.blurtest.util.Average;
import at.favre.app.blurtest.util.BlurUtil;

/**
* Created by PatrickF on 16.04.2014.
*/
public class StatInfo {
	private List<Double> benchmarkData;
	private long benchmarkDuration;
	private long loadBitmap;
	private int bitmapHeight;
	private int bitmapWidth;
	private int blurRadius;
	private BlurUtil.Algorithm algorithm;
	private boolean error=false;
	private String errorDescription;

	private Average<Double> avg;

	public StatInfo() {
	}

	public StatInfo(int bitmapHeight, int bitmapWidth, int blurRadius, BlurUtil.Algorithm algorithm) {
		this.bitmapHeight = bitmapHeight;
		this.bitmapWidth = bitmapWidth;
		this.blurRadius = blurRadius;
		this.algorithm = algorithm;
		benchmarkData = new ArrayList<Double>();
	}

	public StatInfo(String errorDescription, BlurUtil.Algorithm algorithm) {
		this.errorDescription = errorDescription;
		this.error = true;
		this.algorithm =algorithm;
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

	public BlurUtil.Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(BlurUtil.Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public void setBitmapHeight(int bitmapHeight) {
		this.bitmapHeight = bitmapHeight;
	}

	public void setBitmapWidth(int bitmapWidth) {
		this.bitmapWidth = bitmapWidth;
	}

	@JsonIgnore
	public String getBitmapKBSize() {
		return String.valueOf((double)Math.round((double) (bitmapHeight * bitmapWidth) / 1024d * 100d) / 100d)+"kB";
	}

	@JsonIgnore
	public String getMegaPixels() {
		return String.valueOf((double)Math.round((double) (bitmapHeight * bitmapWidth) / 1000000d * 100d) / 100d)+"MP";
	}
	@JsonIgnore
	public Average<Double> getAsAvg() {
		if (avg == null) {
			avg = new Average<Double>((List<Double>) benchmarkData);
		}
		return avg;
	}
}
