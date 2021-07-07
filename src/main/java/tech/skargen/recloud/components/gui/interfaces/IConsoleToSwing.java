package tech.skargen.recloud.components.gui.interfaces;

import javax.swing.JScrollPane;

public abstract interface IConsoleToSwing {
  /**
   * Swing scroll pane containing text area panel.
   *
   * @return JScrollPane containing text area panel.
   */
  public abstract JScrollPane getScrollPane();

  /**
   * Text area as string.
   *
   * @return Text area as string.
   */
  public abstract String getTextArea();
}
