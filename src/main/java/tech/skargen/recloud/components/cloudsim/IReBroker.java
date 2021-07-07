package tech.skargen.recloud.components.cloudsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;

public abstract interface IReBroker {
  /**
   * Update the current progress value by the given number.
   * @param value Number to update progress by.
   */
  public abstract void updateProgress(int value);

  /**
   * Set a title to be displayed on the progrees bar during runtime.
   * @param title Title text.
   */
  public abstract void updateProgressMessage(String title);

  /**
   * Update the maximum bound that the progress bar is trying to achieve.
   * @param value Max bound of progress bar.
   */
  public abstract void updateProgressMax(int value);

  /**
   * Provides this rebroker as the backbone or inherited class that is
   * {@link org.cloudbus.cloudsim.DatacenterBroker}
   *
   * @return DatacenterBroker enitity.
   */
  public abstract DatacenterBroker getEntity();

  /**
   * Submit cloudlet to CloudSim through ReCloud's system.
   * @param task Task to be submitted.
   */
  public <T extends Cloudlet> void submitCloudlet(T task);
}
