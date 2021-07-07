package tech.skargen.recloud.developers.cypherskar.pso;

import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**
 * Implementation based on papers:
 * <ol>
 * <li> "Load Balancing in Cloud Computing using Stochastic Hill Climbing-A Soft Computing Approach"
 * by Brototi Mondal, Kousik Dasgupta and Paramartha Dutta, in 2012.
 * </ol>
 */
public class FitnessFunction {
  protected double[][] runTimes;

  public FitnessFunction(List<? extends Cloudlet> tasks, List<? extends Vm> vms) {
    this.runTimes = this.calculateRunTimeMatrix(tasks, vms);
  }

  public double[][] getRunTimeMatrix() {
    return this.runTimes;
  }

  public double[][] calculateRunTimeMatrix(List<? extends Cloudlet> tasks, List<? extends Vm> vms) {
    double[][] result = new double[vms.size()][tasks.size()];

    for (int i = 0; i < vms.size(); i++) {
      Vm vm = vms.get(i);

      for (int j = 0; j < tasks.size(); j++) {
        Cloudlet task = tasks.get(j);

        if (vm.getHost() == null)
          result[i][j] = task.getCloudletLength() / vm.getMips();
        else
          result[i][j] = task.getCloudletLength() / vm.getHost().getTotalAllocatedMipsForVm(vm);
      }
    }

    return result;
  }

  /**
   * Calculate fitness value of the current particle's solution.
   * @param positions Positions matrix found by current particle.
   * @return Fitness as a double value.
   */
  public double calculate(int[][] positions) {
    return this.calculate(positions, this.runTimes);
  }

  /**
   * Calculate fitness value of the current particle's solution.
   * @param positions Positions matrix found by current particle.
   * @param runTimes Execution times of all cloudlets on all VMs.
   * @return Fitness as a double value.
   */
  public double calculate(int[][] positions, double[][] runTimes) {
    double[] completionTime = new double[runTimes.length];
    int resultIdx = 0;

    for (int i = 0; i < runTimes.length; i++) {
      for (int j = 0; j < runTimes[i].length; j++) {
        // Get computing time of particle on each vm
        completionTime[i] += runTimes[i][j] * positions[i][j]; // No need to multiply
      }

      // will find the highest execution time among all VMs
      if (completionTime[resultIdx] < completionTime[i]) {
        resultIdx = i;
      }
    }

    return completionTime[resultIdx];
  }

  /**
   * Calculate fitness value of a range in the current particle's solution.
   * @param rangeStart Starting pivot for calculations.
   * @param rangeEnd Ending pivot for calculations.
   * @param positions Positions matrix found by current particle.
   * @param runTimes Execution times of all cloudlets on all VMs.
   * @return Fitness as a double value.
   */
  public double calculateRange(int rangeStart, int rangeEnd, int[][] positions) {
    return this.calculateRange(rangeStart, rangeEnd, positions, this.runTimes);
  }

  /**
   * Calculate fitness value of a range in the current particle's solution.
   * @param rangeStart Starting pivot for calculations.
   * @param rangeEnd Ending pivot for calculations.
   * @param positions Positions matrix found by current particle.
   * @param runTimes Execution times of all cloudlets on all VMs.
   * @return Fitness as a double value.
   */
  public double calculateRange(
      int rangeStart, int rangeEnd, int[][] positions, double[][] runTimes) {
    double[] completionTime = new double[runTimes.length];
    double result = 0;

    for (int i = 0; i < runTimes.length; i++) {
      for (int j = rangeStart; j < rangeEnd; j++) {
        // Get computing time of particle on each vm
        if (positions[i][j] == 1) {
          // completionTime[i] += this.tasksRunTime[i][j] * positions[i][j]; // No need to
          // multiply
          completionTime[i] += runTimes[i][j];
        }
      }

      // will find the highest execution time among all VMs
      if (result < completionTime[i]) {
        result = completionTime[i];
      }
    }

    return result;
  }

  /*
   *
   * if (VM.getHost() == null) arr[task] =
   * (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getMips()) +
   * cloudlet.getCloudletFileSize()/VM.getBw()); else arr[task] =
   * (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getHost().
   * getTotalAllocatedMipsForVm(VM)) + cloudlet.getCloudletFileSize()/VM.getBw());
   */
}