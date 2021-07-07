package tech.skargen.recloud.developers.cypherskar.bullet;

import java.util.Comparator;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

/**
 * Bullet Search Algorithm by github.com/cypherskar
 */
public class Bullet_CS extends ASimulation {
  public enum GunType { Magnum, Bazooka }
  protected final GunType gunType;

  public Bullet_CS(GunType gunType) {
    this.gunType = gunType;
  }

  @Override
  public String getAlgorithmName() {
    return "Bullet";
  }

  @Override
  public String getAdditionalInfo() {
    return this.gunType.name();
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
    rebroker.updateProgressMessage("Bullet_" + this.gunType.name() + " By github.com/cypherskar");

    switch (this.gunType) {
      case Magnum:
        this.Magnum(cloudlets, vms, rebroker);
        break;

      case Bazooka:
      default:
        this.Bazooka(cloudlets, vms, rebroker);
        break;
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}

  /**
   * Presumes that all of the tasks are submitted to one grand (bazooka) virtual
   * machine and limits VMs' processing times accordinagly.
   * @param tasks List of cloudlets to schedule.
   * @param vms List of vms to schedule.
   */
  public void Bazooka(final List<Cloudlet> tasks, final List<Vm> vms, IReBroker rebroker) {
    double[] vmsPT = new double[vms.size()];
    int chosenVmIdx = 0;
    double taskPT = 0;

    double totalLengths = 0;
    for (Cloudlet task : tasks) {
      totalLengths += this.TaskStrength(task);
    }

    double totalMips = 0;
    for (int i = 0; i < vmsPT.length; i++) {
      Vm vm = vms.get(i);
      totalMips += this.VmStrength(vm);
    }

    double grandPT = totalLengths / totalMips;

    for (int j = 0, i = 0; j < tasks.size(); j++) {
      if (vmsPT[i] >= grandPT)
        i++;
      Cloudlet task = tasks.get(j);
      // chosenVmIdx = 0;
      taskPT = this.TaskRunTime(task, vms.get(chosenVmIdx));
      double vmTaskPT = this.TaskRunTime(task, vms.get(i));
      if (vmsPT[i] + vmTaskPT < vmsPT[chosenVmIdx] + taskPT) // && vmsPT[i] <= vmsPT[chosenVmIdx])
      {
        chosenVmIdx = i;
        taskPT = vmTaskPT;
      }

      vmsPT[chosenVmIdx] += taskPT;
      task.setVmId(vms.get(chosenVmIdx).getId());
      rebroker.submitCloudlet(task);
      rebroker.updateProgress(1);
    }

    vmsPT = null;
  }

  /**
   * Accurate mapping with minimum imbalance degree but requires sorting of tasks
   * descendantly first.
   * @param tasks Job (tasks) list to be scheduled.
   * @param vms   Virtual machines to handle scheduled tasks.
   */
  public void Magnum(final List<Cloudlet> tasks, final List<Vm> vms, IReBroker rebroker) {
    double[] vmsPT = new double[vms.size()];
    int chosenVmIdx = 0;
    double taskPT = 0;

    Comparator<Cloudlet> comp = new Comparator<Cloudlet>() {
      public int compare(Cloudlet a, Cloudlet b) {
        return (int) ((b.getCloudletLength() * b.getNumberOfPes())
            - (a.getCloudletLength() * a.getNumberOfPes()));
      }
    };
    tasks.sort(comp);

    for (int j = 0; j < tasks.size(); j++) {
      Cloudlet task = tasks.get(j);
      chosenVmIdx = 0;
      taskPT = this.TaskRunTime(task, vms.get(chosenVmIdx));
      for (int i = 0; i < vmsPT.length; i++) {
        if (i == chosenVmIdx)
          continue;

        double vmTaskPT = this.TaskRunTime(task, vms.get(i));
        if (vmsPT[i] + vmTaskPT < vmsPT[chosenVmIdx] + taskPT) // && vmsPT[i] <= vmsPT[chosenVmIdx])
        {
          chosenVmIdx = i;
          taskPT = vmTaskPT;
        }
      }

      vmsPT[chosenVmIdx] += taskPT;
      task.setVmId(vms.get(chosenVmIdx).getId());
      rebroker.submitCloudlet(task);
      rebroker.updateProgress(1);
    }

    vmsPT = null;
  }

  private double TaskRunTime(Cloudlet task, Vm vm) {
    return this.TaskStrength(task) / this.VmStrength(vm);
  }

  private long TaskStrength(Cloudlet task) {
    return task.getCloudletLength() * task.getNumberOfPes();
  }

  private double VmStrength(Vm vm) {
    return vm.getMips() * vm.getNumberOfPes();
  }

  /*
   * while(maxPT - minPT > 2) { totalPT = 0; for (int j = 0; j < tasks.size();
   * j++) { Cloudlet task = tasks.get(j); chosenVmIdx = tasksToVms[j]; for (int i
   * = 0; i < vmsPT.length; i++) { if (i == tasksToVms[j]) continue;
   *
   * if (vmsPT[i] < vmsPT[chosenVmIdx]) { chosenVmIdx = i; } }
   *
   * if (chosenVmIdx != tasksToVms[j]) { vmsPT[tasksToVms[j]] -=
   * this.TaskRunTime(task, vms.get(tasksToVms[j])); vmsPT[chosenVmIdx] +=
   * this.TaskRunTime(task, vms.get(chosenVmIdx));
   *
   * if (minPT > vmsPT[tasksToVms[j]]) minPT = vmsPT[tasksToVms[j]]; if (maxPT <
   * vmsPT[chosenVmIdx]) maxPT = vmsPT[chosenVmIdx]; }
   *
   *
   * tasksToVms[j] = chosenVmIdx; task.setVmId(vms.get(chosenVmIdx).getId()); } }
   */
}