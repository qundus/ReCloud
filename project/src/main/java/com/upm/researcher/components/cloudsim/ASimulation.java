package com.upm.researcher.components.cloudsim;

import java.time.Duration;
import java.time.Instant;

import com.upm.researcher.controllers.Brokers.TaskScheduler;
import com.upm.researcher.controllers.Datacenters.VmScheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;

/**
 * The abstract class to be inherited by any class that wishes to
 * conduct a cloud simulation.
 * @see com.upm.researcher.components.algorithms.standard.StandardSim
 */
public abstract class ASimulation
{
	//  *****************************
    //  Fields
    //  *****************************
    private String hint;
    public VmScheduler vmScheduler;
	public TaskScheduler taskScheduler;
	
	//  *****************************
    //  Public Mehtods
    //  *****************************
	/**
	 * Prepare the class for the simulation.
	 * @param datacenters The list of the dataceneters involved in the simulation.
	 * @param brokers The list of the brokers involved in the simulation.
	 */
	public abstract void Initialize(Datacenter[] datacenters, ABroker[] brokers);

	/**
	 * Cloudsim has reached the point of handing tasks to virtual machines/hosts
	 * for this broker.
	 */
    public abstract void ProcessCloudletSubmit(ABroker broker);

	/**
	 * Cloudsim is sending back the finished tasks.
	 * @param broker The broker with the returning tasks.
	 * @param task The returning task.
	 */
    public abstract <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task);

	/**
	 * This method will be called once all tasks have been sumbitted and it's time 
	 * to destroy all class's fields in preperation for simulation end.
	 */
	public abstract void Destroy();

	/**
	 * @return Task target currently undergo by cloudsim.
	 */
	protected int GetTasksTarget()
	{
		return ABroker.info.progress.GetTasksTarget();
	}

	/**
	 * The upper bound of the progress bar shown in GUI.
	 */
	protected void SetProgressMax(int max)
	{
		ABroker.info.progress.SetAlgorithmMax(max);
	}

	/**
	 * Progress bar message.
	 */
	protected void SetProgressMessage(String msg)
	{
		ABroker.info.progress.SetAlgorithmString(msg);
	}

	/**
	 * Update the progress bar with this scheduling class's progress
	 * everytime this method is called.
	 * usually paired with {@link #ProcessCloudletSubmit(ABroker)} 
	 * and {@link #ProcessCloudletsReturn(ABroker, Cloudlet)}.
	 */
	protected void UpdateProgress(int value)
	{
		ABroker.info.progress.UpdateAlgorithm(value);
	}

	/**
	 * Called to start the local algorithm's benchmark,
	 * it is paired with {@link #BenchmarkEnd()} within the same method.
	 * 
	 * <pre>
	 *{@code 
	 *public void ProcessCloudletSubmit(ABroker broker) 
	 *{
	 *	this.BenchmarkStart();
	 *	[YOUR CODE GOES HERE]
	 *	this.BenchmarkEnd();
	 *	super.ProcessCloudletSubmit(broker);
	 *}
	 * </pre>
	 * @see #BenchmarkEnd()
	 */
	protected void BenchmarkStart()
	{
		ABroker.info.lastTime = Instant.now();
	}

	/**
	 * Called to end the local algorithm's benchmark,
	 * it is paired with {@link #BenchmarkStart()} within the same method.
	 * 
	 * <pre>
	 *{@code 
	 *public void ProcessCloudletSubmit(ABroker broker) 
	 *{
	 *	this.BenchmarkStart();
	 *	[YOUR CODE GOES HERE]
	 *	this.BenchmarkEnd();
	 *	super.ProcessCloudletSubmit(broker);
	 *}
	 * </pre>
	 * @see {@link #BenchmarkStart()}
	 */
	protected void BenchmarkEnd()
	{
		ABroker.info.Algorithm_Time += Duration.between(ABroker.info.lastTime, Instant.now()).toMillis();
	}
	
	/**
	 * Set the hint of this algorithm/class to be distinguished amongst
	 * the final results.
	 */
	public void SetHint(String hint)
	{
		if (hint != null) this.hint = hint;
	}

	/**
	 * Get the given hint to this class.
	 */
	public String GetHint()
	{
		return this.hint;
	}
}