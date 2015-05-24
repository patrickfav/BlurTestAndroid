package at.favre.app.blurbenchmark.activities;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.RenderScript;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {
	public final static String DIALOG_TAG = "blurdialog";
	private RenderScript rs;

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private ListView navListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		initDrawer();

		if (savedInstanceState == null) {
			selectView(0);
		}
	}

	private void initDrawer() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary));
		navListView = (ListView) drawerLayout.findViewById(R.id.left_drawer);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.lv_nav_item, new String[]{"Benchmark", "Results: Table","Results: Chart", "Blur: Static", "Blur: Live"});
		navListView.setAdapter(adapter);
		navListView.setOnItemClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			navListView.setElevation(30);
		}
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.toolbar), R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);

			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);

			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectView(position);
	}

	private void selectView(int position) {

		if (getSupportFragmentManager().getFragments() != null) {
			FragmentTransaction t = getSupportFragmentManager().beginTransaction();
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment != null && fragment.getTag() != null && !MainActivity.DIALOG_TAG.equals(fragment.getTag())) {
					t.detach(fragment);
				}
			}
			t.commitAllowingStateLoss();
		}

		switch (position) {
			case 0:
				if (getSupportFragmentManager().findFragmentByTag(BlurBenchmarkFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(R.id.root, new BlurBenchmarkFragment(), BlurBenchmarkFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(BlurBenchmarkFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				break;
			case 1:
				if (getSupportFragmentManager().findFragmentByTag(ResultsBrowserFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(R.id.root, new ResultsBrowserFragment(), ResultsBrowserFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(ResultsBrowserFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				break;
			case 2:
				if (getSupportFragmentManager().findFragmentByTag(ResultsDiagramFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(R.id.root, new ResultsDiagramFragment(), ResultsDiagramFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(ResultsDiagramFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				break;
			case 3:
				if (getSupportFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(R.id.root, new StaticBlurFragment(), StaticBlurFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				break;
			case 4:
				if (getSupportFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName()) == null) {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.add(R.id.root, new LiveBlurFragment(), LiveBlurFragment.class.getSimpleName());
					t.commitAllowingStateLoss();
				} else {
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					t.attach(getSupportFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName()));
					t.commitAllowingStateLoss();
				}
				break;

			default:
				break;
		}

		navListView.setItemChecked(position, true);
		drawerLayout.closeDrawer(navListView);
		return;
	}


	public RenderScript getRs() {
		if (rs == null) {
			rs = RenderScript.create(this);
		}
		return rs;
	}
}
