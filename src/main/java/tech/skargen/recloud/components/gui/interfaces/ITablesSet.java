package tech.skargen.recloud.components.gui.interfaces;

import java.awt.Color;
import java.awt.Font;

public abstract interface ITablesSet {
  /**
   * Set results font size, standard -> new Font(Font.SERIF, Font.PLAIN, screen_width*0.0108).
   *
   * @param font Font.
   */
  public abstract ITablesSet font(Font font);

  /**
   * Number display style, i.e.: '1000.1', '1,000.1', '1000.100'..etc.
   *
   * @param numberFormat In the form of DecimalFormat like '###,###,###.####'.
   */
  public abstract ITablesSet numberFormat(String numberFormat);

  /**
   * Colors of text and background.
   *
   * @param background Background color.
   * @param foreground Foreground color.
   */
  public abstract ITablesSet colors(Color background, Color foreground);
}
