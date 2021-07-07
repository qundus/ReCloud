package tech.skargen.recloud.controllers.interfaces;

import java.io.File;
import tech.skargen.recloud.controllers.Jobs.TasksSplit;
import tech.skargen.recloud.templates.BrokerSetup;
import tech.skargen.recloud.templates.TaskTypeSetup;
import tech.skargen.recloud.templates.VirtualMachineSetup;
import tech.skargen.skartools.SArrays;
import tech.skargen.skartools.SNumbers.RandomStyle;

public abstract interface IJobsSet {
  /**
   * Generating tasks in runtime depends on distributing tasks in a certain way,
   * here you can select how those tasks are split across brokers and how much of
   * a task type to create.
   *
   * @param splitMode Split mode to apply.
   * @return This jobs interface.
   */
  public abstract IJobsSet taskSplit(TasksSplit splitMode);

  /**
   * Create a new broker using a dedicated interface.
   * @return New instance of broker maker interface.
   */
  public abstract IMakeBroker newBroker();

  /**
   * Create a new task type using a dedicated interface.
   * @return New instance of task-type maker interface.
   */
  public abstract IMakeTask newTask();

  /**
   * Create a new virtual machine using a dedicated interface.
   * @return New instance of virtual-machine maker interface.
   */
  public abstract IMakeVm newVm();

  /**
   * Add a broker to list.
   * @param setup Broker to be added.
   */
  public abstract void newBroker(BrokerSetup setup);

  /**
   * Add setup to list.
   *
   * @param setup Task setup to be added.
   * @param names Names of brokers to assign this task type to (if any).
   */
  public abstract void newTask(TaskTypeSetup setup, String... names);

  /**
   * Add virtual machine to list.
   *
   * @param setup Virtual machine to be added.
   * @param names Names of brokers to assign this VM setup to (if any).
   */
  public abstract void newVm(VirtualMachineSetup setup, String... names);

  public final class IMakeBroker {
    private final IJobsSet jobs;

    private final BrokerSetup setup;

    public IMakeBroker(IJobsSet jobs) {
      this.jobs = jobs;
      this.setup = new BrokerSetup();
    }

    /** Confirm setup cutomization and add it to list. */
    public void make() {
      this.jobs.newBroker(setup);
    }

    /**
     * Name of the broker, make sure it's unique otherwise the whole setup will be
     * skipped.
     * @param name Name of the broker.
     * @return This maker interface.
     */
    public IMakeBroker name(String name) {
      this.setup.name = name;
      return this;
    }
  }

  public final class IMakeTask {
    private final IJobsSet jobs;

    private final TaskTypeSetup setup;

    private String[] brokersNames;

    public IMakeTask(IJobsSet jobs) {
      this.jobs = jobs;
      this.setup = new TaskTypeSetup();
    }

    /** Confirm setup cutomization and add it to list. */
    public void make() {
      this.jobs.newTask(this.setup, brokersNames);
    }

    /**
     * Make tasks on specific brokers for manually assigning them.
     *
     * @param names Names of brokers to assign this task type to.
     * @return This maker interface.
     */
    public IMakeTask on(String... names) {
      this.brokersNames = names;
      return this;
    }

    /**
     * Workload files to be loadded for simulations during runtime. be aware that
     * this method cancels all other customization so that task attributes will be
     * created based on the workload file only.
     *
     * @param rating     The rating of the workload file.
     * @param pathToFile Path to folder, for example "C://path/to/folder" is passed
     *                   as parameters like "C://", "path", "to", "folder" i.e.:
     *                   </p>
     *                   {@code Simulations.WorkloadFolder
     *                   (System.getProperty("user.dir"),
     *                   "resources");}
     * @return This maker interface.
     */
    public IMakeTask workload(int rating, String... pathToFile) {
      String dir = String.join(File.separator, pathToFile) + File.separator;
      File file = new File(dir);

      try {
        if (!file.exists()) {
          throw new Exception("Invalid Workload Directory/File -> " + dir);
        }
        // this.setup = new TaskTypeSetup();
        this.setup.workloadFile = dir;
        this.setup.workloadRating = rating;

      } catch (Exception se) {
        se.printStackTrace();
      }

      return this;
    }

    /**
     * The length of the task in Million Instructions (MI).
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeTask length(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.length = values;
      return this;
    }

    /**
     * The number of cores required by this task.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeTask pes(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.pes = values;
      return this;
    }

    /**
     * The size of the file.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeTask filesize(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.filesize = values;
      return this;
    }

    /**
     * The output size of the file.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeTask outpusize(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.output = values;
      return this;
    }

    /**
     * This random pattern style decides how the random numbers are generated during
     * runtime.
     *
     * <pre></pre>
     *
     * Random values get activated once more than 1 value is passed to a method like
     * {@link #length(long, long...)}, then an array containing all random values is
     * generated and referred to duting runtime.
     *
     * To further explain this, consider the following example:
     * <ol>
     * <li>{@code value(22)}, assigns only 22 as the value of this field.
     * <li>{@code values(22,55)}, assigns a random value between 22 and 55.
     * <li>{@code values(22,55,99,...)}, assigns a random value from the given
     * numbers as the value of this field.
     * </ol>
     *
     * @param style The random numbers style pattern.
     * @return This make interface.
     */
    public IMakeTask randomStyle(RandomStyle style) {
      this.setup.randomStyle = style;
      return this;
    }
  }

  public final class IMakeVm {
    private final IJobsSet jobs;

    private final VirtualMachineSetup setup;

    private String[] brokersNames;

    public IMakeVm(IJobsSet jobs) {
      this.jobs = jobs;
      this.setup = new VirtualMachineSetup();
    }

    /** Confirm setup cutomization and add it to list. */
    public void make() {
      this.jobs.newVm(this.setup, brokersNames);
    }

    /**
     * Make tasks on specific brokers for manually assigning them.
     *
     * @param names Names of brokers to assign this vm to.
     * @return This maker interface.
     */
    public IMakeVm on(String... names) {
      this.brokersNames = names;
      return this;
    }

    /**
     * Virtual machine monitor.
     *
     * @param value Virtual machine monitor.
     * @return This maker interface.
     */
    public IMakeVm vmm(String value) {
      this.setup.vmm = value;
      return this;
    }

    /**
     * Processing speed in Million Instructions Per Second (MIPS).
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeVm mips(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.mips = values;
      return this;
    }

    /**
     * Processing cores.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeVm pes(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.pes = values;
      return this;
    }

    /**
     * RAM in MBs; as a power of 2, like 256 and 512.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeVm ram(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.ram = values;
      return this;
    }

    /**
     * RAM to be a power of 2, like 5 and 8 so it can be used as 2^5 and 2^8.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeVm ram2(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      for (int i = 0; i < values.length; i++) {
        values[i] = (int) Math.pow(2, values[i]);
      }
      this.setup.ram = values;
      return this;
    }

    /**
     * Bandwidth.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeVm bw(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.bw = values;
      return this;
    }

    /**
     * Image size.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeVm image(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.storage = values;
      return this;
    }

    /**
     * Number of identical virtual machines to be created.
     *
     * @param clones Number of clones to produce during runtime.
     * @return This maker interface.
     */
    public IMakeVm clones(int clones) {
      this.setup.clones = clones;
      return this;
    }

    /**
     * This random pattern style decides how the random numbers are generated during
     * runtime.
     *
     * <pre></pre>
     *
     * Random values get activated once more than 1 value is passed to a method like
     * {@link #mips(double, double...)}, then an array containing all random values
     * is generated and referred to duting runtime.
     *
     * To further explain this, consider the following example:
     * <ol>
     * <li>{@code value(22)}, assigns only 22 as the value of this field.
     * <li>{@code values(22,55)}, assigns a random value between 22 and 55.
     * <li>{@code values(22,55,99,...)}, assigns a random value from the given
     * numbers as the value of this field.
     * </ol>
     *
     * @param style The random numbers style pattern.
     * @return This make interface.
     */
    public IMakeVm randomStyle(RandomStyle style) {
      this.setup.randomStyle = style;
      return this;
    }
  }
}
