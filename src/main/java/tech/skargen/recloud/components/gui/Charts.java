package tech.skargen.recloud.components.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;
import tech.skargen.recloud.components.gui.interfaces.ICharts;
import tech.skargen.recloud.components.gui.interfaces.IChartsSet;
import tech.skargen.recloud.components.simulation.ISimulation;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.templates.SimulationResult;
import tech.skargen.skartools.SText;

/** Responsible for creating and updating charts with new results. */
public class Charts
    implements IRecloudSequence, IExperimentSequence, ISimulationSequence, ICharts, IChartsSet {
  private static final String MS = "Makespan";
  private static final String DI = "Deegree Of Imbalance";
  private static final String SD = "Standard Deviation";
  private static final String PS = "Processing Speed";

  private int chartWidth;
  private int chartHeight;
  private ChartTheme theme;
  private LegendPosition legendPosition;
  private LegendLayout legendLayout;
  private CategorySeriesRenderStyle renderStyle;
  private String numberFormat;
  @SuppressWarnings("unused") private double xMin;
  @SuppressWarnings("unused") private double xMax;
  private double yMin;
  private double yMax;
  private double yAxisTickDivider;
  private boolean annotationVisible;
  private int annotationAngle;
  @SuppressWarnings("unused") private boolean plotGridVisible;
  private boolean plotGridVerticalLinesVisible;
  private boolean plotGridHorizontalLinesVisible;

  private ObjectArrayList<XChartPanel<CategoryChart>> chartList;
  private JScrollPane chartsPane;
  private JPanel panel;

  private static Logger _LOG;
  static {
    _LOG = LogManager.getLogger();
  }

  /*
   * Initiates all fields with pre-tested standard values.
   */
  public Charts() {
    // Legacy
    this.chartWidth = 0;
    this.chartHeight = 0;
    this.xMin = 0;
    this.xMax = 0;
    this.yMin = 0;
    this.yMax = 0;
    this.yAxisTickDivider = 0;
    this.theme = ChartTheme.XChart;
    this.renderStyle = CategorySeriesRenderStyle.Bar;
    this.legendPosition = LegendPosition.OutsideS;
    this.legendLayout = LegendLayout.Horizontal;
    this.numberFormat = "#,###.##";
    this.annotationVisible = true;
    this.annotationAngle = 0;
    this.plotGridVerticalLinesVisible = false;
    this.plotGridHorizontalLinesVisible = false;

    // GUI
    this.chartList = new ObjectArrayList<>();
    this.panel = new JPanel();
    this.chartsPane = new JScrollPane(this.panel);
    this.chartsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.chartsPane.getHorizontalScrollBar().setUnitIncrement(16);
    this.chartsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    this.chartsPane.getVerticalScrollBar().setUnitIncrement(16);
  }

  /**
   * Retrieve list containig charts generated.
   *
   * @return List of charts populated.
   */
  @Override
  public ObjectArrayList<XChartPanel<CategoryChart>> getChartList() {
    return this.chartList;
  }

  /**
   * @return Scroll pane carrying the charts for view.
   */
  @Override
  public JScrollPane getChartsPane() {
    return this.chartsPane;
  }

  /**
   * Chart screen size, standard = screen_width*0.5 |
   *
   * @param width  Width of a chart's panel in pixels.
   * @param height Height of a chart's panel in pixels.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet size(int width, int height) {
    this.chartWidth = width;
    this.chartHeight = height;
    return this;
  }

  /**
   * Charts look and feel according to XChart creators.
   *
   * @param theme preferred look and feel.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet theme(ChartTheme theme) {
    this.theme = theme;
    return this;
  }

  /**
   * Internal elements of charts series follow certain rendering styles.
   *
   * @param style Category series render style.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet renderStyle(CategorySeriesRenderStyle style) {
    this.renderStyle = style;
    return this;
  }

  /**
   * Data type indicators placement and style.
   *
   * @param position Placement of indicators on the chart.
   * @param layout   Layout style of indicators.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet legend(LegendPosition position, LegendLayout layout) {
    this.legendPosition = position;
    this.legendLayout = layout;
    return this;
  }

  /**
   * Number display style, i.e.: '1000.1', '1,000.1', '1000.100'..etc.
   *
   * @param numberFormat In the form of DecimalFormat like '###,###,###.####'.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet numberFormat(String numberFormat) {
    this.numberFormat = numberFormat;
    return this;
  }

  /**
   * A value greater than 0 to divide chart height by and to divide y axis tick
   * labels as they grow, helps in reducing the dimensions of the image produced.
   *
   * @param tickD Chart's tick margin.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet tickDivider(double tickD) {
    this.yAxisTickDivider = tickD;
    return this;
  }

  /**
   * Set guidance lines visibility.
   *
   * @param horizontal Horizontal guidance lines visibility.
   * @param vertical   Vertical guidance lines visibility.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet gridlines(boolean horizontal, boolean vertical) {
    this.plotGridHorizontalLinesVisible = horizontal;
    this.plotGridVerticalLinesVisible = vertical;
    return this;
  }

  /**
   * Label of XY elements angle and visibility.
   *
   * @param visible Status of visibility.
   * @param angle   Angle of label in degrees.
   * @return This charts for further configuration.
   */
  @Override
  public IChartsSet annotation(boolean visible, int angle) {
    this.annotationVisible = visible;
    this.annotationAngle = angle;
    return this;
  }

  /**
   * Create a chart panel for visual display.
   *
   * @param title Of the chart.
   * @param xAxis XAxis title.
   * @param yAxis YAxis title.
   * @return Chart paenl to be used with GUI entities.
   */
  protected XChartPanel<CategoryChart> internal_generateChart(
      String title, String xAxis, String yAxis) {
    CategoryChartBuilder builder = new CategoryChartBuilder();
    builder.title(title);
    builder.xAxisTitle(xAxis).yAxisTitle(yAxis);
    builder.theme(this.theme);
    builder.height(this.chartHeight).width(this.chartWidth);

    CategoryChart chart = builder.build();

    // Customize Chart
    chart.getStyler().setDefaultSeriesRenderStyle(this.renderStyle);

    chart.getStyler().setLegendPosition(this.legendPosition);
    chart.getStyler().setLegendLayout(this.legendLayout);
    chart.getStyler().setLegendBorderColor(Color.BLACK);
    chart.getStyler().setDecimalPattern(this.numberFormat);
    chart.getStyler().setHasAnnotations(this.annotationVisible);
    chart.getStyler().setAnnotationsRotation(this.annotationAngle);
    chart.getStyler().setPlotGridHorizontalLinesVisible(this.plotGridHorizontalLinesVisible);
    chart.getStyler().setPlotGridVerticalLinesVisible(this.plotGridVerticalLinesVisible);
    // chart.getStyler().setXAxisMin(this.xMin);
    // chart.getStyler().setXAxisMax(this.xMax);

    // custom y axis tick divider
    if (this.yAxisTickDivider > 0) {
      chart.getStyler().setYAxisTickMarkSpacingHint(
          (int) (this.chartHeight / this.yAxisTickDivider));
      chart.getStyler().setYAxisMin(this.yMin);
      chart.getStyler().setYAxisMax(this.yMax);
    }

    // chart.getStyler().setChartTitleBoxVisible(true);
    // chart.getStyler().setChartTitleBoxBorderColor(Color.black);
    // Hard set
    chart.getStyler().setAxisTickPadding(10);
    chart.getStyler().setAxisTickMarkLength(0);
    chart.getStyler().setPlotMargin(0);
    chart.getStyler().setPlotContentSize(1);
    chart.getStyler().setAxisTicksMarksVisible(true);
    chart.getStyler().setShowWithinAreaPoint(true);

    chart.getStyler().setLegendVisible(true);
    chart.getStyler().setAntiAlias(true);

    chart.getStyler().setChartTitleVisible(false);
    // chart.addSeries("yAxis", new double[]{53}, new double[]{53});

    XChartPanel<CategoryChart> panel = new XChartPanel<CategoryChart>(chart);
    panel.setName(title);

    return panel;
  }

  @Override
  public void validate() throws Exception {}

  /**
   * Prepares charts paenels with their types and on screen measures.
   *
   * @param bounds screen bounds.
   */
  @Override
  public void init(IRecloud recloud) {
    // Legacy
    final Rectangle bounds = recloud.getWindow().getScreenBounds();

    if (this.chartWidth <= 0) {
      this.chartWidth = (int) (bounds.width * 0.5);
    }

    if (this.chartHeight <= 0) {
      this.chartHeight = (int) (bounds.height * 0.5);
    }

    // Creating you're own xchart class for the purpose of making the updating
    // of values easier results in random runtime exceptions; change it as soon as
    // possible.
    this.chartList.add(this.internal_generateChart(MS, "Number Of Tasks", "Makespan (S)"));
    this.chartList.add(this.internal_generateChart(DI, "Number Of Tasks", "Degree of Imbalance"));
    this.chartList.add(this.internal_generateChart(SD, "Number Of Tasks", "Standard Deviation"));
    this.chartList.add(this.internal_generateChart(PS, "Number Of Tasks", "Processing Speed (ms)"));

    // GUI
    this.panel.setLayout(new GridLayout(1, this.chartList.size()));

    // Now, you can add whatever you want to the container
    for (XChartPanel<CategoryChart> chart : chartList) {
      this.panel.add(chart);
    }

    // Finalize
    // this.setLayout(new BorderLayout());
    // this.add(this.scrollPanel, BorderLayout.CENTER);
  }

  @Override
  public void newSequence(IRecloud recloud) {}

  /** Round up the cycle of simulations for this task target. */
  @Override
  public void endSequence(IRecloud recloud) {
    /*
     * for (XChartPanel<CategoryChart> chartPanel : this.charts) {
     * chartPanel.validate(); chartPanel.updateUI(); }
     */

    this.panel.validate();
    this.panel.updateUI();
    this.chartsPane.validate();
    this.chartsPane.updateUI();
    // this.validate();
    // this.updateUI();
  }

  @Override
  public void beforeSimulation(IRecloud recloud) {}

  /**
   * Update charts with last simulation's data.
   *
   * @param info Last simulation data.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void afterSimulation(IRecloud recloud) {
    try {
      final ISimulation simulation = recloud.getExperiment().getSimulation();
      final SimulationResult sr = recloud.getExperiment().getSimulationResults();
      int tasksTarget = recloud.getExperiment().getTasksTarget();
      double yValue = 0;

      String name =
          String.join(":", simulation.getAlgorithmName(), String.valueOf(simulation.getID()));
      name = SText.getInstance().format(-1, name.length(), ' ', name).toString();
      for (XChartPanel<CategoryChart> chartPanel : this.chartList) {
        CategoryChart chart = chartPanel.getChart();

        switch (chart.getTitle()) {
          case MS:
            yValue = sr.makespan;
            break;
          case DI:
            yValue = sr.degreeOfImbalance;
            break;
          case SD:
            yValue = sr.standardDeviation;
            break;
          case PS:
            yValue = sr.simulationDuration;
            break;

          default:
            break;
        }

        if (!chart.getSeriesMap().containsKey(name)) {
          double[] xData = new double[] {tasksTarget};
          double[] yData = new double[] {yValue};
          chart.addSeries(name, xData, yData, null);
        } else {
          CategorySeries series = chart.getSeriesMap().get(name);
          List<Number> xData = (List<Number>) series.getXData();
          List<Number> yData = (List<Number>) series.getYData();
          xData.add(tasksTarget);
          yData.add(yValue);

          chart.updateCategorySeries(name, xData, yData, null);
        }

        // upadate y axis min and max
        if (this.yAxisTickDivider > 0) {
          if (chart.getStyler().getYAxisMin() > yValue) {
            chart.getStyler().setYAxisMin(yValue - (yValue / this.yAxisTickDivider));
          } else if (chart.getStyler().getYAxisMax() < yValue) {
            chart.getStyler().setYAxisMax(yValue + (yValue / this.yAxisTickDivider));
          }
        }
      }
    } catch (Exception e) {
      _LOG.error("can't update charts data", e);
      System.exit(0);
    }
  }

  @Override
  public void finish(IRecloud recloud) {}
}