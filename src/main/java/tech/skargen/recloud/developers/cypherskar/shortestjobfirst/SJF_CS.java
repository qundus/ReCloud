package tech.skargen.recloud.developers.cypherskar.shortestjobfirst;

import java.util.Comparator;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

/**
 * Shortest Job First implementation by github.com/cypherskar.
 */
public class SJF_CS extends ASimulation {
  /** Create a Shortest Job First simulation scheduling algorithm. */
  public SJF_CS() {}

  @Override
  public String getAlgorithmName() {
    return "SJF";
  }

  @Override
  public String getAdditionalInfo() {
    return "none";
  }

  @Override
  public String getDeveloper() {
    return "cypherskar";
  }

  @Override
  public void startEntity(IReBroker rebroker) {}

  @Override
  public void shutdownEntity(IReBroker rebroker) {}

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    List<Cloudlet> cloudlets = rebroker.getEntity().getCloudletList();

    rebroker.updateProgressMax(cloudlets.size());
    rebroker.updateProgressMessage("Shortest Job First By github.com/cypherskar");

    Comparator<Cloudlet> comp = new Comparator<Cloudlet>() {
      public int compare(Cloudlet a, Cloudlet b) {
        return (int) ((a.getCloudletLength()) - (b.getCloudletLength()));
      }
    };

    cloudlets.sort(comp);
    rebroker.updateProgress(cloudlets.size());

    for (Cloudlet cloudlet : rebroker.getEntity().getCloudletList()) {
      rebroker.submitCloudlet(cloudlet);
      // rebroker.updateProgress(1);
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}
}