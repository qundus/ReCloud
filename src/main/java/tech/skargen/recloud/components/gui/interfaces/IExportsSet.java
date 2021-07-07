package tech.skargen.recloud.components.gui.interfaces;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;

public abstract interface IExportsSet {
  /**
   * Choose the preferred exportation image format and quality.
   *
   * @param format Export image format of charts.
   * @param dpi    Image quality value.
   */
  public abstract IExportsSet image(BitmapFormat format, int dpi);

  /**
   * Location to exports experiment files in.
   *
   * @param pathtofolder Path to folder without the '/' and '\' chars to avoid OS
   *                     restrictions.
   */
  public abstract IExportsSet location(String... pathtofolder);
}
