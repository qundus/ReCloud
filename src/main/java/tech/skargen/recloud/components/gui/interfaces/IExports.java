package tech.skargen.recloud.components.gui.interfaces;

import javax.swing.JButton;
import javax.swing.JFrame;
import tech.skargen.recloud.controllers.interfaces.IRecloud;

public abstract interface IExports {
  /**
   * Create the save/export button.
   *
   * @param frame   Main frame button belongs to.
   * @param recloud Recloud data corrior across experiment components.
   * @return JButton enabling user to export results.
   */
  public abstract JButton makeButton(JFrame frame, IRecloud recloud);
}
