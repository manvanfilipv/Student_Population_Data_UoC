package Services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class Charts {

    public static void createChart(String title, String x_title, String y_title, LinkedList<Number> temp, LinkedList<String> column) throws FileNotFoundException, IOException {
        CategoryDataset dataset = createDataset(x_title, temp, column);
        JFreeChart chart = ChartFactory.createBarChart(title, x_title, y_title,dataset, PlotOrientation.VERTICAL, true, true, false);
        File a = new File("a.png");
        ChartUtils.saveChartAsPNG(a, chart, 1000, 1000);       
    }

    private static CategoryDataset createDataset(String year, LinkedList<Number> temp, LinkedList<String> column) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < temp.size(); i++) {
            dataset.addValue(temp.get(i), column.get(i), year);
        }
        return dataset;
    }

}
