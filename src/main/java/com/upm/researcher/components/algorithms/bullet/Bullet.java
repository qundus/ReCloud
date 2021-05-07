package com.upm.researcher.components.algorithms.bullet;

import java.util.Comparator;
import java.util.List;

import com.upm.researcher.components.algorithms.standard.StandardSim;
import com.upm.researcher.components.cloudsim.ABroker;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;

public class Bullet extends StandardSim 
{
	public enum GunType
	{
		Magnum,
		Bazooka
	}
	private final GunType gunType;
	public Bullet(GunType gunType)
	{
		this.gunType = gunType;
		this.SetHint(gunType.name());
	}

	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		this.SetProgressMax(this.GetTasksTarget());
		this.SetProgressMessage("Bullet ");
	}

	@Override
	public void Destroy() 
	{
	}

	@Override
	public void ProcessCloudletSubmit(ABroker broker) 
	{
		this.BenchmarkStart();
		
		switch(this.gunType)
		{
			case Magnum:
			this.Magnum(broker.getCloudletList(), broker.getVmsCreatedList());
			break;

			case Bazooka:
			this.Bazooka(broker.getCloudletList(), broker.getVmsCreatedList());
			break;
		}
		
		this.BenchmarkEnd();

		super.ProcessCloudletSubmit(broker);
	}

	@Override
	public <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task) 
	{
	}

	/**
	 * Presumes that all of the tasks are submitted to one grand (bazooka) virtual
	 * machine and limits VMs' processing times accordinagly.
	 * 
	 * @param tasks
	 * @param vms
	 */
	public void Bazooka(final List<Cloudlet> tasks, final List<Vm> vms) 
	{
		double[] vmsPT = new double[vms.size()];
		int chosenVmIdx = 0;
		double taskPT = 0;

		double totalLengths = 0;
		for (Cloudlet task : tasks) 
		{
			totalLengths += this.TaskStrength(task);
		}

		double totalMips = 0;
		for (int i = 0; i < vmsPT.length; i++) 
		{
			Vm vm = vms.get(i);
			totalMips += this.VmStrength(vm);
		}

		double grandPT = totalLengths / totalMips;

		for (int j = 0, i = 0; j < tasks.size(); j++) 
		{
			if (vmsPT[i] >= grandPT) i++;
			Cloudlet task = tasks.get(j);
			//chosenVmIdx = 0;
			taskPT = this.TaskRunTime(task, vms.get(chosenVmIdx));
			double vmTaskPT = this.TaskRunTime(task, vms.get(i));
			if (vmsPT[i] + vmTaskPT < vmsPT[chosenVmIdx] + taskPT)// && vmsPT[i] <= vmsPT[chosenVmIdx])
			{
				chosenVmIdx = i;
				taskPT = vmTaskPT;
			}

			vmsPT[chosenVmIdx] += taskPT;
			task.setVmId(vms.get(chosenVmIdx).getId());
			this.UpdateProgress(1);
		}

		vmsPT = null;
	}

	/**
	 * Accurate mapping with minimum imbalance degree but requires sorting of tasks
	 * descendantly first.
	 * 
	 * @param tasks Job (tasks) list to be scheduled.
	 * @param vms   Virtual machines to handle scheduled tasks.
	 */
	public void Magnum(final List<Cloudlet> tasks, final List<Vm> vms) 
	{
		double[] vmsPT = new double[vms.size()];
		int chosenVmIdx = 0;
		double taskPT = 0;

		Comparator<Cloudlet> comp = new Comparator<Cloudlet>() {
			public int compare(Cloudlet a, Cloudlet b) {
				return (int) ((b.getCloudletLength() * b.getNumberOfPes())
						- (a.getCloudletLength() * a.getNumberOfPes()));
			}
		};
		tasks.sort(comp);

		for (int j = 0; j < tasks.size(); j++)
		{
			Cloudlet task = tasks.get(j);
			chosenVmIdx = 0;
			taskPT = this.TaskRunTime(task, vms.get(chosenVmIdx));
			for (int i = 0; i < vmsPT.length; i++) 
			{
				if (i == chosenVmIdx)
					continue;

				double vmTaskPT = this.TaskRunTime(task, vms.get(i));
				if (vmsPT[i] + vmTaskPT < vmsPT[chosenVmIdx] + taskPT)// && vmsPT[i] <= vmsPT[chosenVmIdx])
				{
					chosenVmIdx = i;
					taskPT = vmTaskPT;
				}
			}

			vmsPT[chosenVmIdx] += taskPT;
			task.setVmId(vms.get(chosenVmIdx).getId());
			this.UpdateProgress(1);
		}

		vmsPT = null;
	}

	private double TaskRunTime(Cloudlet task, Vm vm) 
	{
		return this.TaskStrength(task) / this.VmStrength(vm);
	}

	private long TaskStrength(Cloudlet task) 
	{
		return task.getCloudletLength() * task.getNumberOfPes();
	}

	private double VmStrength(Vm vm) 
	{
		return vm.getMips() * vm.getNumberOfPes();
	}

    /*while(maxPT - minPT > 2)
    {
        totalPT = 0;
        for (int j = 0; j < tasks.size(); j++)
        {
            Cloudlet task = tasks.get(j);
            chosenVmIdx = tasksToVms[j];
            for (int i = 0; i < vmsPT.length; i++)
            {
                if (i == tasksToVms[j]) continue;

                if (vmsPT[i] < vmsPT[chosenVmIdx])
                {
                    chosenVmIdx = i;
                }
            }

            if (chosenVmIdx != tasksToVms[j])
            {
                vmsPT[tasksToVms[j]] -= this.TaskRunTime(task, vms.get(tasksToVms[j]));
                vmsPT[chosenVmIdx] += this.TaskRunTime(task, vms.get(chosenVmIdx));

                if (minPT > vmsPT[tasksToVms[j]])
                    minPT = vmsPT[tasksToVms[j]];
                if (maxPT < vmsPT[chosenVmIdx])
                    maxPT = vmsPT[chosenVmIdx];
            }

            
            tasksToVms[j] = chosenVmIdx;
            task.setVmId(vms.get(chosenVmIdx).getId());
        }
    }*/
}