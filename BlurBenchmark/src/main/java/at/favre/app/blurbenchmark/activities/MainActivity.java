package at.favre.app.blurbenchmark.activities;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.RenderScript;
import android.view.MenuItem;
import android.view.View;

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
public class MainActivity extends AppCompatActivity {
	public final static String DIALOG_TAG = "blurdialog";
	private RenderScript rs;

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private String currentFragmentTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		navigationView = ((NavigationView) findViewById(R.id.navigationView));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
		initDrawer();


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), getResources().getColor(R.color.color_primary_dark)));
		}

		if (savedInstanceState == null) {
			selectView(R.id.navigation_item_1);
		}
	}

	private void initDrawer() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary));
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
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				return selectView(menuItem.getItemId());
			}
		});
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
				Fragment fragment;
				if((fragment= getFragmentManager().findFragmentByTag(LiveBlurFragment.class.getSimpleName())) != null) {
					((IFragmentWithBlurSettings) fragment).switchShowSettings();
				}
				if((fragment= getFragmentManager().findFragmentByTag(StaticBlurFragment.class.getSimpleName())) != null) {
					((IFragmentWithBlurSettings) fragment).switchShowSettings();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}



	private boolean selectView(@IdRes int menuId) {

		if(currentFragmentTag != null) {
			Fragment f = getFragmentManager().findFragmentByTag(currentFragmentTag);
			getFragmentManager().beginTransaction().detach(f).commitAllowingStateLoss();
		}

		switch (menuId) {
			case R.id.navigation_item_1:
				setFragment(BlurBenchmarkFragment.class.getSimpleName(), new FragmentFactory() {
					@Override
					public Fragment create() {
						return new BlurBenchmarkFragment();
					}
				});
				break;
			case R.id.navigation_item_2:
				setFragment(ResultsBrowserFragment.class.getSimpleName(), new FragmentFactory() {
					@Override
					public Fragment create() {
						return new ResultsBrowserFragment();
					}
				});
				break;
			case R.id.navigation_item_3:
				setFragment(ResultsDiagramFragment.class.getSimpleName(), new FragmentFactory() {
					@Override
					public Fragment create() {
						return new ResultsDiagramFragment();
					}
				});
				break;
			case R.id.navigation_item_4:
				setFragment(StaticBlurFragment.class.getSimpleName(), new FragmentFactory() {
					@Override
					public Fragment create() {
						return new StaticBlurFragment();
					}
				});
				break;
			case R.id.navigation_item_5:
				setFragment(LiveBlurFragment.class.getSimpleName(), new FragmentFactory() {
					@Override
					public Fragment create() {
						return new LiveBlurFragment();
					}
				});
				break;
			default:
				break;
		}
		navigationView.getMenu().findItem(menuId).setChecked(true);
		drawerLayout.closeDrawer(navigationView);
		return true;
	}

	private void setFragment(String tag, FragmentFactory factory) {
		if (getFragmentManager().findFragmentByTag(tag) == null) {
			FragmentTransaction t = getFragmentManager().beginTransaction();
			t.add(R.id.root, factory.create(), tag);
			t.commitAllowingStateLoss();
		} else {
			FragmentTransaction t = getFragmentManager().beginTransaction();
			t.attach(getFragmentManager().findFragmentByTag(tag));
			t.commitAllowingStateLoss();
		}
		currentFragmentTag = tag;
	}


	public RenderScript getRs() {
		if (rs == null) {
			rs = RenderScript.create(this);
		}
		return rs;
	}

	public interface FragmentFactory {
		Fragment create();
	}
}
