package at.favre.app.blurbenchmark.activities;

import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), getResources().getColor(R.color.color_primary_dark)));
        }

        if (savedInstanceState == null) {
            FragmentTransaction t = getFragmentManager().beginTransaction();
            t.add(R.id.root, BenchmarkResultFragment.createInstance(JsonUtil.fromJsonString(getIntent().getStringExtra(BENCHMARK_LIST_KEY), BenchmarkResultList.class)), BenchmarkResultFragment.class.getSimpleName());
            t.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            findViewById(R.id.root).requestLayout();
            findViewById(R.id.root).invalidate();
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

    public void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Benchmark Results");
        getSupportActionBar().setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
    }
}
