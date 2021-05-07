package com.upm.researcher.controllers;

import java.util.Calendar;

import com.upm.researcher.components.cloudsim.ABroker;
import com.upm.researcher.templates.infos.ASimulationInfo;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

/**Handles all simulations and organizes conduction procedures.*/
public final class Experiment
{
    //  *****************************
    //  Fields
    //  *****************************
    private static int cloudsimUsers;
    private static boolean cloudsimTraceFlag;
    private static String signedBy;
    
    static
    {
        cloudsimUsers = 1;
        cloudsimTraceFlag = false;
        signedBy = "University Putra Malaysia";
    }

    //  *****************************
    //  Protected Methods
    //  *****************************

    /**
     * Called everytime there's a new number of tasks to conduct a 
     * round of simulations, it starts by informing the necessary
     * controllers of the action.
     * @return Number of tasks for the next round of simulations.
     */
    protected static int NewTasksTarget()
    {
        int tasksTarget = Simulations.NewTaskTarget();

        if (tasksTarget < 0)
        {
            return -1;
        }
        
        String warnings = Brokers.NewTasksTarget(tasksTarget);
        Window.NewTasksTarget(tasksTarget, warnings);
        return tasksTarget;
	}

    /**
     * For every new simulation, cloudsim is re-intitated and with the
     * help of the other controllers, simulation attrbutes such as tasks,
     * brokers, datacenters..etc is populated by their corrosponding 
     * handlers.
     */
    protected static void NewSimulation()
    {
        ASimulationInfo result = Simulations.NewSimulation();
        if (result == null)
        {
            return;            
        }

        // Initialize the CloudSim library
        CloudSim.init(cloudsimUsers, Calendar.getInstance(), cloudsimTraceFlag);
        
        // Initialize and deploy datacenters and brokers (users).
        Datacenter[] datacentrs = Datacenters.NewSimulation(result.algorithm.vmScheduler);
        ABroker[] brokers = Brokers.NewSimulation(result.algorithm.taskScheduler, result);
		Window.NewSimulation(result);
		
		// Initialize Algorithm/Simulation
		result.algorithm.Initialize(datacentrs, brokers);

		//List<SimEntity> e = CloudSim.getEntityList();
        // Start the simulation
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        EndSimulation(result);
	}

    /**
     * End current simulation and notify other controllers, also, 
     * empty simulation's carrying data object and all of its
     * fields to prepare for next simulation if any.
     * @param info Current simualtion's data.
     */
    protected static void EndSimulation(ASimulationInfo info)
    {
        Simulations.EndSimulation(info);
		Window.EndSimulation(info);
		info.algorithm.Destroy();
		info.lastTime = null;
		info.recievedCloudlets = null;
		info.progress = null;

        NewSimulation();
	}

    /**Inform controllers of the end of this simulations round.*/
    protected static void EndTasksTarget() 
    {
        Window.EndTasksTarget();
	}

    /**End simulations for all rounds and sign off experiments' files. */
    protected static void EndExperiment() 
    {
        Window.EndExperiment(signedBy);

        // Hold On To Data Created
        //while(Thread.activeCount() > 1){}
    }

    //  *****************************
    //  Public Methods
    //  *****************************

    /**Launch experiment the standard way.*/
    public static void LaunchLegacy()
    {
        int initialized = 0;

        if (Datacenters.Initialize()) initialized++;
        if (Brokers.Initialize()) initialized++;
        if (Simulations.Initialize()) initialized++;
        if (Window.Initialize(Simulations.GetList().size())) initialized++;

        if (initialized >= 4)
        {
            while(NewTasksTarget() >= 0)
            {
                NewSimulation();
                EndTasksTarget();
            }

            EndExperiment();
        }
        else
        {
            System.out.print(System.lineSeparator()
            + "*An Error Occured During Initialization."
            //+ " Make Sure That Experiment.New() Method Is Invoked."
            + System.lineSeparator());
        }
    }

    /**
     * Cloudsim logs status.
     * @param state enable/disable cloudsim logs.
     */
    public static void CloudsimLogState(boolean state)
    {
        if (!state)
        {
            Log.disable();
        }
        else
        {
            Log.enable();
        }
    }

    /**
     * Attributes necessary for cloudsim's initialization.
     * @param numOfCloudUsers Number of cloud users.
     * @param traceFlag Trace flag.
     */
    public static void CloudsimInit(int numOfCloudUsers, boolean traceFlag)
    {
        cloudsimUsers = numOfCloudUsers;
        cloudsimTraceFlag = traceFlag;
    }
    
    /**
     * Experiments are marked by a signiture to trace their conductor
     * and to add on to their credebility.
     * @param signiture Signiture text.
     */
    public static void DigitalSigniture(String signiture)
    {
        signedBy = signiture;
    }
}
