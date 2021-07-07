package tech.skargen.recloud.components.simulation;

import tech.skargen.recloud.controllers.interfaces.IRecloud;

public abstract interface ISimulationSequence {
  /**
   * Called once before cloudsim simulation starts and after cloudsim.init() is
   * called.
   *
   * @param seqD Data passed across all components.
   */
  public abstract void beforeSimulation(IRecloud recloud);

  /**
   * Called once after cloudsim has finished simulation.
   *
   * @param seqD Data passed across all components.
   */
  public abstract void afterSimulation(IRecloud recloud);
}
