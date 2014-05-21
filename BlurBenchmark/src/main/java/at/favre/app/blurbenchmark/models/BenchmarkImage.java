package at.favre.app.blurbenchmark.models;

/**
* Created by PatrickF on 21.05.2014.
*/
public class BenchmarkImage {
	private int resId;
	private String absolutePath;

	public BenchmarkImage(int resId) {
		this.resId = resId;
	}

	public BenchmarkImage(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public int getResId() {
		return resId;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public boolean isResId() {
		return absolutePath == null;
	}
}
