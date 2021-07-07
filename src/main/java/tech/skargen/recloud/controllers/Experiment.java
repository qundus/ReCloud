package tech.skargen.recloud.controllers;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import tech.skargen.recloud.components.simulation.ASimulation;
import tech.skargen.recloud.components.simulation.ISimulation;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.Jobs.TaskScheduler;
import tech.skargen.recloud.controllers.Servers.VmScheduler;
import tech.skargen.recloud.controllers.interfaces.IExperiment;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSet;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.templates.SimulationResult;
import tech.skargen.skartools.SText;

/** Handles each simulation's running environemnt. */
public final class Experiment implements IRecloudSequence, IExperimentSequence, ISimulationSequence,
                                         IExperiment, IExperimentSet {
  private ObjectArrayList<ASimulation> simulationList;
  private IntArrayList taskTargetList;
  private int activeTasksTarget;

  private ASimulation activeSimulation;
  private SimulationResult activeSimulationResult;
  private int activeSimulationsIndex;

  private boolean cloudsimTraceFlag;
  private int cloudsimUsers;
  private String signiture;

  private static Logger _LOG;

  static {
    _LOG = LogManager.getLogger();
  }

  /**Constructor.*/
  public Experiment() {
    this.simulationList = new ObjectArrayList<>();
    this.taskTargetList = new IntArrayList();
    this.signiture = "Researchers Cloud";

    this.cloudsimUsers = 1;
    this.cloudsimTraceFlag = false;
  }

  /**
   * Notify experiments of recieving a cloudlet.
   *
   * @param task Cloudlet to be added to list only if it's valid.
   */
  @Override
  public void updateRecievedCloudlets(Cloudlet task) {
    this.activeSimulationResult.recievedCloudlets.add(task);
  }

  /**
   * Calculate how long the simulation algorithm took in milliseconds and pass the
   * final result here to be accumulated with all entities using the active
   * algorithm. It's preferable if java.time.Insant.now() and
   * java.time.Duration.between() is used for the calculation.
   *
   * @param duration Duration simulation algorithm took.
   */
  public void updateSimulationDuration(long duration) {
    this.activeSimulationResult.simulationDuration += duration;
  }

  @Override
  public int getTasksTarget() {
    return this.activeTasksTarget;
  }

  @Override
  public String getSigniture() {
    return this.signiture;
  }

  /**
   * List of all the simulations to be conducted.
   *
   * @return List of simulations.
   */
  @Override
  public ObjectArrayList<ASimulation> getSimulationList() {
    return this.simulationList;
  }

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   *
   * @param sims Array of simulation algorithms.
   */
  @Override
  public void newSimulations(ASimulation... sims) {
    this.newSimulations(VmScheduler.TimeShared, TaskScheduler.TimeShared, sims);
  }

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   *
   * @param vmscheduler Simulation scheduler to be assigned to virtual machines
   *                    and hosts.
   * @param sims        Array of simulation algorithms.
   */
  @Override
  public void newSimulations(VmScheduler vmscheduler, ASimulation... sims) {
    this.newSimulations(vmscheduler, TaskScheduler.TimeShared, sims);
  }

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   *
   * @param taskscheduler Simulation scheduler to be assigned to tasks.
   * @param sims          Array of simulation algorithms.
   */
  @Override
  public void newSimulations(TaskScheduler taskscheduler, ASimulation... sims) {
    this.newSimulations(VmScheduler.TimeShared, taskscheduler, sims);
  }

  /**
   * Add a simulation algorithm to be run by cloudsim, this part is where all the
   * simulations are fetched from.
   *
   * @param vmscheduler   The simulation scheduler to be assigned to virtual
   *                      machines and hosts.
   * @param taskScheduler The simulation scheduler to be assigned to tasks.
   */
  @Override
  public void newSimulations(
      VmScheduler vmscheduler, TaskScheduler taskScheduler, ASimulation... sims) {
    for (int i = 0; i < sims.length; i++) {
      sims[i].schedulers(vmscheduler, taskScheduler);
      this.simulationList.add(sims[i]);
    }
  }

  /**
   * Collection of numbers to be used as 'number of tasks' for all simulations.
   */
  @Override
  public IExperimentSet taskTargets(int... numbers) {
    for (int i = 0; i < numbers.length; i++) {
      if (numbers[i] > 0) {
        this.taskTargetList.add(numbers[i]);
      }
    }
    return this;
  }

  /**
   * Set cloudsim init state fields and whether to show logs.
   *
   * @param users     Users of the cloud
   * @param traceFlag Trace flag.
   * @param logs      Enable/disable cloudsim's logs.
   */
  @Override
  public IExperimentSet cloudsim(int users, boolean traceFlag) {
    this.cloudsimUsers = users;
    this.cloudsimTraceFlag = traceFlag;
    return this;
  }

  /**
   * Experiments are marked by a signiture to trace their conductor and to add on
   * to their credebility.
   * @param signiture Signiture text.
   */
  @Override
  public IExperimentSet signiture(String signiture) {
    this.signiture = signiture;
    return this;
  }

  /**
   * Confirm or deny the existence of next task target in list.
   * @return True if experiment for all tasks targets has finished.
   */
  @Override
  public boolean allSequencesDone() {
    return this.taskTargetList.isEmpty();
  }

  /**
   * Confirm or deny the end of curent sequence, that is all simulations in list
   * have gone over the current active tasks target.
   *
   * @return True if all simulations for current tasks targets has finished.
   */
  public boolean allSimulationsDone() {
    return this.activeSimulationsIndex >= this.simulationList.size();
  }

  /**
   * Retrieve Active simulation algorithm.
   *
   * @return Simulation algorithm interface.
   */
  @Override
  public ISimulation getSimulation() {
    return this.activeSimulation;
  }

  /**
   * Calculate simulation results using this algorithm's logic, override it to
   * calculate results your own way.
   *
   * @return Simulation results.
   */
  @Override
  public SimulationResult getSimulationResults() {
    return this.activeSimulationResult;
  }

  /**
   * Validates simulations added to the list.
   */
  @Override
  public void validate() throws Exception {
    if (this.simulationList.size() <= 0) {
      throw new Exception("no simulation algorithms created");
    }
  }

  @Override
  public void init(IRecloud recloud) {
    if (this.taskTargetList.size() <= 0) {
      this.taskTargetList.add(300);
      _LOG.info(
          "no task target has been set for the experiment, adding default{300} value to list");
    }
  }

  @Override
  public void newSequence(IRecloud recloud) {
    if (!this.allSequencesDone()) {
      this.activeTasksTarget = this.taskTargetList.removeInt(0);
      this.activeSimulationsIndex = 0;

      SText stext = SText.getInstance();
      StringBuilder sb = stext.wrap(0, 50, '|', '_', ':', '|', "New Experiment", "Sequence For ",
          this.activeTasksTarget, " Tasks");
      stext.sequenceWrap(0, '-', '+', sb);

      Log.printLine();
      Log.printLine();
      Log.printLine(sb);
      Log.printLine();
    }
  }

  @Override
  public void beforeSimulation(IRecloud recloud) {
    if (!this.allSimulationsDone()) {
      this.activeSimulation = this.simulationList.get(this.activeSimulationsIndex);
      this.activeSimulationResult = new SimulationResult();

      ISimulation simulation = recloud.getExperiment().getSimulation();
      SText stext = SText.getInstance();
      StringBuilder sb = stext.wrap(0, 50, '|', '_', '\0', '|', "New Simulation", '(',
          "Running Algorithm:", simulation.getAlgorithmName() + " ID:" + simulation.getID(), ')');
      stext.sequenceWrap(0, '-', '+', sb);

      Log.printLine(sb);
      Log.printLine();
      Log.printLine();

      // Initialize the CloudSim library
      CloudSim.init(this.cloudsimUsers, Calendar.getInstance(), this.cloudsimTraceFlag);
    }
  }

  @Override
  public void afterSimulation(IRecloud recloud) {
    if (!this.allSimulationsDone()) {
      // List<SimEntity> e = CloudSim.getEntityList();
      // Start the simulation
      CloudSim.startSimulation();
      CloudSim.stopSimulation();

      this.activeSimulationsIndex++;

      SimulationResult sr = this.activeSimulationResult;
      if (!sr.recievedCloudlets.isEmpty()) {
        final int cloudletstotal = sr.recievedCloudlets.size();

        Comparator<Cloudlet> comp = new Comparator<Cloudlet>() {
          public int compare(Cloudlet a, Cloudlet b) {
            return a.getVmId() - b.getVmId();
          }
        };

        // Sort cloudlets by vm id
        Collections.sort(sr.recievedCloudlets, comp);

        int currentVm = sr.recievedCloudlets.get(0).getVmId();

        double vmMakespan = 0;

        // Calculating makespan
        DoubleArrayList vmsMakespanList = new DoubleArrayList();
        Cloudlet cloudlet = null;
        for (int c = 0; c < cloudletstotal; c++) {
          cloudlet = sr.recievedCloudlets.get(c);
          // System.out.println(cloudlet.getUserId());

          // Makespan
          if (sr.makespan < cloudlet.getFinishTime()) {
            sr.makespan = cloudlet.getFinishTime();
          }

          if (vmMakespan < cloudlet.getFinishTime()) {
            vmMakespan = cloudlet.getFinishTime();
          }

          if (currentVm != cloudlet.getVmId() || c == cloudletstotal - 1) {
            // System.out.print("User : " + cloudlet.getUserId() + " <-> Vm : " +
            // currentVm);
            currentVm = cloudlet.getVmId();
            // System.out.println(" || User : " + cloudlet.getUserId() + " <-> Vm : " +
            // currentVm);

            // Vms Makespan
            sr.vmsMakespan += vmMakespan;

            vmsMakespanList.add(vmMakespan);
            vmMakespan = 0;
          }
        }

        double minCT = Double.POSITIVE_INFINITY;
        double maxCT = 0.0;
        final double noOfVms = (double) vmsMakespanList.size();

        for (double ct : vmsMakespanList) {
          // Finding minimum and maximum completion time amongst all VMs
          if (minCT > ct) {
            minCT = ct;
          }

          if (maxCT < ct) {
            maxCT = ct;
          }

          // Accumulating each VM's value without square root
          sr.standardDeviation += Math.pow(ct - (sr.vmsMakespan / noOfVms), 2);
        }

        // Degree of imbalance
        sr.degreeOfImbalance = noOfVms * ((maxCT - minCT) / sr.vmsMakespan);

        // Standard Deviation
        sr.standardDeviation = Math.sqrt(sr.standardDeviation * (1 / noOfVms));
      } else {
        sr.makespan = -55;
        sr.vmsMakespan = -55;
        sr.standardDeviation = -55;
        sr.degreeOfImbalance = -55;
      }
    }
  }

  @Override
  public void endSequence(IRecloud recloud) {}

  @Override
  public void finish(IRecloud recloud) {}
}