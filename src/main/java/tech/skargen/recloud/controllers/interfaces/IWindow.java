package tech.skargen.recloud.controllers.interfaces;

import java.awt.Rectangle;
import tech.skargen.recloud.components.gui.interfaces.ICharts;
import tech.skargen.recloud.components.gui.interfaces.IExports;
import tech.skargen.recloud.components.gui.interfaces.IProgress;
import tech.skargen.recloud.components.gui.interfaces.ITables;

public abstract interface IWindow {
  /**
   * Obtain screen width, height and other aspects.
   * @return Screen bounds.
   */
  public Rectangle getScreenBounds();

  /**
   * Instance that is used to handles tables prints.
   * @return Tables instance.
   */
  public abstract ITables getTables();

  /**
   * Instance that is used to handles charts views.
   *
   * @return Charts instance.
   */
  public abstract ICharts getCharts();

  /**
   * Progress instance to update progress bars.
   * @return Progress bar instance.
   */
  public abstract IProgress getProgress();

  /**
   * Progress instance to update progress bars.
   *
   * @return Progress bar instance.
   */
  public abstract IExports getExports();
}
