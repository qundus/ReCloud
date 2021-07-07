package tech.skargen.recloud.components.gui.interfaces;

import javax.swing.JScrollPane;

public interface ITables {
  /**
   * Swing scroll pane containing environment setup.
   *
   * @return Environment table as scroll pane.
   */
  public abstract JScrollPane getEnvironmentPane();

  /**
   * Environment table as text.
   *
   * @return Environment table as text.
   */
  public abstract String getEnvironmentTable();

  /**
   * Swing scroll pane containing Experiment results.
   *
   * @return Experiment table as scroll pane.
   */
  public abstract JScrollPane getExperimentPane();

  /**
   * Experiment table as text.
   *
   * @return Experiment table as text.
   */
  public abstract String getExperimentTable();
}
