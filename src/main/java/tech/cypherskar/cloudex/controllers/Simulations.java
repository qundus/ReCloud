package tech.cypherskar.cloudex.controllers;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;

import tech.cypherskar.cloudex.bridges.ASimulationBridge;
import tech.cypherskar.cloudex.components.cloudsim.ASimulation;
import tech.cypherskar.cloudex.templates.infos.ASimulationInfo;

import org.cloudbus.cloudsim.Cloudlet;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**Handles each simulation's running environemnt.*/
public final class Simulations
{
    //  *****************************
    //  Fields
    //  *****************************
    private static ObjectArrayList<ASimulation> simList;
    private static int simulationsIdx;

    private static IntArrayList taskTargetList;
    private static String workloadFolder;

    static
    {
        simList = new ObjectArrayList<>();
        taskTargetList = new IntArrayList();
        workloadFolder = null;
    }

    //  *****************************
    //  Protected Methods
    //  *****************************

    /**
     * Validates simulations added to the list.
     * @return False if there's no simulations added or 
     * no tasks targets been set.
     */
    protected static boolean Initialize()
    {
        String format = "%-35.35s";
        System.out.print("Validating Simulations -----");
        
        if (simList.size() <= 0) 
        {
            System.out.print(String.format(System.lineSeparator() + format + " | "
            + "Use Simulations.List() To Create Some. "
            , "*Simulations List Is Empty"));
        }

        if (taskTargetList.size() <= 0) 
        {
            System.out.print(String.format(System.lineSeparator() + format + " | "
            + "Use Simulations.TaskTargets() To Create Some. "
            , "Task Targets List Is Empty, Adding '300' To List"));
            taskTargetList.add(300);
        }

        System.out.print("--> DONE" + System.lineSeparator());

        return simList.size() > 0 && taskTargetList.size() > 0;
    }

    /**
     * Grabs a new number of tasks from the list.
     * @return Next tasks target; -21 if none are left.
     */
    protected static int NewTaskTarget()
    {
        if (taskTargetList.size() > 0)
        {
            simulationsIdx = -1;
            return taskTargetList.removeInt(0);
        }

        return -21;
    }

    /**
     * Prepare the simulations data courier.
     * @return Simulation's data object.
     */
    protected static ASimulationInfo NewSimulation()
    {
        simulationsIdx++;

        if (simulationsIdx >= simList.size()) return null;

        ASimulationInfo result = new ASimulationInfo();
        result.recievedCloudlets = new ObjectArrayList<>();
        result.algorithm = simList.get(simulationsIdx);
        result.Algorithm_Time = 0;

        result.Name = result.algorithm.getClass().getSimpleName();
        return result;
    }

    /**
     * Calculate results after a simulation is finished.
     * @param info Current simulation's data.
     */
    protected static void EndSimulation(ASimulationInfo info)
    {
        if (info.recievedCloudlets.size() > 0)
        {
            // Reset
            info.Makespan = 0;
            info.Vms_Makespan = 0;
            info.Degree_Of_Imbalance = 0;
            info.Standard_Deviation = 0;

            Comparator<Cloudlet> comp = new Comparator<Cloudlet>() {
                public int compare(Cloudlet a, Cloudlet b)
                { 
                    return a.getVmId() - b.getVmId();
                }
            };
    
            // Sort cloudlets by vm id
            Collections.sort(info.recievedCloudlets, comp);

            int currentVm = info.recievedCloudlets.get(0).getVmId();
            int lastIdx = info.recievedCloudlets.size()-1;
    
            double vmMakespan = 0;
    
            // Calculating makespan
            DoubleArrayList vmsMakespanList = new DoubleArrayList();
            Cloudlet cloudlet = null;
            for (int c = 0; c < info.recievedCloudlets.size(); c++)
            {
                cloudlet = info.recievedCloudlets.get(c);
                //System.out.println(cloudlet.getUserId());
                
                // Makespan
                if (info.Makespan < cloudlet.getFinishTime())
                    info.Makespan = cloudlet.getFinishTime();
                
                
                if (vmMakespan < cloudlet.getFinishTime())
                    vmMakespan = cloudlet.getFinishTime();
    
                
                if (currentVm != cloudlet.getVmId() || c == lastIdx)
                {
					//System.out.print("User : " + cloudlet.getUserId() + " <-> Vm : " + currentVm);
                    currentVm = cloudlet.getVmId();
					//System.out.println(" || User : " + cloudlet.getUserId() + " <-> Vm : " + currentVm);
                    
                    // Vms Makespan
                    info.Vms_Makespan += vmMakespan;
                    
                    vmsMakespanList.add(vmMakespan);
                    vmMakespan = 0;
                }
            }
            
            double minCT = Double.POSITIVE_INFINITY, maxCT = 0.0, noOfVms = (double)vmsMakespanList.size();
            for (double CT : vmsMakespanList)
            {
                // Finding minimum and maximum completion time amongst all VMs
                if (minCT > CT)
                    minCT = CT;
                
                if (maxCT < CT)
                    maxCT = CT;
                
                // Accumulating each VM's value without square root
                info.Standard_Deviation += Math.pow(CT - (info.Vms_Makespan / noOfVms), 2);
            }
            
            // Degree of imbalance
            info.Degree_Of_Imbalance = noOfVms * ((maxCT - minCT) / info.Vms_Makespan);
    
            // Standard Deviation
            info.Standard_Deviation = Math.sqrt(info.Standard_Deviation * (1 / noOfVms));
        }
	}

    //  *****************************
    //  Public Methods
    //  *****************************

    /**
     * @return List of simulations.
     */
    public static ObjectArrayList<ASimulation> GetList()
    {
        return simList;
    }

    /**
     * Folder containing workload files to be loadded for simulations 
     * during runtime.
     * @param folders Path to folder, for example
     * "C://path/to/folder" is passed as parameters like
     * "C://", "path", "to", "folder"
     * <p>
     * i.e.: {@code Simulations.WorkloadFolder(System.getProperty("user.dir"), "resources");}
     */
    public static void WorkloadFolder(String... folders)
    {
        String dir = String.join(File.separator, folders) + File.separator;
        File file = new File(dir);

        try
        {
            if (!file.isDirectory())
            {
                throw new Exception("Invalid Workload Directory -> " + dir);
            }
            else
            {
                workloadFolder = dir + File.separator;
            }
            
        } catch (Exception se) {
            se.printStackTrace();
            System.exit(0);
        } 
    }

    /**
     * @returnWorkload files directory.
     */
    public static String GetWorkloadFolder() 
    {
        return workloadFolder;
    }

    /**
     * <pre>#CHAIN_START</pre>
     * Connects to simulations editing interface.
     * @return The bridge for editing simulations.
     */
    public static ASimulationBridge List()
    {
        return new ASimulationBridge(simList);
    }

    /**
     * Collection of numbers to be used as 'number of tasks'
     * for all simulations.
     */
    public static void TaskTargets(int... numbers)
    {
        for (int i = 0; i < numbers.length; i++)
        {
            if (numbers[i] > 0) taskTargetList.add(numbers[i]);
        }
    }
}