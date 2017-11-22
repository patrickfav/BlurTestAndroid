package at.favre.app.blurbenchmark.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper for easier json serialization.
 *
 * @author pfavre
 */
public class BenchmarkResultList {
    private List<BenchmarkWrapper> benchmarkWrappers = new ArrayList<BenchmarkWrapper>();

    public BenchmarkResultList() {
    }

    public List<BenchmarkWrapper> getBenchmarkWrappers() {
        return benchmarkWrappers;
    }

    public void setBenchmarkWrappers(List<BenchmarkWrapper> benchmarkWrappers) {
        this.benchmarkWrappers = benchmarkWrappers;
    }
}
