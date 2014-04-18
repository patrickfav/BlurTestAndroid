package at.favre.app.blurtest.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.models.BenchmarkResultDatabase;
import at.favre.app.blurtest.models.ResultTableModel;

/**
 * Created by PatrickF on 18.04.2014.
 */
public class ResultTableAdapter extends BaseTableAdapter {

    private ResultTableModel model;
    private Context ctx;

    public ResultTableAdapter(Context ctx, BenchmarkResultDatabase db) {
        model = new ResultTableModel(db);
		this.ctx = ctx;
    }

    @Override
    public int getRowCount() {
        return model.getRows().size();
    }

    @Override
    public int getColumnCount() {
        return model.getColumns().size();
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {
    	int viewType = getItemViewType(row,column);

		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int layoutId;

			switch (viewType) {
				case 0:
					layoutId = R.layout.inc_result_header;
					break;
				case 1:
					layoutId = R.layout.inc_result_cell;
					break;
				case 2:
					layoutId = R.layout.inc_result_cell;
					break;
				default:
					throw new IllegalArgumentException("Could not get layout for table cell");
			}
			convertView = inflater.inflate(layoutId,parent,false);
		}

		((TextView) convertView.findViewById(R.id.text)).setText(getText(row,column));
		return convertView;
    }

	public String getText(int row, int column) {
		if(row < 0 && column <0) {
			return "";
		} else if(row < 0 ) {
			return model.getColumns().get(column);
		} else if(column < 0) {
			return model.getRows().get(row);
		} else {
			return model.getValue(row,column, ResultTableModel.DataType.AVG);
		}
	}

    @Override
    public int getWidth(int column) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, ctx.getResources().getDisplayMetrics());
    }

    @Override
    public int getHeight(int row) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, ctx.getResources().getDisplayMetrics());
    }

    @Override
    public int getItemViewType(int row, int column) {
		if (row < 0) {
			return 0;
		} if (column < 0) {
			return 1;
		} else {
			return 2;
		}
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
