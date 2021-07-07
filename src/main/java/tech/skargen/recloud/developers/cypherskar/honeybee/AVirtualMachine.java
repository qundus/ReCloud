package tech.skargen.recloud.developers.cypherskar.honeybee;

public class AVirtualMachine {
  // protected final double mips;
  // protected final int cores;
  public double processingTime;
  public int requestCounts;
  public boolean isOverloaded;

  // HB_VirtualMachine(double mips, int cores)
  AVirtualMachine() {
    // this.mips = mips;
    // this.cores = cores;
    this.requestCounts = 0;
    this.isOverloaded = false;
  }
}
