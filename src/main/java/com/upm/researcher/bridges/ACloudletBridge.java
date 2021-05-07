package com.upm.researcher.bridges;

import com.upm.researcher.templates.setups.ABrokerSetup;
import com.upm.researcher.templates.setups.ACloudlet;
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
 * <li>BridgeClass: {@link com.upm.researcher.bridges.ACloudletBridge}
 * <li>DataClass: {@link com.upm.researcher.templates.setups.ACloudlet}
 * </ul>
 * </b> 
 * 
 * <p>
 * A Bridge reflects how the data class is used by the handling class
 * in a more clear and isolated nature.
 */
public class ACloudletBridge
{
    //  *****************************
    //  Fields
    //  *****************************
    private ObjectArrayList<ABrokerSetup> brokerList;
    private ObjectArrayList<ACloudlet> taskList;
    private ACloudlet cloudletToUpdate;


    //  *****************************
    //  Constructors
    //  *****************************

    /**
     * @param brokerList The list of brokers to be altered by this bridge.
     * @param taskList The list of tasks to be altered by this bridge.
     */
    public ACloudletBridge(ObjectArrayList<ABrokerSetup> brokerList,
    ObjectArrayList<ACloudlet> taskList)
    {
        this.brokerList = brokerList;
        this.taskList = taskList;

        this.cloudletToUpdate = new ACloudlet();
        this.cloudletToUpdate.minLength = 1000;
        this.cloudletToUpdate.maxLength = -1;
        this.cloudletToUpdate.minPes = 1;
        this.cloudletToUpdate.maxPes = -1;
        this.cloudletToUpdate.minFileSize = 1;
        this.cloudletToUpdate.maxFileSize = -1;
        this.cloudletToUpdate.minOutputSize = 1;
        this.cloudletToUpdate.maxOutputSize = -1;
        this.cloudletToUpdate.randStyle = RandomStyle.Fixed_Pace;
    }

    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param randStyle The random numbers style pattern if used a range #CHAIN_PART
     * @param brokers The brokers to assign this task type to.
     */
	public void Create(RandomStyle randStyle, String... brokers)
	{
		this.cloudletToUpdate.randStyle = randStyle;
		this.Create(brokers);
	}

    /**
     * <pre>#CHAIN_END</pre>
     * Commit changes to chain methods used.
     * @param brokers The brokers to assign this task type to.
     */
    public void Create(String... brokers)
    {
        if (taskList == null) return;
        this.taskList.add(this.cloudletToUpdate);
        
        
        if (brokerList == null || brokerList.size() <= 0) return;
        if (brokers == null || brokers.length <= 0)
        {
            for(int i = 0; i < this.brokerList.size(); i++)
            {
                this.brokerList.get(i).taskIndeces.add(this.taskList.size()-1);
            }
        }
        else
        {
            for(int i = 0; i < brokers.length; i++)
            {
                final int j = i;
                ABrokerSetup setup = 
                this.brokerList.stream().filter(b -> b.name.equals(brokers[j])).findFirst().get();
                setup.taskIndeces.add(this.taskList.size()-1);
            }
        }
    }
    
    /**
     * <pre>#CHAIN_SOLO</pre>
     * Cancels changes to chain methods and uses only this one.
     * @param fileName The file name, the path is provided through Simulations.WorkloadFolder(String...).
     * @param rating The rating of the workload file.
     */
    public void CreateFromWorkload(String fileName, int rating)
    {        
        this.cloudletToUpdate = new ACloudlet();
        this.cloudletToUpdate.workloadFile = fileName;
        this.cloudletToUpdate.workLoadRating = rating;
        this.cloudletToUpdate.minPes = -21; // Send message to runtime loops by id -21
        this.taskList.add(this.cloudletToUpdate);
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value The length of the task in Million Instructions.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge Length(long value)
    {
        this.cloudletToUpdate.minLength = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value The number of cores required by this task.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge Pes(int value)
    {
        this.cloudletToUpdate.minPes = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value The size of the file.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge FileSize(long value)
    {
        this.cloudletToUpdate.minFileSize = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * @param value The output size of the file.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge OutpuSize(long value)
    {
        this.cloudletToUpdate.minOutputSize = value;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * The range of length of the task in Million Instructions,
     * have to use {@link #Create(RandomStyle, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge Length(long min, long max)
    {
        this.cloudletToUpdate.minLength = min;
        this.cloudletToUpdate.maxLength = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * The range number of cores required by this task,
     * have to use {@link #Create(RandomStyle, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge Pes(int min, int max)
    {
        this.cloudletToUpdate.minPes = min;
        this.cloudletToUpdate.maxPes = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * The range size of the file,
     * have to use {@link #Create(RandomStyle, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge FileSize(long min, long max)
    {
        this.cloudletToUpdate.minFileSize = min;
        this.cloudletToUpdate.maxFileSize = max;
        return this;
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * The range output size of the file,
     * have to use {@link #Create(RandomStyle, String...)} to commit
     * changes.
     * @return This bridge instance to further method chaining.
     */
    public ACloudletBridge OutpuSize(long min, long max)
    {
        this.cloudletToUpdate.minOutputSize = min;
        this.cloudletToUpdate.maxOutputSize = max;
        return this;
	}
}