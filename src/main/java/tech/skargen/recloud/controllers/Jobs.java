package tech.skargen.recloud.controllers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.ReBroker;
import tech.skargen.recloud.components.cloudsim.WorkloadFileReader;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IJobs;
import tech.skargen.recloud.controllers.interfaces.IJobsSet;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.templates.BrokerSetup;
import tech.skargen.recloud.templates.TaskTypeSetup;
import tech.skargen.recloud.templates.VirtualMachineSetup;
import tech.skargen.skartools.SNumbers;
import tech.skargen.skartools.STable;
import tech.skargen.skartools.STable.EntryStyle;

/**
 * Responsible for creating brokers, tasks and virtual machines, and maintaining
 * those during runtime. Also, ensuring that randomly generated tasks and/or
 * VMs are the same for every simulation offering the user/researcher with
 * broker options and features that include:
 *
 * <b>
 * <ul>
 * <li>Creating tasks types that translate cloudsim's cloudlets.
 * <li>Create VM containers that store broker pointers so they'll be assigned to
 * same brokers with every round of simulations.
 * <li>Method of distributing tasks amongst brokers (TasksSplit).
 * <li>Method of acquiring vms for brokers.
 * <li>Runtime guarantee of identical tasks distribution in each simulation
 * re-run.
 * </ul>
 * </b>
 */
public final class Jobs
    implements IRecloudSequence, IExperimentSequence, ISimulationSequence, IJobs, IJobsSet {
  /**
   * This element controls the distribution of tasks across brokers during
   * runtime, the tasks must NOT have been assigned to specific brokers though.
   */
  public enum TasksSplit {
    /** Even tasks distribution. */
    Even,
    /** Random tasks distribution. */
    Random,
    /**
     * An uneven task distribution that leans towards assigning more tasks to the
     * brokers created first by user.
     */
    Skewed_Left,
    /**
     * An uneven task distribution that leans towards assigning more tasks to the
     * brokers created last by user.
     */
    Skewed_Right
  }

  /** Task schedulers for cloudsim environment. */
  public enum TaskScheduler { TimeShared, SpaceShared, DynamicWorkload }

  private ObjectArrayList<BrokerSetup> brokerList;
  private ObjectArrayList<VirtualMachineSetup> vmList;
  private ObjectArrayList<TaskTypeSetup> taskTypeList;
  private int[][] distributionMatrix;
  private TasksSplit tasksSplit;

  private static Logger _LOG;
  static {
    _LOG = LogManager.getLogger();
  }

  /**Constructor.*/
  public Jobs() {
    this.brokerList = new ObjectArrayList<>();
    this.vmList = new ObjectArrayList<VirtualMachineSetup>();
    this.taskTypeList = new ObjectArrayList<TaskTypeSetup>();
    this.tasksSplit = TasksSplit.Even;
  }

  /**
   * Retrieve a list containing all brokers this.
   * @return A list of all registered brokers.
   */
  @Override
  public ObjectArrayList<BrokerSetup> getBrokerList() {
    return this.brokerList;
  }

  /**
   * Retrieve a list of all task types; a task type is an object that gets
   * referred to to create runtime tasks everytime they're needed.
   * @return A list of all registered task types.
   */
  @Override
  public ObjectArrayList<TaskTypeSetup> getTaskList() {
    return this.taskTypeList;
  }

  /**
   * Retrieve a list of the created virtual machines.
   * @return A list of all registered VMs.
   */
  @Override
  public ObjectArrayList<VirtualMachineSetup> getVmList() {
    return this.vmList;
  }

  @Override
  public int[][] getDistributionMatrix() {
    return this.distributionMatrix;
  }

  /**
   * Split mode is used to split tasks generated from task-types created by user
   * and distribute them amongst brokers during runtime.
   *
   * @return Tasks split mode in use.
   */
  @Override
  public TasksSplit getTasksSplit() {
    return this.tasksSplit;
  }

  /**
   * Create a new broker using a dedicated interface.
   * @return New instance of broker maker interface.
   */
  @Override
  public IMakeBroker newBroker() {
    return new IMakeBroker(this);
  }

  /**
   * Create a new task type using a dedicated interface.
   * @return New instance of task-type maker interface.
   */
  @Override
  public IMakeTask newTask() {
    return new IMakeTask(this);
  }

  /**
   * Create a new virtual machine using a dedicated interface.
   * @return New instance of virtual-machine maker interface.
   */
  @Override
  public IMakeVm newVm() {
    return new IMakeVm(this);
  }

  /**
   * Add a broker to list.
   * @param setup Broker to be added.
   */
  @Override
  public void newBroker(BrokerSetup setup) {
    BrokerSetup.validate(setup);

    // add setup
    boolean nomatch = this.brokerList.stream().noneMatch(b -> b.name.equals(setup.name));
    if (nomatch) {
      setup.taskIndeces = new IntArrayList();
      setup.vmIndeces = new IntArrayList();

      this.brokerList.add(setup);
    } else {
      _LOG.info("broker with name " + setup.name + "already exists, ignoring!.");
    }
  }

  /**
   * Add setup to list.
   *
   * @param setup Task setup to be added.
   * @param names Names of brokers to assign this task type to (if any).
   */
  @Override
  public void newTask(TaskTypeSetup setup, String... names) {
    TaskTypeSetup.validate(setup);

    // add setup
    final int setupIdx = this.taskTypeList.size();
    if (names == null || names.length <= 0) {
      for (int i = 0; i < this.brokerList.size(); i++) {
        this.brokerList.get(i).taskIndeces.add(setupIdx);
      }
    } else {
      Optional<BrokerSetup> filterVal;
      BrokerSetup broker;

      for (int i = 0; i < names.length; i++) {
        final int j = i;
        filterVal = this.brokerList.stream().filter(b -> b.name.equals(names[j])).findFirst();

        if (filterVal.isPresent()) {
          broker = filterVal.get();
          broker.taskIndeces.add(setupIdx);

        } else {
          _LOG.warn("broker  " + names[j] + " is not in the list, ignoring!");
        }
      }
    }

    this.taskTypeList.add(setup);
  }

  /**
   * Add virtual machine to list.
   * @param setup Virtual machine to be added.
   * @param names Names of brokers to assign this VM to (if any).
   */
  @Override
  public void newVm(VirtualMachineSetup setup, String... names) {
    VirtualMachineSetup.validate(setup);

    final int setupIdx = this.vmList.size();
    if (names == null || names.length <= 0) {
      for (int i = 0; i < brokerList.size(); i++) {
        this.brokerList.get(i).vmIndeces.add(setupIdx);
      }
    } else {
      Optional<BrokerSetup> filterVal;
      BrokerSetup broker;

      for (int i = 0; i < names.length; i++) {
        final int j = i;
        filterVal = this.brokerList.stream().filter(b -> b.name.equals(names[j])).findFirst();
        if (filterVal.isPresent()) {
          broker = filterVal.get();
          broker.vmIndeces.add(setupIdx);
        } else {
          _LOG.warn("broker  " + names[j] + " is not in the list, ignoring!");
        }
      }
    }

    this.vmList.add(setup);
  }

  /**
   * Generating tasks in runtime depends on distributing tasks in a certain way,
   * here you can select how those tasks are split across brokers and how much of
   * a task type to create.
   *
   * @param splitMode Split mode to apply.
   * @return This jobs interface.
   */
  @Override
  public IJobsSet taskSplit(TasksSplit splitMode) {
    this.tasksSplit = splitMode;
    return this;
  }

  /**
   * Populate brokers, tasks and vms table.
   *
   * @param jobsGet Interface to get jobs attributes.
   * @return Brokers, tasks and vms as detailed table.
   */
  @Override
  public StringBuilder generateJobsTable() {
    // tasks and vms specs table
    STable stable = new STable();
    stable.newTable(EntryStyle.Verticle, 3, 20, '|', ' ', '|');
    stable.addEntry(0, "Name (id) \\ Specs ");
    stable.addEntry(-1, "Task Types", "(indices)");
    stable.addEntry(-1, 30, "Virtual Machines", "(indices)");

    int i;
    i = 0;
    for (BrokerSetup setup : getBrokerList()) {
      stable.addCell(-1, setup.name, i);
      stable.addCell(0, "", setup.taskIndeces);
      stable.addCell(0, "", setup.vmIndeces);
      i++;
    }

    // vms specs table
    StringBuilder result = stable.endTable(0, '-', '+', "Brokers");
    stable.newTable(EntryStyle.Horizontle, 9, 20, '|', ' ', '|');
    stable.addEntry(0, "Specs \\ Vm", "(id)");
    stable.addEntry(-1, "Vm Monitor", "VMM");
    stable.addEntry(-1, "Mips");
    stable.addEntry(-1, "Processing Elements", "(Cores-PEs)");
    stable.addEntry(-1, "Memory", "(Ram in MB)");
    stable.addEntry(-1, "Bandwidth", "(in MB\\S)");
    stable.addEntry(-1, "Image size", "(in MBs)");
    stable.addEntry(-1, "Clones");
    stable.addEntry(-1, "Random Style", "(If Any)");

    i = 0;
    for (VirtualMachineSetup setup : getVmList()) {
      stable.addCell(-1, "Vm Type", i);
      stable.addCell(-1, setup.vmm);
      stable.addCell(-1, "", setup.mips);
      stable.addCell(-1, "", setup.pes);
      stable.addCell(-1, "", setup.ram);
      stable.addCell(-1, "", setup.bw);
      stable.addCell(-1, "", setup.storage);
      stable.addCell(-1, String.valueOf(setup.clones));
      stable.addCell(-1, setup.randomStyle.name());
      i++;
    }

    result.append(stable.endTable(0, '-', '+', "Virtual Machines"));

    return result;
  }

  /**
   * Populate distribution matrix table.
   * @param tablestyle Styler for the table.
   * @param numf Number formatter.
   * @return Distribution matrix as detailed table.
   */
  @Override
  public StringBuilder generateDistributionTable(DecimalFormat numf) {
    final int numbrokers = brokerList.size();
    int[] totalbrokertasks = new int[numbrokers];

    STable stable = new STable();
    stable.newTable(EntryStyle.Horizontle, numbrokers + 2, 20, '|', ' ', '|');
    stable.addEntry(-1, "Broker", "(id)");
    int i = 0;
    for (BrokerSetup setup : brokerList) {
      stable.addEntry(-1, setup.name, i);
      i++;
    }
    stable.addEntry(0, "Total"); //, "Per Task Type");

    // BrokerSetup setup = brokerList.get(i);

    for (int j = 0; j < distributionMatrix[0].length; j++) {
      stable.addCell(-1, "Task Type", '(', j, ')');

      int totaltask = 0;
      for (i = 0; i < distributionMatrix.length; i++) {
        stable.addCell(0, numf.format(distributionMatrix[i][j]));
        totaltask += distributionMatrix[i][j];
        totalbrokertasks[i] += distributionMatrix[i][j];
      }
      stable.addCell(0, numf.format(totaltask));
    }

    stable.addCell(0, "Total"); //, "Per Broker");
    // just to make sure that requested number of tasks is same as calculated.
    int totaltasks = 0;
    for (i = 0; i < numbrokers; i++) {
      stable.addCell(0, numf.format(totalbrokertasks[i]));
      totaltasks += totalbrokertasks[i];
    }
    stable.addCell(0, numf.format(totaltasks));

    StringBuilder result = stable.endTable(
        -1, '-', '+', "Distribution Matrix", "TasksSplit." + this.tasksSplit.name());
    return result;
  }

  /**
   * Create VMs for a broker according to their indeces list.
   *
   * @param vmIndeces     Pointers to acrual VMs setups.
   * @param uid           Unique id of the VM set.
   * @param brokerId      Unique id after initializing broker by cloudsim.
   * @param taskScheduler Task scheduler.
   * @return List of VMs ready to be added to brokers.
   */
  private List<Vm> internal_getVmsFor(
      IntArrayList vmIndeces, int uid, int brokerId, TaskScheduler taskScheduler) {
    List<Vm> result = new ArrayList<Vm>();
    try {
      for (int vmIdx : vmIndeces) {
        VirtualMachineSetup s = this.vmList.get(vmIdx);

        for (int c = 0; c < s.clones; c++, uid++) {
          double mips = (s.rtmips == null) ? s.mips[0] : s.rtmips[c];
          int pes = (s.rtpes == null) ? s.pes[0] : s.rtpes[c];
          int ram = (s.rtram == null) ? s.ram[0] : s.rtram[c];
          long bw = (s.rtbw == null) ? s.bw[0] : s.rtbw[c];
          long size = (s.rtstorage == null) ? s.storage[0] : s.rtstorage[c];

          CloudletScheduler cs = null;
          switch (taskScheduler) {
            case TimeShared:
              cs = new CloudletSchedulerTimeShared();
              break;

            case SpaceShared:
              cs = new CloudletSchedulerSpaceShared();
              break;

            case DynamicWorkload:
              cs = new CloudletSchedulerDynamicWorkload(mips, pes);
              break;

            default:
              _LOG.error("new task scheduler enum is not implmented!");
              break;
          }

          Vm vm = new Vm(uid, brokerId, mips, pes, ram, bw, size, s.vmm, cs);

          result.add(vm);
        }
      }
    } catch (Exception e) {
      _LOG.fatal("can't create cloudsim virtual machines", e);
      System.exit(0);
    }

    return result;
  }

  /**
   * Create tasks/cloudlets for a broker according to their indeces list and task
   * distribution matrix.
   *
   * @param setup   Task data details to generate cloudsim tasks from.
   * @param created Number of tasks of this type has been created.
   * @param clones  Required clones.
   * @param tasks   Broker's tasks list.
   * @param uid     Unique set of tasks id.
   * @throws Exception When the workload file path doesn't exist.
   */
  private void internal_getTasksFor(
      TaskTypeSetup setup, int created, int clones, ObjectArrayList<Cloudlet> tasks, int uid) {
    try {
      // Read Cloudlets from workload file in the swf format
      if (setup.workloadFile != null) {
        WorkloadFileReader workloadFileReader = new WorkloadFileReader(
            setup.workloadFile + setup.workloadFile, setup.workloadRating, uid, created, clones);

        tasks.addAll(workloadFileReader.generateWorkload());
      } else {
        for (int task = 0; task < clones; task++, created++) {
          tasks.add(new Cloudlet(uid,
              (setup.rtlength == null) ? setup.length[0] : setup.rtlength[created],
              (setup.rtpes == null) ? setup.pes[0] : setup.rtpes[created],
              (setup.rtfilesize == null) ? setup.filesize[0] : setup.rtfilesize[created],
              (setup.rtoutput == null) ? setup.output[0] : setup.rtoutput[created],
              new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull()));

          uid++;
        }
      }
    } catch (Exception e) {
      _LOG.fatal("can't create cloudsim cloudlets/tasks", e);
      System.exit(0);
    }
  }

  /**
   * Confirms that user setup settings are valid and will cause no issues in the
   * upcoming experiment.
   *
   * @return False if any of the setup lists will cause any issues.
   */
  @Override
  public void validate() throws Exception {
    for (int i = 0; i < this.brokerList.size(); i++) {
      BrokerSetup setup = this.brokerList.get(i);

      if (setup.taskIndeces.size() <= 0) {
        _LOG.warn("broker -> " + setup.name + " is removed, has no Tasks assigned");
        this.brokerList.remove(i);
        i--;
      } else if (setup.vmIndeces.size() <= 0) {
        _LOG.warn("broker -> " + setup.name + " is removed, has no vms assigned");
        this.brokerList.remove(i);
        i--;
      }
    }

    if (this.brokerList.size() <= 0) {
      throw new Exception("no brokers created");
    } else {
    }

    if (this.vmList.size() <= 0) {
      throw new Exception("no virtual machines created");
    }

    if (this.taskTypeList.size() <= 0) {
      throw new Exception("no task types created");
    }
  }

  @Override
  public void init(IRecloud recloud) {
    SNumbers mixer = new SNumbers();
    for (int i = 0; i < this.vmList.size(); i++) {
      VirtualMachineSetup vm = this.vmList.get(i);
      vm.rtmips = mixer.create(vm.clones, vm.randomStyle, vm.mips);
      vm.rtpes = mixer.create(vm.clones, vm.randomStyle, vm.pes);
      vm.rtram = mixer.create(vm.clones, vm.randomStyle, vm.ram);
      vm.rtbw = mixer.create(vm.clones, vm.randomStyle, vm.bw);
      vm.rtstorage = mixer.create(vm.clones, vm.randomStyle, vm.storage);
    }
  }

  /**
   * Convert tasks types to cloudsim ready tasks to be assigned to brokers during
   * runtime everytime a new tasks target is set.
   *
   * @param tasksTarget Number of tasks for the next round of simulations.
   * @return Text carrying details of how tasks are ditributed across all brokers.
   */
  @Override
  public void newSequence(IRecloud recloud) {
    // Check Tasks Distribution Over Number Of Brokers.
    /*
     * if (tasksTarget < TOTAL_BROKERS) { if (string.length() > 0) {
     * string.append(" | "); }
     *
     * string.append("No. Brokers["+TOTAL_BROKERS+"]" + " > Tasks Target["+
     * tasksTarget+"] Ignoring Some Brokers"); }
     *
     * // Check Tasks Distribution Over Built Types Of Tasks. if (tasksTarget <
     * taskList.size()) { string.append("Tasks Target["+ tasksTarget+"]" +
     * " < No. Task Types["+taskList.size()+"] Ignoring Some TaskTypes"); }
     */

    SNumbers mixer = new SNumbers();
    int numTaskTypes = this.taskTypeList.size();
    int lastSlice = 0;
    double taskSlice = 0;
    double brokerSlice = 0;
    double remaining = recloud.getExperiment().getTasksTarget();
    BrokerSetup[] brokers = null;
    this.distributionMatrix = new int[this.brokerList.size()][numTaskTypes];

    for (int i = 0, t = 0; i < numTaskTypes; i++) {
      switch (this.tasksSplit) {
        case Even:
          t = i;
          taskSlice += (1.0 / numTaskTypes) * recloud.getExperiment().getTasksTarget();
          break;

        case Random:
          t = i;
          taskSlice += remaining / (numTaskTypes - t);
          if ((remaining - taskSlice) / (numTaskTypes - t) > 0.0) {
            taskSlice += (remaining - taskSlice) / (numTaskTypes - t);
          }
          if (t < numTaskTypes - 1) {
            taskSlice *= Math.random();
          }
          break;

        case Skewed_Left:
          t = i;
          taskSlice += remaining / (numTaskTypes - t);
          if ((remaining - taskSlice) / (numTaskTypes - t) > 1.0) { // / remaining)
            taskSlice += (remaining - taskSlice) / (numTaskTypes - t);
          }
          break;

        case Skewed_Right:
          t = numTaskTypes - (i + 1);

          taskSlice += (remaining / (t + 1));
          if ((remaining - taskSlice) / (t + 1) > 1.0) { // / remaining)
            taskSlice += (remaining - taskSlice) / (t + 1);
          }
          break;

        default:
          _LOG.fatal("task splits enum has not been implemented");
          break;
      }

      // Set final portion/slice of current tasks target for this task type.
      lastSlice = (int) Math.round(taskSlice);
      remaining -= lastSlice;
      taskSlice -= lastSlice;

      if (lastSlice >= 1) {
        TaskTypeSetup taskType = this.taskTypeList.get(t);

        if (taskType.workloadFile == null) {
          taskType.rtlength = mixer.create(lastSlice, taskType.randomStyle, taskType.length);
          taskType.rtpes = mixer.create(lastSlice, taskType.randomStyle, taskType.pes);
          taskType.rtfilesize = mixer.create(lastSlice, taskType.randomStyle, taskType.filesize);
          taskType.rtoutput = mixer.create(lastSlice, taskType.randomStyle, taskType.output);
        }

        // Evenly Split last task slice over respective brokers
        // Idea: add brokers split mode similar to tasks split mode (Even, Random...etc)
        final int fin = t;
        brokerSlice = 0;
        brokers = this.brokerList.stream()
                      .filter(x -> x.taskIndeces.contains(fin))
                      .toArray(BrokerSetup[] ::new);

        for (int j = 0, b = 0; j < brokers.length; j++) {
          b = this.brokerList.indexOf(brokers[j]);
          brokerSlice += (1.0 / brokers.length) * lastSlice;
          this.distributionMatrix[b][t] += (int) Math.round(brokerSlice);
          brokerSlice -= this.distributionMatrix[b][t];
        }
      }
    }
  }

  @Override
  public void endSequence(IRecloud recloud) {}

  /**
   * Assigns the brokers their shares of the tasks and prepares them for next
   * simulation.
   *
   * @param taskScheduler Type of cloudsim task scheduler.
   * @param algo          New simulation's details.
   * @return Array of ready for simulation brokers.
   */
  @Override
  public void beforeSimulation(IRecloud recloud) {
    try {
      int taskID = 0;
      int vmID = 0;
      int numBrokers = this.brokerList.size();
      int[] taskTracker = new int[this.taskTypeList.size()];

      for (int i = 0; i < numBrokers; i++) {
        BrokerSetup abs = this.brokerList.get(i);
        ObjectArrayList<Cloudlet> tasks = new ObjectArrayList<>();
        for (int t = 0; t < this.distributionMatrix[i].length; t++) {
          if (this.distributionMatrix[i][t] > 0) {
            internal_getTasksFor(this.taskTypeList.get(t), taskTracker[t],
                this.distributionMatrix[i][t], tasks, taskID);

            taskTracker[t] += this.distributionMatrix[i][t];
            taskID += this.distributionMatrix[i][t];
          }
        }

        int numBrokerTasks = tasks.size();
        if (numBrokerTasks > 0) {
          final TaskScheduler taskScheduler =
              recloud.getExperiment().getSimulation().getTaskScheduler();
          final ReBroker broker = new ReBroker(abs.name + '(' + i + ')', recloud);

          broker.submitVmList(
              internal_getVmsFor(abs.vmIndeces, vmID, broker.getId(), taskScheduler));

          for (int t = 0; t < tasks.size(); t++) {
            tasks.get(t).setUserId(broker.getId());
          }

          broker.submitCloudletList(tasks);

          vmID += broker.getVmList().size();
        }
      }
    } catch (Exception e) {
      _LOG.fatal("can't create cloudsim brokers", e);
      System.exit(0);
    }
  }

  @Override
  public void afterSimulation(IRecloud recloud) {}

  @Override
  public void finish(IRecloud recloud) {}
}