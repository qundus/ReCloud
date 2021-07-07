package tech.skargen.recloud.developers.cypherskar.aco;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

/**
 * Implementation of Ant Colony Optimization by github.com/cypherskar.
 * Implementation based on papers/books:
 * <ol>
 * <li> "Cloud Task Scheduling Based on Load Balancing Ant Colony Optimization" by Kun Li, Gaochao
 * Xu, Guangyu Zhao, Yushuang Dong, Dan Wang, in 2011.
 * <li> "Ant Colony Optimization" by Marco Dorigo, Mauro Birattari and Thomas Stützle, in 2006.
 * <li> "Cloud Task Scheduling Based on Ant Colony Optimization" by Medhat Tawfeek, Ashraf El-Sisi,
 * Arabi Keshk and Fawzy Torkey.
 * </ol>
 */
public class ACO_CS extends ASimulation {
  /** Nmber of ants to be used. */
  protected int numAnts;
  /** Number of iterations. */
  protected int numIterations;
  /** Initial pheromone for each VM. */
  protected double initPheromone;
  /** Q or total amount of pheromone left on trail by each Ant. */
  protected double totalPheromone;
  /** Alpha or controls the pheromone importance. */
  protected double alpha;
  /** Beta or controls the distance priority; should be greater than alpha. */
  protected double beta;
  /**
   * rho or vapourization constant for pheromone trail in each iteration. (0.0 = no
   * evaporation, 1.0 = full evaporation)
   */
  protected double rho;

  /**
   * Create a Ant Colony Optimization simulation scheduler.
   * @param ants         No of ants to be used.
   * @param iterations   No of iterations.
   * @param initialPhero Initial pheromone for each VM.
   * @param alpha        Controls the pheromone importance
   * @param beta         Controls the distance priority.
   *                     Should be greater than alpha for the best
   *                     results.
   * @param q            Total amount of pheromone left on trail by each Ant.
   * @param rho          Vapourization constant for pheromone trail in each
   *                     iteration.
   */
  public ACO_CS(
      int ants, int iterations, double initialPhero, int alpha, int beta, int q, double rho) {
    this.numAnts = ants;
    this.numIterations = iterations;
    this.initPheromone = initialPhero;
    this.alpha = alpha;
    this.beta = beta;
    this.totalPheromone = q;
    this.rho = rho;
  }

  @Override
  public String getAlgorithmName() {
    return "ACO";
  }

  @Override
  public String getAdditionalInfo() {
    return "none";
  }

  @Override
  public String getDeveloper() {
    return "cypherskar";
  }

  @Override
  public void startEntity(IReBroker rebroker) {}

  @Override
  public void shutdownEntity(IReBroker rebroker) {}

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    List<Cloudlet> cloudlets = rebroker.getEntity().getCloudletList();
    List<Vm> vms = rebroker.getEntity().getVmsCreatedList();

    rebroker.updateProgressMax(this.numIterations);
    rebroker.updateProgressMessage("AntColonyOptimization By github.com/cypherskar  ");

    double[][] solution = this.findSolution(rebroker);

    Cloudlet task;
    int vmForTask = 0;
    for (int t = 0; t < solution.length; t++) {
      for (int v = 0; v < solution[t].length; v++) {
        // Looping probabilities for chosen vms
        if (solution[t][v] > solution[t][vmForTask]) {
          vmForTask = v;
        }
      }
      task = cloudlets.get(t);
      task.setVmId(vms.get(vmForTask).getId());
      rebroker.submitCloudlet(task);
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker broker, T task) {}

  /**
   * Finds best tasks to vms mapping matrix according to ACO logic.
   * @param rebroker Broker carrying cloudlets and vms.
   * @return Tasks to vms mapped indieces.
   */
  public double[][] findSolution(IReBroker rebroker) {
    FitnessFunction ff = new FitnessFunction(
        rebroker.getEntity().getCloudletList(), rebroker.getEntity().getVmsCreatedList());

    // initialize trail pheromones
    double[][] trails = new double[ff.getNoOfTasks()][ff.getNoOfVMs()];
    for (int task = 0; task < ff.getNoOfTasks(); task++) {
      for (int vm = 0; vm < ff.getNoOfVMs(); vm++) {
        // Choose the pheromone graph not the cost graph.
        // this.trails[task][vm] = this.GetEdgeDistance(task, vm);
        trails[task][vm] = this.initPheromone;
      }
    }

    int[] shuffledVms = IntStream.range(0, ff.getNoOfVMs()).toArray();
    Ant[] ants = new Ant[this.numAnts];
    Random random = new Random();
    // initialize ants
    for (int ant = 0; ant < this.numAnts; ant++) {
      if (ant % shuffledVms.length == 0) {
        this.shuffleArray(shuffledVms, random);
      }
      ants[ant] = new Ant(ff.getNoOfTasks(), shuffledVms[ant % shuffledVms.length]);
    }

    // find solution
    for (int iteration = 0; iteration < this.numIterations; iteration++) {
      // move and update ants
      for (int t = 0, v; t < ff.getNoOfTasks(); t++) {
        if (t != 0 && t % ff.getNoOfVMs() == 0) {
          this.shuffleArray(shuffledVms, random);
        }

        for (int a = 0; a < ants.length; a++) {
          v = calculateNextVmForTask(ants[a], t, trails, ff);
          ants[a].visitVm(v, t);
          ants[a].checkVisited(shuffledVms, ff.areVmsIdentical());
          ants[a].updateTrailLength(ff.getExecutionTime().get(t)[v]);
        }
      }

      this.updateTrails(ants, trails);
      rebroker.updateProgress(1);
    }

    // free memory
    ff = null;
    shuffledVms = null;
    ants = null;
    random = null;
    return trails;
  }

  /**
   * Update trails ants have used.
   */
  private void updateTrails(Ant[] ants, double[][] trails) {
    Ant shortestTrailAnt = ants[0].clone();
    double contribution = 0.0;
    int[] trail;

    // Apply evaporation rate on every trail
    for (int task = 0; task < trails.length; task++) {
      for (int vm = 0; vm < trails[task].length; vm++) {
        trails[task][vm] *= this.rho;
      }
    }

    // Increase the taken trails by ants pheromones contributions
    for (Ant ant : ants) {
      contribution = this.totalPheromone / ant.getTrailLength();

      trail = ant.getTrail();

      for (int task = 0; task < trail.length; task++) {
        trails[task][trail[task]] += contribution;
      }

      if (ant.getTrailLength() < shortestTrailAnt.getTrailLength()) {
        shortestTrailAnt = ant.clone();
      }

      ant.clearTrailLength();
    }

    // Reinforce shortest trail ant's path
    contribution = this.totalPheromone / shortestTrailAnt.getTrailLength();
    trail = shortestTrailAnt.getTrail();
    for (int task = 0; task < trail.length; task++) {
      trails[task][trail[task]] += contribution;
    }
  }

  /** Select next city for each ant. */
  private int calculateNextVmForTask(Ant ant, int task, double[][] trails, FitnessFunction ff) {
    double maxVmPheromone = Double.NEGATIVE_INFINITY;
    double minVmPheromone = Double.POSITIVE_INFINITY;
    int maxVm = 0;

    int minVm = 0;
    for (int vm = 0; vm < ff.getNoOfVMs(); vm++) {
      if (!ant.visited(vm)) {
        double hueristicValue = Math.pow(1 / ff.getExecutionTime().get(task)[vm], this.beta);
        double edgePheromoneValue = Math.pow(trails[task][vm], this.alpha);

        hueristicValue *= edgePheromoneValue;

        if (minVmPheromone > hueristicValue) {
          minVmPheromone = hueristicValue;
          minVm = vm;
        }

        if (maxVmPheromone < hueristicValue) {
          maxVmPheromone = hueristicValue;
          maxVm = vm;
        }
      }
    }

    if (ff.areVmsIdentical()) {
      return minVm;
    }

    return maxVm;
  }

  /** Implementing Fisher–Yates shuffle. */
  public void shuffleArray(int[] ar, Random rand) {
    // If running on Java 6 or older, use `new Random()` on RHS here
    for (int i = ar.length - 1; i > 0; i--) {
      int index = rand.nextInt(i + 1);
      // Simple swap
      int a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    }
  }
}