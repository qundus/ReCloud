package tech.cypherskar;

import java.awt.Color;
import tech.cypherskar.cloudex.components.algorithms.aco.ACO;
import tech.cypherskar.cloudex.components.algorithms.bullet.Bullet;
import tech.cypherskar.cloudex.components.algorithms.bullet.Bullet.GunType;
import tech.cypherskar.cloudex.components.algorithms.honeybee.LBA_HB;
import tech.cypherskar.cloudex.components.algorithms.ipso.IPSO;
import tech.cypherskar.cloudex.components.algorithms.minmin.MinMin;
import tech.cypherskar.cloudex.components.algorithms.minmin.MinMin.Variation;
import tech.cypherskar.cloudex.components.algorithms.pso.PSO;
import tech.cypherskar.cloudex.components.algorithms.pso.PSO.Inertia;
import tech.cypherskar.cloudex.components.algorithms.pso.PSO.Position;
import tech.cypherskar.cloudex.components.algorithms.shortestjobfirst.ShortestJobFirst;
import tech.cypherskar.cloudex.components.algorithms.standard.StandardSim;
import tech.cypherskar.cloudex.components.cloudsim.ASimulation;
import tech.cypherskar.cloudex.components.window.Tables.TableMode;
import tech.cypherskar.cloudex.controllers.Brokers;
import tech.cypherskar.cloudex.controllers.Datacenters;
import tech.cypherskar.cloudex.controllers.Experiment;
import tech.cypherskar.cloudex.controllers.Simulations;
import tech.cypherskar.cloudex.controllers.Window;
import tech.cypherskar.cloudex.controllers.Brokers.TaskScheduler;
import tech.cypherskar.cloudex.controllers.Brokers.TasksSplit;
import tech.cypherskar.cloudex.controllers.Datacenters.VmScheduler;
import tech.cypherskar.cloudex.utils.NumberMixer.RandomStyle;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;


public class Example
{
    public static void main(String[] args)
    {
        // [1]: Create and Configure Datacenters and Hosts with -> Datacenters.
        Datacenters.List().Name("Server").Architecture("x86", "Linux", "Xen").
        //TimeZone(10.0, 15).SecCost(3.0, 10).MemCost(0.05).StorageCost(0.001).BwCost(0.0).Create(10);
        TimeZone(10.0).SecCost(3.0).MemCost(0.05).StorageCost(0.001).BwCost(0.0).Create(1);
        
        // Hosts.
        Datacenters.Hosts().Mips(177730).Pes(6).Ram(16000).Bw(15000).Storage(4000000).Create(2, "Server");
        //Datacenters.Hosts().Mips(220000).Pes(1).Ram(16000).Bw(10000).Storage(4000000).Create(2, "Server");
        
        // [2]: Create and Configure Brokers, Virtual Machines and Cloudlets (Tasks) with -> Brokers.
        Brokers.List().Name("Cypher").Create();
        
        // Virtual Machines.
        Brokers.Vms().Mips(9726).Pes(1).Ram2(9).Bw(1000).Image(10000).Vmm("Xen").Create(5);
        
        // Cloudlets (Tasks).
        Brokers.SetTasksSplitMode(TasksSplit.Even);
        Brokers.Tasks().Length(10000, 20000).Pes(1).FileSize(1).OutpuSize(1).Create(RandomStyle.Fixed_Pace);

        // [3]: Add Wanted Simulations With Hints For Identification -> Simulations.
        Simulations.List().Schedulers(VmScheduler.TimeShared, TaskScheduler.TimeShared).Create(
        new PSO(100, 1000, 0.1, 0.9, 1.49445, 1.49445, 5, Inertia.LDIW_1, Position.Sigmoid),
        new ACO(100, 1000, 0.05, 3, 2, 8, 0.01)
        );

        // [4]: Add Number Of Experiment Tasks.
        Simulations.TaskTargets(10, 20);

        // Set Workload Folder To Use External Jobs Data if any (i.e. CEA, Planetlab...etc)
        Simulations.WorkloadFolder(System.getProperty("user.dir"));

		// [5]: Setup Results Preferances (i.e. font size, background color...etc) -> Window.
		// Setup View Monitor/Screen
        Window.ShowOnScreen(1);
		// Setup Tables
        Window.SetTableHintEnds('(', ')');
        Window.SetTableCellSeparator('|');
        Window.SetTableMode(TableMode.Compact_With_TasksSplit);
        Window.SetTableBackground(Color.BLACK);
        Window.SetTableForeground(Color.WHITE);
        
		// Setup Charts
		Window.SetChartTheme(ChartTheme.XChart);
		Window.SetChartSize(0,0); // Automatic resizing if (0,0), also affects export image size
		Window.SetChartRenderStyle(CategorySeriesRenderStyle.Bar);
		Window.SetChartLegend(LegendPosition.OutsideS, LegendLayout.Horizontal);
        Window.SetChartAnnotation(true, 90);
        Window.SetChartYAxisTickDivider(6);
        Window.SetChartGridLinesVisible(true, false);
		Window.SetChartImageFormat(BitmapFormat.PNG);

        // [6]: Set CloudSim Settings And Sign Your Experiment -> Experiment.
        Experiment.CloudsimLogState(false);
        Experiment.CloudsimInit(1, false);
        Experiment.DigitalSigniture("Cypher Skar");
        
        // Launch :)
        Experiment.Launch();
    }
}
