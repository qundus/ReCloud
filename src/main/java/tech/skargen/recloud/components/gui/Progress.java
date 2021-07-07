package tech.skargen.recloud.components.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.cloudbus.cloudsim.core.CloudSim;
import tech.skargen.recloud.components.cloudsim.ReBroker;
import tech.skargen.recloud.components.gui.interfaces.IProgress;
import tech.skargen.recloud.components.simulation.ISimulation;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;

/**
 * Shows each simulation's progress update by having
 * {@link tech.skargen.recloud.components.simulation.ASimulation} call
 * UpdateProgress(int) method.
 *
 * @see tech.skargen.recloud.components.simulation.ASimulation
 * @see tech.skargen.recloud.components.algorithms.StandardSim
 */
public class Progress
    implements IRecloudSequence, IExperimentSequence, ISimulationSequence, IProgress {
  private JPanel panel;
  private JProgressBar simulationBar;
  private JProgressBar submittedTasksBar;
  private JProgressBar recievedTasksBar;

  /**
   * The panel containing progress bars.
   *
   * @return The panel containing all progress bars.
   */
  @Override
  public JPanel getProgressPanel() {
    return this.panel;
  }

  /**
   * Format the display message.
   * @param bar Progress bar to be updated.
   */
  @Override
  public void updateBarTitle(JProgressBar bar) {
    bar.setString(String.format("%s%100.3f%%", bar.getName(), bar.getPercentComplete() * 100.0));
  }

  /**
   * Update the current progress value by the given number.
   *
   * @param value Number to update progress by.
   */
  @Override
  public void updateSimulationProgress(int value) {
    this.simulationBar.setValue(this.simulationBar.getValue() + value);
    this.updateBarTitle(this.simulationBar);
  }

  /**
   * Set a title to be displayed on the progrees bar during runtime.
   *
   * @param title Title text.
   */
  @Override
  public void updateSimulationTitle(String title) {
    this.simulationBar.setName(title);
    this.updateBarTitle(this.simulationBar);
  }

  /**
   * Update the maximum bound that the progress bar is trying to achieve.
   *
   * @param value Max bound of progress bar.
   */
  @Override
  public void updateSimulationMax(int value) {
    this.simulationBar.setMaximum(this.simulationBar.getMaximum() + value);
    this.updateBarTitle(this.simulationBar);
  }

  /**
   * Update submitted tasks progress bar.
   *
   * @param rebroker Broker responsible for the update, acts as a value validation
   *               cred.
   * @param value  Update simulation's finished tasks number.
   */
  public void updateSubmittedTasksProgress(ReBroker rebroker, int value) {
    this.submittedTasksBar.setValue(this.submittedTasksBar.getValue() + value);
    this.updateBarTitle(this.submittedTasksBar);
  }

  /**
   * Update recieved tasks progress bar.
   *
   * @param rebroker Broker responsible for the update, acts as a value validation
   *               cred.
   * @param value  Update simulation's received tasks number.
   */
  @Override
  public void updateRecievedTasksProgress(ReBroker rebroker, int value) {
    this.recievedTasksBar.setValue(this.recievedTasksBar.getValue() + value);
    this.updateBarTitle(this.recievedTasksBar);
  }

  @Override
  public void validate() throws Exception {}

  /** Initial customization of progress bar. */
  @Override
  public void init(IRecloud recloud) {
    this.simulationBar = new JProgressBar();
    this.submittedTasksBar = new JProgressBar();
    this.recievedTasksBar = new JProgressBar();

    this.simulationBar.setValue(0);
    this.submittedTasksBar.setValue(0);
    this.recievedTasksBar.setValue(0);

    this.simulationBar.setBorderPainted(true);
    this.simulationBar.setStringPainted(true);
    this.submittedTasksBar.setBorderPainted(true);
    this.submittedTasksBar.setStringPainted(true);
    this.recievedTasksBar.setBorderPainted(true);
    this.recievedTasksBar.setStringPainted(true);

    this.simulationBar.setName("CloudSim ");
    this.submittedTasksBar.setName("Sending Tasks ");
    this.recievedTasksBar.setName("Recieving Tasks ");

    this.updateBarTitle(this.simulationBar);
    this.updateBarTitle(this.submittedTasksBar);
    this.updateBarTitle(this.recievedTasksBar);

    JPanel lowerPanel = new JPanel(new GridLayout(1, 2));
    lowerPanel.add(this.submittedTasksBar);
    lowerPanel.add(this.recievedTasksBar);

    this.panel = new JPanel();
    this.panel.setLayout(new BorderLayout());
    this.panel.add(this.simulationBar, BorderLayout.PAGE_START);
    this.panel.add(lowerPanel, BorderLayout.PAGE_END);
  }

  /** Reset progress bar's maximum bound value. */
  @Override
  public void newSequence(IRecloud recloud) {
    final int tasksTarget = recloud.getExperiment().getTasksTarget();

    this.simulationBar.setMaximum(tasksTarget);
    this.submittedTasksBar.setMaximum(tasksTarget);
    this.recievedTasksBar.setMaximum(tasksTarget);

    this.updateBarTitle(this.simulationBar);
    this.updateBarTitle(this.submittedTasksBar);
    this.updateBarTitle(this.recievedTasksBar);
  }

  @Override
  public void endSequence(IRecloud recloud) {}

  /**
   * Reset progress bars' values for the next simulation.
   */
  @Override
  public void beforeSimulation(IRecloud recloud) {
    ISimulation simulation = recloud.getExperiment().getSimulation();

    this.simulationBar.setValue(0);
    this.simulationBar.setMaximum(0);
    this.submittedTasksBar.setValue(0);
    this.recievedTasksBar.setValue(0);

    // this.sentTasksPB.setIndeterminate(true);
    // this.recievedTasksPB.setIndeterminate(true);

    this.simulationBar.setName(simulation.getAlgorithmName() + '_' + simulation.getID() + ' ');
    this.updateBarTitle(this.simulationBar);

    // this.UpdateProgressString(this.sentTasksPB);
    // this.UpdateProgressString(this.recievedTasksPB);
    this.submittedTasksBar.setString("Algorithm Hasn't Sent Any Tasks");
    this.recievedTasksBar.setString("No Cloudlets Have Been Recieved");
    if (CloudSim.running()) {
      // allow update of sent and recieved cloudlets to controllers only
      // CloudSim.getentity(rebroker.getName());
    }
  }

  @Override
  public void afterSimulation(IRecloud recloud) {}

  @Override
  public void finish(IRecloud recloud) {}
}