package tech.skargen.recloud.controllers.interfaces;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.cloudbus.cloudsim.Cloudlet;
import tech.skargen.recloud.components.simulation.ASimulation;
import tech.skargen.recloud.components.simulation.ISimulation;
import tech.skargen.recloud.templates.SimulationResult;

public abstract interface IExperiment {
  /**
   * Retrieve Active simulation algorithm.
   *
   * @return Simulation algorithm interface.
   */
  public abstract ISimulation getSimulation();

  /**
   * Populate simulation results from simulation performance and collected
   * received cloudlets.
   *
   * @return Formulated simulation result.
   */
  public abstract SimulationResult getSimulationResults();

  /**
   * Task target currently in motion.
   * @return Current tasks target aimed for.
   */
  public abstract int getTasksTarget();

  /**
   * Signiture text set by experiment conductor.
   * @return Siginture text.
   */
  public abstract String getSigniture();

  /**
   * List of all the simulations to be conducted.
   *
   * @return List of simulations.
   */
  public abstract ObjectArrayList<ASimulation> getSimulationList();

  /**
   * Confirm or deny the existence of next task target in list.
   * @return True if experiment for all tasks targets has finished.
   */
  public abstract boolean allSequencesDone();

  /**
   * Confirm or deny the end of curent sequence, that is all simulations in list
   * have gone over the current active tasks target.
   *
   * @return True if all simulations for current tasks targets has finished.
   */
  public abstract boolean allSimulationsDone();

  /**
   * Notify experiments of recieving a cloudlet.
   *
   * @param task Cloudlet to be added to list only if it's valid.
   */
  public abstract void updateRecievedCloudlets(Cloudlet task);

  /**
   * Calculate how long the simulation algorithm took in nano seconds and pass the
   * final result here to be accumulated with all entities using the active
   * algorithm.
   * It's preferable if System.nanoTime() is used for the calculation.
   * @param duration Duration simulation algorithm took.
   */
  public abstract void updateSimulationDuration(long duration);
}
