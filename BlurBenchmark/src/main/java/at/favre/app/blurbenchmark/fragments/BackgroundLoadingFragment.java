package at.favre.app.blurbenchmark.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.favre.app.blurbenchmark.BlurBenchmarkTask;

/**
 * Created by PatrickF on 15.04.2014.
 */
public class BackgroundLoadingFragment extends Fragment {
	public static final String TAG = BackgroundLoadingFragment.class.getSimpleName();

	private BlurBenchmarkTask task;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return null;
	}

	public void startNewTask(BlurBenchmarkTask task) {
		destroyTask();
		this.task = task;
		this.task.execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyTask();
	}

	private void destroyTask() {
		if(this.task != null) {
			this.task.cancel(true);
			this.task = null;
		}
	}
}
