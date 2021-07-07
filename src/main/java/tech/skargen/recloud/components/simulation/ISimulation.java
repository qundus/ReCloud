package tech.skargen.recloud.components.simulation;

import org.cloudbus.cloudsim.Cloudlet;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.controllers.Jobs.TaskScheduler;
import tech.skargen.recloud.controllers.Servers.VmScheduler;

public interface ISimulation {
  /**
   * Id of this simulation algorithm.
   * @return unique id.
   */
  public abstract int getID();

  /**
   * Virtual machines scheduler used by cloudsim.
   * @return Vm scheduler.
   */
  public abstract VmScheduler getVmScheduler();

  /**
   * Tasks/cloudlets scheduler used by cloudsim.
   * @return Tasks/cloudlets scheduler.
   */
  public abstract TaskScheduler getTaskScheduler();

  /**
   * Name of this algorithm, usually is a globally known and official name like PSO for Particle
   * Swarm Optimization. This cannot be null.
   * @return Algorithm's name.
   */
  public abstract String getAlgorithmName();

  /**
   * Add additional info to results table within allowed space, This cannot be null.
   * @return Any additional info helpful to outcome comparison or just in general.
   */
  public abstract String getAdditionalInfo();

  /**
   * This is to give credit to the developer who implemented the algorithm, This cannot be null.
   * @return Developer name, website or any string implicating who's behind the implementation.
   */
  public abstract String getDeveloper();

  /**
   * Called when broker is starting.
   * @param rebroker Calling broker.
   */
  public abstract void startEntity(IReBroker rebroker);

  /**
   * Called when broker is shutting down.
   * @param rebroker Calling broker.
   */
  public abstract void shutdownEntity(IReBroker rebroker);

  /**
   * Cloudsim has reached the point of handing tasks to virtual machines/hosts for
   * this broker.
   * @param rebroker Calling broker.
   */
  public abstract void processCloudletsSubmit(IReBroker rebroker);

  /**
   * Cloudsim is sending back the finished tasks.
   * @param rebroker Calling broker.
   * @param task   The returning task.
   */
  public abstract <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task);
}
