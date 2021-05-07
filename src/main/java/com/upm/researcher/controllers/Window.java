package com.upm.researcher.controllers;

import java.text.DecimalFormat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.upm.researcher.components.window.Exports;
import com.upm.researcher.components.window.Progress;
import com.upm.researcher.components.window.Tables;
import com.upm.researcher.components.window.Tables.TableMode;
import com.upm.researcher.components.window.Charts;
import com.upm.researcher.templates.infos.ASimulationInfo;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;

/**Handles GUI elements. */
public final class Window
{
    //  *****************************
    //  Fields
    //  *****************************
    private static Tables tables;
    private static Charts charts;
    private static Progress progress;
    private static Rectangle screenBounds;

    static
    {
        // Choose Initial Screen.
        ShowOnScreen(0);

        tables = new Tables();
        charts = new Charts();
        progress = new Progress();
    }

    //  *****************************
    //  Protected Methods
    //  *****************************

    /**
     * Create and initialize swing elements to be shown on screen.
     * @param numSimulations Number of simulations in the experiment.
     * @return Window creation status.
     */
    protected static boolean Initialize(int numSimulations)
    {
        try
        {

            // Initiate window
            tables.Initialize(numSimulations);
            charts.Initialize(screenBounds);
            progress.Initialize();

            // to be used with multiple window instances during runtime
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            javax.swing.SwingUtilities.invokeLater(new Runnable() {
				
				@Override
                public void run() 
                {
                    // Create and set up the window at the center screen.
                    JFrame frame = new JFrame("UPM Cloud Researcher");
                    frame.setLocation(screenBounds.x, screenBounds.y + frame.getY());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    
                    // Schedule a job for the event-dispatching thread:
                    // creating and showing this application's GUI.
                    // Add results gui as tabs
                    JTabbedPane tabsPanel = new JTabbedPane();
                    tabsPanel.setTabPlacement(JTabbedPane.BOTTOM);
                    tabsPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                    tabsPanel.setAlignmentX(JTabbedPane.CENTER_ALIGNMENT);
                    tabsPanel.setAlignmentY(JTabbedPane.CENTER_ALIGNMENT);

                    JPanel simPanel = new JPanel();
                    simPanel.setLayout(new BorderLayout());
                    simPanel.add(progress.GetPanel(), BorderLayout.NORTH);
                    simPanel.add(tables.GetSimPanel());

                    JPanel chartsPanel = new JPanel();
                    JButton exportsButton = Exports.GetButton(frame, tables.GetSetupTextArea(), 
                    tables.GetSimTextArea(), charts.charts, charts.chartFormat);
                    chartsPanel.setLayout(new BorderLayout());
                    chartsPanel.add(exportsButton, BorderLayout.NORTH);
                    chartsPanel.add(charts.GetScrollPanel());

                    // Add components to frame.
                    tabsPanel.addTab("Setup", tables.GetSetupPanel());
                    tabsPanel.addTab("Simulation", simPanel);
                    tabsPanel.addTab("Charts", chartsPanel);
                    
                    
                    frame.add(tabsPanel);
                    frame.pack();
                    frame.setVisible(true);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Number of tasks for next round of simulations.
     * @param tasksTarget Number of tasks.
     * @param warnings Experiment initialization issues.
     */
    protected static void NewTasksTarget(final int tasksTarget, String warnings)
    {
        tables.NewTasksTarget(tasksTarget, warnings);
        progress.NewTasksTarget(tasksTarget);
    }

    /**
     * Reset GUI elements for next simulation.
     * @param info Next simulation's data.
     */
    protected static void NewSimulation(ASimulationInfo info)
    {
        progress.NewSimulation(info);

        // Set progress 
        info.progress = progress;
    }

    /**
     * Commit end simulation changes to GUI elements.
     * @param info Current simualtion's data.
     */
    protected static void EndSimulation(ASimulationInfo info)
    {
        tables.EndSimulation(info);
        charts.EndSimulation(info);
    }

    /**End current number of tasks for this round of simulations.*/
    protected static void EndTasksTarget()
    {
        tables.EndTasksTarget();
        charts.EndTasksTarget();
    }

    /**
     * Notify GUI elements of experiment end.
     * @param signedBy Signiture of experiment conductor.
     */
    protected static void EndExperiment(String signedBy)
    {
        //Container buttonsP = (Container)centerView.getParent().getComponent(0);
        //Component exportsB = buttonsP.getComponent(buttonsP.getComponentCount()-1);
        //exportsB.setVisible(true);
        //exportsB.setEnabled(true);

        tables.EndExperiment(signedBy);
    }

    //  *****************************
    //  Public Methods
    //  *****************************
    // Interface
    // Table
    /**
     * Table mode modifies the results tables' look style.
     * @param tableMode Preferred results display style.
     */
    public static void SetTableMode(TableMode tableMode)
    {
        tables.tableForm = tableMode;
    }

    /**
     * Customize tables cell space, standard = screen_width*0.015.
     * @param space Preferred size of cell.
     */
    public static void SetTableCellSpace(int space)
    {
        tables.cellSpace = space;
    }

    /**
     * Set results font size, standard = screen_width*0.0108.
     * @param size Font size.
     */
    public static void SetTableFontSize(int size)
    {
        tables.fontSize = size;
    }

    /**
     * Each table has construction style, seperator 
     * helps distinguishing cells and beautifying 
     * tables as a whole.
     * @param separator Character to be used as a seperator.
     */
    public static void SetTableCellSeparator(char separator)
    {
        tables.cellSeperator = separator;
    }

    /**
     * Some information maybe redundant in name but not in value,
     * therefore, hints enable results distinguishment and here
     * you can set the style of the hints' brackets.
     * Hints standard characters are '(' & ')'.
     * @param hintStart Character to be placed before the hint.
     * @param hintEnd Character to be placed after the hint.
     */
    public static void SetTableHintEnds(char hintStart, char hintEnd)
    {
        tables.hintStart = hintStart;
        tables.hintEnd = hintEnd;
    }

    /**
     * Title style is dependant on 2 characters, the footer character
     * is going to be repeatedly printed above and underneath the title;
     * the sides character is to sort of add style independace to the
     * look and feel of the title as a whole.
     * @param sides Character to be place at the sides of the title,
     * standard is '|'.
     * @param footer Character to be printed along the title's line, 
     * standard is '-'.
     */
    public static void SetTableTitleStyle(char sides, char footer)
    {
        tables.titleSides = sides;
        tables.titleFooter = footer;
    }

    /**
     * Number display style, i.e.: '1000.1', '1,000.1', '1000.100'..etc.
     * @param numberFormat In the form of DecimalFormat like '###,###,###.####'.
     */
    public static void SetTableNumberFormatter(String numberFormat)
    {
        tables.numf = new DecimalFormat(numberFormat);
    }

    /**
     * Background color of results text.
     * @param c Color.
     */
    public static void SetTableBackground(Color c)
    {
        tables.bgColor = c;
    }

    /**
     * Foreground color of results text.
     * @param c Color.
     */
    public static void SetTableForeground(Color c)
    {
        tables.fgColor = c;
	}
    
    // Chart
    /**
     * Chart screen size, standard = screen_width*0.5 | 
     * @param width Width of a chart's panel in pixels.
     * @param height Height of a chart's panel in pixels.
     */
	public static void SetChartSize(int width, int height)
    {
		charts.chartWidth = width;
		charts.chartHeight = height;
	}

    /**
     * Charts look and feel according to XChart creators.
     * @param theme preferred look and feel.
     */
	public static void SetChartTheme(ChartTheme theme)
    {
		charts.theme = theme;
	}
	
    /**
     * Internal elements of charts series follow certain rendering styles.
     * @param style Category series render style.
     */
	public static void SetChartRenderStyle(CategorySeriesRenderStyle style)
    {
		charts.renderStyle = style;
	}
	
    /**
     * Data type indicators placement and style.
     * @param position Placement of indicators on the chart.
     * @param layout Layout style of indicators.
     */
	public static void SetChartLegend(LegendPosition position, LegendLayout layout)
    {
		charts.legendPosition = position;
		charts.legendLayout = layout;
	}
	
    /**
     * Number display style, i.e.: '1000.1', '1,000.1', '1000.100'..etc.
     * @param numberFormat In the form of DecimalFormat like '###,###,###.####'.
     */
	public static void SetChartNumberFormat(String numberFormat)
    {
		charts.numberFormat = numberFormat;
    }

    /**
     * A value greater than 0 to divide chart height by and to divide y axis tick labels
     * as they grow, helps in reducing the dimensions of the image produced.
     * @param tickD Chart's tick margin.
     */
    public static void SetChartYAxisTickDivider(double tickD)
    {
		charts.yAxisTickDivider = tickD;
    }

    /**
     * Set guidance lines visibility.
     * @param horizontal Horizontal guidance lines visibility.
     * @param vertical Vertical guidance lines visibility.
     */
    public static void SetChartGridLinesVisible(boolean horizontal, boolean vertical)
    {
		charts.plotGridHorizontalLinesVisible = horizontal;
		charts.plotGridVerticalLinesVisible = vertical;
	}

    /**
     * Label of XY elements angle and visibility.
     * @param visible Status of visibility.
     * @param angle Angle of label in degrees.
     */
	public static void SetChartAnnotation(boolean visible, int angle)
    {
		charts.annotationVisible = visible;
		charts.annotationAngle = angle;
	}

    /**
     * Export image format of charts.
     * @param format Image format.
     */
	public static void SetChartImageFormat(BitmapFormat format)
    {
		charts.chartFormat = format;
	}
	
    /**
     * Display monitor to show main frame on.
     * @param screen Number of dipslay monitor, 
     * standard value for main monitor is '0'.
     */
    public static void ShowOnScreen(int screen)
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if(screen < 0 || screen >= gd.length)
        {
            screen = 0;
        }

        screenBounds = gd[screen].getDefaultConfiguration().getBounds();
    }

    /**
     * Obtain screen width, height and other aspects.
     * @return Screen bounds.
     */
    public static Rectangle GetScreenBounds()
    {
        return screenBounds;
    }
}