package tech.skargen.recloud.controllers.interfaces;

public interface IRecloud {
  public abstract IExperiment getExperiment();

  public abstract IJobs getJobs();

  public abstract IServers getServers();

  public abstract IWindow getWindow();
}
