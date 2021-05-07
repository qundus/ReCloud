package com.upm.researcher.components.algorithms.shortestjobfirst;

import java.util.Comparator;
import java.util.List;

import com.upm.researcher.components.algorithms.standard.StandardSim;
import com.upm.researcher.components.cloudsim.ABroker;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;

public class ShortestJobFirst extends StandardSim 
{
	public ShortestJobFirst()
	{
		this.SetHint("SJF");
	}

	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		this.SetProgressMax(this.GetTasksTarget());
		this.SetProgressMessage("SJF ");
	}

	@Override
	public void Destroy() 
	{
	}

	@Override
	public void ProcessCloudletSubmit(ABroker broker) 
	{
		this.BenchmarkStart();
		
		this.Run(broker.getCloudletList(), broker.getVmsCreatedList());
		
		this.BenchmarkEnd();

		super.ProcessCloudletSubmit(broker);
	}

	@Override
	public <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task) 
	{
	}


	private void Run(final List<Cloudlet> tasks, final List<Vm> vms)
	{
		Comparator<Cloudlet> comp = new Comparator<Cloudlet>() {
			public int compare(Cloudlet a, Cloudlet b) {
				return (int) ((a.getCloudletLength()) - (b.getCloudletLength()));
			}
		};
		tasks.sort(comp);
		this.UpdateProgress(tasks.size());
	}
}