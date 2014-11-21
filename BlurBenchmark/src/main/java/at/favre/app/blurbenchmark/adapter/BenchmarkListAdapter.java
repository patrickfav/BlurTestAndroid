package at.favre.app.blurbenchmark.adapter;

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

import java.util.List;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.activities.MainActivity;
import at.favre.app.blurbenchmark.blur.IBlur;
import at.favre.app.blurbenchmark.fragments.BenchmarkDetailsDialog;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BenchmarkListAdapter extends ArrayAdapter<BenchmarkWrapper> {

	public BenchmarkListAdapter(Context context, int resource, List<BenchmarkWrapper> objects) {
		super(context, resource, objects);
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
			viewHolder.tvErrMsg = (TextView) convertView.findViewById(R.id.tv_errMsg);
			viewHolder.tvAdditionalInfo = (TextView) convertView.findViewById(R.id.tv_algorithm);
			viewHolder.tvOver16ms = (TextView) convertView.findViewById(R.id.tv_over16ms);
			viewHolder.frontImageWrapper = (FrameLayout) convertView.findViewById(R.id.thumbnail_front);
			viewHolder.backImageWrapper = (FrameLayout) convertView.findViewById(R.id.thumbnail_back);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (!getItem(position).getStatInfo().isError()) {

			viewHolder.tvErrMsg.setVisibility(View.GONE);
			viewHolder.tvAvg.setVisibility(View.VISIBLE);
			viewHolder.tvDeviation.setVisibility(View.VISIBLE);
			viewHolder.tvWidthHeight.setVisibility(View.VISIBLE);
			viewHolder.tvImageInfo.setVisibility(View.VISIBLE);
			viewHolder.tvBlurRadius.setVisibility(View.VISIBLE);

            viewHolder.tvAdditionalInfo.setText(BenchmarkUtil.formatNum(getItem(position).getStatInfo().getThroughputMPixelsPerSec()) + " MPixS / " + getItem(position).getStatInfo().getAlgorithm().toString());
			viewHolder.tvAvg.setText(BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getAvg()) + "ms");
			Picasso.with(getContext()).load(getItem(position).getBitmapAsFile()).placeholder(R.drawable.placeholder).into(viewHolder.imageView);
			Picasso.with(getContext()).load(getItem(position).getFlippedBitmapAsFile()).placeholder(R.drawable.placeholder).into((ImageView) viewHolder.backImageWrapper.findViewById(R.id.thumbnail2));
			viewHolder.tvDeviation.setText("+/-" + BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().get90PercentConfidenceIntervall().getStdError()) + "ms");
			((TextView) viewHolder.backImageWrapper.findViewById(R.id.tv_imageInfo2)).setText("bmp loading: "+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getLoadBitmap())+"ms\n"+
					"blur min/max: "+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getMin())+"ms/"+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getMax())+"ms\n"+
					"blur median: "+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getMedian())+"ms\n"+
					"blur avg/normalized: "+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getAvg())+"ms/"+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getAvg())+"ms\n"+
					"benchmark: "+BenchmarkUtil.formatNum(getItem(position).getStatInfo().getBenchmarkDuration())+"ms\n");

			viewHolder.tvOver16ms.setText(BenchmarkUtil.formatNum(getItem(position).getStatInfo().getAsAvg().getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH)) + "% over "+IBlur.MS_THRESHOLD_FOR_SMOOTH+"ms");
			viewHolder.tvOver16ms.getLayoutParams().height = ((int) ((double) viewHolder.frontImageWrapper.getLayoutParams().height * getItem(position).getStatInfo().getAsAvg().getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH)/100d));
			viewHolder.tvOver16ms.requestLayout();

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
					dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), MainActivity.DIALOG_TAG);
				}
			});
		} else {
			viewHolder.imageView.setImageDrawable(new BitmapDrawable(getContext().getResources()));
			Picasso.with(getContext()).load(R.drawable.placeholder).into(viewHolder.imageView);
			viewHolder.tvErrMsg.setVisibility(View.VISIBLE);
			viewHolder.tvErrMsg.setText(getItem(position).getStatInfo().getErrorDescription());

			viewHolder.tvAvg.setVisibility(View.GONE);
			viewHolder.tvDeviation.setVisibility(View.GONE);
			viewHolder.tvWidthHeight.setVisibility(View.VISIBLE);
			viewHolder.tvImageInfo.setVisibility(View.VISIBLE);
			viewHolder.tvBlurRadius.setVisibility(View.VISIBLE);
			viewHolder.tvAdditionalInfo.setVisibility(View.VISIBLE);
			viewHolder.tvOver16ms.setVisibility(View.GONE);

            viewHolder.tvAdditionalInfo.setText(getItem(position).getStatInfo().getAlgorithm().toString());

            viewHolder.frontImageWrapper.setOnClickListener(null);
            viewHolder.backImageWrapper.setOnClickListener(null);
            convertView.setOnClickListener(null);
		}

        viewHolder.tvBlurRadius.setText(getItem(position).getStatInfo().getBlurRadius() + "px");
        viewHolder.tvImageInfo.setText(getItem(position).getStatInfo().getBitmapByteSize());
        viewHolder.tvWidthHeight.setText(getItem(position).getStatInfo().getBitmapHeight() + " x " + getItem(position).getStatInfo().getBitmapWidth() + " / " + getItem(position).getStatInfo().getMegaPixels());

		return convertView;
	}

	static class ViewHolder {
		TextView tvAvg;
		TextView tvDeviation;
		TextView tvWidthHeight;
		TextView tvImageInfo;
		TextView tvBlurRadius;
		TextView tvErrMsg;
		TextView tvAdditionalInfo;
		TextView tvOver16ms;
		ImageView imageView;
		FrameLayout frontImageWrapper;
		FrameLayout backImageWrapper;
	}
}
