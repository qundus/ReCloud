package tech.skargen.recloud.components.simulation;

import tech.skargen.recloud.controllers.Jobs.TaskScheduler;
import tech.skargen.recloud.controllers.Servers.VmScheduler;

/**
 * Class to be inherited by any class that wishes to conduct a cloud simulation.
 * @see tech.skargen.recloud.components.simulations
 */
public abstract class ASimulation implements ISimulation {
  /** The simulation scheduler to be assigned to virtual machines and hosts. */
  private VmScheduler vmScheduler;

  /** The simulation scheduler to be assigned to tasks. */
  private TaskScheduler taskScheduler;

  /** Unique identificatin title. */
  private final int uid;
  private static int id;

  static {
    id = 0;
  }

  /**
   * Create a new simulation with a given name.
   */
  public ASimulation() {
    this.taskScheduler = TaskScheduler.TimeShared;
    this.vmScheduler = VmScheduler.TimeShared;
    this.uid = id;
    id++;
  }

  /**
   * Set cloudsim environment schedulers.
   * @param vms The simulation scheduler to be assigned to virtual machines and hosts.
   * @param ts The simulation scheduler to be assigned to tasks.
   * @return This interface.
   */
  public final ASimulation schedulers(VmScheduler vms, TaskScheduler ts) {
    this.vmScheduler = vms;
    this.taskScheduler = ts;
    return this;
  }

  @Override
  public final int getID() {
    return this.uid;
  }

  @Override
  public final VmScheduler getVmScheduler() {
    return this.vmScheduler;
  }

  @Override
  public final TaskScheduler getTaskScheduler() {
    return this.taskScheduler;
  }
}