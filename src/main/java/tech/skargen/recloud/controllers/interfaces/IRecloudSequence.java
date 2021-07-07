package tech.skargen.recloud.controllers.interfaces;

public abstract interface IRecloudSequence {
  /**
   * Called once after user has launched an experiment to validate simulations
   * environment.
   *
   * @return whether this component is ready to function properly or not.
   */
  public abstract void validate() throws Exception;

  /** Called once to initialize necessary data. */
  public abstract void init(IRecloud recloud);

  /**
   * Called once when the experiment with all sequences have finished.
   *
   * @param seqD Data passed across all components.
   */
  public abstract void finish(IRecloud recloud);
}
