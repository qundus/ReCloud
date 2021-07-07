package tech.skargen.recloud.components.cloudsim;

import java.time.Duration;
import java.time.Instant;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import tech.skargen.recloud.components.gui.interfaces.IProgress;
import tech.skargen.recloud.controllers.interfaces.IExperiment;
import tech.skargen.recloud.controllers.interfaces.IRecloud;

public class ReBroker extends DatacenterBroker implements IReBroker {
  protected final IProgress progress;
  protected final IExperiment experiment;
  private long simulationMethodsDuration;
  private int nextVmIndex;

  /**
   * Created a new DatacenterBroker object.
   *
   * @param name    name to be associated with this entity (as required by
   *                Sim_entity class from simjava package)
   * @param refAlgo Reference to simulation algorithm.
   */
  public ReBroker(String name, IRecloud recloud) throws Exception {
    super(name);
    this.progress = recloud.getWindow().getProgress();
    this.experiment = recloud.getExperiment();
    this.simulationMethodsDuration = 0;
    this.nextVmIndex = 0;
  }

  @Override
  public void updateProgress(int value) {
    Instant startTime = Instant.now();
    this.progress.updateSimulationProgress(value);
    this.simulationMethodsDuration -= Duration.between(startTime, Instant.now()).toMillis();
  }

  @Override
  public void updateProgressMessage(String title) {
    Instant startTime = Instant.now();
    this.progress.updateSimulationTitle(title);
    this.simulationMethodsDuration -= Duration.between(startTime, Instant.now()).toMillis();
  }

  @Override
  public void updateProgressMax(int value) {
    Instant startTime = Instant.now();
    this.progress.updateSimulationMax(value);
    this.simulationMethodsDuration -= Duration.between(startTime, Instant.now()).toMillis();
  }

  @Override
  public DatacenterBroker getEntity() {
    return this;
  }

  @Override
  public void startEntity() {
    super.startEntity();
    this.experiment.getSimulation().startEntity(this);
  }

  @Override
  public void shutdownEntity() {
    super.shutdownEntity();
    this.experiment.updateSimulationDuration(this.simulationMethodsDuration);
    this.experiment.getSimulation().shutdownEntity(this);
  }

  @Override
  protected void processCloudletReturn(SimEvent ev) {
    Cloudlet task = (Cloudlet) ev.getData();

    Instant startTime = Instant.now();
    this.experiment.getSimulation().processCloudletReturn(this, task);
    this.simulationMethodsDuration += Duration.between(startTime, Instant.now()).toMillis();

    this.checkSubmittedTasks();

    this.progress.updateRecievedTasksProgress(this, 1);
    this.experiment.updateRecievedCloudlets(task);
    super.processCloudletReturn(ev);
  }

  /**
   * Submit cloudlets to the created VMs. This function is modified to simulate
   * the working of postponing cloudlets based on the dependency
   */
  @Override
  protected void submitCloudlets() {
    Instant startTime = Instant.now();
    this.experiment.getSimulation().processCloudletsSubmit(this);
    this.simulationMethodsDuration += Duration.between(startTime, Instant.now()).toMillis();

    this.checkSubmittedTasks();
  }

  /**
   * Submit cloudlet to CloudSim through ReCloud's system.
   * @param task Task to be submitted.
   */
  @Override
  public <T extends Cloudlet> void submitCloudlet(T task) {
    Instant startTime = Instant.now();

    Vm vm;
    // if user didn't bind this cloudlet and it has not been executed yet
    if (task.getVmId() == -1) {
      vm = this.getVmsCreatedList().get(this.nextVmIndex);
      this.nextVmIndex = (this.nextVmIndex + 1) % this.getVmsCreatedList().size();
    } else {
      // submit to the specific vm
      vm = VmList.getById(getVmsCreatedList(), task.getVmId());
    }

    if (vm != null) {
      task.setVmId(vm.getId());
      sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, task);
      Log.printConcatLine(CloudSim.clock(),
          ": " + this.experiment.getSimulation().getAlgorithmName() + " | " + getName(),
          ": Sending cloudlet " + task.getCloudletId(), " to VM #", task.getVmId());

      this.cloudletsSubmitted++;
      this.getCloudletSubmittedList().add(task);
      this.progress.updateSubmittedTasksProgress(this, 1);
    } else {
      // vm was not created
      Log.printConcatLine(CloudSim.clock(),
          ": " + this.experiment.getSimulation().getAlgorithmName() + " | " + getName(),
          ": Postponing execution of cloudlet ", task.getCloudletId(), ": bount VM not available");
    }

    this.simulationMethodsDuration -= Duration.between(startTime, Instant.now()).toMillis();
  }

  /** Check if submitted tasks are still in waiting tasks list.  */
  public void checkSubmittedTasks() {
    this.getCloudletList().removeAll(this.getCloudletSubmittedList());
  }
}
