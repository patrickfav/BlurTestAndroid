package at.favre.app.blurtest.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

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
        return null;
    }

    @Override
    public int getWidth(int column) {
        return 0;
    }

    @Override
    public int getHeight(int row) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics());
    }

    @Override
    public int getItemViewType(int row, int column) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
