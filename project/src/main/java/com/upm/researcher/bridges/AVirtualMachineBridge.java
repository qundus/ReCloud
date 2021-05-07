package com.upm.researcher.bridges;

import com.upm.researcher.templates.setups.ABrokerSetup;
import com.upm.researcher.templates.setups.AVirtualMachine;
import com.upm.researcher.utils.NumberMixer.RandomStyle;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Bridges are disposable interfaces used within classes 
 * to control the instantiation  and/or update of a corrospondant 
 * data structure template.
 * 
 * <b>
 * <ul>
 * <li>HandlingClass: {@link com.upm.researcher.controllers.Brokers}
 * <li>BridgeClass: {@link com.upm.researcher.bridges.AVirtualMachineBridge}
 * <li>DataClass: {@link com.upm.researcher.templates.setups.AVirtualMachine}
 * </ul>
 * </b> 
 * 
 * <p>
 * A Bridge reflects how the data class is used by the handling class
 * in a more clear and isolated nature.
 */
public class AVirtualMachineBridge 
{
    //  *****************************
    //  Fields
    //  *****************************
    private ObjectArrayList<ABrokerSetup> brokerList;
    private ObjectArrayList<AVirtualMachine> vmList;
    private AVirtualMachine vmToUpdate;

    //  *****************************
    //  Constructors
    //  *****************************

    /**
     * @param brokerList The list of brokers to be altered by this bridge.
     * @param vmList The list of VMs to be altered by this bridge.
     */
    public AVirtualMachineBridge(ObjectArrayList<ABrokerSetup> brokerList,
    ObjectArrayList<AVirtualMachine> vmList)
    {
        this.vmList = vmList;
        this.brokerList = brokerList;

		this.vmToUpdate = new AVirtualMachine();
		this.vmToUpdate.vmm = "Xen";
		
		this.vmToUpdate.minMips = 9726;
		this.vmToUpdate.maxMips = -1;
        this.vmToUpdate.minPes = 1;
		this.vmToUpdate.maxPes = -1;
        this.vmToUpdate.minRam = 2048;
		this.vmToUpdate.maxRam = -1;
        this.vmToUpdate.minBw = 1000;
		this.vmToUpdate.maxBw = -1;
        this.vmToUpdate.minSize = 10000;
		this.vmToUpdate.maxSize = -1;
		
		this.vmToUpdate.clones = 1;
		this.vmToUpdate.randStyle = RandomStyle.Fixed_Pace;
    }

    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param randStyle The random numbers style pattern if used a range #CHAIN_PART
     * @param clones Number of clones wanted with the same settings.
     * @param brokers The brokers to assign this VM setup to.
     */
	public void Create(RandomStyle randStyle, int clones, String... brokers)
	{
		this.vmToUpdate.randStyle = randStyle;
		this.Create(clones, brokers);
	}

    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param clones Number of clones wanted with the same settings.
     * @param brokers The brokers to assign this VM setup to.
     */
    public void Create(int clones, String... brokers)
    {
		if (vmList == null) return;
		if (clones > 1) this.vmToUpdate.clones = clones;
        this.vmList.add(this.vmToUpdate);
        
        if (brokerList == null || brokerList.size() <= 0) return;
        if (brokers == null || brokers.length <= 0)
        {
            for(int i = 0; i < this.brokerList.size(); i++)
            {
                this.brokerList.get(i).vmIndeces.add(this.vmList.size()-1);
            }
        }
        else
        {
            for(int i = 0; i < brokers.length; i++)
            {
                final int j = i;
                ABrokerSetup setup = 
                this.brokerList.stream().filter(b -> b.name.equals(brokers[j])).findFirst().get();
                
                setup.vmIndeces.add(this.vmList.size()-1);
            }
        }
	}
	
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Virtual machine monitor.
     * @return This bridge instance to further method chaining.
     */
	public AVirtualMachineBridge Vmm(String value)
    {
        this.vmToUpdate.vmm = value;
        return this;
	}
	
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Processing speed in MIPS.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Mips(int value)
    {
        this.vmToUpdate.minMips = value;
        return this;
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Processing cores.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Pes(int value)
    {
        this.vmToUpdate.minPes = value;
        return this;
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value RAM in MBs.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Ram(int value) 
    {
        this.vmToUpdate.minRam = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value RAM as a power of 2.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Ram2(int value) 
    {
        this.vmToUpdate.minRam = (int)Math.pow(2, value);
        return this;
    }
    
    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Banswidth.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Bw(long value) 
    {
        this.vmToUpdate.minBw = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value Image size.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Image(long value) 
    {
        this.vmToUpdate.minSize = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Processing speed in MIPS,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Mips(double min, double max)
    {
        this.vmToUpdate.minMips = min;
        this.vmToUpdate.maxMips = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Processing cores,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Pes(int min, int max)
    {
		this.vmToUpdate.minPes = min;
        this.vmToUpdate.maxPes = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * RAM in MBs,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Ram(int min, int max) 
    {
		this.vmToUpdate.minRam = min;
        this.vmToUpdate.maxRam = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * RAM as a power of 2,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Ram2(int min, int max) 
    {
		this.vmToUpdate.minRam = (int)Math.pow(2, min);
		this.vmToUpdate.maxRam = (int)Math.pow(2, max);
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Banswidth,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Bw(long min, long max)
    {
        this.vmToUpdate.minBw = min;
        this.vmToUpdate.maxBw = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * Image size,
     * have to use {@link #Create(RandomStyle, int, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public AVirtualMachineBridge Image(long min, long max) 
    {
        this.vmToUpdate.minSize = min;
        this.vmToUpdate.maxSize = max;
        return this;
    }
}