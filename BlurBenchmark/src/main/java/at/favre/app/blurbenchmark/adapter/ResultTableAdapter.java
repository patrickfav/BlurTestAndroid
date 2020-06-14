package at.favre.app.blurbenchmark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

import at.favre.app.blurbenchmark.R;
import at.favre.app.blurbenchmark.activities.MainActivity;
import at.favre.app.blurbenchmark.fragments.BenchmarkDetailsDialog;
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase;
import at.favre.app.blurbenchmark.models.ResultTableModel;

/**
 * Created by PatrickF on 18.04.2014.
 */
public class ResultTableAdapter extends BaseTableAdapter {

    private ResultTableModel model;
    private ResultTableModel.DataType dataType;
    private Context ctx;

    public ResultTableAdapter(Context ctx, BenchmarkResultDatabase db, ResultTableModel.DataType dataType) {
        this.dataType = dataType;
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
        int viewType = getItemViewType(row, column);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layoutId;

            switch (viewType) {
                case 0:
                    layoutId = R.layout.inc_result_column_header;
                    break;
                case 1:
                    layoutId = R.layout.inc_result_row_header;
                    break;
                case 2:
                    layoutId = R.layout.inc_result_cell;
                    break;
                default:
                    throw new IllegalArgumentException("Could not get layout for table cell");
            }
            convertView = inflater.inflate(layoutId, parent, false);
        }
        convertView.setOnClickListener(null);
        if (viewType == 2) {
            switch (model.getRelativeType(row, column, dataType, dataType.isMinIsBest())) {
                case BEST:
                    ((TextView) convertView.findViewById(R.id.text)).setTextColor(ctx.getResources().getColor(R.color.graphBgGreen));
                    break;
                case WORST:
                    ((TextView) convertView.findViewById(R.id.text)).setTextColor(ctx.getResources().getColor(R.color.graphBgRed));
                    break;
                default:
                    ((TextView) convertView.findViewById(R.id.text)).setTextColor(ctx.getResources().getColor(R.color.tableCellTextColor));
                    break;
            }
            BenchmarkResultDatabase.BenchmarkEntry value = model.getCell(row, column);
            convertView.setTag(value);
            if (BenchmarkResultDatabase.getRecentWrapper(value) != null) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BenchmarkDetailsDialog dialog = BenchmarkDetailsDialog.createInstance(BenchmarkResultDatabase.getRecentWrapper((BenchmarkResultDatabase.BenchmarkEntry) view.getTag()));
                        dialog.show(((AppCompatActivity) ctx).getFragmentManager(), MainActivity.DIALOG_TAG);

                    }
                });
            }
        }

        ((TextView) convertView.findViewById(R.id.text)).setText(getText(row, column));
        return convertView;
    }

    public String getText(int row, int column) {
        if (row < 0 && column < 0) {
            return "";
        } else if (row < 0) {
            return model.getColumns().get(column);
        } else if (column < 0) {
            return model.getRows().get(row);
        } else {
            return model.getValue(row, column, dataType);
        }
    }

    @Override
    public int getWidth(int column) {
        if (column < 0) {
            return (int) ctx.getResources().getDimension(R.dimen.table_row_header_width);
        } else {
            return (int) ctx.getResources().getDimension(R.dimen.table_cell_width);
        }
    }

    @Override
    public int getHeight(int row) {
        if (row < 0) {
            return (int) ctx.getResources().getDimension(R.dimen.table_column_header_height);
        } else {
            return (int) ctx.getResources().getDimension(R.dimen.table_cell_height);
        }
    }

    @Override
    public int getItemViewType(int row, int column) {
        if (row < 0) {
            return 0;
        }
        if (column < 0) {
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
