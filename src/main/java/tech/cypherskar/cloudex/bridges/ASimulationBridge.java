package tech.cypherskar.cloudex.bridges;

import tech.cypherskar.cloudex.components.cloudsim.ASimulation;
import tech.cypherskar.cloudex.controllers.Brokers.TaskScheduler;
import tech.cypherskar.cloudex.controllers.Datacenters.VmScheduler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


/**
 * Bridges are disposable interfaces used within classes 
 * to control the instantiation  and/or update of a corrospondant 
 * data structure template.
 * 
 * <b>
 * <ul>
 * <li>HandlingClass: {@link tech.cypherskar.cloudex.controllers.Simulations}
 * <li>BridgeClass: {@link tech.cypherskar.cloudex.bridges.ASimulationBridge}
 * <li>DataClass: {@link tech.cypherskar.cloudex.components.cloudsim.ASimulation}
 * </ul>
 * </b> 
 * 
 * <p>
 * A Bridge reflects how the data class is used by the handling class
 * in a more clear and isolated nature.
 */
public class ASimulationBridge
{
    //  *****************************
    //  Fields
    //  *****************************
    private ObjectArrayList<ASimulation> simList;
    private VmScheduler vmScheduler;
    private TaskScheduler taskScheduler;
    private String hint;

    //  *****************************
    //  Constructors
    //  *****************************

    /**
     * 
     * @param sims The simulation list to be altered by this bridge.
     */
    public ASimulationBridge(ObjectArrayList<ASimulation> sims)
    {
        this.simList = sims;
        this.vmScheduler = VmScheduler.TimeShared;
        this.taskScheduler = TaskScheduler.TimeShared;
        this.hint = "";
    }

    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param sims The array of simulations to be run.
     */
    public void Create(ASimulation... sims)
    {
        if (sims == null || sims.length <= 0) return;

        for(int i = 0; i < sims.length; i++)
        {
            sims[i].vmScheduler = this.vmScheduler;
			sims[i].taskScheduler = this.taskScheduler;
			if (!this.hint.isEmpty())
			sims[i].SetHint(this.hint);
			else
			if (sims[i].GetHint() == null || sims[i].GetHint().isEmpty())
            sims[i].SetHint(this.hint);

            this.simList.add(sims[i]);
        }
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * The simulation scheduler to be assigned to virtual machines and hosts
     * in the list of schedulers passed in {@link #Create(ASimulation...)}.
     * @return This bridge instance to further method chaining.
     */
    public ASimulationBridge Schedulers(VmScheduler vmScheduler, TaskScheduler taskScheduler)
    {
        this.vmScheduler = vmScheduler;
        this.taskScheduler = taskScheduler;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * The identification title that seperates the simulations in the list 
     * passed to {@link #Create(ASimulation...)}, 
     * and it shows in the results.
     * @param hint The distinction string.
     * @return This bridge instance to further method chaining.
     */
    public ASimulationBridge Hint(String hint)
    {
        if (hint != null) this.hint = hint;
        return this;
    }
}