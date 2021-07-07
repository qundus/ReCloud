package tech.skargen.recloud.templates;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.cloudbus.cloudsim.Cloudlet;

/** Responsible for carrying the simulations' results. */
public final class SimulationResult {
  /** Referred to as the last VM to finish a task. */
  public double makespan;

  /** The collective finish time of all VMs. */
  public double vmsMakespan;

  /** How further from the mean this group of machines are. */
  public double standardDeviation;

  /** Imbalance measure across all machines. */
  public double degreeOfImbalance;

  /** The benchmarking of the algorithm. */
  public long simulationDuration;

  /** The list that keeps received tasks. */
  public ObjectArrayList<Cloudlet> recievedCloudlets = new ObjectArrayList<>();
}