package com.upm.researcher.components.algorithms.standard;

import com.upm.researcher.components.cloudsim.ABroker;
import com.upm.researcher.components.cloudsim.ASimulation;
import com.upm.researcher.controllers.Brokers.TaskScheduler;
import com.upm.researcher.controllers.Datacenters.VmScheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;

public class StandardSim extends ASimulation
{
    public String hint;
    public VmScheduler vmScheduler;
    public TaskScheduler taskScheduler;

	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		//this.SetProgressMax(1);
		this.UpdateProgress(this.GetTasksTarget());
	}

    public void ProcessCloudletSubmit(ABroker broker)
    {
        for (int vmIdx = 0; broker.getCloudletList().size() > 0;)
        {
            Cloudlet task = broker.getCloudletList().get(0);
            Vm vm = null;
            
            // if user didn't bind this cloudlet and it has not been executed yet
            if (task.getVmId() == -1)
            {
                vm = broker.getVmsCreatedList().get(vmIdx % broker.getVmsCreatedList().size());
            }
            else
            {
                // submit to the specific vm
                vm = VmList.getById(broker.getVmsCreatedList(), task.getVmId());

                if (vm == null)
                {
                    Log.printConcatLine(CloudSim.clock(), ": ", broker.getName(), ": Postponing execution of cloudlet ",
                    task.getCloudletId(), ": bount VM not available");

                    continue;
                }
            }

            task.setVmId(vm.getId());

			vmIdx = (vmIdx + 1) % broker.getVmsCreatedList().size();
			broker.SubmitCloudlet(task);
        }
	}
	
	@Override
    public <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task)
    {
        
	}

	@Override
	public void Destroy() 
	{
		
	}
}