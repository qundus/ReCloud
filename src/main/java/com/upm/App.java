package com.upm;

import java.awt.Color;
import com.upm.researcher.components.algorithms.aco.ACO;
import com.upm.researcher.components.algorithms.bullet.Bullet;
import com.upm.researcher.components.algorithms.bullet.Bullet.GunType;
import com.upm.researcher.components.algorithms.honeybee.LBA_HB;
import com.upm.researcher.components.algorithms.ipso.IPSO;
import com.upm.researcher.components.algorithms.minmin.MinMin;
import com.upm.researcher.components.algorithms.minmin.MinMin.Variation;
import com.upm.researcher.components.algorithms.pso.PSO;
import com.upm.researcher.components.algorithms.pso.PSO.Inertia;
import com.upm.researcher.components.algorithms.pso.PSO.Position;
import com.upm.researcher.components.algorithms.shortestjobfirst.ShortestJobFirst;
import com.upm.researcher.components.algorithms.standard.StandardSim;
import com.upm.researcher.components.cloudsim.ASimulation;
import com.upm.researcher.components.window.Tables.TableMode;
import com.upm.researcher.controllers.Brokers;
import com.upm.researcher.controllers.Datacenters;
import com.upm.researcher.controllers.Experiment;
import com.upm.researcher.controllers.Simulations;
import com.upm.researcher.controllers.Window;
import com.upm.researcher.controllers.Brokers.TaskScheduler;
import com.upm.researcher.controllers.Brokers.TasksSplit;
import com.upm.researcher.controllers.Datacenters.VmScheduler;
import com.upm.researcher.utils.NumberMixer.RandomStyle;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;


public class App
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
        
        // [3]: Create and Configure Brokers, Virtual Machines and Cloudlets (Tasks) with -> Brokers.
        Brokers.List().Name("Eslam").Create();
        //Brokers.List().Name("Broker").Create();
        
        // Virtual Machines.
        Brokers.Vms().Mips(9726).Pes(1).Ram2(9).Bw(1000).Image(10000).Vmm("Xen").Create(5);
        //Brokers.Vms().Mips(250).Pes(1).Ram2(7).Bw(1000).Image(10000).Vmm("Xen").Create(1);
        //Brokers.Vms().Mips(500).Pes(1).Ram2(7).Bw(1000).Image(10000).Vmm("Xen").Create(1);
        //Brokers.Vms().Mips(9726).Pes(1).Ram2(9, 11).Bw(1000, 5000).Image(10000).Vmm("Xen").Create(1);
        
        // Cloudlets (Tasks).
        //Brokers.SetTasksSplitMode(TasksSplit.Even);
        //Brokers.Tasks().Length(1000, 15300).Pes(1).FileSize(1).OutpuSize(1).Create(RandomStyle.Arbitrary);
        Brokers.Tasks().Length(10000, 20000).Pes(1).FileSize(1).OutpuSize(1).Create(RandomStyle.Fixed_Pace);
        //Brokers.Tasks().Length(10000, 15000).Pes(1).FileSize(1).OutpuSize(1).Create(RandomStyle.Arbitrary);

        // [4]: Add Wanted Simulations With Hints For Identification -> Simulations.
        Simulations.List().Schedulers(VmScheduler.TimeShared, TaskScheduler.TimeShared).Create(
        //new PSO(100, 1000, 0.1, 0.9, 1.49445, 1.49445, 5, Inertia.LDIW_1, Position.Sigmoid),
        //new ACO(100, 1000, 0.05, 3, 2, 8, 0.01),
        //new LBA_HB(0.1), // HB works only with 1 broker (SHOCKER >_>), no time to further modify it.
        //new IPSO(100, 1000, 0.1, 0.9, 1.49445, 1.49445, 5, Inertia.LDIW_1, Position.Sigmoid),
        new Bullet(GunType.Magnum),
        new Bullet(GunType.Bazooka)
        //new ShortestJobFirst(),
        //new MinMin(Variation.Min_Min),
        //new MinMin(Variation.Max_Min)
        );

        // Add Number Of Experiment Tasks.
        Simulations.TaskTargets(10, 20);

        // Set Workload Folder To Use External Jobs Data (i.e. CEA, Planetlab...etc)
        //Simulations.WorkloadFolder(System.getProperty("user.dir"), "resources");

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
        Experiment.DigitalSigniture("Eslam Sharif");
        
        // Launch :)
        Experiment.LaunchLegacy();
    }
}
