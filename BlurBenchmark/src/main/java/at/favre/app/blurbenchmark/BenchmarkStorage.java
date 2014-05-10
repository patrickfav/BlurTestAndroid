package at.favre.app.blurbenchmark;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;
import at.favre.app.blurbenchmark.util.JsonUtil;

/**
 * Created by PatrickF on 23.04.2014.
 */
public class BenchmarkStorage {
	private static final String TAG = BenchmarkStorage.class.getSimpleName();
	private static final String PREF_NAME = "at.favre.app.blurbenchmark.sharedpref";
	private static final String PREF_RESULTS = "results";

	private static BenchmarkStorage ourInstance;

	public static BenchmarkStorage getInstance(Context ctx) {
		if (ourInstance == null) {
			ourInstance = new BenchmarkStorage(ctx);
		}
		return ourInstance;
	}

	private BenchmarkStorage(Context ctx) {
		this.ctx = ctx;
	}

	private BenchmarkResultDatabase db;
	private Context ctx;

	private void resetCache() {
		db = null;
	}

	public void saveTest(List<BenchmarkWrapper> wrapperList) {
		// Restore preferences
		SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String resultsString = settings.getString(PREF_RESULTS,null);
		BenchmarkResultDatabase db;

		if(resultsString == null) {
			db = new BenchmarkResultDatabase();
		} else {
			db = JsonUtil.fromJsonString(resultsString,BenchmarkResultDatabase.class);
		}


		for (BenchmarkWrapper benchmarkWrapper : wrapperList) {
			if(!benchmarkWrapper.getStatInfo().isError()) {
				BenchmarkResultDatabase.BenchmarkEntry template = new BenchmarkResultDatabase.BenchmarkEntry(benchmarkWrapper);
				if(db.getEntryList().contains(template)) {
					db.getEntryList().get(db.getEntryList().indexOf(template)).getWrapper().add(benchmarkWrapper);
				} else {
					template.getWrapper().add(benchmarkWrapper);
					db.getEntryList().add(template);
				}
			}
		}

		settings.edit().putString(PREF_RESULTS,JsonUtil.toJsonString(db)).commit();
		resetCache();
	}

	public void deleteData() {
		SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		settings.edit().putString(PREF_RESULTS,JsonUtil.toJsonString(new BenchmarkResultDatabase())).commit();
		resetCache();
	}

	public BenchmarkResultDatabase loadResultsDB() {
		if (db == null) {
			Log.d(TAG, "start load db");
			SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
			String resultsString = settings.getString(PREF_RESULTS, null);
			if (resultsString != null) {
				db = JsonUtil.fromJsonString(resultsString, BenchmarkResultDatabase.class);
				Log.d(TAG, "done load db");
			} else {
				Log.d(TAG, "done load db");
			}
		}
		return db;
	}

	public static class AsyncLoadResults extends AsyncTask<Context,Void,BenchmarkResultDatabase> {
		@Override
		protected BenchmarkResultDatabase doInBackground(Context... ctx) {
			return BenchmarkStorage.getInstance(ctx[0]).loadResultsDB();
		}
	}
}
