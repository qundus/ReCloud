package tech.skargen.recloud.templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.skargen.skartools.SNumbers.RandomStyle;

/** A class to store the task's/cloudlet's data. */
public class TaskTypeSetup {
  /** Length of the task in MI.*/
  public long[] length;
  /** Number of processing units.*/
  public int[] pes;
  /** Length of the file size.*/
  public long[] filesize;
  /** Length of the file size.*/
  public long[] output;

  /** Length array created during runtime. */
  public long[] rtlength;
  /** Pes array created during runtime. */
  public int[] rtpes;
  /** Filesize array created during runtime. */
  public long[] rtfilesize;
  /** Outputsize array created during runtime. */
  public long[] rtoutput;

  // public Class<? extends UtilizationModel> utilizationModelCpu;
  // public Class<? extends UtilizationModel> utilizationModelRam;
  // public Class<? extends UtilizationModel> utilizationModelBw;

  /** External file rating. */
  public int workloadRating;
  /** External file path. */
  public String workloadFile;

  /** The random number generation style if any.*/
  public RandomStyle randomStyle;

  /**
   * Validate the setup fields, fixes some initial values if non exists.
   *
   * @param setup Setup to validate.
   */
  public static void validate(TaskTypeSetup setup) {
    // cancel customization if workloadfile exists.
    if (setup.workloadFile != null && !setup.workloadFile.isEmpty()) {
      setup.length = null;
      setup.pes = null;
      setup.filesize = null;
      setup.output = null;
      return;
    }

    final Logger log = LogManager.getLogger();

    // validate and report setup configs
    StringBuilder sb = new StringBuilder();
    if (setup.length == null || setup.length.length <= 0) {
      setup.length = new long[] {1000};
      sb.append("->length(default 1000)");
    }
    if (setup.pes == null || setup.pes.length <= 0) {
      setup.pes = new int[] {1};
      sb.append("->pes(default 1)");
    }
    if (setup.filesize == null || setup.filesize.length <= 0) {
      setup.filesize = new long[] {1};
      sb.append("->filesize(default 1)");
    }
    if (setup.output == null || setup.output.length <= 0) {
      setup.output = new long[] {1};
      sb.append("->output(default 1)");
    }
    if (setup.workloadFile != null && setup.workloadFile.isEmpty()) {
      sb.append("->empty workload file(default null)");
      setup.workloadFile = null;
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