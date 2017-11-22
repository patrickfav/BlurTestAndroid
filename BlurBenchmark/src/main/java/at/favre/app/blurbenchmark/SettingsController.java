package at.favre.app.blurbenchmark;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm;

/**
 * Logic of the top-settings in some fragments
 *
 * @author pfavre
 */
public class SettingsController {
    private View settingsWrapper;
    private SeekBar seekRadius;
    private SeekBar seekInSampleSize;
    private TextView tvRadius;
    private TextView tvInSample;
    private Spinner algorithmSpinner;

    private int radius;
    private int inSampleSize;
    private EBlurAlgorithm algorithm = EBlurAlgorithm.RS_GAUSS_FAST;
    private List<EBlurAlgorithm> algorithmList = Arrays.asList(EBlurAlgorithm.values());
    private boolean showCrossfade = true;

    public SettingsController(View v, final SeekBar.OnSeekBarChangeListener radiusChangeListener, final SeekBar.OnSeekBarChangeListener sampleSizeChangeListener,
                              final AdapterView.OnItemSelectedListener algorithmSelectListener, final View.OnClickListener btnOnClickListener) {
        settingsWrapper = v.findViewById(R.id.settings);

        seekInSampleSize = v.findViewById(R.id.seek_insample);
        seekRadius = v.findViewById(R.id.seek_radius);

        inSampleSize = seekInSampleSize.getProgress() + 1;
        radius = seekRadius.getProgress() + 1;

        tvInSample = v.findViewById(R.id.tv_insample_value);
        tvRadius = v.findViewById(R.id.tv_radius_value);

        tvInSample.setText(getInsampleText(inSampleSize));
        tvRadius.setText(getRadiusText(radius));

        seekInSampleSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                inSampleSize = i + 1;
                tvInSample.setText("1/" + inSampleSize * inSampleSize);
                sampleSizeChangeListener.onProgressChanged(seekBar, i, b);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sampleSizeChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sampleSizeChangeListener.onStopTrackingTouch(seekBar);
            }
        });
        seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radius = i + 1;
                tvRadius.setText(getRadiusText(radius));
                radiusChangeListener.onProgressChanged(seekBar, i, b);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                radiusChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                radiusChangeListener.onStopTrackingTouch(seekBar);
            }
        });

        algorithmSpinner = v.findViewById(R.id.spinner_algorithm);
        algorithmSpinner.setAdapter(new ArrayAdapter<>(v.getContext(), R.layout.inc_spinner_light, algorithmList));
        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                algorithm = algorithmList.get(i);
                if (algorithmSelectListener != null) {
                    algorithmSelectListener.onItemSelected(adapterView, view, i, l);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (algorithmSelectListener != null) {
                    algorithmSelectListener.onNothingSelected(adapterView);
                }
            }
        });

        algorithmSpinner.setSelection(algorithmList.indexOf(EBlurAlgorithm.RS_GAUSS_FAST));

        ((CheckBox) v.findViewById(R.id.cb_crossfade)).setChecked(showCrossfade);
        ((CheckBox) v.findViewById(R.id.cb_crossfade)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showCrossfade = b;
            }
        });

        v.findViewById(R.id.btn_redraw).setOnClickListener(btnOnClickListener);

        initializeSettingsPosition();
    }

    private void initializeSettingsPosition() {
        settingsWrapper.setVisibility(View.INVISIBLE);
    }

    public void switchShow() {
        if (settingsWrapper.getVisibility() == View.VISIBLE) {
            final Animation anim = AnimationUtils.loadAnimation(settingsWrapper.getContext(), R.anim.slide_out_from_top);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    settingsWrapper.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            settingsWrapper.startAnimation(anim);

        } else {
            settingsWrapper.setVisibility(View.VISIBLE);

            final Animation anim = AnimationUtils.loadAnimation(settingsWrapper.getContext(), R.anim.slide_in_from_top);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            settingsWrapper.startAnimation(anim);
        }
    }

    public void setVisibility(boolean inSampleVisible, boolean radiusVisibile, boolean checkBoxVisible, boolean btnVisible) {
        if (!inSampleVisible) {
            seekInSampleSize.setVisibility(View.GONE);
            tvInSample.setVisibility(View.GONE);
            settingsWrapper.findViewById(R.id.tv_insample_label).setVisibility(View.GONE);
        }
        if (!radiusVisibile) {
            seekRadius.setVisibility(View.GONE);
            tvRadius.setVisibility(View.GONE);
            settingsWrapper.findViewById(R.id.tv_radius_label).setVisibility(View.GONE);
        }
        if (!checkBoxVisible) {
            settingsWrapper.findViewById(R.id.cb_crossfade).setVisibility(View.GONE);
        }
        if (!btnVisible) {
            settingsWrapper.findViewById(R.id.btn_redraw).setVisibility(View.GONE);
        }
    }

    public void setBtnText(String text) {
        ((Button) settingsWrapper.findViewById(R.id.btn_redraw)).setText(text);
    }

    public static String getInsampleText(int inSampleSize) {
        return "1/" + inSampleSize * inSampleSize;
    }

    public static String getRadiusText(int radius) {
        return radius + "px";
    }

    public int getRadius() {
        return radius;
    }

    public int getInSampleSize() {
        return inSampleSize;
    }

    public EBlurAlgorithm getAlgorithm() {
        return algorithm;
    }

    public boolean isShowCrossfade() {
        return showCrossfade;
    }
}
