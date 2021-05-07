package com.upm.researcher.controllers;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import com.upm.researcher.bridges.ADatacenterSetupBridge;
import com.upm.researcher.bridges.AHostBridge;
import com.upm.researcher.components.window.Tables;
import com.upm.researcher.templates.setups.ADatacenterSetup;
import com.upm.researcher.templates.setups.AHost;
import com.upm.researcher.utils.NumberMixer;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Responsible for creating and maintaining dataceneters and hosts.
 */
public final class Datacenters
{
	//  *****************************
    //  Enums
    //  *****************************

    /**Virtual machine schedulers for cloudsim environment.*/
	public enum VmScheduler
	{
        TimeShared,
        TimeShared_OS, // os = over subscribtion
        SpaceShared,
    }
    
	//  *****************************
    //  Fields
    //  *****************************
    private static ObjectArrayList<ADatacenterSetup> datacenterList;
    private static ObjectArrayList<AHost> hostList;

    static
    {
        datacenterList = new ObjectArrayList<>();
        hostList = new ObjectArrayList<>();
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
        System.out.print(System.lineSeparator() + "Validating Datacenters -----");
		NumberMixer mixer = new NumberMixer();
		if (datacenterList.size() <= 0)
        {
			System.out.print(String.format(System.lineSeparator() + format + " | "
            + "Use Datacenters.List() To Create Some. "
            , "*Datacenter List Is Empty"));
		}
		else
		{
			// Remove Datacenters With No Assigned Hosts.
			for (int i = 0; i < datacenterList.size(); i++)
			{
				ADatacenterSetup setup = datacenterList.get(i);
				if (setup.hostIndeces.size() <= 0)
				{
					System.out.print(String.format(System.lineSeparator()
					+ format + " | Missing Hosts To Be Assigned"
					, "Datacenter -> " + setup.name + " Is Removed"));
					datacenterList.remove(i);
					i--;
				}
				else
				{
					if (setup.maxTimeZone > setup.minTimeZone)
					setup.timeZones = 
					mixer.Create(setup.clones, setup.randStyle, setup.minTimeZone, setup.maxTimeZone);
					if (setup.maxSecCost > setup.minSecCost)
					setup.secCosts = 
					mixer.Create(setup.clones, setup.randStyle, setup.minSecCost, setup.maxSecCost);
					if (setup.maxMemCost > setup.minMemCost)
					setup.memCosts = 
					mixer.Create(setup.clones, setup.randStyle, setup.minMemCost, setup.maxMemCost);
					if (setup.maxBwCost > setup.minBwCost)
					setup.BwCosts = 
					mixer.Create(setup.clones, setup.randStyle, setup.minBwCost, setup.maxBwCost);
					if (setup.maxStorageCost > setup.minStorageCost)
					setup.storageCosts = 
					mixer.Create(setup.clones, setup.randStyle, setup.minStorageCost, setup.maxStorageCost);
					if (setup.maxInterval > setup.minInterval)
					setup.intervals = 
					mixer.Create(setup.clones, setup.randStyle, setup.minInterval, setup.maxInterval);
				}
			}
		}

        if (hostList.size() <= 0)
        {
            System.out.print(String.format(System.lineSeparator() + format + " | "
            + "Use Datacenters.Hosts() To Create Some. "
            , "*Host List Is Empty"));
		}
		else
		{
			for (int i = 0; i < hostList.size(); i++)
			{
				AHost host = hostList.get(i);

				if (host.maxMips > host.minMips)
				host.mips = mixer.Create(host.clones, host.randStyle, host.minMips, host.maxMips);
				if (host.maxPes > host.minPes)
				host.pes = mixer.Create(host.clones, host.randStyle, host.minPes, host.maxPes);
				if (host.maxRam > host.minRam)
				host.rams = mixer.Create(host.clones, host.randStyle, host.minRam, host.maxRam);
				if (host.maxBw > host.minBw)
				host.bws = mixer.Create(host.clones, host.randStyle, host.minBw, host.maxBw);
				if (host.maxStorage > host.minStorage)
				host.storages = mixer.Create(host.clones, host.randStyle, host.minStorage, host.maxStorage);
			}
		}

        System.out.print("--> DONE" + System.lineSeparator());


        return datacenterList.size() > 0 && hostList.size() > 0;
	}
	
	/**
	 * Create list of datacenters for new simulation.
	 * @param vmScheduler Virtual machine scheduler for cloudsim.
	 * @return Datacenters list.
	 */
	protected static Datacenter[] NewSimulation(VmScheduler vmScheduler) 
    {
		ObjectArrayList<Datacenter> datacenters = new ObjectArrayList<>();
		try
        {
            int hostUID = 0;
            for (int i = 0; i < datacenterList.size(); i++)
            {
                ADatacenterSetup ads = datacenterList.get(i);
                
                for (int c = 0; c < ads.clones; c++)
                {
                    // Copying affects performance so, have to create new hosts list for every datacenter.
                    //hosts = (hosts == null)? this.GetHosts(aHost, simSetup.vmSchedulerClass) : new ArrayList<>(hosts); 
                    List<Host> hosts = GetHosts(ads.hostIndeces, vmScheduler, hostUID);
                    
                    DatacenterCharacteristics dc = 
                    new DatacenterCharacteristics(ads.architecture, ads.os, ads.vmm, hosts,
                    (ads.timeZones == null)? ads.minTimeZone: ads.timeZones[c],
                    (ads.secCosts == null)? ads.minSecCost: ads.secCosts[c],
                    (ads.memCosts == null)? ads.minMemCost: ads.memCosts[c],
                    (ads.BwCosts == null)? ads.minBwCost: ads.BwCosts[c],
                    (ads.storageCosts == null)? ads.minStorageCost: ads.storageCosts[c]);
					
					datacenters.add(
					new Datacenter(ads.name + '[' + c + ']', dc, 
					new VmAllocationPolicySimple(hosts),
					new LinkedList<>(ads.storageList), 
					(ads.intervals == null)? ads.minInterval:ads.intervals[c]));

                    hostUID += hosts.size();
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
		}

		return datacenters.toArray(new Datacenter[0]);
	}

	//  *****************************
    //  Private Methods
    //  *****************************

	 /**
	  * Populate hosts and add them to the appropriate datacenters.
	  * @param hostIndeces Host index pointers.
	  * @param vmScheduler Virtual machine scheduler for cloudsim.
	  * @param uid Unique host id.
	  * @return List of datacenter's hosts.
	  */
    private static List<Host> GetHosts(IntArrayList hostIndeces, VmScheduler vmScheduler, int uid) 
    {
        List<Host> result = new ArrayList<>();

        for(int hostIdx : hostIndeces)
        {
            AHost aHost = hostList.get(hostIdx);

            for(int c = 0; c < aHost.clones; c++, uid++)
            {
				double mips = (aHost.mips == null)? aHost.minMips:aHost.mips[c];
				int pes = (aHost.pes == null)? aHost.minPes:aHost.pes[c];
				int ram = (aHost.rams == null)? aHost.minRam:aHost.rams[c];
				long bw = (aHost.bws == null)? aHost.minBw:aHost.bws[c];
				long storage = (aHost.storages == null)? aHost.minStorage:aHost.storages[c];
				List<Pe> coresList = new ArrayList<Pe>(); // aka peList()
                
                for (int p = 0; p < pes; p++)
                {
                    // mips/cores => MIPS value is cumulative for all cores so we divide
                    // the MIPS value among all of the cores

                    // need to store Pe id and MIPS Rating
                    coresList.add(new Pe(c, new PeProvisionerSimple(mips / pes)));
                    //coresList.add(new Pe(c, new PeProvisionerSimple(aHost.mips)));
                }
    
                // Create our machine will be
                RamProvisionerSimple rp = new RamProvisionerSimple(ram);
                BwProvisionerSimple bwp = new BwProvisionerSimple(bw);
                
                Host host = null;
                switch(vmScheduler)
                {
                    case TimeShared:
                        host = new Host(uid, rp, bwp, storage, coresList,
                        new VmSchedulerTimeShared(coresList));
                        break;
                    case TimeShared_OS:
                        host = new Host(uid, rp, bwp, storage, coresList,
                        new VmSchedulerTimeSharedOverSubscription(coresList));
                        break;
                    case SpaceShared:
                        host = new Host(uid, rp, bwp, storage, coresList,
                        new VmSchedulerSpaceShared(coresList));
                        break;
                }
                result.add(host); 
            }
        }

        return result;
	}

    //  *****************************
    //  Public Methods
    //  *****************************

	/**
	 * Populate datacenters and hosts details text to better provide 
	 * setup data. 
	 * @param tables Object responsible for displaying text onto screen.
	 * @return Detailed cloudism environement setup.
	 */
	public String[] GetSetupTable(Tables tables)
	{
		String[] specs = new String[13];

		// Datacenter specs table
		specs[0] = tables.CellFormat(tables.cellSeperator+"Specs \\ Name", "-", "index", false);
		specs[1] = tables.CellFormat(tables.cellSeperator+"Architecture", "-", null, false);
		specs[2] = tables.CellFormat(tables.cellSeperator+"Operating System", "-", null, false);
		specs[3] = tables.CellFormat(tables.cellSeperator+"Vm Monitor", "-", null, false);
		specs[4] = tables.CellFormat(tables.cellSeperator+"Time Zone", "-", null, false);
		specs[5] = tables.CellFormat(tables.cellSeperator+"Second Cost", "-", null, false);
		specs[6] = tables.CellFormat(tables.cellSeperator+"Memory Cost", "-", null, false);
		specs[7] = tables.CellFormat(tables.cellSeperator+"Bandwidth Cost", "-", null, false);
		specs[8] = tables.CellFormat(tables.cellSeperator+"Storage Cost", "-", null, false);
		specs[9] = tables.CellFormat(tables.cellSeperator+"Scheduling Interval", "-", null, false);
		specs[10] = tables.CellFormat(tables.cellSeperator+"Clones", "-", null, false);
		specs[11] = tables.CellFormat(tables.cellSeperator+"Random Style", "-", "If Any", false);
		specs[12] = tables.CellFormat(tables.cellSeperator+"Hosts", "-", "Per Clone", false);
		
		for (int i = 0; i < datacenterList.size(); i++)
		{
			ADatacenterSetup setup = datacenterList.get(i);
			specs[0] += tables.CellFormat(setup.name, "-", "" + i, false);
			specs[1] += tables.CellFormat(setup.architecture, "-", null, false);
			specs[2] += tables.CellFormat(setup.os, "-", null, false);
			specs[3] += tables.CellFormat(setup.vmm, "-", null, false);
			
			if (setup.maxTimeZone > setup.minTimeZone)
			specs[4] += tables.CellFormat(setup.minTimeZone+"-"+setup.maxTimeZone, "", null, false);
			else
			specs[4] += tables.CellFormat(setup.minTimeZone+"", "", null, false);

			if (setup.maxSecCost > setup.minSecCost)
			specs[5] += tables.CellFormat(setup.minSecCost+"-"+setup.maxSecCost, "", null, false);
			else
			specs[5] += tables.CellFormat(setup.minSecCost+"", "", null, false);

			if (setup.maxMemCost > setup.minMemCost)
			specs[6] += tables.CellFormat(setup.minMemCost+"-"+setup.maxMemCost, "", null, false);
			else
			specs[6] += tables.CellFormat(setup.minMemCost+"", "", null, false);

			if (setup.maxBwCost > setup.minBwCost)
			specs[7] += tables.CellFormat(setup.minBwCost+"-"+setup.maxBwCost, "", null, false);
			else
			specs[7] += tables.CellFormat(setup.minBwCost+"", "", null, false);

			if (setup.maxStorageCost > setup.minStorageCost)
			specs[8] += tables.CellFormat(setup.minStorageCost+"-"+setup.maxStorageCost, "", null, false);
			else
			specs[8] += tables.CellFormat(setup.minStorageCost+"", "", null, false);

			if (setup.maxInterval > setup.minInterval)
			specs[9] += tables.CellFormat(setup.minInterval+"-"+setup.maxInterval, "", null, false);
			else
			specs[9] += tables.CellFormat(setup.minInterval+"", "", null, false);
			
			specs[10] += tables.CellFormat(setup.clones+"", "", null, false);
			specs[11] += tables.CellFormat(setup.randStyle.name(), "-", null, false);
			specs[12] += tables.CellFormat(setup.hostIndeces.toString(), "-", "Indeces", false);
		}

		// Hosts specs table
		specs[0] += tables.CellFormat(" ", "-", null, false);
		specs[0] += tables.CellFormat("Specs \\ Host", "-", "index", false);
		specs[1] += tables.CellFormat(" ", "-", null, false);
		specs[1] += tables.CellFormat("Mips", "-", null, false);
		specs[2] += tables.CellFormat(" ", "-", null, false);
		specs[2] += tables.CellFormat("Processing Elements", "-", "Cores", false);
		specs[3] += tables.CellFormat(" ", "-", null, false);
		specs[3] += tables.CellFormat("Memory (Ram)", "-", "MB", false);
		specs[4] += tables.CellFormat(" ", "-", null, false);
		specs[4] += tables.CellFormat("Bandwidth", "-", "MB\\S", false);
		specs[5] += tables.CellFormat(" ", "-", null, false);
		specs[5] += tables.CellFormat("Storage", "-", "MBs", false);
		specs[6] += tables.CellFormat(" ", "-", null, false);
		specs[6] += tables.CellFormat("Clones", "-", null, false);
		specs[7] += tables.CellFormat(" ", "-", null, false);
		specs[7] += tables.CellFormat("Random Style", "-", "If Any", false);

		for (int i = 0; i < hostList.size(); i++)
		{
			AHost host = hostList.get(i);
			specs[0] += tables.CellFormat("Host Type", "-", "" + i, false);

			if (host.maxMips > host.minMips)
			specs[1] += tables.CellFormat(host.minMips+"-"+host.maxMips, "", null, false);
			else
			specs[1] += tables.CellFormat(host.minMips+"", "", null, false);

			if (host.maxPes > host.minPes)
			specs[2] += tables.CellFormat(host.minPes+"-"+host.maxPes, "", null, false);
			else
			specs[2] += tables.CellFormat(host.minPes+"", "", null, false);

			if (host.maxRam > host.minRam)
			specs[3] += tables.CellFormat(host.minRam+"-"+host.maxRam, "", null, false);
			else
			specs[3] += tables.CellFormat(host.minRam+"", "", null, false);

			if (host.maxBw > host.minBw)
			specs[4] += tables.CellFormat(host.minBw+"-"+host.maxBw, "", null, false);
			else
			specs[4] += tables.CellFormat(host.minBw+"", "", null, false);

			if (host.maxStorage > host.minStorage)
			specs[5] += tables.CellFormat(host.minStorage+"-"+host.maxStorage, "", null, false);
			else
			specs[5] += tables.CellFormat(host.minStorage+"", "", null, false);

			specs[6] += tables.CellFormat(host.clones+"", "", null, false);
			specs[7] += tables.CellFormat(host.randStyle.name(), "-", null, false);
		}

		return specs;
	}

    /**
     * <pre>#CHAIN_START</pre>
     * Connects to datacenters editing interface.
     * @return The bridge for editing datacenters.
	 */
    public static ADatacenterSetupBridge List()
    {
        return new ADatacenterSetupBridge(datacenterList);
    }

	/**
     * <pre>#CHAIN_START</pre>
     * Connects to hosts editing interface.
     * @return The bridge for editing hosts.
	 */
    public static AHostBridge Hosts()
    {
        return new AHostBridge(datacenterList, hostList);
    }
}