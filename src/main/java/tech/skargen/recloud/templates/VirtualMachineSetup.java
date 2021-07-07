package tech.skargen.recloud.templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.skargen.skartools.SNumbers.RandomStyle;

/** A class to store the virtual machine's data */
public class VirtualMachineSetup {
  /** No. of clones wanted.*/
  public int clones;
  /** Virtual machine monitor.*/
  public String vmm;

  /** Speed length. */
  public double[] mips;
  /** Processing units.*/
  public int[] pes;
  /** Ram in MB.*/
  public int[] ram;
  /** Bandwidth in MB/s.*/
  public long[] bw;
  /** Storage in MBs.*/
  public long[] storage;

  /** Length array created during runtime.*/
  public double[] rtmips;
  /** Pes array created during runtime.*/
  public int[] rtpes;
  /** RAM array created during runtime.*/
  public int[] rtram;
  /** Bandwidth array created during runtime.*/
  public long[] rtbw;
  /** Storage array created during runtime.*/
  public long[] rtstorage;

  /** The random number generation style if any.*/
  public RandomStyle randomStyle;

  /**
   * Validate the setup fields, fixes some initial values if non exists.
   * @param setup Setup to validate.
   */
  public static void validate(VirtualMachineSetup setup) {
    final Logger log = LogManager.getLogger();

    StringBuilder sb = new StringBuilder();
    if (setup.mips == null || setup.mips.length <= 0) {
      setup.mips = new double[] {9726};
      sb.append("->mips(default 9726)");
    }
    if (setup.pes == null || setup.pes.length <= 0) {
      setup.pes = new int[] {1};
      sb.append("->pes(default 1)");
    }
    if (setup.ram == null || setup.ram.length <= 0) {
      setup.ram = new int[] {512};
      sb.append("->ram(default 512MB)");
    }
    if (setup.bw == null || setup.bw.length <= 0) {
      setup.bw = new long[] {1000};
      sb.append("->bw(default 1000MB/s)");
    }
    if (setup.storage == null || setup.storage.length <= 0) {
      setup.storage = new long[] {10000};
      sb.append("->image(default 10000MBs)");
    }
    if (setup.clones <= 0) {
      setup.clones = 1;
      sb.append("->clones(default 1)");
    }
    if (setup.vmm == null || setup.vmm.isEmpty()) {
      setup.vmm = "Xen";
      sb.append("->vmm(default 'Xen')");
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