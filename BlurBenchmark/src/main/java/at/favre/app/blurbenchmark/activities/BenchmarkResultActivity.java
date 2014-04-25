package at.favre.app.blurbenchmark.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.fragments.BlurBenchmarkResultFragment;
import at.favre.app.blurbenchmark.models.BenchmarkResultList;
import at.favre.app.blurbenchmark.util.JsonUtil;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class BenchmarkResultActivity extends ActionBarActivity {
	public static final String BENCHMARK_LIST_KEY = "benchmark_list";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_benchmark_result);
		getSupportActionBar().hide();
		if(savedInstanceState == null) {
			FragmentTransaction t = getSupportFragmentManager().beginTransaction();
			t.add(android.R.id.content,BlurBenchmarkResultFragment.createInstance(JsonUtil.fromJsonString(getIntent().getStringExtra(BENCHMARK_LIST_KEY), BenchmarkResultList.class)),BlurBenchmarkResultFragment.class.getSimpleName());
			t.commit();
		}
	}
}
