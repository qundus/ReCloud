package tech.cypherskar.cloudex.bridges;

import tech.cypherskar.cloudex.templates.setups.ABrokerSetup;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Bridges are disposable interfaces used within classes 
 * to control the instantiation  and/or update of a corrospondant 
 * data structure template.
 * 
 * <b>
 * <ul>
 * <li>HandlingClass: {@link tech.cypherskar.cloudex.controllers.Brokers}
 * <li>BridgeClass: {@link tech.cypherskar.cloudex.bridges.ABrokerSetupBridge}
 * <li>DataClass: {@link tech.cypherskar.cloudex.templates.setups.ABrokerSetup}
 * </ul>
 * </b> 
 * 
 * <p>
 * A Bridge reflects how the data class is used by the handling class
 * in a more clear and isolated nature.
 */
public class ABrokerSetupBridge 
{
    //  *****************************
    //  Fields
    //  *****************************
    private ObjectArrayList<ABrokerSetup> brokerList;
    private ABrokerSetup brokerToUpdate;
    
    
    //  *****************************
    //  Constructors
    //  *****************************

    /**
     * 
     * @param brokerList The list of brokers to be altered by this bridge.
     */
    public ABrokerSetupBridge(ObjectArrayList<ABrokerSetup> brokerList)
    {
        this.brokerList = brokerList;
        this.brokerToUpdate = new ABrokerSetup();
        this.brokerToUpdate.name = null;
        //this.brokerToUpdate.clones = 0;
    }

    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * <pre>#CHAIN_END</pre>
     * 
     * Commit changes to chain methods used.
     */
    public void Create()
    {
        if (brokerList == null) return;

        boolean addBroker = 
        this.brokerList.stream().noneMatch(b -> b.name.equals(this.brokerToUpdate.name));

        if (addBroker)
        {
            if (this.brokerToUpdate.name == null) this.brokerToUpdate.name = "Broker";
            //this.brokerToUpdate.clones = (clones <= 0)? 1 : clones;
            this.brokerToUpdate.taskIndeces = new IntArrayList();
            this.brokerToUpdate.vmIndeces = new IntArrayList();
            this.brokerList.add(this.brokerToUpdate);
        }
    }

    /**
     * <pre>#CHAIN_PART</pre>
     * 
     * @param name name of the broker.
     * @return This bridge instance to further method chaining.
     */
    public ABrokerSetupBridge Name(String name)
    {
        this.brokerToUpdate.name = name;
        return this;
    }
}