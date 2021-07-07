package tech.skargen.recloud.developers.cypherskar.minmin;

import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

/** Min Min implemenation by github.com/cypherskar. */
public class MM_CS extends ASimulation {
  public enum Variation { MinMin, MaxMin }

  protected final Variation type;

  /** Create a MinMin simulation scheduling algorithm. */
  public MM_CS(Variation type) {
    this.type = type;
  }

  @Override
  public String getAlgorithmName() {
    return this.type.name();
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

    rebroker.updateProgressMax(cloudlets.size());
    rebroker.updateProgressMessage(this.getAlgorithmName() + " By github.com/cypherskar");

    switch (this.type) {
      case MaxMin:
        this.maxmin(cloudlets, vms, rebroker);
        break;
      case MinMin:
      default:
        this.minmin(cloudlets, vms, rebroker);
        break;
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}

  /**
   * Min-Min scheduling algorithm
   *
   * @param tasks Cloudlets to schedule.
   * @param vms Virtual machines to schedule.
   */
  public void minmin(final List<Cloudlet> tasks, final List<Vm> vms, IReBroker rebroker) {
    int numTasks = tasks.size();
    int numVMs = vms.size();

    // compute ready time for vms; they all start with zero
    double[] readyTimes = new double[vms.size()];

    // execution time matrix
    double[][] executionTimes = new double[numTasks][numVMs];

    // completion time matrix
    double[][] completionTimes = new double[numTasks][numVMs];

    // compute completion time
    for (int i = 0; i < numTasks; i++) {
      Cloudlet task = tasks.get(i);

      // looping all vms for this task
      for (int j = 0; j < numVMs; j++) {
        Vm vm = vms.get(j);

        executionTimes[i][j] = this.GetExecutionTime(task, vm);

        completionTimes[i][j] = executionTimes[i][j] + readyTimes[j];
      }
    }

    // loop all tasks
    int allTasksMapped = 0;
    int minCTTask = 0;
    int minCTTaskVm = 0;

    for (int i = 0; i < numTasks; i++) {
      Cloudlet task = tasks.get(i);
      if (task.getVmId() <= -1) // if task is not assigned
      {
        for (int j = 0; j < numVMs; j++) {
          // choosing the task with MINIMUM completion time and finding minimum vm
          // completion time
          if (completionTimes[i][j] < completionTimes[minCTTask][minCTTaskVm]) {
            minCTTask = i;
            minCTTaskVm = j;
          }
        }
      }

      if (i + 1 >= numTasks) {
        // assign task with minimum completion time
        tasks.get(minCTTask).setVmId(vms.get(minCTTaskVm).getId());
        rebroker.submitCloudlet(tasks.get(minCTTask));
        rebroker.updateProgress(1);

        // update vm completion time
        readyTimes[minCTTaskVm] += executionTimes[minCTTask][minCTTaskVm];

        // remove task
        executionTimes[minCTTask] = null;
        completionTimes[minCTTask] = null;

        // update completion time of unmapped tasks
        minCTTask = -1;
        for (i = 0; i < numTasks; i++) {
          if (completionTimes[i] != null) {
            completionTimes[i][minCTTaskVm] =
                executionTimes[i][minCTTaskVm] + readyTimes[minCTTaskVm];

            // to avoid null pointers
            if (minCTTask == -1)
              minCTTask = i;
          }
        }

        // repeat loop
        i = -1;
        allTasksMapped++;
      }

      if (allTasksMapped >= numTasks)
        break;
    }
  }

  /**
   * Max-Min scheduling algorithm
   *
   * @param tasks Cloudlets to schedule.
   * @param vms Virtual machines to schedule.
   */
  public void maxmin(final List<Cloudlet> tasks, final List<Vm> vms, IReBroker rebroker) {
    int numTasks = tasks.size();
    int numVMs = vms.size();

    // compute ready time for vms; they all start with zero
    double[] readyTimes = new double[vms.size()];

    // execution time matrix
    double[][] executionTimes = new double[numTasks][numVMs];

    // completion time matrix
    double[][] completionTimes = new double[numTasks][numVMs];

    // compute completion time
    for (int i = 0; i < numTasks; i++) {
      Cloudlet task = tasks.get(i);

      // looping all vms for this task
      for (int j = 0; j < numVMs; j++) {
        Vm vm = vms.get(j);

        executionTimes[i][j] = this.GetExecutionTime(task, vm);

        completionTimes[i][j] = executionTimes[i][j] + readyTimes[j];
      }
    }

    // loop all tasks
    int allTasksMapped = 0;
    int maxCTTask = 0;
    // actually maximum vm CT,
    // it'll be changed to minimum CT once the maximum completion time task is found
    int minCTTaskVm = 0;

    for (int i = 0; i < numTasks; i++) {
      Cloudlet task = tasks.get(i);
      // if task is not assigned
      if (task.getVmId() <= -1) {
        for (int j = 0; j < numVMs; j++) {
          // choosing the task with MINIMUM completion time and finding minimum vm
          // completion time
          if (completionTimes[i][j] > completionTimes[maxCTTask][minCTTaskVm]) {
            maxCTTask = i;
            minCTTaskVm = j;
          }
        }
      }

      if (i + 1 >= numTasks) {
        // find minimum vm completion time
        // for (int j = 0; j < executionTimes[maxCTTask].length; j++)
        for (int j = 0; j < numVMs; j++) {
          // if (executionTimes[maxCTTask][j] + readyTimes[j] <
          // executionTimes[maxCTTask][minCTTaskVm] + vmsReadyTime[minCTTaskVm])
          if (completionTimes[maxCTTask][j] < completionTimes[maxCTTask][minCTTaskVm]) {
            minCTTaskVm = j;
          }
        }

        // assign task with minimum completion time
        tasks.get(maxCTTask).setVmId(vms.get(minCTTaskVm).getId());
        rebroker.submitCloudlet(tasks.get(maxCTTask));
        rebroker.updateProgress(1);

        // update vm completion time
        readyTimes[minCTTaskVm] += executionTimes[maxCTTask][minCTTaskVm];

        // remove task
        executionTimes[maxCTTask] = null;
        completionTimes[maxCTTask] = null;

        // update completion time of unmapped tasks
        maxCTTask = -1;
        for (i = 0; i < numTasks; i++) {
          if (completionTimes[i] != null) {
            completionTimes[i][minCTTaskVm] =
                executionTimes[i][minCTTaskVm] + readyTimes[minCTTaskVm];

            // to avoid null pointers
            if (maxCTTask == -1)
              maxCTTask = i;
          }
        }

        // repeat loop
        i = -1;
        allTasksMapped++;
      }

      if (allTasksMapped >= numTasks)
        break;
    }
  }

  public double[][] GetExecutionTimeMatrix(List<? extends Cloudlet> tasks, List<? extends Vm> vms) {
    // better list than a 2D array cause each task will be removed later
    int numTasks = tasks.size();
    int numVMs = vms.size();
    double[][] result = new double[numTasks][numVMs];

    // looping all tasks
    for (int i = 0; i < numTasks; i++) {
      Cloudlet task = tasks.get(i);

      // looping all vms for this task
      for (int j = 0; j < numVMs; j++) {
        Vm vm = vms.get(j);

        result[i][j] = this.GetExecutionTime(task, vm);
      }
    }

    return result;
  }

  public double GetExecutionTime(Cloudlet task, Vm vm) {
    if (vm.getHost() == null)
      return task.getCloudletLength() / vm.getMips();

    return task.getCloudletLength() / vm.getHost().getTotalAllocatedMipsForVm(vm);
  }

  public double GetCompletionTime(double taskET, double vmRT) {
    // task execution time + vm ready time
    return taskET + vmRT;
  }
}