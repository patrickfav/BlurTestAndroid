package at.favre.app.blurbenchmark.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.fragments.BenchmarkResultFragment;
import at.favre.app.blurbenchmark.models.BenchmarkResultList;
import at.favre.app.blurbenchmark.util.JsonUtil;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class BenchmarkResultActivity extends AppCompatActivity {
	public static final String BENCHMARK_LIST_KEY = "benchmark_list";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_benchmark_result);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("Benchmark Results");
		getSupportActionBar().setElevation(7.5f);

		if(savedInstanceState == null) {
			FragmentTransaction t = getSupportFragmentManager().beginTransaction();
			t.add(R.id.root, BenchmarkResultFragment.createInstance(JsonUtil.fromJsonString(getIntent().getStringExtra(BENCHMARK_LIST_KEY), BenchmarkResultList.class)),BenchmarkResultFragment.class.getSimpleName());
			t.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (Build.VERSION.SDK_INT >= 16) {
					NavUtils.navigateUpFromSameTask(this);
				} else {
					finish();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
