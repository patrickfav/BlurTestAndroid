package at.favre.app.blurbenchmark.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;

/**
 * This is the main logic on retrieving specific data from the benchmark database
 *
 * @author pfavre
 */
public class BenchmarkResultDatabase {
    private List<BenchmarkEntry> entryList = new ArrayList<BenchmarkEntry>();

    public List<BenchmarkEntry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<BenchmarkEntry> entryList) {
        this.entryList = entryList;
    }

    @JsonIgnore
    public BenchmarkEntry getByName(String name) {
        for (BenchmarkEntry benchmarkEntry : entryList) {
            if (benchmarkEntry.getName().equals(name)) {
                return benchmarkEntry;
            }
        }
        return null;
    }

    @JsonIgnore
    public List<BenchmarkEntry> getAllByCategory(String category) {
        List<BenchmarkEntry> list = new ArrayList<BenchmarkEntry>();
        for (BenchmarkEntry benchmarkEntry : entryList) {
            if (benchmarkEntry.getCategory().equals(category)) {
                list.add(benchmarkEntry);
            }
        }
        return list;
    }

    @JsonIgnore
    public List<BenchmarkEntry> getAllByBlurRadius(int radius) {
        List<BenchmarkEntry> list = new ArrayList<BenchmarkEntry>();
        for (BenchmarkEntry benchmarkEntry : entryList) {
            if (benchmarkEntry.getRadius() == radius) {
                list.add(benchmarkEntry);
            }
        }
        return list;
    }

    @JsonIgnore
    public TreeSet<ImageSize> getAllImageSizes() {
        TreeSet<ImageSize> set = new TreeSet<ImageSize>();
        for (BenchmarkEntry benchmarkEntry : entryList) {
            set.add(benchmarkEntry.getAsImageSize());
        }
        return set;
    }

    @JsonIgnore
    public BenchmarkEntry getByImageSizeAndRadiusAndAlgorithm(String imageSize, int radius, EBlurAlgorithm algorithm) {
        List<BenchmarkEntry> list = new ArrayList<BenchmarkEntry>();
        for (BenchmarkEntry benchmarkEntry : entryList) {
            if (benchmarkEntry.getImageSizeString().equals(imageSize) && benchmarkEntry.getRadius() == radius && !benchmarkEntry.getWrapper().isEmpty() && benchmarkEntry.getWrapper().get(0).getStatInfo().getAlgorithm().equals(algorithm)) {
                return (benchmarkEntry);
            }
        }
        return null;
    }

    @JsonIgnore
    public Set<Integer> getAllBlurRadii() {
        TreeSet<Integer> list = new TreeSet<Integer>();
        for (BenchmarkEntry benchmarkEntry : entryList) {
            list.add(benchmarkEntry.getRadius());
        }
        return list;
    }

    @JsonIgnore
    public BenchmarkEntry getByCategoryAndAlgorithm(String category, EBlurAlgorithm algorithm) {
        for (BenchmarkEntry benchmarkEntry : entryList) {
            if (benchmarkEntry.getCategory().equals(category)) {
                if (!benchmarkEntry.getWrapper().isEmpty() && benchmarkEntry.getWrapper().get(0).getStatInfo().getAlgorithm().equals(algorithm)) {
                    return benchmarkEntry;
                }
            }
        }
        return null;
    }

    @JsonIgnore
    public static BenchmarkWrapper getRecentWrapper(BenchmarkEntry entry) {
        if (entry != null && !entry.getWrapper().isEmpty()) {
            Collections.sort(entry.getWrapper());
            return entry.getWrapper().get(0);
        } else {
            return null;
        }
    }

    public static class BenchmarkEntry implements Comparable<BenchmarkEntry> {
        private String name;
        private String category;
        private int radius;
        private int height;
        private int width;
        private List<BenchmarkWrapper> wrapper = new ArrayList<BenchmarkWrapper>();

        public BenchmarkEntry() {
        }

        public BenchmarkEntry(String name, String category, int radius, int height, int width, List<BenchmarkWrapper> wrapper) {
            this.name = name;
            this.category = category;
            this.wrapper = wrapper;
            this.radius = radius;
            this.height = height;
            this.width = width;
        }

        public BenchmarkEntry(BenchmarkWrapper benchmarkWrapper) {
            this(benchmarkWrapper.getStatInfo().getKeyString(), benchmarkWrapper.getStatInfo().getCategoryString(), benchmarkWrapper.getStatInfo().getBlurRadius(), benchmarkWrapper.getStatInfo().getBitmapHeight(), benchmarkWrapper.getStatInfo().getBitmapWidth(), new ArrayList<BenchmarkWrapper>());
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<BenchmarkWrapper> getWrapper() {
            return wrapper;
        }

        public void setWrapper(List<BenchmarkWrapper> wrapper) {
            this.wrapper = wrapper;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @JsonIgnore
        public Category getCategoryObj() {
            return new Category(getAsImageSize(), radius, category);
        }

        @JsonIgnore
        public Integer getResolution() {
            return new Integer(height * width);
        }

        @JsonIgnore
        public String getImageSizeString() {
            return height + "x" + width;
        }

        @JsonIgnore
        public ImageSize getAsImageSize() {
            return new ImageSize(height, width, getImageSizeString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BenchmarkEntry that = (BenchmarkEntry) o;

            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public int compareTo(BenchmarkEntry benchmarkEntry) {
            return getResolution().compareTo(benchmarkEntry.getResolution());
        }
    }

    public static class Category implements Comparable<Category> {
        public final ImageSize imageSize;
        public final int radius;
        public final String category;

        public Category(ImageSize imageSize, int radius, String category) {
            this.imageSize = imageSize;
            this.radius = radius;
            this.category = category;
        }

        public ImageSize getImageSize() {
            return imageSize;
        }

        public Integer getRadius() {
            return radius;
        }

        public String getCategory() {
            return category;
        }

        @Override
        public int compareTo(Category category) {
            int resultResolution = imageSize.getResolution().compareTo(category.getImageSize().getResolution());

            if (resultResolution == 0) {
                return getRadius().compareTo(category.getRadius());
            } else {
                return resultResolution;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Category category = (Category) o;

            if (radius != category.radius) return false;
            return imageSize.equals(category.imageSize);
        }

        @Override
        public int hashCode() {
            int result = imageSize.hashCode();
            result = 31 * result + radius;
            return result;
        }
    }

    public static class ImageSize implements Comparable<ImageSize> {
        private final int height;
        private final int width;
        private final String imageSizeString;

        public ImageSize(int height, int width, String imageSizeString) {
            this.height = height;
            this.width = width;
            this.imageSizeString = imageSizeString;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getImageSizeString() {
            return imageSizeString;
        }

        public Integer getResolution() {
            return height * width;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageSize imageSize = (ImageSize) o;

            if (height != imageSize.height) return false;
            return width == imageSize.width;
        }

        @Override
        public int hashCode() {
            int result = height;
            result = 31 * result + width;
            return result;
        }

        @Override
        public int compareTo(ImageSize imageSize) {
            return getResolution().compareTo(imageSize.getResolution());
        }
    }
}
