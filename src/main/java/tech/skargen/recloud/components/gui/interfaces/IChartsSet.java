package tech.skargen.recloud.components.gui.interfaces;

import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;

public abstract interface IChartsSet {
  /**
   * Chart screen size, standard = screen_width*0.5 |
   * @param width  Width of a chart's panel in pixels.
   * @param height Height of a chart's panel in pixels.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet size(int width, int height);

  /**
   * Charts look and feel according to XChart creators.
   * @param theme preferred look and feel.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet theme(ChartTheme theme);

  /**
   * Internal elements of charts series follow certain rendering styles.
   * @param style Category series render style.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet renderStyle(CategorySeriesRenderStyle style);

  /**
   * Data type indicators placement and style.
   * @param position Placement of indicators on the chart.
   * @param layout   Layout style of indicators.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet legend(LegendPosition position, LegendLayout layout);

  /**
   * Number display style, i.e.: '1000.1', '1,000.1', '1000.100'..etc.
   * @param numberFormat In the form of DecimalFormat like '###,###,###.####'.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet numberFormat(String numberFormat);

  /**
   * A value greater than 0 to divide chart height by and to divide y axis tick
   * labels as they grow, helps in reducing the dimensions of the image produced.
   * @param tickD Chart's tick margin.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet tickDivider(double tickD);

  /**
   * Set guidance lines visibility.
   * @param horizontal Horizontal guidance lines visibility.
   * @param vertical   Vertical guidance lines visibility.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet gridlines(boolean horizontal, boolean vertical);

  /**
   * Label of XY elements angle and visibility.
   * @param visible Status of visibility.
   * @param angle   Angle of label in degrees.
   * @return This charts for further configuration.
   */
  public abstract IChartsSet annotation(boolean visible, int angle);
}
