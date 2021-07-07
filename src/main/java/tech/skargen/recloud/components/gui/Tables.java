package tech.skargen.recloud.components.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import tech.skargen.recloud.components.gui.interfaces.ITables;
import tech.skargen.recloud.components.gui.interfaces.ITablesSet;
import tech.skargen.recloud.components.simulation.ISimulation;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IJobs;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.templates.SimulationResult;
import tech.skargen.skartools.STable;
import tech.skargen.skartools.STable.EntryStyle;
import tech.skargen.skartools.SText;

public class Tables
    implements IRecloudSequence, IExperimentSequence, ISimulationSequence, ITables, ITablesSet {
  private STable stable;
  private DecimalFormat numf;

  private int activeTableIndex;

  protected JScrollPane enviPane;
  protected JTextArea enviText;

  protected JScrollPane experPane;
  protected JTextArea experText;

  /**Contructor.*/
  public Tables() {
    // Legacy
    this.stable = new STable();
    this.numf = new DecimalFormat("###,###,###.####");

    // GUI
    this.enviText = makeTextArea();
    this.experText = makeTextArea();
  }

  /**
   * Swing scroll pane containing environment setup.
   * @return Environment table as scroll pane.
   */
  @Override
  public JScrollPane getEnvironmentPane() {
    return this.enviPane;
  }

  /**
   * Environment table as text.
   * @return Environment table as text.
   */
  @Override
  public String getEnvironmentTable() {
    return this.enviText.getText();
  }

  /**
   * Swing scroll pane containing Experiment results.
   * @return Experiment table as scroll pane.
   */
  @Override
  public JScrollPane getExperimentPane() {
    return this.experPane;
  }

  /**
   * Experiment table as text.
   * @return Experiment table as text.
   */
  @Override
  public String getExperimentTable() {
    return this.experText.getText();
  }

  /**
   * Set results font size, standard -> new Font(Font.SERIF, Font.PLAIN, screen_width*0.0108).
   *
   * @param font Font.
   */
  @Override
  public ITablesSet font(Font font) {
    this.experText.setFont(font);
    this.enviText.setFont(font);
    return this;
  }

  /**
   * Number display style, i.e.: '1000.1', '1,000.1', '1000.100'..etc.
   *
   * @param numberFormat In the form of DecimalFormat like '###,###,###.####'.
   */
  @Override
  public ITablesSet numberFormat(String numberFormat) {
    this.numf = new DecimalFormat(numberFormat);
    return this;
  }

  /**
   * Colors of text and background.
   *
   * @param background Background color.
   * @param foreground Foreground color.
   */
  @Override
  public ITablesSet colors(Color background, Color foreground) {
    this.enviText.setBackground(background);
    this.enviText.setForeground(foreground);

    this.experText.setBackground(background);
    this.experText.setForeground(foreground);
    return this;
  }

  /**
   * Create a swing scroll pane.
   * @return Configured scroll pane.
   */
  public JScrollPane makeScrollPane(Component comp) {
    JScrollPane result = new JScrollPane(comp);
    result.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    result.getHorizontalScrollBar().setUnitIncrement(16);
    result.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    result.getVerticalScrollBar().setUnitIncrement(16);
    return result;
  }

  /**
   * Create a swing text area.
   * @return Configured text area.
   */
  public JTextArea makeTextArea() {
    JTextArea result = new JTextArea();
    result.setFont(null);
    result.setAutoscrolls(true);
    result.setEditable(false);
    result.setBackground(Color.WHITE);
    result.setForeground(Color.BLACK);
    return result;
  }

  @Override
  public void validate() throws Exception {}

  @Override
  public void init(IRecloud recloud) {
    // Legacy
    final int minWidth = recloud.getWindow().getScreenBounds().width;

    // GUI
    final int fontsize = (int) (minWidth * 0.0108);
    final Font font = new Font(Font.DIALOG_INPUT, Font.PLAIN, fontsize);
    if (this.enviText.getFont() == null) {
      this.enviText.setFont(font);
    }
    if (this.experText.getFont() == null) {
      this.experText.setFont(font);
    }

    // Finalize
    StringBuilder sb = this.stable.stext().wrap(0, 50, '|', ' ', '\0', '|', "Environment Setup");
    this.stable.stext().sequenceWrap(0, '-', '+', sb);
    sb.append(SText.NEWLINE);
    sb.append(SText.NEWLINE);
    sb.append(recloud.getServers().generateTable());
    sb.append(SText.NEWLINE);
    sb.append(recloud.getJobs().generateJobsTable());
    this.enviText.setText(sb.toString());
    this.enviPane = makeScrollPane(this.enviText);

    sb = this.stable.stext().wrap(0, 50, '|', ' ', '\0', '|', "Experiment");
    this.stable.stext().sequenceWrap(0, '-', '+', sb);
    this.experText.setText(sb.toString());
    this.experPane = makeScrollPane(this.experText);
  }

  @Override
  public void newSequence(IRecloud recloud) {
    final int tasksTarget = recloud.getExperiment().getTasksTarget();

    this.stable.newTable(EntryStyle.Horizontle, 9, 20, '|', ' ', '|');
    this.stable.addEntry(-1, "Algorithm", "(id)");
    this.stable.addEntry(-1, "Makespan", "(S)");
    this.stable.addEntry(-1, "Vms Makespan", "(S)");
    this.stable.addEntry(-1, "Imbalance Degree");
    this.stable.addEntry(-1, "Standard Deviation");
    this.stable.addEntry(-1, "ProcessingSpeed", "(ms)");
    this.stable.addEntry(-1, "Vm Scheduler");
    this.stable.addEntry(-1, "Task Scheduler");
    this.stable.addEntry(-1, "Special Info");
    this.stable.addEntry(-1, "Developer");

    StringBuilder result = new StringBuilder(this.experText.getText());
    result.append(SText.NEWLINE);
    this.activeTableIndex = result.length();
    result.append(this.stable.endTable(-1, '-', '+', this.numf.format(tasksTarget), " Tasks"));

    this.experText.setText(result.toString());
    this.experText.updateUI();
  }

  @Override
  public void beforeSimulation(IRecloud recloud) {}

  /**
   * Formulate results text to be displayed according to pre-formatted table
   * styles (TableMode).
   *
   * @param recloud Data carrier across sequences.
   */
  @Override
  public void afterSimulation(IRecloud recloud) {
    final int tasksTarget = recloud.getExperiment().getTasksTarget();
    final ISimulation simulation = recloud.getExperiment().getSimulation();
    final SimulationResult simresult = recloud.getExperiment().getSimulationResults();

    this.stable.addCell(-1, simulation.getAlgorithmName(), '(', simulation.getID(), ')');
    this.stable.addCell(1, this.numf.format(simresult.makespan));
    this.stable.addCell(1, this.numf.format(simresult.vmsMakespan));
    this.stable.addCell(1, this.numf.format(simresult.degreeOfImbalance));
    this.stable.addCell(1, this.numf.format(simresult.standardDeviation));
    this.stable.addCell(1, this.numf.format(simresult.simulationDuration));
    this.stable.addCell(-1, simulation.getVmScheduler().name());
    this.stable.addCell(-1, simulation.getTaskScheduler().name());
    this.stable.addCell(-1, simulation.getAdditionalInfo());
    this.stable.addCell(-1, simulation.getDeveloper());

    StringBuilder result = new StringBuilder(this.experText.getText());
    result.setLength(this.activeTableIndex);
    result.append(this.stable.endTable(
        -1, '-', '+', "Simulation for", this.numf.format(tasksTarget), "-Tasks"));

    this.experText.setText(result.toString());
    this.experText.updateUI();
  }

  /**
   * Append stylezation text to current results text to clearly mark the
   * seperation line between every experiment done for a specific number of tasks.
   */
  @Override
  public void endSequence(IRecloud recloud) {
    final int tasksTarget = recloud.getExperiment().getTasksTarget();
    final IJobs jobs = recloud.getJobs();

    StringBuilder result = new StringBuilder(this.experText.getText());
    result.setLength(this.activeTableIndex);
    result.append(this.stable.endTable(
        -1, '-', '+', "Simulation for", this.numf.format(tasksTarget), "-Tasks"));

    result.append(jobs.generateDistributionTable(this.numf));

    this.experText.setText(result.toString());
    this.experText.updateUI();
  }

  /**
   * Finalizing results texts, therefore, experiments done are signed off by
   * their research conductor to eliminate experiments fraud and/or impurity. For
   * now, the exported results are locked from being written on but in the future
   * it's planned to be encrypted by the signee's name and never allowing any way
   * of manipulating it.
   */
  @Override
  public void finish(IRecloud recloud) {
    // Legacy
    String signedBy = recloud.getExperiment().getSigniture();
    if (signedBy == null || signedBy.isEmpty()) {
      signedBy = "Researchers Cloud";
    }

    StringBuilder sb = this.stable.stext().wrap(0, 50, '_', '_', '\0', '_', "Experiment Done");
    sb.append(SText.NEWLINE);
    sb.append(this.stable.stext().wrap(0, 50, '_', '_', ':', '_', "", "Researcher", signedBy));

    this.stable.stext().sequenceWrap(0, '-', '^', sb);

    StringBuilder result = new StringBuilder(this.experText.getText());
    result.append(SText.NEWLINE);
    result.append(SText.NEWLINE);
    result.append(sb);

    // GUI
    this.experText.setText(result.toString());
    this.experText.updateUI();
  }
}