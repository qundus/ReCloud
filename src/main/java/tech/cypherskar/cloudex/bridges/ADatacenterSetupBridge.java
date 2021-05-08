package tech.cypherskar.cloudex.bridges;

import java.util.LinkedList;

import tech.cypherskar.cloudex.templates.setups.ADatacenterSetup;
import tech.cypherskar.cloudex.utils.NumberMixer.RandomStyle;

import org.cloudbus.cloudsim.Storage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Bridges are disposable interfaces used within classes 
 * to control the instantiation  and/or update of a corrospondant 
 * data structure template.
 * 
 * <b>
 * <ul>
 * <li>HandlingClass: {@link tech.cypherskar.cloudex.controllers.Datacenters}
 * <li>BridgeClass: {@link tech.cypherskar.cloudex.bridges.ADatacenterSetupBridge}
 * <li>DataClass: {@link tech.cypherskar.cloudex.templates.setups.ADatacenterSetup}
 * </ul>
 * </b> 
 * 
 * <p>
 * A Bridge reflects how the data class is used by the handling class
 * in a more clear and isolated nature.
 */
public class ADatacenterSetupBridge
{
    //  *****************************
    //  Fields
    //  *****************************
    private ObjectArrayList<ADatacenterSetup> datacenterList;
    private ADatacenterSetup dcToUpdate;

    //  *****************************
    //  Constructors
    //  *****************************

    /**
     * @param datacenterList The list of datacenters to be altered by this bridge.
     */
    public ADatacenterSetupBridge(ObjectArrayList<ADatacenterSetup> datacenterList)
    {
        this.datacenterList = datacenterList;
        
        this.dcToUpdate = new ADatacenterSetup();
        this.dcToUpdate.name = "Datacenter";
        this.dcToUpdate.architecture = "x86";
        this.dcToUpdate.os = "Linux";
		this.dcToUpdate.vmm = "Xen";

		this.dcToUpdate.minTimeZone = 10;
		this.dcToUpdate.maxTimeZone = -1;
		this.dcToUpdate.minSecCost = 3;
		this.dcToUpdate.maxSecCost = -1;
		this.dcToUpdate.minMemCost = 0.05;
		this.dcToUpdate.maxMemCost = -1;
		this.dcToUpdate.minBwCost = 0;
		this.dcToUpdate.maxBwCost = -1;
		this.dcToUpdate.minStorageCost = 0.001;
		this.dcToUpdate.maxStorageCost = -1;
		this.dcToUpdate.minInterval = 0;
		this.dcToUpdate.maxInterval = -1;

        this.dcToUpdate.storageList = new LinkedList<>();
        this.dcToUpdate.hostIndeces =  new IntArrayList();
        this.dcToUpdate.clones = 1;
        this.dcToUpdate.randStyle = RandomStyle.Fixed_Pace;
    }

    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param randStyle The random numbers style pattern if used a range #CHAIN_PART
     * @param clones Number of clones wanted with the same settings.
     */
	public void Create(RandomStyle randStyle, int clones)
    {
        this.dcToUpdate.randStyle = randStyle;
        this.Create(clones);
	}
	
    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param clones Number of clones wanted with the same settings.
     */
    public void Create(int clones)
    {
        if (datacenterList == null) return;

        boolean addDC = 
        this.datacenterList.stream().noneMatch(b -> b.name.equals(this.dcToUpdate.name));

        if (addDC)
        {
            this.dcToUpdate.clones = (clones <= 0)? 1 : clones;
            this.datacenterList.add(this.dcToUpdate);
        }
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param name Name of the datacenter.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge Name(String name)
    {
        this.dcToUpdate.name = name;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Machine setup specifics.
     * @param architecture example "x86".
     * @param os example "Linux".
     * @param vmm example "Xen".
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge Architecture(String architecture, String os, String vmm)
    {
        this.dcToUpdate.architecture = architecture;
        this.dcToUpdate.os = os;
        this.dcToUpdate.vmm = vmm;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Time zone of the machine.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge TimeZone(double value)
    {
        this.dcToUpdate.minTimeZone = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Second cost.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge SecCost(double value)
    {
        this.dcToUpdate.minSecCost = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Memory cost.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge MemCost(double value)
    {
        this.dcToUpdate.minMemCost = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Storage cost.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge StorageCost(double value)
    {
        this.dcToUpdate.minStorageCost = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Bandwidth cost.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge BwCost(double value)
    {
        this.dcToUpdate.minBwCost = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Scheduling interval.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge SchedulingInterval(double value)
    {
        this.dcToUpdate.minInterval = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param storageList Storage list of the machine.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge Storage(LinkedList<Storage> storageList)
    {
        this.dcToUpdate.storageList = storageList;
        return this;
    }

    
    /**
     * <pre>#CHAIN_PART</pre>
     * Time zone of the machine,
     * have to use {@link #Create(RandomStyle, int)} to commit changes.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge TimeZone(double min, double max)
    {
        this.dcToUpdate.minTimeZone = min;
        this.dcToUpdate.maxTimeZone = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Second cost,
     * have to use {@link #Create(RandomStyle, int)} to commit changes.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge SecCost(double min, double max)
    {
        this.dcToUpdate.minSecCost = min;
        this.dcToUpdate.maxSecCost = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Memory cost,
     * have to use {@link #Create(RandomStyle, int)} to commit changes.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge MemCost(double min, double max)
    {
        this.dcToUpdate.minMemCost = min;
        this.dcToUpdate.maxMemCost = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Storage cost,
     * have to use {@link #Create(RandomStyle, int)} to commit changes.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge StorageCost(double min, double max)
    {
        this.dcToUpdate.minStorageCost = min;
        this.dcToUpdate.maxStorageCost = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Bandwidth cost,
     * have to use {@link #Create(RandomStyle, int)} to commit changes.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge BwCost(double min, double max)
    {
        this.dcToUpdate.minBwCost = min;
        this.dcToUpdate.maxBwCost = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Scheduling interval,
     * have to use {@link #Create(RandomStyle, int)} to commit changes.
     * @return This bridge instance to further method chaining.
     */
    public ADatacenterSetupBridge SchedulingInterval(double min, double max)
    {
        this.dcToUpdate.minInterval = min;
        this.dcToUpdate.maxInterval = max;
        return this;
    }
}