package tech.skargen.recloud.controllers.interfaces;

public abstract interface IExperimentSequence {
  /**
   * Called once to start a new sequence of events for all simulation algorithms,
   * usually marks the begining with a new task target.
   * @param recloud Data passed across all components.
   */
  public abstract void newSequence(IRecloud recloud);

  /**
   * Called once after all simulations at hand have finished for this sequence.
   * @param recloud Data passed across all components.
   */
  public abstract void endSequence(IRecloud recloud);
}
