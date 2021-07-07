package tech.skargen.recloud.components.cloudsim;

import org.cloudbus.cloudsim.Cloudlet;
import tech.skargen.recloud.components.simulation.ASimulation;

public class CloudsimSimulation extends ASimulation {
  @Override
  public String getAlgorithmName() {
    return "Cloudsim Standard";
  }

  @Override
  public String getAdditionalInfo() {
    return "none";
  }

  @Override
  public String getDeveloper() {
    return "Cloudsim";
  }

  @Override
  public void startEntity(IReBroker rebroker) {}

  @Override
  public void shutdownEntity(IReBroker rebroker) {}

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    rebroker.updateProgressMax(rebroker.getEntity().getCloudletList().size());
    rebroker.updateProgressMessage("Cloudsim Standard Simulation");
    for (Cloudlet cloudlet : rebroker.getEntity().getCloudletList()) {
      rebroker.submitCloudlet(cloudlet);
      rebroker.updateProgress(1);
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}
}
