package tech.skargen.recloud.controllers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.controllers.interfaces.IServers;
import tech.skargen.recloud.controllers.interfaces.IServersSet;
import tech.skargen.recloud.templates.HostSetup;
import tech.skargen.recloud.templates.ServerSetup;
import tech.skargen.skartools.SNumbers;
import tech.skargen.skartools.STable;
import tech.skargen.skartools.STable.EntryStyle;

/**
 * Responsible for creating and maintaining dataceneters and hosts.
 */
public final class Servers implements IRecloudSequence, ISimulationSequence, IServers, IServersSet {
  /** Virtual machine schedulers for cloudsim environment. */
  public enum VmScheduler {
    TimeShared,
    /** os = over subscribtion. */
    TimeShared_OS,
    SpaceShared,
  }

  private ObjectArrayList<ServerSetup> serverList;
  private ObjectArrayList<HostSetup> hostList;

  private static Logger _LOG;
  static {
    _LOG = LogManager.getLogger();
  }

  public Servers() {
    this.serverList = new ObjectArrayList<>();
    this.hostList = new ObjectArrayList<>();
  }

  /**
   * Retrieve a list of created servers (datacenters).
   *
   * @return Servers (Datacenters) list.
   */
  @Override
  public ObjectArrayList<ServerSetup> getServerList() {
    return this.serverList;
  }

  /**
   * Retrieve a list of created hosts.
   *
   * @return List of registered hosts.
   */
  @Override
  public ObjectArrayList<HostSetup> getHostList() {
    return this.hostList;
  }

  /**
   * Create a new server (datacenter) using a dedicated interface.
   *
   * @return New instance of server maker interface.
   */
  @Override
  public IMakeServer newServer() {
    return new IMakeServer(this);
  }

  /**
   * Create a new host using a dedicated interface.
   * @return New instance of host maker interface.
   */
  @Override
  public IMakeHost newHost() {
    return new IMakeHost(this);
  }

  /**
   * Add a server (datacenter) to list.
   */
  @Override
  public void newServer(ServerSetup setup) {
    ServerSetup.validate(setup);

    boolean nomatch = this.serverList.stream().noneMatch(b -> b.name.equals(setup.name));
    if (nomatch) {
      setup.storageList = new LinkedList<>();
      setup.hostIndeces = new IntArrayList();
      this.serverList.add(setup);
    } else {
      _LOG.info("server(datacenter) with name " + setup.name + "already exists, ignoring!.");
    }
  }

  /**
   * Add host to list.
   *
   * @param setup Host to be added.
   * @param names Names of servers to deploy this server on, leave null to add
   *              host to all servers.
   */
  @Override
  public void newHost(HostSetup setup, String... names) {
    HostSetup.validate(setup);

    final int setupIdx = this.hostList.size();
    if (names == null || names.length <= 0) {
      for (int i = 0; i < this.serverList.size(); i++) {
        this.serverList.get(i).hostIndeces.add(setupIdx);
      }
    } else {
      Optional<ServerSetup> filterVal;
      ServerSetup server;
      for (int i = 0; i < names.length; i++) {
        final int j = i;
        filterVal = this.serverList.stream().filter(b -> b.name.equals(names[j])).findFirst();
        if (filterVal.isPresent()) {
          server = filterVal.get();
          server.hostIndeces.add(setupIdx);
        } else {
          _LOG.warn("server  " + names[j] + " is not in the list, ignoring!");
        }
      }
    }

    this.hostList.add(setup);
  }

  /**
   * Populate hosts and add them to the appropriate datacenters.
   *
   * @param hostIndeces Host index pointers.
   * @param vmScheduler Virtual machine scheduler for cloudsim.
   * @param uid         Unique host id.
   * @return List of datacenter's hosts.
   */
  private List<Host> internal_getHosts(IntArrayList hostIndeces, VmScheduler vmScheduler, int uid) {
    List<Host> result = new ArrayList<>();
    try {
      for (int hostIdx : hostIndeces) {
        HostSetup s = this.hostList.get(hostIdx);

        for (int c = 0; c < s.clones; c++, uid++) {
          double mips = (s.rtmips == null) ? s.mips[0] : s.rtmips[c];
          int pes = (s.rtpes == null) ? s.pes[0] : s.rtpes[c];
          int ram = (s.rtram == null) ? s.ram[0] : s.rtram[c];
          long bw = (s.rtbw == null) ? s.bw[0] : s.rtbw[c];
          long size = (s.rtstorage == null) ? s.storage[0] : s.rtstorage[c];
          List<Pe> coresList = new ArrayList<Pe>(); // aka peList()

          for (int p = 0; p < pes; p++) {
            // mips/cores => MIPS value is cumulative for all cores so we divide
            // the MIPS value among all of the cores

            // need to store Pe id and MIPS Rating
            coresList.add(new Pe(c, new PeProvisionerSimple(mips / pes)));
            // coresList.add(new Pe(c, new PeProvisionerSimple(aHost.mips)));
          }

          // Create our machine will be
          RamProvisionerSimple rp = new RamProvisionerSimple(ram);
          BwProvisionerSimple bwp = new BwProvisionerSimple(bw);

          Host host = null;
          switch (vmScheduler) {
            case TimeShared:
              host = new Host(uid, rp, bwp, size, coresList, new VmSchedulerTimeShared(coresList));
              break;
            case TimeShared_OS:
              host = new Host(uid, rp, bwp, size, coresList,
                  new VmSchedulerTimeSharedOverSubscription(coresList));
              break;
            case SpaceShared:
              host = new Host(uid, rp, bwp, size, coresList, new VmSchedulerSpaceShared(coresList));
              break;
          }
          result.add(host);
        }
      }
    } catch (Exception e) {
      _LOG.fatal("can't generate cloudsim hosts", e);
      System.exit(0);
    }

    return result;
  }

  /**
   * Populate servers and hosts table.
   *
   * @param serversGet Interface to get servers attributes.
   * @return Servers and host as detailed table.
   */
  @Override
  public StringBuilder generateTable() {
    // Datacenter specs table
    STable stable = new STable();

    stable.newTable(EntryStyle.Horizontle, 13, 20, '|', ' ', '|');
    stable.addEntry(0, "Specs \\ Name", "(id)");
    stable.addEntry(-1, "Architecture");
    stable.addEntry(-1, "Operating System");
    stable.addEntry(-1, "Vm Monitor", "(VMM)");
    stable.addEntry(-1, "Time Zone");
    stable.addEntry(-1, "Second Cost");
    stable.addEntry(-1, "Memory Cost");
    stable.addEntry(-1, "Bandwidth Cost");
    stable.addEntry(-1, "Storage Cost");
    stable.addEntry(-1, "Scheduling Interval");
    stable.addEntry(-1, "Hosts", "(Per Clone)");
    stable.addEntry(-1, "Clones");
    stable.addEntry(-1, "Random Style", "(If Any)");

    int i = 0;
    for (ServerSetup setup : getServerList()) {
      stable.addCell(-1, setup.name, i);
      stable.addCell(-1, setup.architecture);
      stable.addCell(-1, setup.os);
      stable.addCell(-1, setup.vmm);
      stable.addCell(1, "", setup.timezones);
      stable.addCell(1, "", setup.seccosts);
      stable.addCell(1, "", setup.memcosts);
      stable.addCell(1, "", setup.bwcosts);
      stable.addCell(1, "", setup.storagecosts);
      stable.addCell(1, "", setup.intervals);
      stable.addCell(1, "", setup.hostIndeces);
      stable.addCell(0, String.valueOf(setup.clones));
      stable.addCell(-1, setup.randomStyle.name());
      i++;
    }

    // Hosts specs table
    StringBuilder result = stable.endTable(0, '-', '+', "Servers");
    stable.newTable(EntryStyle.Horizontle, 8, 20, '|', ' ', '|');
    stable.addEntry(0, "Specs \\ Host", "(id)");
    stable.addEntry(-1, "Mips");
    stable.addEntry(-1, "Processing Elements", "(Cores-PEs)");
    stable.addEntry(-1, "Memory", "(Ram in MB)");
    stable.addEntry(-1, "Bandwidth", "(in MB\\S)");
    stable.addEntry(-1, "Storage", "(in MBs)");
    stable.addEntry(-1, "Clones");
    stable.addEntry(-1, "Random Style", "(If Any)");

    i = 0;
    for (HostSetup setup : getHostList()) {
      stable.addCell(-1, "Host", i);
      stable.addCell(-1, "", setup.mips);
      stable.addCell(-1, "", setup.pes);
      stable.addCell(-1, "", setup.ram);
      stable.addCell(-1, "", setup.bw);
      stable.addCell(-1, "", setup.storage);
      stable.addCell(0, String.valueOf(setup.clones));
      stable.addCell(-1, setup.randomStyle.name());
      i++;
    }

    result.append(stable.endTable(0, '-', '+', "Hosts"));
    return result;
  }

  /**
   * Confirms that user setup settings are valid and will cause no issues in the
   * upcoming experiment.
   */
  @Override
  public void validate() throws Exception {
    // Remove Datacenters With No Assigned Hosts.
    for (int i = 0; i < this.serverList.size(); i++) {
      ServerSetup s = this.serverList.get(i);
      if (s.hostIndeces.size() <= 0) {
        _LOG.warn("datacenter (server) with name " + s.name + " is removed, has no hosts assigned");
        this.serverList.remove(i);
        i--;
      }
    }

    if (this.serverList.size() <= 0) {
      throw new Exception("no servers (datacenters) created.");
    }

    if (this.hostList.size() <= 0) {
      throw new Exception("no host has been created");
    }
  }

  @Override
  public void init(IRecloud recloud) {
    SNumbers mixer = new SNumbers();
    for (int i = 0; i < this.serverList.size(); i++) {
      ServerSetup s = this.serverList.get(i);
      s.rttimezones = mixer.create(s.clones, s.randomStyle, s.timezones);
      s.rtseccosts = mixer.create(s.clones, s.randomStyle, s.seccosts);
      s.rtmemcosts = mixer.create(s.clones, s.randomStyle, s.memcosts);
      s.rtbwcosts = mixer.create(s.clones, s.randomStyle, s.bwcosts);
      s.rtstoragecosts = mixer.create(s.clones, s.randomStyle, s.storagecosts);
      s.rtintervals = mixer.create(s.clones, s.randomStyle, s.intervals);
    }

    for (int i = 0; i < this.hostList.size(); i++) {
      HostSetup s = this.hostList.get(i);
      s.rtmips = mixer.create(s.clones, s.randomStyle, s.mips);
      s.rtpes = mixer.create(s.clones, s.randomStyle, s.pes);
      s.rtram = mixer.create(s.clones, s.randomStyle, s.ram);
      s.rtbw = mixer.create(s.clones, s.randomStyle, s.bw);
      s.rtstorage = mixer.create(s.clones, s.randomStyle, s.storage);
    }
  }

  /** Create list of datacenters for new simulation. */
  @Override
  public void beforeSimulation(IRecloud recloud) {
    ObjectArrayList<Datacenter> datacenters = new ObjectArrayList<>();
    try {
      int hostUID = 0;
      for (int i = 0; i < this.serverList.size(); i++) {
        ServerSetup ads = this.serverList.get(i);
        VmScheduler vmScheduler = recloud.getExperiment().getSimulation().getVmScheduler();
        for (int c = 0; c < ads.clones; c++) {
          // Copying affects performance so, have to create new hosts list for every
          // datacenter.
          // hosts = (hosts == null)? this.GetHosts(aHost, simSetup.vmSchedulerClass) :
          // new ArrayList<>(hosts);
          List<Host> hosts = internal_getHosts(ads.hostIndeces, vmScheduler, hostUID);

          DatacenterCharacteristics dc = new DatacenterCharacteristics(ads.architecture, ads.os,
              ads.vmm, hosts, (ads.rttimezones == null) ? ads.timezones[0] : ads.rttimezones[c],
              (ads.rtseccosts == null) ? ads.seccosts[0] : ads.rtseccosts[c],
              (ads.rtmemcosts == null) ? ads.memcosts[0] : ads.rtmemcosts[c],
              (ads.rtbwcosts == null) ? ads.bwcosts[0] : ads.rtbwcosts[c],
              (ads.rtstoragecosts == null) ? ads.storagecosts[0] : ads.rtstoragecosts[c]);

          datacenters.add(new Datacenter(ads.name + '[' + c + ']', dc,
              new VmAllocationPolicySimple(hosts), new LinkedList<>(ads.storageList),
              (ads.rtintervals == null) ? ads.intervals[0] : ads.rtintervals[c]));

          hostUID += hosts.size();
        }
      }
    } catch (Exception e) {
      _LOG.fatal("can't generate cloudsim servers (datacenters)", e);
      System.exit(0);
    }
  }

  @Override
  public void afterSimulation(IRecloud recloud) {}

  @Override
  public void finish(IRecloud recloud) {}
}