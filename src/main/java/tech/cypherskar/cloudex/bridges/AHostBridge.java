package tech.cypherskar.cloudex.bridges;

import tech.cypherskar.cloudex.templates.setups.ADatacenterSetup;
import tech.cypherskar.cloudex.templates.setups.AHost;
import tech.cypherskar.cloudex.utils.NumberMixer.RandomStyle;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Bridges are disposable interfaces used within classes 
 * to control the instantiation  and/or update of a corrospondant 
 * data structure template.
 * 
 * <b>
 * <ul>
 * <li>HandlingClass: {@link tech.cypherskar.cloudex.controllers.Datacenters}
 * <li>BridgeClass: {@link tech.cypherskar.cloudex.bridges.AHostBridge}
 * <li>DataClass: {@link tech.cypherskar.cloudex.templates.setups.AHost}
 * </ul>
 * </b> 
 * 
 * <p>
 * A Bridge reflects how the data class is used by the handling class
 * in a more clear and isolated nature.
 */
public class AHostBridge
{
    //  *****************************
    //  Fields
    //  *****************************
    private ObjectArrayList<ADatacenterSetup> datacenterList;
    private ObjectArrayList<AHost> hostList;
    private AHost hostToUpdate;

    //  *****************************
    //  Constructors
    //  *****************************

    /**
     * @param datacenterList The list of datacenters to be altered by this bridge.
     * @param hostList The list of host to be altered by this bridge.
     */
    public AHostBridge(ObjectArrayList<ADatacenterSetup> datacenterList,
    ObjectArrayList<AHost> hostList)
    {
        this.datacenterList = datacenterList;
        this.hostList = hostList;

        this.hostToUpdate = new AHost();
        this.hostToUpdate.minMips = 177730;
		this.hostToUpdate.maxMips = -1;
        this.hostToUpdate.minPes = 6;
		this.hostToUpdate.maxPes = -1;
        this.hostToUpdate.minRam = 16000;
		this.hostToUpdate.maxRam = -1;
        this.hostToUpdate.minBw = 15000;
		this.hostToUpdate.maxBw = -1;
        this.hostToUpdate.minStorage = 4000000;
		this.hostToUpdate.maxStorage = -1;
		
		this.hostToUpdate.clones = 1;
		this.hostToUpdate.randStyle = RandomStyle.Fixed_Pace;
	}

    //  *****************************
    //  Public Mehtods
    //  *****************************
	
    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param randStyle The random numbers style pattern if used a range #CHAIN_PART
     * @param clones Number of clones wanted with the same settings.
     * @param datacenters The datacenters to assign this host setup to.
     */
	public void Create(RandomStyle randStyle, int clones, String... datacenters)
	{
		this.hostToUpdate.randStyle = randStyle;
		this.Create(clones, datacenters);
	}
    
    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param clones Number of clones wanted with the same settings.
     * @param datacenters The datacenters to assign this host setup to.
     */
    public void Create(int clones, String... datacenters)
    {
		if (hostList == null) return;
		if (clones > 1) this.hostToUpdate.clones = clones;
        this.hostList.add(this.hostToUpdate);
        
        if (datacenterList == null || datacenterList.size() <= 0) return;
        if (datacenters == null || datacenters.length <= 0)
        {
            for(int i = 0; i < this.datacenterList.size(); i++)
            {
                this.datacenterList.get(i).hostIndeces.add(this.hostList.size()-1);
            }
        }
        else
        {
            for(int i = 0; i < datacenters.length; i++)
            {
                final int j = i;
                ADatacenterSetup setup = 
                this.datacenterList.stream().filter(b -> b.name.equals(datacenters[j])).findFirst().get();
                setup.hostIndeces.add(this.hostList.size()-1);
            }
        }
    }

    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Processing speed in MIPS.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Mips(double value)
    {
        this.hostToUpdate.minMips = value;
        return this;
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Processing cores.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Pes(int value)
    {
        this.hostToUpdate.minPes = value;
        return this;
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value RAM in MBs.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Ram(int value)
    {
        this.hostToUpdate.minRam = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value RAM as a power of 2.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Ram2(int value)
    {
        this.hostToUpdate.minRam = (int)Math.pow(2, value);
        return this;
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Bandwidth.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Bw(long value)
    {
        this.hostToUpdate.minBw = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Storage value in MBs
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Storage(long value)
    {
        this.hostToUpdate.minStorage = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Processing speed in MIPS,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Mips(double min, double max)
    {
        this.hostToUpdate.minMips = min;
        this.hostToUpdate.maxMips = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Processing cores,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Pes(int min, int max)
    {
        this.hostToUpdate.minPes = min;
        this.hostToUpdate.maxPes = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * RAM in MBs,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Ram(int min, int max) 
    {
        this.hostToUpdate.minRam = min;
        this.hostToUpdate.maxRam = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * RAM as a power of 2,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Ram2(int min, int max) 
    {
		this.hostToUpdate.minRam = (int)Math.pow(2, min);
		this.hostToUpdate.maxRam = (int)Math.pow(2, max);
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Bandwidth,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Bw(long min, long max)
    {
        this.hostToUpdate.minBw = min;
        this.hostToUpdate.maxBw = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Storage value in MBs,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AHostBridge Storage(long min, long max)
    {
		this.hostToUpdate.minStorage = min;
        this.hostToUpdate.maxStorage = max;
        return this;
    }
}