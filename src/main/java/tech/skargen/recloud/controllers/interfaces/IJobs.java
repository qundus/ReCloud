package tech.skargen.recloud.controllers.interfaces;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.text.DecimalFormat;
import tech.skargen.recloud.controllers.Jobs.TasksSplit;
import tech.skargen.recloud.templates.BrokerSetup;
import tech.skargen.recloud.templates.TaskTypeSetup;
import tech.skargen.recloud.templates.VirtualMachineSetup;

public abstract interface IJobs {
  /**
   * Retrieve a list containing all brokers data.
   *
   * @return A list of all registered brokers.
   */
  public abstract ObjectArrayList<BrokerSetup> getBrokerList();

  /**
   * Retrieve a list of all task types; a task type is an object that gets
   * referred to create runtime tasks everytime they're needed. referred to create
   * runtime tasks everytime they're needed.
   *
   * @return A list of all registered task types.
   */
  public abstract ObjectArrayList<TaskTypeSetup> getTaskList();

  /**
   * Retrieve a list of the created virtual machines.
   *
   * @return A list of all registered VMs.
   */
  public abstract ObjectArrayList<VirtualMachineSetup> getVmList();

  /**
   * Retrieve a list of the runtime generate tasks distribution matrix.
   *
   * @return Task distribution matrix.
   */
  public abstract int[][] getDistributionMatrix();

  /**
   * Split mode is used to split tasks generated from task-types created by user
   * and distribute them amongst brokers during runtime.
   *
   * @return Tasks split mode in use.
   */
  public abstract TasksSplit getTasksSplit();

  /**
   * Populate brokers, tasks and vms table.
   *
   * @param jobsGet Interface to get jobs attributes.
   * @return Brokers, tasks and vms as detailed table.
   */
  public abstract StringBuilder generateJobsTable();

  /**
   * Populate distribution matrix table.
   * @param tablestyle Styler for the table.
   * @param numf Number formatter.
   * @return Distribution matrix as detailed table.
   */
  public abstract StringBuilder generateDistributionTable(DecimalFormat numf);
}
