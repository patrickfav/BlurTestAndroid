package at.favre.app.blurbenchmark.models;

/**
 * Wrapper for an image, that either holds a file path or resource id
 *
 * @author pfavre
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
