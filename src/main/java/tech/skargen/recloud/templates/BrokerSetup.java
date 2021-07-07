package tech.skargen.recloud.templates;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A class to store the brokers data. */
public class BrokerSetup {
  /**
   * Name of the broker.
   */
  public String name;

  /**
   * The indeces of the tasks/cloudlets stored by Brokers in object
   * {@link tech.skargen.recloud.controllers.Jobs.taskList}.
   */
  public IntArrayList taskIndeces;

  /**
   * The indeces of the virtual machines stored by Brokers in object
   * {@link tech.skargen.recloud.controllers.Jobs.vmList}.
   */
  public IntArrayList vmIndeces;

  /**
   * Validate the setup fields, fixes some initial values if non exists.
   *
   * @param setup Setup to validate.
   */
  public static void validate(BrokerSetup setup) {
    final Logger log = LogManager.getLogger();

    // validate and report setup configs
    if (setup.name == null || setup.name.isEmpty()) {
      setup.name = "Broker";
      log.info("Broker has no name, assigning 'Broker' as name.");
    }
  }
}