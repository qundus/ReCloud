package tech.skargen.recloud.controllers;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.Log;
import tech.skargen.recloud.components.gui.Charts;
import tech.skargen.recloud.components.gui.ConsoleToSwing;
import tech.skargen.recloud.components.gui.Exports;
import tech.skargen.recloud.components.gui.Progress;
import tech.skargen.recloud.components.gui.Tables;
import tech.skargen.recloud.components.gui.interfaces.ICharts;
import tech.skargen.recloud.components.gui.interfaces.IChartsSet;
import tech.skargen.recloud.components.gui.interfaces.IExports;
import tech.skargen.recloud.components.gui.interfaces.IExportsSet;
import tech.skargen.recloud.components.gui.interfaces.IProgress;
import tech.skargen.recloud.components.gui.interfaces.ITables;
import tech.skargen.recloud.components.gui.interfaces.ITablesSet;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.controllers.interfaces.IWindow;
import tech.skargen.recloud.controllers.interfaces.IWindowSet;

/** Handles GUI elements. */
public final class Window
    implements IRecloudSequence, IExperimentSequence, ISimulationSequence, IWindowSet, IWindow {
  private Tables tables;
  private Charts charts;
  private Progress progress;
  private Exports exports;
  private ConsoleToSwing cloudsimLogsStream;
  private ConsoleToSwing systemStream;

  private Rectangle screenBounds;
  private static Logger _LOG;

  static {
    _LOG = LogManager.getLogger();
  }

  /**
   * Constructor.
   */
  public Window() {
    this.tables = new Tables();
    this.charts = new Charts();
    this.progress = new Progress();
    this.exports = new Exports();
    this.cloudsimLogsStream = new ConsoleToSwing();
    this.systemStream = new ConsoleToSwing();

    // Choose Initial Screen.
    this.monitor(0);
  }

  /**
   * Display monitor to show main frame on.
   *
   * @param screen Number of dipslay monitor, standard value for main monitor is
   *               '0'.
   * @return This window interface.
   */
  @Override
  public IWindowSet monitor(int screen) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    if (screen < 0 || screen >= gd.length) {
      screen = 0;
    }

    this.screenBounds = gd[screen].getDefaultConfiguration().getBounds();
    return this;
  }

  /**
   * Obtain screen width, height and other aspects.
   *
   * @return Screen bounds.
   */
  @Override
  public Rectangle getScreenBounds() {
    return this.screenBounds;
  }

  /**
   * Instance that is used to handles tables prints.
   *
   * @return Tables instance.
   */
  @Override
  public ITables getTables() {
    return this.tables;
  }

  /**
   * Instance that is used to handles charts views.
   *
   * @return Charts instance.
   */
  public ICharts getCharts() {
    return this.charts;
  }

  /**
   * Progress instance to update progress bars.
   *
   * @return Progress bar instance.
   */
  @Override
  public IProgress getProgress() {
    return this.progress;
  }

  @Override
  public IExports getExports() {
    return this.exports;
  }

  /**
   * Configure tables component.
   *
   * @return Tables component interface.
   */
  @Override
  public ITablesSet tables() {
    return this.tables;
  }

  /**
   * Configure charts component.
   *
   * @return charts component interface.
   */
  @Override
  public IChartsSet charts() {
    return this.charts;
  }

  /**
   * Configure exports component.
   *
   * @return exports component interface.
   */
  @Override
  public IExportsSet exports() {
    return this.exports;
  }

  @Override
  public void validate() throws Exception {
    this.tables.validate();
    this.charts.validate();
    this.progress.validate();
    this.exports.validate();
  }

  /** Create and initialize swing elements to be shown on screen.*/
  @Override
  public void init(IRecloud recloud) {
    this.tables.init(recloud);
    this.charts.init(recloud);
    this.progress.init(recloud);
    this.exports.init(recloud);

    // redirect cloudsim logs
    PrintStream cloudsimout = new PrintStream(this.cloudsimLogsStream);
    Log.setOutput(cloudsimout);

    PrintStream systemout = new PrintStream(this.systemStream);
    // redirect standard output stream to the TextAreaOutputStream
    System.setOut(systemout);
    // redirect standard error stream to the TextAreaOutputStream
    System.setErr(systemout);

    try {
      // to be used with multiple window instances during runtime
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          // Create and set up the window at the center screen.
          JFrame frame = new JFrame("REsearchers Cloud");
          frame.setLocation(screenBounds.x, screenBounds.y + frame.getY());
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

          // prepare experiment panel
          JPanel simPanel = new JPanel();
          simPanel.setLayout(new BorderLayout());
          simPanel.add(progress.getProgressPanel(), BorderLayout.NORTH);
          simPanel.add(tables.getExperimentPane());

          // Add panels to tabs.
          JTabbedPane tabsPanel = new JTabbedPane();
          tabsPanel.setTabPlacement(JTabbedPane.BOTTOM);
          tabsPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
          tabsPanel.setAlignmentX(JTabbedPane.CENTER_ALIGNMENT);
          tabsPanel.setAlignmentY(JTabbedPane.CENTER_ALIGNMENT);

          tabsPanel.addTab("Experiment", simPanel);
          tabsPanel.addTab("Charts", charts.getChartsPane());
          tabsPanel.addTab("Cloudsim Logs", cloudsimLogsStream.getScrollPane());
          tabsPanel.addTab("Console", systemStream.getScrollPane());
          tabsPanel.addTab("Environment", tables.getEnvironmentPane());
          tabsPanel.addTab("Export", exports.makeButton(frame, recloud));

          // add components to frame.
          frame.add(tabsPanel);

          // finalize
          frame.pack();
          frame.setVisible(true);
        }
      });

    } catch (Exception e) {
      _LOG.fatal("can't initiate window frame", e);
      System.exit(0);
    }
  }

  /** Prepare GUI elements for next sequence. */
  @Override
  public void newSequence(IRecloud recloud) {
    this.tables.newSequence(recloud);
    this.charts.newSequence(recloud);
    this.progress.newSequence(recloud);
    // this.exports.newSequence(recloud);
  }

  /** End current number of tasks for this round of simulations. */
  @Override
  public void endSequence(IRecloud recloud) {
    this.tables.endSequence(recloud);
    this.charts.endSequence(recloud);
    this.progress.endSequence(recloud);
    // this.exports.newSequence(recloud);
  }

  /** Reset GUI elements for next simulation. */
  @Override
  public void beforeSimulation(IRecloud recloud) {
    this.tables.beforeSimulation(recloud);
    this.charts.beforeSimulation(recloud);
    this.progress.beforeSimulation(recloud);
    // this.exports.beforeSimulation(recloud);
  }

  /** Commit end simulation changes to GUI elements. */
  @Override
  public void afterSimulation(IRecloud recloud) {
    this.tables.afterSimulation(recloud);
    this.charts.afterSimulation(recloud);
    this.progress.afterSimulation(recloud);
    // this.exports.afterSimulation(recloud);
  }

  /** Notify GUI elements of experiment end.*/
  @Override
  public void finish(IRecloud recloud) {
    this.tables.finish(recloud);
    // this.charts.finish(recloud);
    // this.progress.finish(recloud);
    // this.exports.finish(recloud);
  }
}