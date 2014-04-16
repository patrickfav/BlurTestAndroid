package at.favre.app.blurtest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

import at.favre.app.blurtest.util.BlurUtil;

/**
 * Created by PatrickF on 16.04.2014.
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
			if(benchmarkEntry.equals(name)) {
				return benchmarkEntry;
			}
		}
		return null;
	}

	@JsonIgnore
	public List<BenchmarkEntry> getAllByCategory(String category) {
		List<BenchmarkEntry> list = new ArrayList<BenchmarkEntry>();
		for (BenchmarkEntry benchmarkEntry : entryList) {
			if(benchmarkEntry.getCategory().equals(category)) {
				list.add(benchmarkEntry);
			}
		}
		return list;
	}

	@JsonIgnore
	public BenchmarkEntry getByCategoryAndAlgorithm(String category, BlurUtil.Algorithm algorithm) {
		for (BenchmarkEntry benchmarkEntry : entryList) {
			if(benchmarkEntry.getCategory().equals(category)) {
				if(!benchmarkEntry.getWrapper().isEmpty() && benchmarkEntry.getWrapper().get(0).getStatInfo().getAlgorithm().equals(algorithm)) {
					return benchmarkEntry;
				}
			}
		}
		return null;
	}


	public static class BenchmarkEntry {
		private String name;
		private String category;
		private List<BenchmarkWrapper> wrapper = new ArrayList<BenchmarkWrapper>();

		public BenchmarkEntry() {
		}

		public BenchmarkEntry(String name, String category, List<BenchmarkWrapper> wrapper) {
			this.name = name;
			this.category = category;
			this.wrapper = wrapper;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BenchmarkEntry that = (BenchmarkEntry) o;

			if (name != null ? !name.equals(that.name) : that.name != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}
	}
}
