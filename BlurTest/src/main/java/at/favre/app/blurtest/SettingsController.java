package at.favre.app.blurtest;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.favre.app.blurtest.util.BlurUtil;

/**
 * Created by PatrickF on 10.04.2014.
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
	private BlurUtil.Algorithm algorithm = BlurUtil.Algorithm.RENDERSCRIPT;
	private List<BlurUtil.Algorithm> algorithmList = new ArrayList<BlurUtil.Algorithm>(Arrays.asList(BlurUtil.Algorithm.values()));
	private boolean showCrossfade = true;

	private SeekBar.OnSeekBarChangeListener radiusChangeListener;
	private SeekBar.OnSeekBarChangeListener sampleSizeChangeListener;
	private AdapterView.OnItemSelectedListener algorithmSelectListener;

	public SettingsController(View v,final SeekBar.OnSeekBarChangeListener radiusChangeListener,final SeekBar.OnSeekBarChangeListener sampleSizeChangeListener,
							  final AdapterView.OnItemSelectedListener algorithmSelectListener, final View.OnClickListener fullRedrawOnClickListener, boolean hideButtonAndCheckbox) {
		settingsWrapper = v.findViewById(R.id.settings);
		settingsWrapper.setVisibility(View.GONE);

		seekInSampleSize = (SeekBar)  v.findViewById(R.id.seek_insample);
		seekRadius = (SeekBar)  v.findViewById(R.id.seek_radius);

		inSampleSize = seekInSampleSize.getProgress()+1;
		radius= seekRadius.getProgress()+1;

		tvInSample = (TextView)  v.findViewById(R.id.tv_insample_value);
		tvRadius = (TextView)  v.findViewById(R.id.tv_radius_value);

		tvInSample.setText(getInsampleText(inSampleSize));
		tvRadius.setText(getRadiusText(radius));

		seekInSampleSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				inSampleSize = i+1;
				tvInSample.setText("1/"+inSampleSize*inSampleSize);
				sampleSizeChangeListener.onProgressChanged(seekBar,i,b);
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
				radius=i+1;
				tvRadius.setText(getRadiusText(radius));
				radiusChangeListener.onProgressChanged(seekBar,i,b);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {radiusChangeListener.onStartTrackingTouch(seekBar);}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {radiusChangeListener.onStopTrackingTouch(seekBar);}
		});

		ArrayAdapter<BlurUtil.Algorithm> alogrithmArrayAdapter = new ArrayAdapter<BlurUtil.Algorithm>(v.getContext(),R.layout.inc_spinner_textview, algorithmList);
		alogrithmArrayAdapter.setDropDownViewResource(R.layout.inc_spinner_item);
		algorithmSpinner = (Spinner)  v.findViewById(R.id.spinner_algorithm);
		algorithmSpinner.setAdapter(alogrithmArrayAdapter);
		algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				algorithm = algorithmList.get(i);
				algorithmSelectListener.onItemSelected(adapterView,view,i,l);
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				algorithmSelectListener.onNothingSelected(adapterView);
			}
		});

		if(!hideButtonAndCheckbox) {
			((CheckBox) v.findViewById(R.id.cb_crossfade)).setChecked(showCrossfade);
			((CheckBox) v.findViewById(R.id.cb_crossfade)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					showCrossfade = b;
				}
			});

			v.findViewById(R.id.btn_redraw).setOnClickListener(fullRedrawOnClickListener);
		} else {
			v.findViewById(R.id.cb_crossfade).setVisibility(View.GONE);
			v.findViewById(R.id.btn_redraw).setVisibility(View.GONE);
		}
	}

	public void switchShow(){
		if(settingsWrapper.getVisibility() == View.VISIBLE) {
			settingsWrapper.setVisibility(View.GONE);
		} else {
			settingsWrapper.setVisibility(View.VISIBLE);
		}
//		if(findViewById(R.id.settings).getVisibility() == View.VISIBLE) {
//			final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.slide_out_top);
//			anim.setFillAfter(true);
//			findViewById(R.id.settings).startAnimation(anim);
//			findViewById(R.id.settings).setVisibility(View.GONE);
//		} else {
//			findViewById(R.id.settings).setVisibility(View.VISIBLE);
//			final Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.animator.slide_in_top);
//			anim.setFillAfter(true);
//			findViewById(R.id.settings).startAnimation(anim);
//		}
	}

	public static String getInsampleText(int inSampleSize) {
		return "1/"+inSampleSize*inSampleSize;
	}
	public static String getRadiusText(int radius) {
		return radius+"px";
	}

	public int getRadius() {
		return radius;
	}

	public int getInSampleSize() {
		return inSampleSize;
	}

	public BlurUtil.Algorithm getAlgorithm() {
		return algorithm;
	}

	public boolean isShowCrossfade() {
		return showCrossfade;
	}
}
