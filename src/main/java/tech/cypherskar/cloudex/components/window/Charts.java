package tech.cypherskar.cloudex.components.window;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tech.cypherskar.cloudex.templates.infos.ASimulationInfo;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**Responsible for creating and updating charts with new results.*/
public class Charts
{
    //  *****************************
    //  Constants
    //  *****************************
    private static final String MS = "Makespan";
    private static final String DI = "Deegree Of Imbalance";
    private static final String SD = "Standard Deviation";
    private static final String PS = "Processing Speed";

    //  *****************************
    //  Fields
    //  *****************************
	public int chartWidth, chartHeight;
	public ChartTheme theme;
	public LegendPosition legendPosition;
	public LegendLayout legendLayout;
	public CategorySeriesRenderStyle renderStyle;
	public String numberFormat;
	public double xMin, xMax;
	public double yMin, yMax;
    public double yAxisTickDivider;
	public boolean annotationVisible;
    public int annotationAngle;
    public BitmapFormat chartFormat;
    public boolean plotGridVisible, plotGridVerticalLinesVisible, plotGridHorizontalLinesVisible;
    
    //  *****************************
    //  Fields (GUI)
    //  *****************************
    public ObjectArrayList<XChartPanel<CategoryChart>> charts;
    private JScrollPane scrollPanel;
    private JPanel chartsPanel;

    //  *****************************
    //  Constructors
    //  *****************************
    /*
     * Initiates all fields with pre-tested standard values.
     */
    public Charts()
    {
        // Legacy
		this.chartWidth = 0; this.chartHeight = 0;
		this.xMin = 0; this.xMax = 0;
        this.yMin = 0; this.yMax = 0;
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
        this.charts = new ObjectArrayList<>();
        this.chartsPanel = new JPanel();
        this.scrollPanel = new JScrollPane(this.chartsPanel);
        this.scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrollPanel.getHorizontalScrollBar().setUnitIncrement(16);
        this.scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollPanel.getVerticalScrollBar().setUnitIncrement(16);
    }
    
    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * Prepares charts paenels with their types and on screen measures.
     * @param bounds screen bounds.
     */
    public void Initialize(Rectangle bounds)
    {
        // Legacy
		if (this.chartWidth <= 0)
		this.chartWidth = (int)(bounds.width * 0.5);
		if (this.chartHeight <= 0)
		this.chartHeight = (int)(bounds.height * 0.5);

        // Creating you're own xchart class for the purpose of making the updating
        // of values easier results in random runtime exceptions; change it as soon as possible.
        this.charts.add(this.GetChart(MS, "Number Of Tasks", "Makespan (S)"));
        this.charts.add(this.GetChart(DI, "Number Of Tasks", "Degree of Imbalance"));
        this.charts.add(this.GetChart(SD, "Number Of Tasks", "Standard Deviation"));
        this.charts.add(this.GetChart(PS, "Number Of Tasks", "Processing Speed (ms)"));
        
        // GUI
        this.chartsPanel.setLayout(new GridLayout(1, this.charts.size()));

        // Now, you can add whatever you want to the container
        for (XChartPanel<CategoryChart> chart : charts)
        {
            this.chartsPanel.add(chart);
            //tabsPanel.addTab(chart.getName(), chart);
        }
        
        // Finalize
        // this.setLayout(new BorderLayout());
        //this.add(this.scrollPanel, BorderLayout.CENTER);
    }

    /**
     * Update charts with last simulation's data.
     * @param info Last simulation data.
     */
    @SuppressWarnings("unchecked")
    public void EndSimulation(ASimulationInfo info)
    {
        try
        {
            String name = this.GetAlgorithmName(info);
            int tasksTarget = info.recievedCloudlets.size();
            double yValue = 0;
            
            for (XChartPanel<CategoryChart> chartPanel : this.charts)
            {
                CategoryChart chart = chartPanel.getChart();
                
                switch(chart.getTitle())
                {
                    case MS:
                    yValue = info.Makespan;
                    break;
                    case DI:
                    yValue = info.Degree_Of_Imbalance;
                    break;
                    case SD:
                    yValue = info.Standard_Deviation;
                    break;
                    case PS:
                    yValue = info.Algorithm_Time;
                    break;
    
                    default:
                    break;
                }
                
                if (!chart.getSeriesMap().containsKey(name))
                {
                    double[] xData = new double[]{tasksTarget};
                    double[] yData = new double[]{yValue};
                    chart.addSeries(name, xData, yData, null);
                }
                else
                {
                    CategorySeries series = chart.getSeriesMap().get(name);
                    List<Number> xData = (List<Number>)series.getXData();
                    List<Number> yData = (List<Number>)series.getYData();
                    xData.add(tasksTarget);
                    yData.add(yValue);

                    chart.updateCategorySeries(name, xData, yData, null);
                }

                // upadate y axis min and max
                if (this.yAxisTickDivider > 0)
                {
                    if (chart.getStyler().getYAxisMin() > yValue)
                    {
                        chart.getStyler().setYAxisMin(yValue - (yValue / this.yAxisTickDivider));
                    }
                    else
                    if (chart.getStyler().getYAxisMax() < yValue)
                    {
                        chart.getStyler().setYAxisMax(yValue + (yValue / this.yAxisTickDivider));
                    }
                }
            }
        } catch(Exception e) {

        }
    }
    
    /**Round up the cycle of simulations for this task target.*/
    public void EndTasksTarget() 
    {
		/*for (XChartPanel<CategoryChart> chartPanel : this.charts)
        {
			chartPanel.validate();
			chartPanel.updateUI();
		}*/
        
        this.chartsPanel.validate();
        this.chartsPanel.updateUI();
        this.scrollPanel.validate();
        this.scrollPanel.updateUI();
        // this.validate();
		// this.updateUI();
    }

    /**
     * @return Scroll pane carrying the charts for view. 
     */
    public JScrollPane GetScrollPanel()
    {
        return this.scrollPanel;
    }

    //  *****************************
    //  Protected Mehtods
    //  *****************************

    /**
     * Simulation's algorithm name in table format.
     * @param info Current simulation's data.
     * @return Simulation formatted name.
     */
    protected String GetAlgorithmName(ASimulationInfo info)
    {
		if (!info.algorithm.GetHint().isEmpty())
		return info.Name + " (" + info.algorithm.GetHint() + ')';

		return info.Name;
    }

    /**
     * Create a chart panel for visual display.
     * @param title Of the chart.
     * @param xAxis XAxis title.
     * @param yAxis YAxis title.
     * @return Chart paenl to be used with GUI entities.
     */
    protected XChartPanel<CategoryChart> GetChart(String title, String xAxis, String yAxis)
    {
        CategoryChart chart = new CategoryChartBuilder().title(title)
        .xAxisTitle(xAxis).yAxisTitle(yAxis).theme(this.theme)
        .height(this.chartHeight).width(this.chartWidth).build();

		// Customize Chart
        chart.getStyler().setDefaultSeriesRenderStyle(this.renderStyle);
        
        chart.getStyler().setLegendPosition(this.legendPosition);
        chart.getStyler().setLegendLayout(this.legendLayout);
        chart.getStyler().setDecimalPattern(this.numberFormat);
        chart.getStyler().setHasAnnotations(this.annotationVisible);
        chart.getStyler().setAnnotationsRotation(this.annotationAngle);
        chart.getStyler().setPlotGridHorizontalLinesVisible(this.plotGridHorizontalLinesVisible);
        chart.getStyler().setPlotGridVerticalLinesVisible(this.plotGridVerticalLinesVisible);
        //chart.getStyler().setXAxisMin(this.xMin);
        //chart.getStyler().setXAxisMax(this.xMax);
        
        // custom y axis tick divider
        if (this.yAxisTickDivider > 0)
        {
            chart.getStyler().setYAxisTickMarkSpacingHint((int)(this.chartHeight / this.yAxisTickDivider));
            chart.getStyler().setYAxisMin(this.yMin);
            chart.getStyler().setYAxisMax(this.yMax);
        }
        
		//chart.getStyler().setChartTitleBoxVisible(true);
		//chart.getStyler().setChartTitleBoxBorderColor(Color.black);
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
        //chart.addSeries("yAxis", new double[]{53}, new double[]{53});

        XChartPanel<CategoryChart> panel = new XChartPanel<CategoryChart>(chart);
        panel.setName(title);


        return panel;
    }
}