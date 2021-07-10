package tech.skargen.recloud;

import tech.skargen.recloud.components.cloudsim.CloudsimSimulation;
import tech.skargen.recloud.controllers.Jobs.TasksSplit;
import tech.skargen.recloud.controllers.ReCloud;
import tech.skargen.recloud.developers.cypherskar.bullet.Bullet_CS;
import tech.skargen.recloud.developers.cypherskar.bullet.Bullet_CS.GunType;
import tech.skargen.recloud.developers.cypherskar.pso.PSO_CS;
import tech.skargen.recloud.developers.cypherskar.pso.PSO_CS.Inertia;
import tech.skargen.recloud.developers.cypherskar.pso.PSO_CS.Position;

/**
 * Shows the steps to create and configure an experiment, try maintaining the
 * sequence of the steps because that's how cloudsim is usually initialized.
 */
public class Example {
  /**
   * Launch the app.
   * @param args main args
   */
  public static void main(String[] args) throws Exception {
    ReCloud recloud = new ReCloud();

    // [01]
    recloud.servers().newServer().name("server_1").make();
    recloud.servers().newServer().name("server_2").make();

    // [02]
    recloud.servers().newHost().on("server_1", "server_2").make();
    recloud.servers().newHost().on("server_1").make();

    // [03]
    recloud.servers().newHost().on("server_2").make();

    // [04]
    recloud.jobs().newBroker().name("koala_1").make();
    recloud.jobs().newBroker().name("koala_2").make();

    // [05]
    recloud.jobs().newVm().on("koala_1").make();
    recloud.jobs().newVm().on("koala_1").clones(3).make();

    // [06]
    recloud.jobs().newVm().on("koala_2").make();

    // [07]
    recloud.jobs().newTask().length(10000).pes(1).filesize(1).outpusize(1).make();

    // [08]
    recloud.jobs().newTask().on("koala_2").length(10000, 20000).pes(1).filesize(1).outpusize(1).make();

    // [09]
    recloud.jobs().taskSplit(TasksSplit.Random);

    // [10]
    recloud.experiment().newSimulations(
        new CloudsimSimulation(), 
        new Bullet_CS(GunType.Magnum),
        new PSO_CS(100, 1000, 0.1, 0.9, 1.49445, 1.49445, 5, Inertia.LDIW_1, Position.Sigmoid)
    );

    // [11]
    recloud.experiment().taskTargets(100, 200, 300);

    // Launch.
    ReCloud.launch(recloud);
  }
}
