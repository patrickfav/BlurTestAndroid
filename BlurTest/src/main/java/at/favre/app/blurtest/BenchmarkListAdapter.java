package at.favre.app.blurtest;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import at.favre.app.blurtest.fragments.BenchmarkDetailsDialog;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BenchmarkListAdapter extends ArrayAdapter<BlurBenchmarkTask.BenchmarkWrapper> {
	private DecimalFormat format;

	public BenchmarkListAdapter(Context context, int resource, List<BlurBenchmarkTask.BenchmarkWrapper> objects) {
		super(context, resource, objects);
		format = new DecimalFormat("#.0");
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		format.setRoundingMode(RoundingMode.HALF_UP);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_benchmark_result, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.tvAvg = (TextView) convertView.findViewById(R.id.tv_avg);
			viewHolder.tvBlurRadius = (TextView) convertView.findViewById(R.id.tv_radius);
			viewHolder.tvWidthHeight = (TextView) convertView.findViewById(R.id.tv_width_height);
			viewHolder.tvImageInfo = (TextView) convertView.findViewById(R.id.tv_imageInfo);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
			viewHolder.tvDeviation = (TextView) convertView.findViewById(R.id.tv_deviation);
			viewHolder.frontImageWrapper = (FrameLayout) convertView.findViewById(R.id.thumbnail_front);
			viewHolder.backImageWrapper = (FrameLayout) convertView.findViewById(R.id.thumbnail_back);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (!getItem(position).getStatInfo().isError()) {
			viewHolder.tvAvg.setText(format.format(getItem(position).getStatInfo().getAsAvg().getNormalizedAvg()) + "ms");
			Picasso.with(getContext()).load(getItem(position).getBitmapAsFile()).into(viewHolder.imageView);
			Picasso.with(getContext()).load(getItem(position).getFlippedBitmapAsFile()).into((ImageView) viewHolder.backImageWrapper.findViewById(R.id.thumbnail2));
			viewHolder.tvBlurRadius.setText(getItem(position).getStatInfo().getBlurRadius() + "px");
			viewHolder.tvWidthHeight.setText(getItem(position).getStatInfo().getBitmapHeight() + " x " + getItem(position).getStatInfo().getBitmapHeight() + " / " + getItem(position).getStatInfo().getMegaPixels());
			viewHolder.tvImageInfo.setText(getItem(position).getStatInfo().getBitmapKBSize());
			viewHolder.tvDeviation.setText("+/-" + format.format(getItem(position).getStatInfo().getAsAvg().get90PercentConfidenceIntervall().getDeviationsInPercent()) + "ms");
			((TextView) viewHolder.backImageWrapper.findViewById(R.id.tv_imageInfo2)).setText("bmp loading: "+format.format(getItem(position).getStatInfo().getLoadBitmap())+"ms\n"+
					"blur min/max: "+format.format(getItem(position).getStatInfo().getAsAvg().getMin())+"ms/"+format.format(getItem(position).getStatInfo().getAsAvg().getMax())+"ms\n"+
					"blur median: "+format.format(getItem(position).getStatInfo().getAsAvg().getMedian())+"ms\n"+
					"blur avg/normalized: "+format.format(getItem(position).getStatInfo().getAsAvg().getAvg())+"ms/"+format.format(getItem(position).getStatInfo().getAsAvg().getNormalizedAvg())+"ms\n"+
					"benchmark: "+format.format(getItem(position).getStatInfo().getBenchmarkDuration())+"ms\n");

			if (!getItem(position).isAdditionalInfoVisibility()) {
				viewHolder.frontImageWrapper.setVisibility(View.VISIBLE);
				viewHolder.backImageWrapper.setVisibility(View.GONE);
			} else {
				viewHolder.frontImageWrapper.setVisibility(View.GONE);
				viewHolder.backImageWrapper.setVisibility(View.VISIBLE);
			}

			viewHolder.frontImageWrapper.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_out);
					AnimatorSet set2 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_in);
					set.setTarget(viewHolder.frontImageWrapper);
					set2.setTarget(viewHolder.backImageWrapper);
					set2.addListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animator) {
						}

						@Override
						public void onAnimationEnd(Animator animator) {
							viewHolder.frontImageWrapper.setVisibility(View.GONE);
							viewHolder.frontImageWrapper.setAlpha(1.0f);
							viewHolder.frontImageWrapper.setRotationY(0.0f);
						}

						@Override
						public void onAnimationCancel(Animator animator) {
						}

						@Override
						public void onAnimationRepeat(Animator animator) {
						}
					});
					viewHolder.backImageWrapper.setVisibility(View.VISIBLE);
					set.start();
					set2.start();
					getItem(position).setAdditionalInfoVisibility(true);

				}
			});
			viewHolder.backImageWrapper.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_out);
					AnimatorSet set2 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_in);
					set.setTarget(viewHolder.backImageWrapper);
					set2.setTarget(viewHolder.frontImageWrapper);
					set2.addListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animator) {
						}

						@Override
						public void onAnimationEnd(Animator animator) {
							viewHolder.backImageWrapper.setVisibility(View.GONE);
							viewHolder.backImageWrapper.setAlpha(1.0f);
							viewHolder.backImageWrapper.setRotationY(0.0f);
						}

						@Override
						public void onAnimationCancel(Animator animator) {
						}

						@Override
						public void onAnimationRepeat(Animator animator) {
						}
					});
					viewHolder.frontImageWrapper.setVisibility(View.VISIBLE);
					set.start();
					set2.start();
					getItem(position).setAdditionalInfoVisibility(false);
				}
			});

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					BenchmarkDetailsDialog dialog = BenchmarkDetailsDialog.createInstance(getItem(position));
					dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(),"details");
				}
			});
		} else {
			viewHolder.tvAvg.setText(getItem(position).getStatInfo().getErrorDescription());
			viewHolder.imageView.setImageDrawable(new BitmapDrawable(getContext().getResources()));
		}



		return convertView;
	}

	static class ViewHolder {
		TextView tvAvg;
		TextView tvDeviation;
		TextView tvWidthHeight;
		TextView tvImageInfo;
		TextView tvBlurRadius;
		ImageView imageView;
		FrameLayout frontImageWrapper;
		FrameLayout backImageWrapper;
	}
}
