package at.favre.app.blurbenchmark.adapter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.activities.MainActivity;
import at.favre.app.blurbenchmark.blur.IBlur;
import at.favre.app.blurbenchmark.fragments.BenchmarkDetailsDialog;
import at.favre.app.blurbenchmark.models.BenchmarkWrapper;
import at.favre.app.blurbenchmark.util.BenchmarkUtil;

public class BenchmarkResultHolder extends RecyclerView.ViewHolder {
    private View root;
    private TextView tvAvg;
    private TextView tvDeviation;
    private TextView tvWidthHeight;
    private TextView tvImageInfo;
    private TextView tvBlurRadius;
    private TextView tvErrMsg;
    private TextView tvAdditionalInfo;
    private TextView tvOver16ms;
    private ImageView imageView;
    private FrameLayout frontImageWrapper;
    private FrameLayout backImageWrapper;

    private Context ctx;
    private final FragmentManager fragmentManager;

    public BenchmarkResultHolder(View itemView, FragmentManager fragmentManager) {
        super(itemView);
        this.ctx = itemView.getContext();
        this.fragmentManager = fragmentManager;

        root = itemView;
        tvAvg = itemView.findViewById(R.id.tv_avg);
        tvBlurRadius = itemView.findViewById(R.id.tv_radius);
        tvWidthHeight = itemView.findViewById(R.id.tv_width_height);
        tvImageInfo = itemView.findViewById(R.id.tv_imageInfo);
        imageView = itemView.findViewById(R.id.thumbnail);
        tvDeviation = itemView.findViewById(R.id.tv_deviation);
        tvErrMsg = itemView.findViewById(R.id.tv_errMsg);
        tvAdditionalInfo = itemView.findViewById(R.id.tv_algorithm);
        tvOver16ms = itemView.findViewById(R.id.tv_over16ms);
        frontImageWrapper = itemView.findViewById(R.id.thumbnail_front);
        backImageWrapper = itemView.findViewById(R.id.thumbnail_back);
    }

    public void onBind(final BenchmarkWrapper wrapper) {
        if (!wrapper.getStatInfo().isError()) {

            tvErrMsg.setVisibility(View.GONE);
            tvAvg.setVisibility(View.VISIBLE);
            tvDeviation.setVisibility(View.VISIBLE);
            tvWidthHeight.setVisibility(View.VISIBLE);
            tvImageInfo.setVisibility(View.VISIBLE);
            tvBlurRadius.setVisibility(View.VISIBLE);

            tvAdditionalInfo.setText(BenchmarkUtil.formatNum(wrapper.getStatInfo().getThroughputMPixelsPerSec()) + " MPixS / " + wrapper.getStatInfo().getAlgorithm().toString());
            tvAvg.setText(BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg()) + "ms");
            Picasso.with(ctx).load(wrapper.getBitmapAsFile()).placeholder(R.drawable.placeholder).into(imageView);
            Picasso.with(ctx).load(wrapper.getFlippedBitmapAsFile()).placeholder(R.drawable.placeholder).into((ImageView) backImageWrapper.findViewById(R.id.thumbnail2));
            tvDeviation.setText("+/-" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().get90PercentConfidenceIntervall().getStdError()) + "ms");
            ((TextView) backImageWrapper.findViewById(R.id.tv_imageInfo2)).setText("bmp loading: " + BenchmarkUtil.formatNum(wrapper.getStatInfo().getLoadBitmap()) + "ms\n" +
                    "blur min/max: " + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMin()) + "ms/" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMax()) + "ms\n" +
                    "blur median: " + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getMedian()) + "ms\n" +
                    "blur avg/normalized: " + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg()) + "ms/" + BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getAvg()) + "ms\n" +
                    "benchmark: " + BenchmarkUtil.formatNum(wrapper.getStatInfo().getBenchmarkDuration()) + "ms\n");

            tvOver16ms.setText(BenchmarkUtil.formatNum(wrapper.getStatInfo().getAsAvg().getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH)) + "% over " + IBlur.MS_THRESHOLD_FOR_SMOOTH + "ms");
            tvOver16ms.getLayoutParams().height = ((int) ((double) frontImageWrapper.getLayoutParams().height * wrapper.getStatInfo().getAsAvg().getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH) / 100d));
            tvOver16ms.requestLayout();

            if (!wrapper.isAdditionalInfoVisibility()) {
                frontImageWrapper.setVisibility(View.VISIBLE);
                backImageWrapper.setVisibility(View.GONE);
            } else {
                frontImageWrapper.setVisibility(View.GONE);
                backImageWrapper.setVisibility(View.VISIBLE);
            }

            frontImageWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_left_out);
                    AnimatorSet set2 = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_left_in);
                    set.setTarget(frontImageWrapper);
                    set2.setTarget(backImageWrapper);
                    set2.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            frontImageWrapper.setVisibility(View.GONE);
                            frontImageWrapper.setAlpha(1.0f);
                            frontImageWrapper.setRotationY(0.0f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                        }
                    });
                    backImageWrapper.setVisibility(View.VISIBLE);
                    set.start();
                    set2.start();
                    wrapper.setAdditionalInfoVisibility(true);

                }
            });
            backImageWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_right_out);
                    AnimatorSet set2 = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_right_in);
                    set.setTarget(backImageWrapper);
                    set2.setTarget(frontImageWrapper);
                    set2.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            backImageWrapper.setVisibility(View.GONE);
                            backImageWrapper.setAlpha(1.0f);
                            backImageWrapper.setRotationY(0.0f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                        }
                    });
                    frontImageWrapper.setVisibility(View.VISIBLE);
                    set.start();
                    set2.start();
                    wrapper.setAdditionalInfoVisibility(false);
                }
            });

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BenchmarkDetailsDialog dialog = BenchmarkDetailsDialog.createInstance(wrapper);
                    dialog.show(fragmentManager, MainActivity.DIALOG_TAG);
                }
            });
        } else {
            imageView.setImageDrawable(new BitmapDrawable(ctx.getResources()));
            Picasso.with(ctx).load(R.drawable.placeholder).into(imageView);
            tvErrMsg.setVisibility(View.VISIBLE);
            tvErrMsg.setText(wrapper.getStatInfo().getErrorDescription());

            tvAvg.setVisibility(View.GONE);
            tvDeviation.setVisibility(View.GONE);
            tvWidthHeight.setVisibility(View.VISIBLE);
            tvImageInfo.setVisibility(View.VISIBLE);
            tvBlurRadius.setVisibility(View.VISIBLE);
            tvAdditionalInfo.setVisibility(View.VISIBLE);
            tvOver16ms.setVisibility(View.GONE);

            tvAdditionalInfo.setText(wrapper.getStatInfo().getAlgorithm().toString());

            frontImageWrapper.setOnClickListener(null);
            backImageWrapper.setOnClickListener(null);
            root.setOnClickListener(null);
        }

        tvBlurRadius.setText(wrapper.getStatInfo().getBlurRadius() + "px");
        tvImageInfo.setText(wrapper.getStatInfo().getBitmapByteSize());
        tvWidthHeight.setText(wrapper.getStatInfo().getBitmapHeight() + " x " + wrapper.getStatInfo().getBitmapWidth() + " / " + wrapper.getStatInfo().getMegaPixels());

    }
}
