package tech.skargen.recloud.developers.cypherskar.aco;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

// (12), base: (09) (13)
/**
 * Implementation based on papers:
 * <ol>
 * <li> "Load Balancing in Cloud Computing using Stochastic Hill Climbing-A Soft Computing Approach"
 * by Brototi Mondal, Kousik Dasgupta and Paramartha Dutta, in 2012.
 * <li> "Cloud Task Scheduling Based on Load Balancing Ant Colony Optimization" by Kun Li, Gaochao
 * Xu, Guangyu Zhao, Yushuang Dong, Dan Wang, in 2011.
 * </ol>
 */
public class FitnessFunction {
  protected int noOfTasks;
  protected int noOfVMs;
  protected List<double[]> executionTimeList;
  protected List<Double> computingCapacityList;
  protected boolean resourcesAreIdentical;

  /**
   * Contructor.
   *
   * @param cloudletList Tasks involved in the calculations.
   * @param vmList       Virtual machines list.
   */
  public FitnessFunction(List<? extends Cloudlet> cloudletList, List<? extends Vm> vmList) {
    this.noOfTasks = cloudletList.size();
    this.noOfVMs = vmList.size();
    this.executionTimeList = new ArrayList<double[]>();
    this.computingCapacityList = new ArrayList<Double>();

    this.calculateExecutionTimes(cloudletList, vmList);
    this.calculateComputingCapacity(vmList);
  }

  /**
   * Will calculate the execution time each cloudlet takes if it runs on one of
   * the VMs.
   */
  private void calculateExecutionTimes(
      List<? extends Cloudlet> cloudletList, List<? extends Vm> vmList) {
    for (int task = 0; task < noOfTasks; task++) {
      Cloudlet cloudlet = cloudletList.get(task);

      double[] arr = new double[noOfVMs];

      for (int i = 0; i < noOfVMs; i++) {
        Vm vm = vmList.get(i);

        // aco (01) - et + er
        if (vm.getHost() == null) {
          arr[i] = (cloudlet.getCloudletLength() / (vm.getNumberOfPes() * vm.getMips())
              + cloudlet.getCloudletFileSize() / vm.getBw());
        } else {
          arr[i] = (cloudlet.getCloudletLength()
                  / (vm.getNumberOfPes() * vm.getHost().getTotalAllocatedMipsForVm(vm))
              + cloudlet.getCloudletFileSize() / vm.getBw());
        }
      }

      this.executionTimeList.add(arr);
    }
  }

  private void calculateComputingCapacity(List<? extends Vm> vmList) {
    double cc = 0.0;
    double denomenator = 0.0;

    for (Vm vm : vmList) {
      cc = vm.getNumberOfPes() * vm.getMips() + vm.getBw();

      denomenator += cc;

      this.computingCapacityList.add(cc);
    }

    if (cc / denomenator * this.noOfVMs == 1) {
      this.resourcesAreIdentical = true;
    }
  }

  public List<double[]> getExecutionTime() {
    return this.executionTimeList;
  }

  public List<Double> getComputingCapacity() {
    return this.computingCapacityList;
  }

  public int getNoOfTasks() {
    return this.noOfTasks;
  }

  public boolean areVmsIdentical() {
    return this.resourcesAreIdentical;
  }

  public int getNoOfVMs() {
    return this.noOfVMs;
  }
}