package at.favre.app.blurtest.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import at.favre.app.blurtest.BenchmarkListAdapter;
import at.favre.app.blurtest.R;
import at.favre.app.blurtest.activities.BenchmarkResultActivity;
import at.favre.app.blurtest.models.BenchmarkResultList;
import at.favre.app.blurtest.util.JsonUtil;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BlurBenchmarkResultFragment extends Fragment {
	private static final String TAG = BlurBenchmarkResultFragment.class.getSimpleName();

	private BenchmarkResultList benchmarkResultList = new BenchmarkResultList();

	private ListAdapter adapter;
	private ListView listView;
	private View headerView;

	public static BlurBenchmarkResultFragment createInstance(BenchmarkResultList resultList) {
		BlurBenchmarkResultFragment fragment = new BlurBenchmarkResultFragment();
		fragment.setBenchmarkResultList(resultList);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			benchmarkResultList = JsonUtil.fromJsonString(savedInstanceState.getString(BenchmarkResultActivity.BENCHMARK_LIST_KEY), BenchmarkResultList.class);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_benchmark_results,container,false);
		headerView = inflater.inflate(R.layout.list_benchmark_header,null);

		listView = (ListView) v.findViewById(R.id.listview);
		setUpListView();
		return v;
	}


	@Override
	public void onResume() {
		super.onResume();
		setBackground();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BenchmarkResultActivity.BENCHMARK_LIST_KEY, JsonUtil.toJsonString(benchmarkResultList));
	}

	private void setBackground() {
		if(!benchmarkResultList.getBenchmarkWrappers().isEmpty()) {
			getView().findViewById(R.id.innerRoot).setBackgroundColor(getResources().getColor(R.color.halftransparent));

			new AsyncTask<Void,Void,Bitmap>() {
				@Override
				protected Bitmap doInBackground(Void... voids) {
					try {
						return Picasso.with(getActivity()).load(benchmarkResultList.getBenchmarkWrappers().get(benchmarkResultList.getBenchmarkWrappers().size() - 1).getBitmapAsFile()).get();
					} catch (IOException e) {
						Log.w(TAG, "Could not set background", e);
						return null;
					}
				}
				@Override
				protected void onPostExecute(Bitmap bitmap) {
					if(getView() != null) {
						getView().findViewById(R.id.root).setBackgroundDrawable(new BitmapDrawable(getActivity().getResources(), bitmap));
					}
				}
			}.execute();
		}
	}

	private void setUpListView() {
		if(!benchmarkResultList.getBenchmarkWrappers().isEmpty()) {
			((TextView) headerView.findViewById(R.id.tv_header)).setText(benchmarkResultList.getBenchmarkWrappers().get(0).getStatInfo().getAlgorithm().toString());
            listView.removeHeaderView(headerView);
			listView.addHeaderView(headerView);
			adapter = new BenchmarkListAdapter(getActivity(), R.id.list_item, benchmarkResultList.getBenchmarkWrappers());
			listView.setAdapter(adapter);
		}
	}


	public void setBenchmarkResultList(BenchmarkResultList benchmarkResultList) {
		this.benchmarkResultList = benchmarkResultList;
	}
}
