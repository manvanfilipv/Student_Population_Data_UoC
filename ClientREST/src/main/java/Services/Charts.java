package Services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class Charts {
    
    public static void createChart(StringBuilder title, String x_title, String y_title, LinkedList<Number> temp, LinkedList<String> column, StringBuilder label) throws FileNotFoundException, IOException {
        CategoryDataset dataset = createDataset(x_title, temp, column);
        JFreeChart chart = ChartFactory.createBarChart(title.toString(), x_title, y_title,dataset, PlotOrientation.VERTICAL, true, true, false);
        StringBuilder final_path = new StringBuilder();
        Path path = Paths.get("file.txt");
        final_path.append(path.toAbsolutePath().getParent()).append("\\webapps\\ClientREST\\").append(label).append(".png");
        File a = new File(final_path.toString());
        ChartUtils.saveChartAsPNG(a, chart, 700, 600);
    }

    private static CategoryDataset createDataset(String year, LinkedList<Number> temp, LinkedList<String> column) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < temp.size(); i++) {
            dataset.addValue(temp.get(i), column.get(i), year);
        }
        return dataset;
    }

}
