package tech.skargen.recloud.templates;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.Storage;
import tech.skargen.skartools.SNumbers.RandomStyle;

public class ServerSetup {
  /** Number of machines with the same setup. */
  public int clones;
  /** Name of the datacenter setup. */
  public String name;
  /** Machine build architecture; x86 or x64. */
  public String architecture;
  /** operating system. */
  public String os;
  /** Virtual machine monitor. */
  public String vmm;

  /** Time zone. */
  public double[] timezones;
  /** Second cost. */
  public double[] seccosts;
  /** Memory cost. */
  public double[] memcosts;
  /** Storage cost. */
  public double[] storagecosts;
  /** Bandwidth cost. */
  public double[] bwcosts;
  /** Scheduling interval. */
  public double[] intervals;

  /** Time zone array filled during runtime. */
  public double[] rttimezones;
  /** Second cost array filled during runtime. */
  public double[] rtseccosts;
  /** Memory cost array filled during runtime. */
  public double[] rtmemcosts;
  /** Storage cost array filled during runtime. */
  public double[] rtstoragecosts;
  /** Bandwidth cost array filled during runtime. */
  public double[] rtbwcosts;
  /** Scheduling interval array filled during runtime. */
  public double[] rtintervals;

  public LinkedList<Storage> storageList;

  /** The random number generation style if any. */
  public RandomStyle randomStyle;

  /** Host Ids to be deployed on this datacenter. */
  public IntArrayList hostIndeces;

  /**
   * Validate the setup fields, fixes some initial values if non exists.
   *
   * @param setup Setup to validate.
   */
  public static void validate(ServerSetup setup) {
    final Logger log = LogManager.getLogger();

    StringBuilder sb = new StringBuilder();
    if (setup.name == null || setup.name.isEmpty()) {
      setup.name = "Server";
      sb.append("->name(default 'Server')");
    }
    if (setup.architecture == null || setup.architecture.isEmpty()) {
      setup.architecture = "x86";
      sb.append("->architecture(default 'x86')");
    }
    if (setup.os == null || setup.os.isEmpty()) {
      setup.os = "Server";
      sb.append("->operating system(default 'Linux')");
    }
    if (setup.vmm == null || setup.vmm.isEmpty()) {
      setup.vmm = "Xen";
      sb.append("->vmm(default 'Xen')");
    }

    if (setup.timezones == null || setup.timezones.length <= 0) {
      setup.timezones = new double[] {10};
      sb.append("->timezones(default 10)");
    }
    if (setup.seccosts == null || setup.seccosts.length <= 0) {
      setup.seccosts = new double[] {3.0};
      sb.append("->second costs(default 3.0)");
    }
    if (setup.memcosts == null || setup.memcosts.length <= 0) {
      setup.memcosts = new double[] {0.05};
      sb.append("->memory costs(default 0.05)");
    }
    if (setup.storagecosts == null || setup.storagecosts.length <= 0) {
      setup.storagecosts = new double[] {0.001};
      sb.append("->storage costs(default 0.001)");
    }
    if (setup.bwcosts == null || setup.bwcosts.length <= 0) {
      setup.bwcosts = new double[] {0.0};
      sb.append("->bwcosts(default 0.0)");
    }
    if (setup.intervals == null || setup.intervals.length <= 0) {
      setup.intervals = new double[] {0.0};
      sb.append("->intervals(default 0.0)");
    }

    if (setup.clones <= 0) {
      setup.clones = 1;
      sb.append("->clones(default 1)");
    }

    if (sb.length() > 0) {
      sb.insert(0, "next values have been set to default");
      log.warn(sb);
    }

    if (setup.randomStyle == null) {
      setup.randomStyle = RandomStyle.Fixed_Pace;
    }
  }
}