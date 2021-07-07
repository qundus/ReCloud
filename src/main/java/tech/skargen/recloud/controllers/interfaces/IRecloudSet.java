package tech.skargen.recloud.controllers.interfaces;

public abstract interface IRecloudSet {
  public abstract IExperimentSet experiment();

  public abstract IJobsSet jobs();

  public abstract IServersSet servers();

  public abstract IWindowSet window();
}
