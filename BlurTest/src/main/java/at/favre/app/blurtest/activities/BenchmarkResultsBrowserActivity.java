package at.favre.app.blurtest.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.fragments.BlurBenchmarkResultsBrowserFragment;

/**
 * Created by PatrickF on 20.04.2014.
 */
public class BenchmarkResultsBrowserActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benchmark_result);

        if(savedInstanceState == null) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.add(android.R.id.content, new BlurBenchmarkResultsBrowserFragment(),BlurBenchmarkResultsBrowserFragment.class.getSimpleName());
            t.commit();
        }
    }
}
