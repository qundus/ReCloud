package tech.skargen.recloud.controllers.interfaces;

import java.util.LinkedList;
import org.cloudbus.cloudsim.Storage;
import tech.skargen.recloud.templates.HostSetup;
import tech.skargen.recloud.templates.ServerSetup;
import tech.skargen.skartools.SArrays;
import tech.skargen.skartools.SNumbers.RandomStyle;

public abstract interface IServersSet {
  /**
   * Create a new server (datacenter) using a dedicated interface.
   * @return New instance of server maker interface.
   */
  public abstract IMakeServer newServer();

  /**
   * Create a new host using a dedicated interface.
   * @return New instance of host maker interface.
   */
  public abstract IMakeHost newHost();

  /**
   * Add server (datacenter) to list.
   * @param setup server (datacenter) to be added.
   */
  public abstract void newServer(ServerSetup setup);

  /**
   * Add host to list.
   *
   * @param setup Host to be added.
   * @param names Names of servers to deploy this server on, leave null to add
   *              host to all servers.
   */
  public abstract void newHost(HostSetup setup, String... names);

  public final class IMakeServer {
    private final IServersSet servers;

    private final ServerSetup setup;

    public IMakeServer(IServersSet servers) {
      this.servers = servers;
      this.setup = new ServerSetup();
    }

    /**Confirm setup cutomization and add it to list.*/
    public void make() {
      this.servers.newServer(this.setup);
    }

    /**
     * Unique string identifier of server.
     *
     * @param name Name of the server.
     * @return This maker interface.
     */
    public IMakeServer name(String name) {
      this.setup.name = name;
      return this;
    }

    /**
     * Machine setup specifics.
     *
     * @param arch example "x86".
     * @param os   example "Linux".
     * @param vmm  example "Xen".
     * @return This maker interface.
     */
    public IMakeServer environment(String arch, String os, String vmm) {
      this.setup.architecture = arch;
      this.setup.os = os;
      this.setup.vmm = vmm;
      return this;
    }

    /**
     * Time zone of the server.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeServer timeZone(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.timezones = values;
      return this;
    }

    /**
     * Second cost.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeServer secCost(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.seccosts = values;
      return this;
    }

    /**
     * Memory cost.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeServer memCost(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.memcosts = values;
      return this;
    }

    /**
     * Storage cost.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeServer storageCost(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.storagecosts = values;
      return this;
    }

    /**
     * Bandwidth cost.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeServer bwCost(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.bwcosts = values;
      return this;
    }

    /**
     * Scheduling interval.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeServer intervals(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.intervals = values;
      return this;
    }

    /**
     * Storage list of server.
     *
     * @param storageList Storage list of the machine.
     * @return This maker interface.
     */
    public IMakeServer storage(LinkedList<Storage> storageList) {
      this.setup.storageList = storageList;
      return this;
    }

    /**
     * Number of identical servers to be created.
     *
     * @param clones Number of clones to produce during runtime.
     * @return This maker interface.
     */
    public IMakeServer clones(int clones) {
      this.setup.clones = clones;
      return this;
    }

    /**
     * This random pattern style decides how the random numbers are generated during
     * runtime.
     *
     * <pre></pre>
     *
     * Random values get activated once more than 1 value is passed to a method like
     * {@link #timeZone(double, double...)}, then an array containing all random
     * values is generated and referred to duting runtime.
     *
     * To further explain this, consider the following example:
     * <ol>
     * <li>{@code value(22)}, assigns only 22 as the value of this field.
     * <li>{@code values(22,55)}, assigns a random value between 22 and 55.
     * <li>{@code values(22,55,99,...)}, assigns a random value from the given
     * numbers as the value of this field.
     * </ol>
     *
     * @param style The random numbers style pattern.
     * @return This maker interface.
     */
    public IMakeServer randomStyle(RandomStyle style) {
      this.setup.randomStyle = style;
      return this;
    }
  }

  public final class IMakeHost {
    private final IServersSet servers;

    private final HostSetup setup;

    private String[] serverNames;

    public IMakeHost(IServersSet servers) {
      this.servers = servers;
      this.setup = new HostSetup();
    }

    /** Confirm setup cutomization and add it to list. */
    public void make() {
      this.servers.newHost(this.setup, this.serverNames);
    }

    /**
     * Make hosts on specific servers (datacenters) for manually assigning them.
     *
     * @param names Names of servers to assign this host to.
     * @return This maker interface.
     */
    public IMakeHost on(String... names) {
      this.serverNames = names;
      return this;
    }

    /**
     * Processing speed in Million Instructions Per Second (MIPS).
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeHost mips(double value, double... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.mips = values;
      return this;
    }

    /**
     * Processing cores.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeHost pes(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.pes = values;
      return this;
    }

    /**
     * RAM in MBs; as a power of 2, like 256 and 512.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeHost ram(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.ram = values;
      return this;
    }

    /**
     * RAM to be a power of 2, like 5 and 8 so it can be used as 2^5 and 2^8.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeHost ram2(int value, int... values) {
      values = SArrays.insert(values, 0, value);
      for (int i = 0; i < values.length; i++) {
        values[i] = (int) Math.pow(2, values[i]);
      }
      this.setup.ram = values;
      return this;
    }

    /**
     * Bandwidth.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeHost bw(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.bw = values;
      return this;
    }

    /**
     * Storage value in MBs.
     *
     * @param value  Mandatory value for this field.
     * @param values Values to generate random numbers array, checkout
     *               #{@link #randomStyle(RandomStyle)}.
     * @return This maker interface.
     */
    public IMakeHost storage(long value, long... values) {
      values = SArrays.insert(values, 0, value);
      this.setup.storage = values;
      return this;
    }

    /**
     * Number of identical hosts to be created.
     *
     * @param clones Number of clones to produce during runtime.
     * @return This maker interface.
     */
    public IMakeHost clones(int clones) {
      this.setup.clones = clones;
      return this;
    }

    /**
     * This random pattern style decides how the random numbers are generated during
     * runtime.
     *
     * <pre></pre>
     *
     * Random values get activated once more than 1 value is passed to a method like
     * {@link #timeZone(double, double...)}, then an array containing all random
     * values is generated and referred to duting runtime.
     *
     * To further explain this, consider the following example:
     * <ol>
     * <li>{@code value(22)}, assigns only 22 as the value of this field.
     * <li>{@code values(22,55)}, assigns a random value between 22 and 55.
     * <li>{@code values(22,55,99,...)}, assigns a random value from the given
     * numbers as the value of this field.
     * </ol>
     *
     * @param style The random numbers style pattern.
     * @return This maker interface.
     */
    public IMakeHost randomStyle(RandomStyle style) {
      this.setup.randomStyle = style;
      return this;
    }
  }
}
