package tech.skargen.recloud.developers.cypherskar.honeybee;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

public class HoneyBee_CS extends ASimulation {
  protected Int2ObjectArrayMap<AHost> hostsTracker;
  protected Int2ObjectArrayMap<AVirtualMachine> vmsTracker;
  protected Host activeHost;
  private double cpuThreshold;
  private int numBrokers;

  public HoneyBee_CS(double threshold) {
    this.hostsTracker = null;
    this.vmsTracker = null;
    this.activeHost = null;
    this.cpuThreshold = threshold;
    this.numBrokers = 0;
  }

  @Override
  public String getAlgorithmName() {
    return "Honey Bee";
  }

  @Override
  public String getAdditionalInfo() {
    return "Load Balancer";
  }

  @Override
  public String getDeveloper() {
    return "cypherskar";
  }

  @Override
  public void startEntity(IReBroker rebroker) {
    if (this.numBrokers == 0) {
      this.hostsTracker = new Int2ObjectArrayMap<>();
      this.vmsTracker = new Int2ObjectArrayMap<>();
    }
    this.numBrokers++;
  }

  @Override
  public void shutdownEntity(IReBroker rebroker) {
    this.numBrokers--;
    if (this.numBrokers <= 0) {
      this.hostsTracker = null;
      this.vmsTracker = null;
      this.activeHost = null;
    }
  }

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    rebroker.updateProgressMax(rebroker.getEntity().getCloudletList().size());
    rebroker.updateProgressMessage("Honey Bee By github.com/cypherskar");

    for (Vm vm : rebroker.getEntity().getVmsCreatedList()) {
      Host host = vm.getHost();
      if (!this.hostsTracker.containsKey(host.getId())) {
        this.hostsTracker.put(host.getId(), new AHost());
        if (this.activeHost == null) {
          this.activeHost = host;
        }
      }

      if (!this.vmsTracker.containsKey(vm.getId())) {
        this.vmsTracker.put(vm.getId(), new AVirtualMachine());
      }
    }

    this.checkQueue(rebroker);
  }

  public void checkQueue(IReBroker rebroker) {
    List<Cloudlet> cloudlets = rebroker.getEntity().getCloudletList();
    List<Vm> vms = rebroker.getEntity().getVmsCreatedList();
    int numTasks = cloudlets.size();

    for (int i = 0; i < numTasks; i++) {
      Cloudlet task = cloudlets.get(i);

      if (!this.allocateVmForTask(rebroker.getEntity().getId(), vms, task)) {
        return;
      }

      rebroker.submitCloudlet(task);
      rebroker.updateProgress(1);
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {
    Vm vm = rebroker.getEntity()
                .getVmsCreatedList()
                .stream()
                .filter(v -> v.getId() == task.getVmId())
                .findFirst()
                .get();

    this.deallocateVmForTask(vm, task);

    this.checkQueue(rebroker);
  }

  public void deallocateVmForTask(Vm vm, Cloudlet task) {
    // System.out.println("Task id: " + task.getCloudletId() + "Vm id: " + vm.getId());

    AVirtualMachine hbvm = this.vmsTracker.get(vm.getId());
    if (hbvm != null) {
      // Update vm information
      // hbvm.totalLengthOfTasks = task.getCloudletLength();
      hbvm.processingTime -= this.calculateTaskPT(vm, task.getCloudletLength());
      hbvm.requestCounts--;
      hbvm.isOverloaded = false;
    }

    AHost hbhost = this.hostsTracker.get(vm.getHost().getId());
    if (hbhost != null) {
      // Update host information
      hbhost.processingTime -= this.calculateTaskPT(vm.getHost(), task.getCloudletLength());
    }
  }

  public boolean allocateVmForTask(int brokerId, final List<Vm> vms, Cloudlet task) {
    // Search for available vms within current host
    Vm chosenVm = this.findVm(this.activeHost.getVmList(), brokerId);

    // Host is overloaded
    if (chosenVm == null) {
      // Find the minimum processing time host
      Host chosenHost = this.findHost(vms);

      // Search for available vms within this host
      chosenVm = this.findVm(chosenHost.getVmList(), brokerId);

      // Set host as current
      this.activeHost = chosenHost;
    }

    // Found a vm
    if (chosenVm != null) {
      AHost hbhost = this.hostsTracker.get(this.activeHost.getId());
      AVirtualMachine hbvm = this.vmsTracker.get(chosenVm.getId());

      // Update host information
      hbhost.processingTime += this.calculateTaskPT(this.activeHost, task.getCloudletLength());

      // Update vm information
      hbvm.processingTime += this.calculateTaskPT(chosenVm, task.getCloudletLength());
      hbvm.requestCounts++;

      // Assign task
      task.setVmId(chosenVm.getId());

      // System.out.println(
      //     "Task : " + task.getCloudletId() + " is assigned to Vm : " + chosenVm.getId());
      return true;
    } else {
      // All hosts overloaded; queue task
      // System.out.println("Task : " + task.getCloudletId() + " is queued");
      return false;
    }
  }

  protected Host findHost(List<Vm> vms) {
    int hostId = -1;
    double minPT = Integer.MAX_VALUE;
    for (Int2ObjectArrayMap.Entry<AHost> hbhost : this.hostsTracker.int2ObjectEntrySet()) {
      if (hbhost.getValue().processingTime < minPT) {
        minPT = hbhost.getValue().processingTime;
        hostId = hbhost.getIntKey();
      }
    }

    // Host found
    final int id = hostId;
    try {
      return vms.stream().map(x -> x.getHost()).filter(h -> h.getId() == id).findFirst().get();

    } catch (NoSuchElementException e) {
      e.printStackTrace();
      return null;
    }
  }

  protected Vm findVm(List<? extends Vm> hostVms, int brokerid) throws NoSuchElementException {
    // Calculate average vms processing time
    IntArrayList brokerVms = new IntArrayList();
    double avgVmsPT = 0;
    for (int i = 0, numVms = hostVms.size(); i < numVms; i++) {
      if (hostVms.get(i).getUserId() == brokerid) {
        AVirtualMachine hbvm = this.vmsTracker.get(hostVms.get(i).getId());

        avgVmsPT += hbvm.processingTime;

        brokerVms.add(i);
      }
    }

    avgVmsPT *= 1.0 / brokerVms.size();

    // Calculate Load Standard Deviation
    double standardDeviation = 0;
    for (int index : brokerVms) {
      AVirtualMachine hbvm = this.vmsTracker.get(hostVms.get(index).getId());
      standardDeviation += Math.pow(hbvm.processingTime - avgVmsPT, 2);
    }

    standardDeviation = Math.sqrt(standardDeviation * (1.0 / brokerVms.size()));

    int vmIdx = -1;
    if (1.0 / standardDeviation == Double.POSITIVE_INFINITY) {
      if (!brokerVms.isEmpty()) {
        vmIdx = brokerVms.getInt(0);
      }
    } else {
      int minCount = Integer.MAX_VALUE;

      for (int index : brokerVms) {
        AVirtualMachine hbvm = this.vmsTracker.get(hostVms.get(index).getId());

        // Vm is available or overloaded
        if (!hbvm.isOverloaded) {
          if ((hbvm.processingTime - avgVmsPT) / standardDeviation <= this.cpuThreshold) {
            if (hbvm.requestCounts < minCount) {
              minCount = hbvm.requestCounts;
              vmIdx = index;
            }
          } else {
            hbvm.isOverloaded = true;
          }
        }
      }
    }

    // Host found
    if (vmIdx != -1) {
      return hostVms.get(vmIdx);
    }

    // All vms are overloaded
    return null;
  }

  protected double calculateTaskPT(Host host, double taskLength) {
    return taskLength / (host.getNumberOfPes() * host.getTotalMips());
  }

  /*
   * public double CalculateTaskPT(HB_VirtualMachine vm, double taskLength) {
   * return taskLength / (vm.cores * vm.mips); }
   */

  public double calculateTaskPT(Vm vm, double taskLength) {
    return taskLength / (vm.getNumberOfPes() * vm.getMips());
  }
}
