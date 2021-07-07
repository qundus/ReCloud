package tech.skargen.recloud.components.gui.interfaces;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.swing.JScrollPane;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XChartPanel;

public abstract interface ICharts {
  /**
   * @return Scroll pane carrying the charts for view.
   */
  public abstract JScrollPane getChartsPane();

  /**
   * Retrieve list containig charts generated.
   * @return List of charts populated.
   */
  public abstract ObjectArrayList<XChartPanel<CategoryChart>> getChartList();
}
