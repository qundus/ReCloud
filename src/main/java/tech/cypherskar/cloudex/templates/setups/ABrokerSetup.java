package tech.cypherskar.cloudex.templates.setups;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**A class to store the brokers data*/
public class ABrokerSetup 
{
    //public int clones;
    /**
     * Name of the broker
     */
    public String name;

    /**
     * The indeces of the tasks/cloudlets stored by Brokers in object
     * {@link tech.cypherskar.cloudex.controllers.Brokers.taskList}
     */
    public IntArrayList taskIndeces;

    /**
     * The indeces of the virtual machines stored by Brokers in object
     * {@link tech.cypherskar.cloudex.controllers.Brokers.vmList}
     */
    public IntArrayList vmIndeces;
}