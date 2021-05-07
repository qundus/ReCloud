package com.upm.researcher.components.algorithms.honeybee;

import java.util.List;
import java.util.NoSuchElementException;

import com.upm.researcher.components.cloudsim.ABroker;
import com.upm.researcher.components.cloudsim.ASimulation;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public class LBA_HB extends ASimulation
{
	protected Int2ObjectArrayMap<AHost> hostsTracker;
	protected Int2ObjectArrayMap<AVirtualMachine> vmsTracker;
    protected Host currentHost;
    private double Cpu_Threshold;

	public LBA_HB(double threshold)
    {
        this.Cpu_Threshold = threshold;
    }

	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		this.SetProgressMax(this.GetTasksTarget());
		this.SetProgressMessage("HoneyBee Is Doing Magic");
	}

	@Override
	public void Destroy() 
	{
		this.hostsTracker = null;
		this.vmsTracker = null;
		this.currentHost = null;
	}

    @Override
    public void ProcessCloudletSubmit(ABroker broker) 
    {
		this.BenchmarkStart();

		this.hostsTracker = new Int2ObjectArrayMap<>();
		this.vmsTracker = new Int2ObjectArrayMap<>();
		this.currentHost = null;
	
		for(Vm vm : broker.getVmsCreatedList())
		{
			this.Add(vm.getHost(), vm);
		}
		
		this.BenchmarkEnd();

		this.CheckQueue(broker);
	}
	
    @Override
	public void ProcessCloudletsReturn(ABroker broker, Cloudlet task)
    {
		Vm vm = 
		broker.getVmsCreatedList().stream().filter(v -> v.getId() == task.getVmId()).findFirst().get();

		this.DeallocateVmForTask(vm.getHost(), task);
		
		this.CheckQueue(broker);
	}

	public void Add(Host host, Vm vm)
	{
		if (!this.hostsTracker.containsKey(host.getId()))
		{
			this.hostsTracker.put(host.getId(), new AHost());
			if (this.currentHost == null)
				this.currentHost = host;
		}

		if (!this.vmsTracker.containsKey(vm.getId()))
		{
			this.vmsTracker.put(vm.getId(), new AVirtualMachine());
		}
	}

	public void Remove(Vm vm)
	{
		double id = -0.0014;

		for (Int2ObjectArrayMap.Entry<AVirtualMachine> hbvm : this.vmsTracker.int2ObjectEntrySet())
		{
			if (hbvm.getIntKey() == vm.getId())
			{
				id = hbvm.getIntKey();
				break;
			}
		}

		if (id != -0.0014)
		{
			this.vmsTracker.remove(vm.getId());
		}
	}

	@SuppressWarnings("unused")
	public void CheckQueue(ABroker broker)
	{
		for (int i = 0; broker.getCloudletList().size() > 0; i++)
        {
			Cloudlet task = broker.getCloudletList().get(0);
            
            this.AllocateVmForTask(broker.getVmsCreatedList(), task);

			if (task.getVmId() < 0)
				break;

			broker.SubmitCloudlet(task);
			this.UpdateProgress(1);
		}
	}

	public void DeallocateVmForTask(final Host host, final Cloudlet task)
	{
		this.BenchmarkStart();

		AHost hbhost = this.hostsTracker.get(host.getId());
		if (hbhost != null)
		{
			// Update host information
			hbhost.processingTime -= this.CalculateTaskPT(host, task.getCloudletLength());
		}
		else
		{
			//System.out.println("Host Doesn't Exist!");
		}

		AVirtualMachine hbvm = this.vmsTracker.get(task.getVmId());
			
		if (hbvm != null)
		{
			Vm vm = host.getVmList().stream().filter(id -> id.getId() == task.getVmId()).findFirst().get();

			// Update vm information
			//hbvm.totalLengthOfTasks = task.getCloudletLength();
			hbvm.processingTime -= this.CalculateTaskPT(vm, task.getCloudletLength());
			hbvm.requestCounts--;
			hbvm.isOverloaded = false;
		}
		else
		{
			//System.out.println("Vm Doesn't Exist!");
		}

		this.BenchmarkEnd();
	}

	public void AllocateVmForTask(final List<Vm> vms, final Cloudlet task)
	{
		this.BenchmarkStart();

		// Search for available vms within current host
		Vm chosenVm = this.FindVm(this.currentHost.getVmList());
		
		// Host is overloaded
		if (chosenVm == null)
		{
			// Find the minimum processing time host
			Host chosenHost = this.FindHost(vms);
			
			// Search for available vms within this host
			chosenVm = this.FindVm(chosenHost.getVmList());

			// Set host as current
			this.currentHost = chosenHost;
		}


		// Found a vm
		if (chosenVm != null)
		{
			AHost hbhost = this.hostsTracker.get(this.currentHost.getId());
			AVirtualMachine hbvm = this.vmsTracker.get(chosenVm.getId());

			// Update host information
			hbhost.processingTime += this.CalculateTaskPT(this.currentHost, task.getCloudletLength());

			// Update vm information
			hbvm.processingTime += this.CalculateTaskPT(chosenVm, task.getCloudletLength());
			hbvm.requestCounts++;

			// Assign task
			task.setVmId(chosenVm.getId());

			//System.out.println("Task : " + task.getCloudletId() + " is assigned to Vm : " + chosenVm.getId());
		}
		else
		{
			// All hosts overloaded; queue task
			task.setVmId(-1);
			//System.out.println("Task : " + task.getCloudletId() + " is queued");
		}

		this.BenchmarkEnd();
	}

	protected Host FindHost(final List<Vm> vms)
	{
		int hostId = -1;
		double minPT = Integer.MAX_VALUE;
		for (Int2ObjectArrayMap.Entry<AHost> hbhost : this.hostsTracker.int2ObjectEntrySet())
		{
			if (hbhost.getValue().processingTime < minPT)
			{
				minPT = hbhost.getValue().processingTime;
				hostId = hbhost.getIntKey();
			}
		}

		// Host found
		final int id = hostId;
		try
		{
			return vms.stream().map(x -> x.getHost()).filter(h -> h.getId() == id).findFirst().get();

		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected Vm FindVm(List<? extends Vm> hostVms) throws NoSuchElementException
	{
		AVirtualMachine[] availableVms = new AVirtualMachine[hostVms.size()];
		double standardDeviation = 0, avgVmsPT = 0;

		// Calculate average vms processing time
		for (int i = 0; i < availableVms.length; i++)
		{
			AVirtualMachine hbvm = this.vmsTracker.get(hostVms.get(i).getId());

			avgVmsPT += hbvm.processingTime;

			availableVms[i] = hbvm; 
		}
		
		avgVmsPT *= 1.0 / availableVms.length;

		// Calculate Load Standard Deviation
		for (AVirtualMachine hbvm : availableVms)
		{
			standardDeviation += Math.pow(hbvm.processingTime - avgVmsPT, 2);
		}

		standardDeviation = Math.sqrt(standardDeviation * (1.0 / availableVms.length));


		int vmIdx = -1;
		if (1.0 / standardDeviation == Double.POSITIVE_INFINITY)
		{
			vmIdx = 0;
		}
		else
		{
			int minCount = Integer.MAX_VALUE;

			for (int i = 0; i < availableVms.length; i++)
			{
				AVirtualMachine hbvm = availableVms[i];

				// Vm is available or overloaded
				if (!hbvm.isOverloaded)
				{
					if ((hbvm.processingTime - avgVmsPT) / standardDeviation <= this.Cpu_Threshold)
					{
						if (hbvm.requestCounts < minCount)
						{
							minCount = hbvm.requestCounts;
							vmIdx = i;
						}
					}
					else
					{
						hbvm.isOverloaded = true;
					}
				}
			}
		}

		// Host found
		if (vmIdx != -1)
		{
			return hostVms.get(vmIdx);
		}
		
		// All vms are overloaded
		return null;
	}

	protected double CalculateTaskPT(Host host, double taskLength)
	{
		return taskLength / (host.getNumberOfPes() * host.getTotalMips());
	}

	/*public double CalculateTaskPT(HB_VirtualMachine vm, double taskLength)
	{
		return taskLength / (vm.cores * vm.mips);
	}*/

	
	public double CalculateTaskPT(Vm vm, double taskLength)
	{
		return taskLength / (vm.getNumberOfPes() * vm.getMips());
	}
}

