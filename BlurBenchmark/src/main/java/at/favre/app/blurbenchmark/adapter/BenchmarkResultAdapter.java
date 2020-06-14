package at.favre.app.blurbenchmark.adapter;

import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;

/**
 * Created by PatrickF on 25.05.2015.
 */
public class BenchmarkResultAdapter extends RecyclerView.Adapter<BenchmarkResultHolder> {

    private List<BenchmarkWrapper> results;
    private FragmentManager fragmentManager;

    public BenchmarkResultAdapter(List<BenchmarkWrapper> results, FragmentManager fragmentManager) {
        this.results = results;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public BenchmarkResultHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.list_benchmark_result, viewGroup, false);
        return new BenchmarkResultHolder(convertView, fragmentManager);
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
