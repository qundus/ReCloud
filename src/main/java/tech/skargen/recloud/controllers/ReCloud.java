package tech.skargen.recloud.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.skargen.recloud.components.simulation.ISimulationSequence;
import tech.skargen.recloud.controllers.interfaces.IExperiment;
import tech.skargen.recloud.controllers.interfaces.IExperimentSequence;
import tech.skargen.recloud.controllers.interfaces.IExperimentSet;
import tech.skargen.recloud.controllers.interfaces.IJobs;
import tech.skargen.recloud.controllers.interfaces.IJobsSet;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;
import tech.skargen.recloud.controllers.interfaces.IRecloudSet;
import tech.skargen.recloud.controllers.interfaces.IServers;
import tech.skargen.recloud.controllers.interfaces.IServersSet;
import tech.skargen.recloud.controllers.interfaces.IWindow;
import tech.skargen.recloud.controllers.interfaces.IWindowSet;

public final class ReCloud
    implements IRecloudSequence, IExperimentSequence, ISimulationSequence, IRecloud, IRecloudSet {
  private final Jobs jobs;
  private final Servers servers;
  private final Experiment experiment;
  private final Window window;

  private static Logger _LOG;
  static {
    _LOG = LogManager.getLogger();
  }

  public ReCloud() {
    this.jobs = new Jobs();
    this.servers = new Servers();
    this.experiment = new Experiment();
    this.window = new Window();
  }

  public static void launch(ReCloud instance) {
    try {
      instance.validate();
    } catch (Exception e) {
      _LOG.fatal("recloud can't start, initialization error occurd!", e);
      System.exit(0);
    }
  }

  @Override
  public IExperiment getExperiment() {
    return this.experiment;
  }

  @Override
  public IJobs getJobs() {
    return this.jobs;
  }

  @Override
  public IServers getServers() {
    return this.servers;
  }

  @Override
  public IWindow getWindow() {
    return this.window;
  }

  @Override
  public IExperimentSet experiment() {
    return this.experiment;
  }

  @Override
  public IJobsSet jobs() {
    return this.jobs;
  }

  @Override
  public IServersSet servers() {
    return this.servers;
  }

  @Override
  public IWindowSet window() {
    return this.window;
  }

  @Override
  public void validate() throws Exception {
    this.experiment.validate();
    this.jobs.validate();
    this.servers.validate();
    this.window.validate();
    this.init(this);
  }

  @Override
  public void init(IRecloud recloud) {
    this.experiment.init(recloud);
    this.jobs.init(recloud);
    this.servers.init(recloud);
    this.window.init(recloud);
    this.newSequence(this);
  }

  /**
   * Called everytime there's a new number of tasks to conduct a round of
   * simulations, it starts by informing the necessary controllers of the action.
   *
   * @return Number of tasks for the next round of simulations.
   */
  @Override
  public void newSequence(IRecloud recloud) {
    this.experiment.newSequence(recloud);
    this.jobs.newSequence(recloud);
    // this.servers.newSequence(recloud);
    this.window.newSequence(recloud);
    this.beforeSimulation(this);
  }

  /**
   * For every new simulation, cloudsim is re-intitated and with the help of the
   * other controllers, simulation attrbutes such as tasks, brokers,
   * datacenters..etc is populated by their corrosponding handlers.
   */
  @Override
  public void beforeSimulation(IRecloud recloud) {
    this.experiment.beforeSimulation(recloud);
    this.jobs.beforeSimulation(recloud);
    this.servers.beforeSimulation(recloud);
    this.window.beforeSimulation(recloud);

    this.afterSimulation(this);
  }

  /**
   * End current simulation and notify other controllers to prepare for next
   * simulation if any.
   */
  @Override
  public void afterSimulation(IRecloud recloud) {
    this.experiment.afterSimulation(recloud);
    this.jobs.afterSimulation(recloud);
    this.servers.afterSimulation(recloud);
    this.window.afterSimulation(recloud);

    if (this.experiment.allSimulationsDone()) {
      this.endSequence(this);
    } else {
      this.beforeSimulation(this);
    }
  }

  /** Inform controllers of the end of this simulations round. */
  @Override
  public void endSequence(IRecloud recloud) {
    this.experiment.endSequence(recloud);
    this.jobs.endSequence(recloud);
    // this.servers.endSequence(recloud);
    this.window.endSequence(recloud);

    if (this.experiment.allSequencesDone()) {
      this.finish(this);
    } else {
      this.newSequence(this);
    }
  }

  /** End simulations for all rounds and sign off experiments' files. */
  @Override
  public void finish(IRecloud recloud) {
    // this.experiment.finish(recloud);
    // this.jobs.finish(recloud);
    // this.servers.finish(recloud);
    this.window.finish(recloud);
  }
}
