package com.upm.researcher.templates.setups;

import com.upm.researcher.utils.NumberMixer.RandomStyle;

/** Creates hosts with given parameters in each datacenter group */
public class AHost 
{
    /**Number of machines with the same setup.*/
	public int clones;
	/**Speed length.*/
	public double minMips, maxMips;
    /**Number of processing units.*/
	public int minPes, maxPes;
	/**RAM in MB.*/
	public int minRam, maxRam;
	/**Bandwidth in MB/s.*/
	public long minBw, maxBw;
	/**Storage in MBs.*/
	public long minStorage, maxStorage;
    /**The random number generation style if any*/
	public RandomStyle randStyle;
	
	
	/**Speed length array filled during runtime.*/
    public double[] mips;
    /**Number of processing units array filled during runtime.*/
    public int[] pes;
	/**RAM in MB array filled during runtime.*/
    public int[] rams; // Memory (MB)
	/**Bandwidth in MB/s array filled during runtime.*/
    public long[] bws; // Bandwidth in MB/s
	/**Storage in MBs array filled during runtime.*/
    public long[] storages; // Storage in MBs
}