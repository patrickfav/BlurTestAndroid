package at.favre.app.blurbenchmark.util;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

/**
 * Created by PatrickF on 23.04.2014.
 */
public class GraphUtil {

    public static GraphViewSeries getStraightLine(int heightY, int maxX, String name, GraphViewSeries.GraphViewSeriesStyle seriesStyle) {
        GraphView.GraphViewData[] data = new GraphView.GraphViewData[2];
        data[0] = new GraphView.GraphViewData(0, heightY);
        data[1] = new GraphView.GraphViewData(maxX, heightY);
        return new GraphViewSeries(name, seriesStyle, data);
    }
}
