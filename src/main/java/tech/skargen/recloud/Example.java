package tech.skargen.recloud;

import java.awt.Color;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;
import tech.skargen.recloud.components.cloudsim.CloudsimSimulation;
import tech.skargen.recloud.controllers.Jobs.TaskScheduler;
import tech.skargen.recloud.controllers.Jobs.TasksSplit;
import tech.skargen.recloud.controllers.ReCloud;
import tech.skargen.recloud.controllers.Servers.VmScheduler;
import tech.skargen.recloud.developers.cypherskar.aco.ACO_CS;
import tech.skargen.recloud.developers.cypherskar.bullet.Bullet_CS;
import tech.skargen.recloud.developers.cypherskar.bullet.Bullet_CS.GunType;
import tech.skargen.recloud.developers.cypherskar.honeybee.HoneyBee_CS;
import tech.skargen.recloud.developers.cypherskar.minmin.MM_CS;
import tech.skargen.recloud.developers.cypherskar.minmin.MM_CS.Variation;
import tech.skargen.recloud.developers.cypherskar.pso.IPSO_CS;
import tech.skargen.recloud.developers.cypherskar.pso.PSO_CS;
import tech.skargen.recloud.developers.cypherskar.pso.PSO_CS.Inertia;
import tech.skargen.recloud.developers.cypherskar.pso.PSO_CS.Position;
import tech.skargen.recloud.developers.cypherskar.shortestjobfirst.SJF_CS;
import tech.skargen.recloud.developers.sidjee.aco.ACO_Sidjee;
import tech.skargen.skartools.SNumbers.RandomStyle;

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
    // [1] : Create and Configure Datacenters and Hosts.
    recloud.servers()
        .newServer()
        .name("Server")
        .environment("x86", "Linux", "Xen")
        .timeZone(10.0)
        .secCost(3.0)
        .memCost(0.05)
        .storageCost(0.001)
        .bwCost(0.0)
        .intervals(0)
        .clones(1)
        .make();

    // Hosts.
    recloud.servers()
        .newHost()
        .on("Server")
        .mips(177730)
        .pes(6)
        .ram(16000)
        .bw(15000)
        .storage(4000000)
        .clones(2)
        .make();

    // [2]: Create and Configure Brokers, Virtual Machines and Task Types.
    recloud.jobs().newBroker().name("Cypher").make();
    recloud.jobs().newBroker().name("mama").make();
    recloud.jobs().newBroker().name("BEAR").make();

    // Virtual Machines.
    recloud.jobs()
        .newVm()
        .mips(9726)
        .pes(1)
        .ram2(9)
        .bw(1000)
        .image(10000)
        .vmm("Xen")
        .clones(5)
        .make();

    // Cloudlets (Tasks).
    recloud.jobs().taskSplit(TasksSplit.Random);
    recloud.jobs()
        .newTask()
        .randomStyle(RandomStyle.Fixed_Pace)
        .length(10000, 20000)
        .pes(1)
        .filesize(1)
        .outpusize(1)
        .make();
    recloud.jobs()
        .newTask()
        .randomStyle(RandomStyle.Fixed_Pace)
        .length(10000, 20000)
        .pes(1)
        .filesize(1)
        .outpusize(1)
        .make();

    // [3]: Add Wanted Simulations With Hints For Identification.
    recloud.experiment().newSimulations(VmScheduler.TimeShared, TaskScheduler.TimeShared,
        new CloudsimSimulation(), new ACO_CS(100, 1000, 0.05, 3, 2, 8, 0.01),
        new ACO_Sidjee(100, 1000, 0.05, 3, 2, 8, 0.01), new Bullet_CS(GunType.Magnum),
        new Bullet_CS(GunType.Bazooka).schedulers(VmScheduler.TimeShared, TaskScheduler.TimeShared),
        new SJF_CS(), new MM_CS(Variation.MinMin), new MM_CS(Variation.MaxMin),
        new PSO_CS(100, 1000, 0.1, 0.9, 1.49445, 1.49445, 5, Inertia.LDIW_1, Position.Sigmoid),
        new IPSO_CS(100, 1000, 0.1, 0.9, 1.49445, 1.49445, 5, Inertia.LDIW_1, Position.Sigmoid),
        new HoneyBee_CS(0.1));

    // [4]: Configure Experiment Settings.
    recloud.experiment().cloudsim(1, false);
    recloud.experiment().signiture("github.com/cypherskar");
    recloud.experiment().taskTargets(100, 200);

    // [5]: Setup Results Preferances (i.e. font size, background color...etc).
    // Setup View Monitor/Screen
    recloud.window().monitor(1);

    // Setup Tables
    recloud.window().tables().colors(Color.BLACK, Color.WHITE);

    // Setup Charts
    recloud.window().charts().gridlines(true, true);
    recloud.window().charts().legend(LegendPosition.OutsideE, LegendLayout.Vertical);
    recloud.window().charts().renderStyle(CategorySeriesRenderStyle.Bar);
    recloud.window().charts().theme(ChartTheme.XChart);

    // [6]: Setup Exportation methods.
    recloud.window().exports().image(BitmapFormat.JPG, 500);

    // Launch.
    ReCloud.launch(recloud);
  }
}
