package at.favre.app.blurtest;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Created by PatrickF on 14.04.2014.
 */
public class BenchmarkListAdapter extends ArrayAdapter<BlurBenchmarkTask.BenchmarkWrapper> {
	private ViewHolder viewHolder;
	private DecimalFormat format;
	public BenchmarkListAdapter(Context context, int resource, List<BlurBenchmarkTask.BenchmarkWrapper> objects) {
		super(context, resource, objects);
		format = new DecimalFormat("#.0");
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		format.setRoundingMode(RoundingMode.HALF_UP);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_benchmark_result, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.tvAvg = (TextView) convertView.findViewById(R.id.tv_avg);
			viewHolder.tvBlurRadius = (TextView) convertView.findViewById(R.id.tv_radius);
			viewHolder.tvWidthHeight = (TextView) convertView.findViewById(R.id.tv_width_height);
			viewHolder.tvImageInfo = (TextView) convertView.findViewById(R.id.tv_imageInfo);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
			viewHolder.tvDeviation = (TextView) convertView.findViewById(R.id.tv_deviation);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if(!getItem(position).getStatInfo().isError()) {
			viewHolder.tvAvg.setText(format.format(getItem(position).getStatInfo().getAvgBlur().getNormalizedAvg())+"ms");
			viewHolder.imageView.setImageDrawable(new BitmapDrawable(getContext().getResources(), getItem(position).getResultBitmap()));
			viewHolder.tvBlurRadius.setText(getItem(position).getStatInfo().getBlurRadius()+"px");
			viewHolder.tvWidthHeight.setText(getItem(position).getStatInfo().getBitmapHeight()+" x "+getItem(position).getStatInfo().getBitmapHeight() +" / "+getItem(position).getStatInfo().getMegaPixels());
			viewHolder.tvImageInfo.setText(getItem(position).getStatInfo().getBitmapKBSize());
			viewHolder.tvDeviation.setText("+/-"+format.format(getItem(position).getStatInfo().getAvgBlur().get90PercentConfidenceIntervall().getDeviationsInPercent())+"ms");
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
	}
}
