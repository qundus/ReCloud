package tech.skargen.recloud.controllers.interfaces;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tech.skargen.recloud.templates.HostSetup;
import tech.skargen.recloud.templates.ServerSetup;

public abstract interface IServers {
  /**
   * Retrieve a list of created servers (datacenters).
   * @return Servers (Datacenters) list.
   */
  public abstract ObjectArrayList<ServerSetup> getServerList();

  /**
   * Retrieve a list of created hosts.
   * @return List of registered hosts.
   */
  public abstract ObjectArrayList<HostSetup> getHostList();

  /**
   * Populate servers and hosts table.
   *
   * @param serversGet Interface to get servers attributes.
   * @return Servers and host as detailed table.
   */
  public abstract StringBuilder generateTable();
}
