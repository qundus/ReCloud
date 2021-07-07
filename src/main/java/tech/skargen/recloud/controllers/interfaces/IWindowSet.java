package tech.skargen.recloud.controllers.interfaces;

import tech.skargen.recloud.components.gui.interfaces.IChartsSet;
import tech.skargen.recloud.components.gui.interfaces.IExportsSet;
import tech.skargen.recloud.components.gui.interfaces.ITablesSet;

public abstract interface IWindowSet {
  /**
   * Configure tables component.
   * @return Tables component interface.
   */
  public abstract ITablesSet tables();

  /**
   * Configure charts component.
   *
   * @return charts component interface.
   */
  public abstract IChartsSet charts();

  /**
   * Configure exports component.
   *
   * @return exports component interface.
   */
  public abstract IExportsSet exports();

  /**
   * Display monitor to show main frame on.
   *
   * @param screen Number of dipslay monitor, standard value for main monitor is
   *               '0'.
   */
  public abstract IWindowSet monitor(int screen);
}
