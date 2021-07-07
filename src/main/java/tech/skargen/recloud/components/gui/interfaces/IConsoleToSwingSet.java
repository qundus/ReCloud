package tech.skargen.recloud.components.gui.interfaces;

import java.awt.Color;

public abstract interface IConsoleToSwingSet {
  /**
   * Colors of text and background.
   *
   * @param background Background color.
   * @param foreground Foreground color.
   */
  public abstract IConsoleToSwingSet colors(Color background, Color foreground);
}
