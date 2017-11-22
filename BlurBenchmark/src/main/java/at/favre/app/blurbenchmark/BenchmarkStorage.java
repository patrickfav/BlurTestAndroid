package at.favre.app.blurbenchmark;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.List;

import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;
import at.favre.app.blurbenchmark.util.BitmapUtil;
import at.favre.app.blurbenchmark.util.JsonUtil;

/**
 * This is responsible for storeing and retrieving the benchmark data.
 * As of now, this is a hack, since it stores the data in json format in
 * the shared preference and loads it as a whole in memory. This
 * appoach was used because it is easier to implement than a sophisticated
 * DB solution.
 *
 * @author pfavre
 */
public class BenchmarkStorage {
    private static final String TAG = BenchmarkStorage.class.getSimpleName();
    private static final String PREF_NAME = "at.favre.app.blurbenchmark.sharedpref";
    private static final String PREF_RESULTS = "results";
    private static final int MAX_SAVED_BENCHMARKS = 3;

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

    public void saveTest(final List<BenchmarkWrapper> wrapperList) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Restore preferences
                SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String resultsString = settings.getString(PREF_RESULTS, null);
                BenchmarkResultDatabase db;

                if (resultsString == null) {
                    db = new BenchmarkResultDatabase();
                } else {
                    db = JsonUtil.fromJsonString(resultsString, BenchmarkResultDatabase.class);
                }

                for (BenchmarkWrapper benchmarkWrapper : wrapperList) {
                    if (!benchmarkWrapper.getStatInfo().isError()) {
                        BenchmarkResultDatabase.BenchmarkEntry benchmarkEntry = new BenchmarkResultDatabase.BenchmarkEntry(benchmarkWrapper);
                        if (db.getEntryList().contains(benchmarkEntry)) {
                            db.getEntryList().get(db.getEntryList().indexOf(benchmarkEntry)).getWrapper().add(benchmarkWrapper);
                        } else {
                            while (benchmarkEntry.getWrapper().size() > MAX_SAVED_BENCHMARKS) {
                                benchmarkEntry.getWrapper().remove(0);
                            }

                            benchmarkEntry.getWrapper().add(benchmarkWrapper);
                            db.getEntryList().add(benchmarkEntry);
                        }
                    }
                }

                settings.edit().putString(PREF_RESULTS, JsonUtil.toJsonString(db)).commit();
                resetCache();
                return null;
            }
        }.execute();

    }

    public void deleteData() {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        settings.edit().putString(PREF_RESULTS, JsonUtil.toJsonString(new BenchmarkResultDatabase())).commit();
        BitmapUtil.clearCacheDir(new File(BitmapUtil.getCacheDir(ctx)));
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

    public static class AsyncLoadResults extends AsyncTask<Context, Void, BenchmarkResultDatabase> {
        @Override
        protected BenchmarkResultDatabase doInBackground(Context... ctx) {
            return BenchmarkStorage.getInstance(ctx[0]).loadResultsDB();
        }
    }
}
