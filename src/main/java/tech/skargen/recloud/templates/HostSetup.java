package tech.skargen.recloud.templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.skargen.skartools.SNumbers.RandomStyle;

/** Creates hosts with given parameters in each datacenter group. */
public class HostSetup {
  /** Number of machines with the same setup. */
  public int clones;

  /** Speed length. */
  public double[] mips;
  /** Number of processing units. */
  public int[] pes;
  /** RAM in MB. */
  public int[] ram;
  /** Bandwidth in MB/s. */
  public long[] bw;
  /** Storage in MBs. */
  public long[] storage;

  /** Speed length array filled during runtime. */
  public double[] rtmips;
  /** Number of processing units array filled during runtime. */
  public int[] rtpes;
  /** RAM in MB array filled during runtime. */
  public int[] rtram;
  /** Bandwidth in MB/s array filled during runtime. */
  public long[] rtbw;
  /** Storage in MBs array filled during runtime. */
  public long[] rtstorage;

  /** The random number generation style if any. */
  public RandomStyle randomStyle;

  /**
   * Validate the setup fields, fixes some initial values if non exists.
   *
   * @param setup Setup to validate.
   */
  public static void validate(HostSetup setup) {
    final Logger log = LogManager.getLogger();

    StringBuilder sb = new StringBuilder();
    if (setup.mips == null || setup.mips.length <= 0) {
      setup.mips = new double[] {177730};
      sb.append("->mips(default 177730)");
    }
    if (setup.pes == null || setup.pes.length <= 0) {
      setup.pes = new int[] {6};
      sb.append("->pes(default 6)");
    }
    if (setup.ram == null || setup.ram.length <= 0) {
      setup.ram = new int[] {16000};
      sb.append("->ram(default 16000MB)");
    }
    if (setup.bw == null || setup.bw.length <= 0) {
      setup.bw = new long[] {15000};
      sb.append("->bw(default 15000MB/s)");
    }
    if (setup.storage == null || setup.storage.length <= 0) {
      setup.storage = new long[] {4000000};
      sb.append("->starge(default 4,000,000MBs)");
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