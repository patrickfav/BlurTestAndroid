package at.favre.app.blurbenchmark.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.SettingsController;
import at.favre.app.blurbenchmark.activities.MainActivity;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;
import at.favre.app.blurbenchmark.util.BitmapUtil;
import at.favre.app.blurbenchmark.util.BlurUtil;
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil;

/**
 * Simple canvas with an image that can be blurred with parameters that
 * are set by the user. It also features a simple alpha fade.
 *
 * @author pfavre
 */
public class StaticBlurFragment extends Fragment implements IFragmentWithBlurSettings {
    private static final String TAG = StaticBlurFragment.class.getSimpleName();

    private ImageView imageViewBlur;
    private ImageView imageViewNormal;

    private Bitmap blurTemplate;
    private SettingsController settingsController;

    public StaticBlurFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_staticblur, container, false);

        imageViewNormal = v.findViewById(R.id.normal_image);
        imageViewBlur = v.findViewById(R.id.blur_image);
        settingsController = new SettingsController(v, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                reBlur();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blurTemplate = null;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                reBlur();
            }
        }, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                reBlur();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blurTemplate = null;
                startBlur();
            }
        });

        Bitmap originalBitmap = ((BitmapDrawable) imageViewNormal.getDrawable()).getBitmap();
        ((TextView) v.findViewById(R.id.tv_resolution_normal)).setText("Original: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight() + " / " + BenchmarkUtil.getScalingUnitByteSize(BitmapUtil.sizeOf(originalBitmap)));

        TranslucentLayoutUtil.setTranslucentThemeInsets(getActivity(), v.findViewById(R.id.contentWrapper));
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startBlur();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    private void startBlur() {
        new BlurTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void reBlur() {
        new BlurTask(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void switchShowSettings() {
        settingsController.switchShow();
    }

    public class BlurTask extends AsyncTask<Void, Void, Bitmap> {
        private long startWholeProcess;
        private long readBitmapDuration;
        private long blurDuration;

        private boolean onlyReBlur;

        public BlurTask() {
            this(false);
        }

        public BlurTask(boolean onlyReBlur) {
            this.onlyReBlur = onlyReBlur;
        }

        @Override
        protected void onPreExecute() {
            startWholeProcess = SystemClock.elapsedRealtime();
            if (!onlyReBlur) {
                imageViewNormal.setAlpha(1f);
                imageViewBlur.setAlpha(1f);
            }
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (blurTemplate == null) {
                Log.d(TAG, "Load Bitmap");
                long startReadBitmap = SystemClock.elapsedRealtime();
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = settingsController.getInSampleSize();
                blurTemplate = BitmapFactory.decodeResource(getResources(), R.drawable.photo1, options);
                readBitmapDuration = SystemClock.elapsedRealtime() - startReadBitmap;
            }

            Log.d(TAG, "Start blur algorithm");
            long startBlur = SystemClock.elapsedRealtime();
            Bitmap blurredBitmap = null;

            try {
                blurredBitmap = BlurUtil.blur(((MainActivity) getActivity()).getRs(), getActivity(), blurTemplate.copy(blurTemplate.getConfig(), true), settingsController.getRadius(), settingsController.getAlgorithm());
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            blurDuration = SystemClock.elapsedRealtime() - startBlur;
            Log.d(TAG, "Done blur algorithm");
            return blurredBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Log.d(TAG, "Set image to imageView");
                imageViewBlur.setImageBitmap(bitmap);
                long duration = (SystemClock.elapsedRealtime() - startWholeProcess);
                Log.d(TAG, "Bluring duration " + duration + "ms");

                if (settingsController.isShowCrossfade() && !onlyReBlur) {
                    final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_fadeout);
                    anim.setFillAfter(true);
                    imageViewNormal.startAnimation(anim);
                    final Animation anim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_fadein);
                    anim2.setFillAfter(true);
                    imageViewBlur.startAnimation(anim2);
                } else {
                    imageViewBlur.setAlpha(1.0f);
                    imageViewNormal.setAlpha(0.0f);
                }

                Bitmap blurBitmap = ((BitmapDrawable) imageViewBlur.getDrawable()).getBitmap();
                ((TextView) getView().findViewById(R.id.tv_resolution_blur)).setText(blurBitmap.getWidth() + "x" + blurBitmap.getHeight() + " / " + BenchmarkUtil.getScalingUnitByteSize(BitmapUtil.sizeOf(blurBitmap)) + " / " + settingsController.getAlgorithm() + " / r:" + settingsController.getRadius() + "px / blur: " + blurDuration + "ms / " + duration + "ms");
            }
        }
    }

}
