package at.favre.app.blurbenchmark.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;

/**
 * Created by PatrickF on 25.05.2015.
 */
public class BenchmarkResultAdapter extends RecyclerView.Adapter<BenchmarkResultHolder> {
	private List<BenchmarkWrapper> results;

	public BenchmarkResultAdapter(List<BenchmarkWrapper> results) {
		this.results = results;
	}

	@Override
	public BenchmarkResultHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = inflater.inflate(R.layout.list_benchmark_result, viewGroup, false);


		return new BenchmarkResultHolder(convertView);
	}

	@Override
	public void onBindViewHolder(BenchmarkResultHolder benchmarkResultHolder, int i) {
		benchmarkResultHolder.onBind(results.get(i));
	}

	@Override
	public int getItemCount() {
		return results.size();
	}
}
