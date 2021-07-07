package tech.skargen.recloud.controllers.interfaces;

import tech.skargen.recloud.components.simulation.ASimulation;
import tech.skargen.recloud.controllers.Jobs.TaskScheduler;
import tech.skargen.recloud.controllers.Servers.VmScheduler;

public abstract interface IExperimentSet {
  /**
   * Collection of numbers to be used as 'number of tasks' for all simulations.
   */
  public abstract IExperimentSet taskTargets(int... numbers);

  /**
   * Set cloudsim init state fields and whether to show logs.
   * @param users     Users of the cloud
   * @param traceFlag Trace flag.
   */
  public abstract IExperimentSet cloudsim(int users, boolean traceFlag);

  /**
   * Experiments are marked by a signiture to trace their conductor and to add on
   * to their credebility.
   * @param signiture Signiture text.
   */
  public abstract IExperimentSet signiture(String signiture);

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   * @param sims Array of simulation algorithms.
   */
  public abstract void newSimulations(ASimulation... sims);

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   * @param vmscheduler Simulation scheduler to be assigned to virtual machines
   *                    and hosts.
   * @param sims        Array of simulation algorithms.
   */
  public abstract void newSimulations(VmScheduler vmscheduler, ASimulation... sims);

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   * @param taskscheduler Simulation scheduler to be assigned to tasks.
   * @param sims          Array of simulation algorithms.
   */
  public abstract void newSimulations(TaskScheduler taskscheduler, ASimulation... sims);

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   * @param vmscheduler   The simulation scheduler to be assigned to virtual
   *                      machines and hosts.
   * @param taskScheduler The simulation scheduler to be assigned to tasks.
   * @return This editing interface.
   */
  public abstract void newSimulations(
      VmScheduler vmscheduler, TaskScheduler taskScheduler, ASimulation... sims);
}
