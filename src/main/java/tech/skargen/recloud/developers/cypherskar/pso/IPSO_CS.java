package tech.skargen.recloud.developers.cypherskar.pso;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;

/**
 * Improved Particle Swarm Optimization implementation by github.com/cypherskar.
 * Implementatiun based on papers:
 * <ol>
 * <li> "Particle Swarm Optimization: Developments, Applications and Resources" by Russell C.
 * Eberhart and Yuhui Shi, in 2001.
 * <li> "Cloudlet Scheduling with Particle Swarm Optimization" by Hussein S. Al-Olimat, Mansoor
 * Alam, Robert Green and Jong Kwan Lee, in 2015.
 * <li> "A hybrid particle swarm optimization and hill climbing algorithm for task scheduling in the
 * cloud environments" by Negar Dordaie, Nima Jafari Navimipour, in 2017.
 * </ol>
 */
public class IPSO_CS extends PSO_CS {
  @SuppressWarnings("unused") private int noOfBatches;

  protected int rangeStart;
  protected int rangeEnd;

  /**
   * Create a Particle Swarm Optimization simulation scheduling algorithm.
   * @param p Number of particles.
   * @param iter Number of iterations.
   * @param min Inertia min.
   * @param max Inertia max.
   * @param c1 Cognitive/local coefficient.
   * @param c2 Social/global coefficient.
   * @param k Contriction factor
   * @param m Method used to update inertia.
   * @param pos Method used to update particle position.
   */
  public IPSO_CS(int p, int iter, double min, double max, double c1, double c2, int k, Inertia m,
      Position pos) {
    super(p, iter, min, max, c1, c2, k, m, pos);
  }

  @Override
  public String getAlgorithmName() {
    return "Improved PSO";
  }

  @Override
  public void startEntity(IReBroker rebroker) {
    super.startEntity(rebroker);
  }

  @Override
  public void shutdownEntity(IReBroker rebroker) {
    super.shutdownEntity(rebroker);
  }

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    List<Cloudlet> cloudlets = rebroker.getEntity().getCloudletList();
    List<Vm> vms = rebroker.getEntity().getVmsCreatedList();

    rebroker.updateProgressMax((int) this.numIterations);
    rebroker.updateProgressMessage("Improved Particle Swarm Optimization By github.com/cypherskar");

    int[] result = this.findSolution(cloudlets, vms, rebroker);

    for (int i = 0; i < result.length; i++) {
      cloudlets.get(i).setVmId(vms.get(result[i]).getId());
      rebroker.submitCloudlet(cloudlets.get(i));
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}

  // Calculate batch limit according to IPSO paper.
  // mu : resource utilization indicator.
  public double CalculateBatchLimit(List<? extends Vm> vms) {
    int noOfVMs = vms.size();
    double batchLimit = 0;

    for (int vm = 0; vm < noOfVMs; vm++) {
      Vm VM = vms.get(vm);

      if (VM.getHost() == null)
        batchLimit += VM.getMips();
      else
        batchLimit += VM.getHost().getTotalAllocatedMipsForVm(VM);
    }

    return batchLimit / noOfVMs;
  }

  /**
   * Use PSO attributes to find the vms suitable for each task/cloudlet alongside some improved PSO
   * operations to refine results.
   * @param cloudlets List of cloudlets.
   * @param vms List of vms.
   * @param rebroker Current Broker.
   * @return 1D array of mapping cloudlets to vms.
   */
  @Override
  public int[] findSolution(List<Cloudlet> cloudlets, List<Vm> vms, IReBroker rebroker) {
    FitnessFunction ff = new FitnessFunction(cloudlets, vms);
    Particle[] particles =
        this.createParticles(cloudlets.size(), vms.size(), this.numParticles, ff);
    Random rand = new Random();

    Particle bestParticle = particles[0];
    for (int iteration = 0; iteration < this.numIterations; iteration++) {
      for (int p = 0; p < particles.length; p++) {
        this.updateParticle(particles[p], bestParticle, rand, iteration);

        this.updateParticleBest(particles[p], ff);

        bestParticle = this.getSwarmBest(particles[p], bestParticle);
      }
      rebroker.updateProgress(1);
    }

    int[] cloudletsToVmMap = this.getCloudletsToVMsPositions(bestParticle);
    cloudletsToVmMap = this.rebalanceFinalSolution(
        cloudletsToVmMap, ff.getRunTimeMatrix(), cloudlets.size(), vms.size());
    ff = null;
    particles = null;
    bestParticle = null;

    return cloudletsToVmMap;
    //   final double batchLimit = this.CalculateBatchLimit(this.vms);
    //   double batchLength = 0;
    //   int[] result = new int[tasks.size()];
    //   int q = 0;
    //   double tasksSize = this.tasks.size();

    //   for (this.rangeStart = 0, this.rangeEnd = 0; this.rangeStart < tasks.size();
    //   this.rangeEnd++) {
    //     if (this.rangeEnd < tasks.size()) //&& this.noOfBatches >
    //  0) {
    //    batchLength += tasks.get(this.rangeEnd).getCloudletLength();

    //    if (batchLength <= batchLimit)
    //      continue;
    //  }
    //     else {
    //       this.rangeEnd = this.tasks.size();
    //     }

    //     this.FindPSOSolution(tasksSize);

    //     this.AppendSolution(result);

    //     this.RebalanceFinalSolution(result);

    //     batchLength = 0;
    //     this.noOfBatches--;
    //     this.rangeStart = this.rangeEnd;
    //   }

    //   this.InitializeParticles();

    //   int[] result = new int[tasks.size()];

    //   this.FindPSOSolution(this.tasks.size());

    //   // this.RebalanceFinalSolution();

    //   // this.AppendSolution(result);
    //   result = this.GetCloudletsToVMsPositions();

    //   this.RebalanceFinalSolution(result);
  }
  protected void appendSolution(int[] result, Particle bestp, List<Vm> vms) {
    int numVms = vms.size();
    for (int i = 0; i < numVms; i++) {
      for (int j = this.rangeStart; j < this.rangeEnd; j++) {
        if (bestp.bestPositions[i][j] == 1) {
          result[j] = i;
        }
      }
    }
  }

  @Override
  public int[] getCloudletsToVMsPositions(Particle p) {
    int[][] positions = p.bestPositions;
    final int noOfTasks = positions[0].length;
    final int noOfVMs = positions.length;

    int[] result = new int[noOfTasks];

    for (int i = 0; i < noOfVMs; i++) {
      for (int j = 0; j < noOfTasks; j++) {
        if (positions[i][j] == 1) {
          result[j] = i;
        }
      }
    }

    return result;
  }

  /*
   * private void RebalanceFinalSolution() { int noOfVms = this.vms.size(); int
   * noOfTasks = this.tasks.size(); int[][] bestPos =
   * this.particles[this.bestParticle].GetBestPositions();
   *
   *
   * // Completion time array List<Double> completionTimes = new ArrayList<>();
   *
   * for (int i = 0; i < noOfVms; i++) { double ct = 0.0; for (int j = 0; j <
   * noOfTasks; j++) { if (bestPos[i][j] == 1) { ct += this.runTimes[i][j]; } }
   * completionTimes.add(ct); }
   *
   * while(completionTimes.size() > 1) { int heaviestVMIdx = 0;
   *
   * // Finding heaviest loaded machines. for(int vmIdx = 1; vmIdx <
   * completionTimes.size(); vmIdx++) { if(completionTimes.get(heaviestVMIdx) <
   * completionTimes.get(vmIdx)) { heaviestVMIdx = vmIdx; } }
   *
   * // Looking for tasks in heavy loaded vm. for(int j = 0; j < noOfTasks; j++) {
   * if (bestPos[heaviestVMIdx][j] == 1) { int lightestVMIdx = 0;
   *
   * // Finding lightest loaded machines. for(int vmIdx = 0; vmIdx <
   * completionTimes.size(); vmIdx++) { if(completionTimes.get(lightestVMIdx) >
   * completionTimes.get(vmIdx)) { lightestVMIdx = vmIdx; } }
   *
   * double heaviestVMAfterTaskMoved = completionTimes.get(heaviestVMIdx) -
   * 1;//this.runTimes[heaviestVMIdx][j]; double lightestVMAfterTaskMoved =
   * completionTimes.get(lightestVMIdx) + 1;//this.runTimes[heaviestVMIdx][j];
   *
   *
   *
   * // Does swapping improve makespan? if not look for next task under heaviest
   * vm if (heaviestVMAfterTaskMoved < lightestVMAfterTaskMoved) { // Swapping
   * task machines. bestPos[heaviestVMIdx][j] = 0; bestPos[lightestVMIdx][j] = 1;
   *
   * completionTimes.set(heaviestVMIdx, heaviestVMAfterTaskMoved);
   * completionTimes.set(lightestVMIdx, lightestVMAfterTaskMoved); } } }
   *
   * completionTimes.remove(heaviestVMIdx); } }
   */

  // This function will re-balance the solution found by PSO for better solutions
  public int[] rebalanceFinalSolution(int[] result, double[][] runTimes, int numTasks, int numVms) {
    // Completion time array
    HashSet<Integer> checkedResources = new HashSet<>();
    double[] completionTimes = new double[numVms];
    for (int j = 0; j < result.length; j++) {
      completionTimes[result[j]] += runTimes[result[j]][j];
    }

    while (checkedResources.size() < numVms - 2) {
      int heaviestVmIdx = -1;

      // Finding heaviest loaded machines.
      for (int vmIdx = 0; vmIdx < completionTimes.length; vmIdx++) {
        if (checkedResources.contains(vmIdx))
          continue;
        else if (heaviestVmIdx == -1)
          heaviestVmIdx = vmIdx;

        if (completionTimes[heaviestVmIdx] < completionTimes[vmIdx]) {
          heaviestVmIdx = vmIdx;
        }
      }

      // Swapping task machines.
      /*
       * for(int j = 0; j < result.length; j++) { if (result[j] == heaviestVMIdx) {
       * result[j] = lightestVMIdx;
       *
       * completionTimes[heaviestVMIdx] -= this.runTimes[heaviestVMIdx][j];
       * completionTimes[lightestVMIdx] += this.runTimes[lightestVMIdx][j];
       *
       * if(completionTimes[heaviestVMIdx] <= completionTimes[lightestVMIdx]) break; }
       * }
       */

      // Looking for tasks in heavy loaded vm.
      for (int j = 0; j < numTasks; j++) {
        if (result[j] == heaviestVmIdx) {
          int lightestVmIdx = 0;

          // Finding lightest loaded machines.
          for (int vmIdx = 0; vmIdx < completionTimes.length; vmIdx++) {
            if (checkedResources.contains(vmIdx))
              continue;

            if (completionTimes[lightestVmIdx] > completionTimes[vmIdx] && vmIdx != heaviestVmIdx) {
              lightestVmIdx = vmIdx;
            }
          }

          double heaviestVmAfterTaskMoved =
              completionTimes[heaviestVmIdx] - runTimes[heaviestVmIdx][j];
          double lightestVmAfterTaskMoved =
              completionTimes[lightestVmIdx] + runTimes[lightestVmIdx][j];
          // Swapping task machines.
          result[j] = lightestVmIdx;

          completionTimes[heaviestVmIdx] = heaviestVmAfterTaskMoved;
          completionTimes[lightestVmIdx] = lightestVmAfterTaskMoved;

          // Does swapping improve makespan? if not look for next task under heaviest vm
          if (heaviestVmAfterTaskMoved < lightestVmAfterTaskMoved) {
            break;
          }
        }
      }

      // noOfVms--;
      checkedResources.add(heaviestVmIdx);
    }
    return result;
  }
}