package tech.skargen.recloud.components.gui.interfaces;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import tech.skargen.recloud.components.cloudsim.ReBroker;

public abstract interface IProgress {
  /**
   * The panel containing progress bars.
   *
   * @return The panel containing all progress bars.
   */
  public abstract JPanel getProgressPanel();

  /**
   * Format the display message.
   *
   * @param bar Progress bar to be updated.
   */
  public abstract void updateBarTitle(JProgressBar bar);

  /**
   * Update the current progress value by the given number.
   * @param value Number to update progress by.
   */
  public abstract void updateSimulationProgress(int value);

  /**
   * Set a title to be displayed on the progrees bar during runtime.
   * @param title Title text.
   */
  public abstract void updateSimulationTitle(String title);

  /**
   * Update the maximum bound that the progress bar is trying to achieve.
   * @param value Max bound of progress bar.
   */
  public abstract void updateSimulationMax(int value);

  /**
   * Update recieved tasks progress bar.
   *
   * @param broker Broker responsible for the update, acts as a value validation
   *               cred.
   * @param value  Update simulation's received tasks number.
   */
  public abstract void updateRecievedTasksProgress(ReBroker broker, int value);

  /**
   * Update submitted tasks progress bar.
   *
   * @param broker Broker responsible for the update, acts as a value validation
   *               cred.
   * @param value  Update simulation's finished tasks number.
   */
  public abstract void updateSubmittedTasksProgress(ReBroker broker, int value);
}
