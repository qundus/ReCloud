package com.upm.researcher.components.algorithms.honeybee;

public class AVirtualMachine 
{
	//protected final double mips;
	//protected final int cores;
	protected double processingTime;
	protected int requestCounts;
	protected boolean isOverloaded;

	//HB_VirtualMachine(double mips, int cores)
	AVirtualMachine()
	{
		//this.mips = mips;
		//this.cores = cores;
		this.requestCounts = 0;
		this.isOverloaded = false;
	}
}
