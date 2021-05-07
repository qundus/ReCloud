package com.upm.researcher.controllers;

import java.util.ArrayList;
import java.util.List;

import com.upm.researcher.bridges.ABrokerSetupBridge;
import com.upm.researcher.bridges.ACloudletBridge;
import com.upm.researcher.bridges.AVirtualMachineBridge;
import com.upm.researcher.components.cloudsim.ABroker;
import com.upm.researcher.components.cloudsim.AWorkloadFileReader;
import com.upm.researcher.components.window.Tables;
import com.upm.researcher.templates.infos.ASimulationInfo;
import com.upm.researcher.templates.setups.ABrokerSetup;
import com.upm.researcher.templates.setups.ACloudlet;
import com.upm.researcher.templates.setups.AVirtualMachine;
import com.upm.researcher.utils.NumberMixer;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;


/**
 * Responsible for creating brokers, tasks and virtual machines,
 * and maintaining those during runtime as well as ensuring that
 * randomly generated tasks and/or VMs are the same for every
 * simulation offering the user/researcher with broker options
 * and features that include:
 * 
 * <b>
 * <ul>
 * <li>Creating tasks types that translate cloudsim's cloudlets.
 * <li>Create VM containers that store broker pointers so they'll
 *     be assigned to same brokers with every round of simulations.
 * <li>Method of distributing tasks amongst brokers (TasksSplit).
 * <li>Method of acquiring vms for brokers.
 * <li>Runtime guarantee of identical tasks distribution in each simulation re-run.
 * </ul>
 * </b>
 */
public final class Brokers
{
    //  *****************************
    //  Enums
    //  *****************************

    /**
     * This element controls the distribution of tasks
     * across brokers during runtime.
     */
    public enum TasksSplit
    {
        /**Even tasks distribution.*/
        Even,
        /**Random tasks distribution.*/
        Random,
        /**
         * An uneven task distribution that leans towards assigning
         * more tasks to the brokers created first by user. 
         */
        Skewed_Left,
        /**
         * An uneven task distribution that leans towards assigning
         * more tasks to the brokers created last by user. 
         */
        Skewed_Right
    }

    /**Task schedulers for cloudsim environment.*/
    public enum TaskScheduler
    {
        TimeShared,
        SpaceShared,
        DynamicWorkload
    }

    //  *****************************
    //  Fields
    //  *****************************
    private static ObjectArrayList<ABrokerSetup> brokerList;
    private static ObjectArrayList<AVirtualMachine> vmList;
    private static ObjectArrayList<ACloudlet> taskList;
    private static TasksSplit splitMode;
    private static int[][] distributionMatrix;

    static
    {
        brokerList = new ObjectArrayList<>();
        vmList = new ObjectArrayList<AVirtualMachine>();
        taskList = new ObjectArrayList<ACloudlet>();
        splitMode = TasksSplit.Even;
    }

    //  *****************************
    //  Protected Methods
    //  *****************************

    /**
     * Confirms that user setup settings are valid and will cause
     * no issues in the upcoming experiment.
     * @return False if any of the setup lists will cause any issues.
     */
    protected static boolean Initialize()
    {
        String format = "%-35.35s";
        System.out.print("Validating Brokers -----");
        if (brokerList.size() <= 0)
        {
			System.out.print(String.format(System.lineSeparator() + format + " | "
            + "Use Brokers.List() To Create Some. "
            , "*Broker List Is Empty"));
		}
		else
		{
			// Remove Brokers With No Assigned Vms.
			for (int i = 0; i < brokerList.size(); i++)
			{
				ABrokerSetup setup = brokerList.get(i);
				boolean validBroker = setup.vmIndeces.size() > 0;
				if (setup.taskIndeces.size() <= 0) validBroker = false;
				
				if (!validBroker)
				{
					System.out.print(String.format(System.lineSeparator()
					+ format + " | Missing Tasks Or Vms To Be Assigned "
					, "Broker -> " + setup.name + " Is Removed"));
					brokerList.remove(i);
					i--;
				}
			}
		}
		
        if (vmList.size() <= 0)
        {
			System.out.print(String.format(System.lineSeparator() + format + " | "
            + "Use Brokers.Vms() To Create Some. "
            , "*Virtual Machine List Is Empty"));
		}
		else
		{
			NumberMixer mixer = new NumberMixer();
			for (int i = 0; i < vmList.size(); i++)
			{
				AVirtualMachine vm = vmList.get(i);

				if (vm.maxMips > vm.minMips)
				vm.mips = mixer.Create(vm.clones, vm.randStyle, vm.minMips, vm.maxMips);
				if (vm.maxPes > vm.minPes)
				vm.pes = mixer.Create(vm.clones, vm.randStyle, vm.minPes, vm.maxPes);
				if (vm.maxRam > vm.minRam)
				vm.rams = mixer.Create(vm.clones, vm.randStyle, vm.minRam, vm.maxRam);
				if (vm.maxBw > vm.minBw)
				vm.bws = mixer.Create(vm.clones, vm.randStyle, vm.minBw, vm.maxBw);
				if (vm.maxSize > vm.minSize)
				vm.sizes = mixer.Create(vm.clones, vm.randStyle, vm.minSize, vm.maxSize);
			}
		}
		
		if (taskList.size() <= 0)
		{
			System.out.print(String.format(System.lineSeparator() + format + " | "
			+ "Use Brokers.Tasks() To Create Some. "
			, "*Task List Is Empty"));
		}

        System.out.print("--> DONE" + System.lineSeparator());

        return brokerList.size() > 0 && taskList.size() > 0 && vmList.size() > 0;
    }

    /**
     * Convert tasks types to cloudsim ready tasks to be assigned to 
     * brokers during runtime everytime a new tasks target is set.
     * @param tasksTarget Number of tasks for the next round of simulations.
     * @return Text carrying details of how tasks are ditributed across all brokers.
     */
    protected static String NewTasksTarget(int tasksTarget)
    {
        StringBuilder string = new StringBuilder();

        // Check Tasks Distribution Over Number Of Brokers.
        /*if (tasksTarget < TOTAL_BROKERS)
        {
            if (string.length() > 0)
            {
                string.append(" | ");
            }

            string.append("No. Brokers["+TOTAL_BROKERS+"]"
            + " > Tasks Target["+ tasksTarget+"] Ignoring Some Brokers");
        }

        // Check Tasks Distribution Over Built Types Of Tasks.
        if (tasksTarget < taskList.size())
        {
            string.append("Tasks Target["+ tasksTarget+"]"
            + " < No. Task Types["+taskList.size()+"] Ignoring Some TaskTypes");
        }*/

        NumberMixer mixer = new NumberMixer();
        int numTaskTypes = taskList.size();
        int lastSlice = 0;
        double taskSlice = 0;
        double brokerSlice = 0;
        double remaining = tasksTarget;
        ABrokerSetup[] brokers = null;
        distributionMatrix = new int[brokerList.size()][numTaskTypes];
        for (int i = 0, t = 0; i < numTaskTypes; i++)
        {
            switch(splitMode)
            {
                case Even:
                t=i;
                taskSlice += (1.0 / numTaskTypes) * tasksTarget;
                break;
                
                case Random:
                t=i;
                taskSlice += remaining/(numTaskTypes-t);
                if ((remaining-taskSlice)/(numTaskTypes-t) > 0.0)
                {
                    taskSlice += (remaining-taskSlice)/(numTaskTypes-t);
                }
                if (t < numTaskTypes-1)
                {
                    taskSlice *= Math.random();
                }
                break;
                
                case Skewed_Left:
                t=i;
                taskSlice += remaining/(numTaskTypes-t);
                if ((remaining-taskSlice)/(numTaskTypes-t) > 1.0)// / remaining)
                {
                    taskSlice += (remaining-taskSlice)/(numTaskTypes-t);
                }
                break;
                
                case Skewed_Right:
                t=numTaskTypes-(i+1);

                taskSlice += (remaining / (t+1));
                if ((remaining - taskSlice)/(t+1) > 1.0)// / remaining)
                {
                    taskSlice += (remaining-taskSlice)/(t+1);
                }
                break;
            }

            // Set final portion/slice of current tasks target for this task type.
            lastSlice = (int)Math.round(taskSlice);
            remaining -= lastSlice;
            taskSlice -= lastSlice;

            if (lastSlice >= 1)
            {
                
                ACloudlet cloudlet = taskList.get(t);
                if (cloudlet.minPes != -21)
                {
                    if (cloudlet.maxLength > cloudlet.minLength)
					cloudlet.lengths = 
					mixer.Create(lastSlice, cloudlet.randStyle, 
					cloudlet.minLength, cloudlet.maxLength);
                    if (cloudlet.maxPes > cloudlet.minPes)
					cloudlet.pes = 
					mixer.Create(lastSlice, cloudlet.randStyle, 
					cloudlet.minPes, cloudlet.maxPes);
                    if (cloudlet.maxFileSize > cloudlet.minFileSize)
					cloudlet.fileSizes = 
					mixer.Create(lastSlice, cloudlet.randStyle, 
					cloudlet.minFileSize, cloudlet.maxFileSize);
                    if (cloudlet.maxOutputSize > cloudlet.minOutputSize)
					cloudlet.outputSizes = 
					mixer.Create(lastSlice, cloudlet.randStyle, 
					cloudlet.minOutputSize, cloudlet.maxOutputSize);
                }

                // Evenly Split last task slice over repective brokers 
                // Idea: add brokers split mode similar to tasks split mode (Even, Random...etc)
                final int fin = t;
                brokerSlice = 0;
                brokers = 
                brokerList.stream().filter(x -> x.taskIndeces.contains(fin)).toArray(ABrokerSetup[]::new);
                for(int j = 0, b = 0; j < brokers.length; j++)
                {
                    b = brokerList.indexOf(brokers[j]);
                    brokerSlice += (1.0/brokers.length) * lastSlice;
                    distributionMatrix[b][t] += (int)Math.round(brokerSlice);
                    brokerSlice -= distributionMatrix[b][t];
                }
            }
        }

        return string.toString();
    }

    /**
     * Assigns the brokers their shares of the tasks and prepares them for next simulation.
     * @param taskScheduler Type of cloudsim task scheduler.
     * @param info New simulation's details.
     * @return Array of ready for simulation brokers.
     */
    protected static ABroker[] NewSimulation(TaskScheduler taskScheduler, ASimulationInfo info)
    {
		ObjectArrayList<ABroker> brokers = new ObjectArrayList<>();
		try
        {
            int taskUID = 0;
            int vmUID = 0;
            int numBrokers = brokerList.size();
            int[] taskTracker = new int[taskList.size()];
            for (int i = 0; i < numBrokers; i++)
            {
				ABrokerSetup abs = brokerList.get(i);
                
				ObjectArrayList<Cloudlet> tasks = new ObjectArrayList<>();
                for (int t = 0; t < distributionMatrix[i].length; t++)
                {
                    if (distributionMatrix[i][t] > 0)
                    {
                        GetTasksFor(
                        taskList.get(t), taskTracker[t], distributionMatrix[i][t], tasks, taskUID);
                        taskTracker[t] += distributionMatrix[i][t];
                        taskUID += distributionMatrix[i][t];
                    }
                }
                
                int numBrokerTasks = tasks.size();
                if (numBrokerTasks > 0)
                {
					ABroker broker = new ABroker(abs.name + '(' + i + ')', info);
					//DatacenterBroker broker = 
					//new DatacenterBroker(abs.name + '|' + kvp.getIntKey());
					
					broker.submitVmList(
					GetVmsFor(abs.vmIndeces, vmUID, broker.getId(), taskScheduler));
					
					for (int t = 0; t < tasks.size(); t++)
					tasks.get(t).setUserId(broker.getId());
					
					broker.submitCloudletList(tasks);
					
					brokers.add(broker);
					
					vmUID += broker.getVmList().size();
                }
            }
        } catch(Exception v) {
            v.printStackTrace();
            System.exit(0);
		}
		
		return brokers.toArray(new ABroker[0]);
    }

    //  *****************************
    //  Private Methods
    //  *****************************

    /**
     * Create VMs for a broker according to their indeces list.
     * @param vmIndeces Pointers to acrual VMs setups.
     * @param uid Unique id of the VM set.
     * @param brokerId Unique id after initializing broker by cloudsim.
     * @param taskScheduler Task scheduler.
     * @return List of VMs ready to be added to brokers.
     */
    private static List<Vm> GetVmsFor(IntArrayList vmIndeces, int uid,
    int brokerId, TaskScheduler taskScheduler)
    {
        List<Vm> result = new ArrayList<Vm>();
        
        for (int vmIdx : vmIndeces)
        {
            AVirtualMachine avm = vmList.get(vmIdx);

            for (int c = 0; c < avm.clones; c++, uid++)
            {
                double mips = (avm.mips == null)? avm.minMips:avm.mips[c];
                int pes = (avm.pes == null)? avm.minPes:avm.pes[c];
                int ram = (avm.rams == null)? avm.minRam:avm.rams[c];
                long bw = (avm.bws == null)? avm.minBw:avm.bws[c];
                long size = (avm.sizes == null)? avm.minSize:avm.sizes[c];
				
        		CloudletScheduler cs = null;
                switch(taskScheduler)
                {
					case TimeShared:
                    cs = new CloudletSchedulerTimeShared();
                    break;
                    case SpaceShared:
                    cs = new CloudletSchedulerSpaceShared();
                    break;
                    case DynamicWorkload:
                    cs = new CloudletSchedulerDynamicWorkload(mips, pes);
                    break;
                }
				
				Vm vm = new Vm(uid, brokerId, mips, pes, ram, bw, size, avm.vmm, cs);

                result.add(vm);
            }
        }
        
        return result;
    }

    /**
     * Create tasks/cloudlets for a broker according to their indeces list
     * and task distribution matrix.
     * @param taskType Task data details to generate cloudsim tasks from.
     * @param created Number of tasks of this type has been created.
     * @param clones Required clones.
     * @param tasks Broker's tasks list.
     * @param uid Unique set of tasks id.
     * @throws Exception When the workload file path doesn't exist.
     */
    private static void GetTasksFor(ACloudlet taskType, int created, int clones,
    ObjectArrayList<Cloudlet> tasks, int uid)
    throws Exception
    {
        // Read Cloudlets from workload file in the swf format
        if (taskType.minPes == -21)
        {
            if (Simulations.GetWorkloadFolder() == null || Simulations.GetWorkloadFolder().isEmpty())
            throw new Exception("Resources folder is undefined for file -> " + taskType.workloadFile);
            
            AWorkloadFileReader workloadFileReader = 
            new AWorkloadFileReader(Simulations.GetWorkloadFolder() + 
            taskType.workloadFile, taskType.workLoadRating, uid, created, clones);
            
            tasks.addAll(workloadFileReader.generateWorkload());
        }
        else
        {
            for (int task = 0; task < clones; task++, created++)
            {
                tasks.add(new Cloudlet(uid,
                (taskType.lengths == null)?taskType.minLength:taskType.lengths[created],
                (taskType.pes == null)?taskType.minPes:taskType.pes[created],
                (taskType.fileSizes == null)?taskType.minFileSize:taskType.fileSizes[created],
                (taskType.outputSizes == null)?taskType.minOutputSize:taskType.outputSizes[created],
                new UtilizationModelFull(),
                new UtilizationModelFull(),
                new UtilizationModelFull()));

                uid++;
            }
        }
	}
	
    //  *****************************
    //  Public Methods
    //  *****************************

    /**
     * This method prepares a table according to the TasksSplit chosen by user
     * and shows the distribution of tasks.
     * @param tables The tables object responsible for transforming 
     * simulation environement data into well formatted GUI tables.
     * @return The formatted string carrying tasks to brokers distribution info.
     */
	public String[] GetDistributionTable(Tables tables)
    {
        String[] splits = new String[2 + brokerList.size()];
        int[] totalTasks = new int[taskList.size()];
        
        for (int i = 0; i < distributionMatrix.length; i++)
        {
            ABrokerSetup setup = brokerList.get(i);
			splits[1 + i] = tables.CellFormat(tables.cellSeperator+setup.name, "-", "" + i, false);

			int totalBrokerTasks = 0;
			for (int j = 0; j < distributionMatrix[i].length; j++)
            {
                totalBrokerTasks += distributionMatrix[i][j];
				totalTasks[j] += distributionMatrix[i][j];
				splits[1+i] +=
				tables.CellFormat(tables.numf.format(distributionMatrix[i][j]), "", null, false);
			}
			
			if (totalBrokerTasks <= 0)
			splits[1+i] += tables.CellFormat("No Tasks", "-", "Ignored", true);
			else
			splits[1+i] += tables.CellFormat(tables.numf.format(totalBrokerTasks), "", null, true);
		}
		
		splits[0] = 
        tables.CellFormat(tables.cellSeperator + "Distribution Matrix", "-", null, false);

        int lastIdx = splits.length-1;
        int tasksTarget = 0;
        splits[lastIdx] = 
        tables.CellFormat(tables.cellSeperator+splitMode.name()+" Split", "-", "Total", false);
        for (int i = 0; i < totalTasks.length; i++)
        {
			tasksTarget += totalTasks[i];
			splits[0] += tables.CellFormat("Task Type", "-", "" + i, false);
            splits[lastIdx] += tables.CellFormat(tables.numf.format(totalTasks[i]), "", null, false);
		}
		
		splits[0] += tables.CellFormat("Even Split", "-", "Total", true);
        splits[lastIdx] +=
        tables.CellFormat(tables.numf.format(tasksTarget), "-", null, true);

        return splits;
	}
	
    /**
     * This method prepares column titles to be filled by tables object.
     * @param tables The tables object responsible for transforming 
     * simulation environement data into well formatted GUI tables.
     * @return The well formatted string carrying all brokers, task types and vms info.
     */
	public String[] GetSetupTable(Tables tables)
	{
		String[] specs = new String[9];

		// Datacenter specs table
		specs[0] = tables.CellFormat(tables.cellSeperator+"Specs \\ Name", "-", "index", false);
		specs[1] = tables.CellFormat(tables.cellSeperator+"Task Types", "-", null, false);
		specs[2] = tables.CellFormat(tables.cellSeperator+"Virtual Machines", "-", null, false);
		specs[3] = " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		specs[4] = " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		specs[5] = " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		specs[6] = " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		specs[7] = " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		specs[8] = " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		
		for (int i = 0; i < brokerList.size(); i++)
		{
			ABrokerSetup setup = brokerList.get(i);
			specs[0] += tables.CellFormat(setup.name, "-", "" + i, false);
			specs[1] += tables.CellFormat(setup.taskIndeces.toString(), "-", "Indeces", false);
			specs[2] += tables.CellFormat(setup.vmIndeces.toString(), "-", "Indeces", false);
			specs[3] += " " + tables.CellFormatNoSeparator(" ", "-", null, false);
			specs[4] += " " + tables.CellFormatNoSeparator(" ", "-", null, false);
			specs[5] += " " + tables.CellFormatNoSeparator(" ", "-", null, false);
			specs[6] += " " + tables.CellFormatNoSeparator(" ", "-", null, false);
			specs[7] += " " + tables.CellFormatNoSeparator(" ", "-", null, false);
			specs[8] += " " + tables.CellFormatNoSeparator(" ", "-", null, false);
		}

		// Hosts specs table
		specs[0] += tables.CellFormat(" ", "-", null, false);
		specs[0] += tables.CellFormat("Specs \\ Vm", "-", "index", false);
		specs[1] += tables.CellFormat(" ", "-", null, false);
		specs[1] += tables.CellFormat("Mips", "-", null, false);
		specs[2] += tables.CellFormat(" ", "-", null, false);
		specs[2] += tables.CellFormat("Processing Elements", "-", "Cores", false);
		specs[3] += tables.CellFormat(" ", "-", null, false);
		specs[3] += tables.CellFormat("Memory (Ram)", "-", "MB", false);
		specs[4] += tables.CellFormat(" ", "-", null, false);
		specs[4] += tables.CellFormat("Bandwidth", "-", "MB\\S", false);
		specs[5] += tables.CellFormat(" ", "-", null, false);
		specs[5] += tables.CellFormat("Image Size", "-", "MBs", false);
		specs[6] += tables.CellFormat(" ", "-", null, false);
		specs[6] += tables.CellFormat("Vm Monitor", "-", null, false);
		specs[7] += tables.CellFormat(" ", "-", null, false);
		specs[7] += tables.CellFormat("Clones", "-", null, false);
		specs[8] += tables.CellFormat(" ", "-", null, false);
		specs[8] += tables.CellFormat("Random Style", "-", "If Any", false);

		for (int i = 0; i < vmList.size(); i++)
		{
			AVirtualMachine vm = vmList.get(i);
			specs[0] += tables.CellFormat("Vm Type", "-", "" + i, false);

			if (vm.maxMips > vm.minMips)
			specs[1] += tables.CellFormat(vm.minMips+"-"+vm.maxMips, "", null, false);
			else
			specs[1] += tables.CellFormat(vm.minMips+"", "", null, false);

			if (vm.maxPes > vm.minPes)
			specs[2] += tables.CellFormat(vm.minPes+"-"+vm.maxPes, "", null, false);
			else
			specs[2] += tables.CellFormat(vm.minPes+"", "", null, false);

			if (vm.maxRam > vm.minRam)
			specs[3] += tables.CellFormat(vm.minRam+"-"+vm.maxRam, "", null, false);
			else
			specs[3] += tables.CellFormat(vm.minRam+"", "", null, false);

			if (vm.maxBw > vm.minBw)
			specs[4] += tables.CellFormat(vm.minBw+"-"+vm.maxBw, "", null, false);
			else
			specs[4] += tables.CellFormat(vm.minBw+"", "", null, false);

			if (vm.maxSize > vm.minSize)
			specs[5] += tables.CellFormat(vm.minSize+"-"+vm.maxSize, "", null, false);
			else
			specs[5] += tables.CellFormat(vm.minSize+"", "", null, false);

			specs[6] += tables.CellFormat(vm.vmm, "-", null, false);
			specs[7] += tables.CellFormat(vm.clones+"", "", null, false);
			specs[8] += tables.CellFormat(vm.randStyle.name(), "-", null, false);
		}

		return specs;
	}

    /**
     * Set how the tasks distribution mode across brokers.
     * @param mode The preferred tasks split mode
     */
    public static void SetTasksSplitMode(TasksSplit mode)
    {
        splitMode = mode;
    }

    /**
     * <pre>#CHAIN_START</pre>
     * 
     * Connects to the brokers editing interface.
     * @return The bridge for editing brokers' data.
     * 
     * @see com.upm.researcher.bridges.ABrokerSetupBridge
     */
    public static ABrokerSetupBridge List()
    {
        return new ABrokerSetupBridge(brokerList);
    }

    /**
     * <pre>#CHAIN_START</pre>
     * Connects to tasks/cloudlets editing interface.
     * @return The bridge for editing tasks/cloudlets
     * @see com.upm.researcher.bridges.ACloudletBridge
     */
    public static ACloudletBridge Tasks()
    {
        return new ACloudletBridge(brokerList, taskList);
    }

    /**
     * <pre>#CHAIN_START</pre>
     * Connects to virtual machines editing interface.
     * @return The bridge for editing VMs.
     * @see com.upm.researcher.bridges.AVirtualMachineBridge
     */
    public static AVirtualMachineBridge Vms()
    {
        return new AVirtualMachineBridge(brokerList, vmList);
    }

    /**
     * @return An array of all registered brokers.
     */
    public static ABrokerSetup[] GetList()
    {
        return brokerList.toArray(new ABrokerSetup[brokerList.size()]);
    }

    /**
     * @return An array of allregistered tasks.
     */
    public static ACloudlet[] GetTasks()
    {
        return taskList.toArray(new ACloudlet[taskList.size()]);
    }

     /**
     * @return An array of all registered VMs.
     */
    public static AVirtualMachine[] GetVms()
    {
        return vmList.toArray(new AVirtualMachine[vmList.size()]);
    }
}