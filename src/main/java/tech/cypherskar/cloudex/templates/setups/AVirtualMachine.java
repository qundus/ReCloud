package tech.cypherskar.cloudex.templates.setups;

import tech.cypherskar.cloudex.utils.NumberMixer.RandomStyle;

/**A class to store the virtual machine's data */
public class AVirtualMachine
{
	/**No. of clones wanted*/
	public int clones;
	/**Virtual machine monitor*/
	public String vmm;
	
	/**Speed length.*/
	public double minMips, maxMips;
	/**Processing units*/
	public int minPes, maxPes;
	/**RAM in MB*/
	public int minRam, maxRam;
	/**Bandwidth in MB/s*/
	public long minBw, maxBw;
	/**Storage in MBs*/
	public long minSize, maxSize;
	/**The random number generation style if any*/
	public RandomStyle randStyle;
	
    /**Length array created during runtime.*/
    public double[] mips;
	/**Pes array created during runtime.*/
    public int[] pes;
	/**RAM array created during runtime.*/
    public int[] rams;
	/**Bandwidth array created during runtime.*/
    public long[] bws;
	/**Storage array created during runtime.*/
    public long[] sizes;
}