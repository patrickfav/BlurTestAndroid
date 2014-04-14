package at.favre.app.blurtest.activities;

import android.os.Build;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.fragments.BlurBenchmarkFragment;
import at.favre.app.blurtest.fragments.IFragmentWithBlurSettings;
import at.favre.app.blurtest.fragments.LiveBlurFragment;
import at.favre.app.blurtest.fragments.StaticBlurFragment;

/**
 * Created by PatrickF on 10.04.2014.
 */
public class MainActivity extends ActionBarActivity implements  ActionBar.OnNavigationListener {

	private RenderScript rs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.inc_spinner_item2,new String[]{"Static","Live","Benchmark"});
		adapter.setDropDownViewResource(R.layout.inc_spinner_textview2);
		getSupportActionBar().setListNavigationCallbacks(adapter,this);

		setContentView(R.layout.activity_main);

		if(savedInstanceState == null) {
			FragmentTransaction t = getSupportFragmentManager().beginTransaction();
			t.add(android.R.id.content,new StaticBlurFragment(),StaticBlurFragment.class.getSimpleName());
			t.commit();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(rs != null) {
			rs.destroy();
			rs = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				for (Fragment fragment : getSupportFragmentManager().getFragments()) {
					if(fragment.isAdded() && fragment instanceof IFragmentWithBlurSettings) {
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
		FragmentTransaction t1 = getSupportFragmentManager().beginTransaction();
		for (Fragment fragment : getSupportFragmentManager().getFragments()) {
			t1.detach(fragment);
		}
		t1.commit();

		switch (i) {
			case 0:
				if(getSupportFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content,new StaticBlurFragment(),StaticBlurFragment.class.getSimpleName());
					t.commit();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName()));
					t.commit();
				}
				return true;
			case 1:
				if(getSupportFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content,new LiveBlurFragment(),LiveBlurFragment.class.getSimpleName());
					t.commit();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName()));
					t.commit();
				}
				return true;
			case 2:
				if(getSupportFragmentManager().findFragmentByTag(BlurBenchmarkFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(android.R.id.content,new BlurBenchmarkFragment(),BlurBenchmarkFragment.class.getSimpleName());
					t.commit();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(BlurBenchmarkFragment.class.getSimpleName()));
					t.commit();
				}
				return true;
			default:
				break;
		}
		return false;
	}

	public RenderScript getRs() {
		if (rs == null && Build.VERSION.SDK_INT > 16) {
			rs = RenderScript.create(this);
		}
		return rs;
	}
}
