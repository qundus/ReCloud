package com.upm.researcher.components.algorithms.minmin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.upm.researcher.components.algorithms.standard.StandardSim;
import com.upm.researcher.components.cloudsim.ABroker;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;

public class MinMin extends StandardSim 
{
	public enum Variation
	{
		Min_Min,
		Max_Min
	}
	private final Variation type;
	public MinMin(Variation type)
	{
		this.type = type;
		this.SetHint(type.name());
	}

	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		this.SetProgressMax(this.GetTasksTarget());
		this.SetProgressMessage(this.type.name() + " ");
	}

	@Override
	public void Destroy() 
	{
	}

	@Override
	public void ProcessCloudletSubmit(ABroker broker) 
	{
		this.BenchmarkStart();
		
		switch(this.type)
		{
			case Min_Min:
			this.Origin(broker.getCloudletList(), broker.getVmList());
			break;

			case Max_Min:
			this.Max(broker.getCloudletList(), broker.getVmList());
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
	 * Min-Min scheduling algorithm
	 * @param tasks
	 * @param vms
	 */
	private void Origin(final List<Cloudlet> tasks, final List<Vm> vms)
	{
		int numTasks = tasks.size();
		int numVMs = vms.size();

		// compute ready time for vms; they all start with zero
		double[] readyTimes = new double[vms.size()];

		// execution time matrix
		double[][] executionTimes = new double[numTasks][numVMs];

		// completion time matrix
		double[][] completionTimes = new double[numTasks][numVMs];
		
		// compute completion time
		for (int i = 0; i < numTasks; i++)
        {
			Cloudlet task = tasks.get(i);
			
			// looping all vms for this task
			for (int j = 0; j < numVMs; j++)
            {
				Vm vm = vms.get(j);

				executionTimes[i][j] = this.GetExecutionTime(task, vm);

				completionTimes[i][j] = executionTimes[i][j] + readyTimes[j];
			}
        }
        
		// loop all tasks
		int allTasksMapped = 0;
		int minCTTask = 0;
		int minCTTaskVm = 0;

		for (int i = 0; i < numTasks; i++)
		{
			Cloudlet task = tasks.get(i);
			if (task.getVmId() <= -1) // if task is not assigned
			{
				for (int j = 0; j < numVMs; j++)
				{
					// choosing the task with MINIMUM completion time and finding minimum vm completion time
					if (completionTimes[i][j] < completionTimes[minCTTask][minCTTaskVm])
					{
						minCTTask = i;
						minCTTaskVm = j;
					}
				}
			}

			if (i+1 >= numTasks)
			{
				// assign task with minimum completion time
				tasks.get(minCTTask).setVmId(minCTTaskVm);

				// update vm completion time
				readyTimes[minCTTaskVm] += executionTimes[minCTTask][minCTTaskVm];

				// remove task
				executionTimes[minCTTask] = null;
				completionTimes[minCTTask] = null;
				
				// update completion time of unmapped tasks
				minCTTask = -1;
				for (i = 0; i < numTasks; i++)
				{
					if (completionTimes[i] != null)
					{
						completionTimes[i][minCTTaskVm] = executionTimes[i][minCTTaskVm] + readyTimes[minCTTaskVm];

						// to avoid null pointers
						if (minCTTask == -1) minCTTask = i;
					}
				}
				
				// repeat loop
				i = -1;
				allTasksMapped++;
				this.UpdateProgress(1);
			}

			if (allTasksMapped >= numTasks) 
				break;
		}
	}

	/**
	 * Max-Min scheduling algorithm
	 * @param tasks
	 * @param vms
	 */
	private void Max(final List<Cloudlet> tasks, final List<Vm> vms)
	{
		int numTasks = tasks.size();
		int numVMs = vms.size();

		// compute ready time for vms; they all start with zero
		double[] readyTimes = new double[vms.size()];

		// execution time matrix
		double[][] executionTimes = new double[numTasks][numVMs];

		// completion time matrix
		double[][] completionTimes = new double[numTasks][numVMs];
		
		// compute completion time
		for (int i = 0; i < numTasks; i++)
        {
			Cloudlet task = tasks.get(i);
			
			// looping all vms for this task
			for (int j = 0; j < numVMs; j++)
            {
				Vm vm = vms.get(j);

				executionTimes[i][j] = this.GetExecutionTime(task, vm);

				completionTimes[i][j] = executionTimes[i][j] + readyTimes[j];
			}
        }
        
		// loop all tasks
		int allTasksMapped = 0;
		int maxCTTask = 0;
		// actually maximum vm CT, 
		// it'll be changed to minimum CT once the maximum completion time task is found
		int minCTTaskVm = 0;

		for (int i = 0; i < numTasks; i++)
		{
			Cloudlet task = tasks.get(i);
			if (task.getVmId() <= -1) // if task is not assigned
			{
				for (int j = 0; j < numVMs; j++)
				{
					// choosing the task with MINIMUM completion time and finding minimum vm completion time
					if (completionTimes[i][j] > completionTimes[maxCTTask][minCTTaskVm])
					{
						maxCTTask = i;
						minCTTaskVm = j;
					}
				}
			}
			
			if (i+1 >= numTasks)
			{
				// find minimum vm completion time
				//for (int j = 0; j < executionTimes[maxCTTask].length; j++)
				for (int j = 0; j < numVMs; j++)
				{
					//if (executionTimes[maxCTTask][j] + readyTimes[j] < executionTimes[maxCTTask][minCTTaskVm] + vmsReadyTime[minCTTaskVm])
					if (completionTimes[maxCTTask][j] < completionTimes[maxCTTask][minCTTaskVm])
					{
						minCTTaskVm = j;
					}
				}

				// assign task with minimum completion time
				tasks.get(maxCTTask).setVmId(minCTTaskVm);

				// update vm completion time
				readyTimes[minCTTaskVm] += executionTimes[maxCTTask][minCTTaskVm];

				// remove task
				executionTimes[maxCTTask] = null;
				completionTimes[maxCTTask] = null;
				
				// update completion time of unmapped tasks
				maxCTTask = -1;
				for (i = 0; i < numTasks; i++)
				{
					if (completionTimes[i] != null)
					{
						completionTimes[i][minCTTaskVm] = executionTimes[i][minCTTaskVm] + readyTimes[minCTTaskVm];

						// to avoid null pointers
						if (maxCTTask == -1) maxCTTask = i;
					}
				}
				
				// repeat loop
				i = -1;
				allTasksMapped++;
				this.UpdateProgress(1);
			}

			if (allTasksMapped >= numTasks)
				break;
		}
	}

	public double[][] GetExecutionTimeMatrix(List<? extends Cloudlet> tasks, List<? extends Vm> vms)
    {
		// better list than a 2D array cause each task will be removed later
		int numTasks = tasks.size();
		int numVMs = vms.size();
        double[][] result = new double[numTasks][numVMs];

		// looping all tasks
		for (int i = 0; i < numTasks; i++)
        {
			Cloudlet task = tasks.get(i);
			
			// looping all vms for this task
			for (int j = 0; j < numVMs; j++)
            {
				Vm vm = vms.get(j);

				result[i][j] = this.GetExecutionTime(task, vm);
			}
        }
        
        return result;
	}
	
	public double GetExecutionTime(Cloudlet task, Vm vm)
	{
		if (vm.getHost() == null)
			return task.getCloudletLength() / vm.getMips();

		return task.getCloudletLength() / vm.getHost().getTotalAllocatedMipsForVm(vm);
	}

	public double GetCompletionTime(double taskET, double vmRT)
	{
		// task execution time + vm ready time
		return taskET + vmRT;
	}
}