package at.favre.app.blurbenchmark.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.RenderScript;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.fragments.BlurBenchmarkFragment;
import at.favre.app.blurbenchmark.fragments.IFragmentWithBlurSettings;
import at.favre.app.blurbenchmark.fragments.LiveBlurFragment;
import at.favre.app.blurbenchmark.fragments.ResultsBrowserFragment;
import at.favre.app.blurbenchmark.fragments.ResultsDiagramFragment;
import at.favre.app.blurbenchmark.fragments.StaticBlurFragment;

/**
 * Created by PatrickF on 10.04.2014.
 */
public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
	public final static String DIALOG_TAG = "blurdialog";
	private RenderScript rs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Benchmark", "Resultstable","Resultschart", "Static", "Live"});
		getSupportActionBar().setListNavigationCallbacks(adapter, this);

		if (savedInstanceState == null) {
			onNavigationItemSelected(0, 0);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (rs != null) {
			rs.destroy();
			rs = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				for (Fragment fragment : getSupportFragmentManager().getFragments()) {
					if (fragment != null && fragment.isAdded() && fragment instanceof IFragmentWithBlurSettings) {
						((IFragmentWithBlurSettings) fragment).switchShowSettings();
					}
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int i, long l) {

		if (getSupportFragmentManager().getFragments() != null) {
			FragmentTransaction t = getSupportFragmentManager().beginTransaction();
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment != null && fragment.getTag() != null && !MainActivity.DIALOG_TAG.equals(fragment.getTag())) {
					t.detach(fragment);
				}
			}
			t.commitAllowingStateLoss();
		}

		switch (i) {
			case 0:
				if (getSupportFragmentManager().findFragmentByTag(BlurBenchmarkFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content, new BlurBenchmarkFragment(), BlurBenchmarkFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(BlurBenchmarkFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				return true;
			case 1:
				if (getSupportFragmentManager().findFragmentByTag(ResultsBrowserFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content, new ResultsBrowserFragment(), ResultsBrowserFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(ResultsBrowserFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				return true;
			case 2:
				if (getSupportFragmentManager().findFragmentByTag(ResultsDiagramFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content, new ResultsDiagramFragment(), ResultsDiagramFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(ResultsDiagramFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				return true;
			case 3:
				if (getSupportFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content, new StaticBlurFragment(), StaticBlurFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				return true;
			case 4:
				if (getSupportFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content, new LiveBlurFragment(), LiveBlurFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				return true;

			default:
				break;
		}
		return false;
	}


	public RenderScript getRs() {
		if (rs == null) {
			rs = RenderScript.create(this);
		}
		return rs;
	}
}
