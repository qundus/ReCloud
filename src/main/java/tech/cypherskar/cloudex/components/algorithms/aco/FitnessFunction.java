package tech.cypherskar.cloudex.components.algorithms.aco;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

// (12), base: (09) (13)
public class FitnessFunction
{
    private int noOfTasks;
    private int noOfVMs;
    private List<double[]> executionTimeList;
    private List<Double> computingCapacityList;
    private boolean resourcesAreIdentical;

    public FitnessFunction(List<? extends Cloudlet> cloudletList, List<? extends Vm> vmList)
    {
        this.noOfTasks = cloudletList.size();
        this.noOfVMs = vmList.size();
        this.executionTimeList = new ArrayList<double[]>();
        this.computingCapacityList = new ArrayList<Double>();


        this.CalculateExecutionTimes(cloudletList, vmList);
        this.CalculateComputingCapacity(vmList);
    }

    /**
     *  Will calculate the execution time each cloudlet takes if it runs on one of the VMs
     */
    private void CalculateExecutionTimes(List<? extends Cloudlet> cloudletList, List<? extends Vm> vmList)
    {
        for (int task = 0; task < noOfTasks; task++)
        {
            Cloudlet cloudlet = cloudletList.get(task);
            
            double[] arr = new double[noOfVMs];
            
            for (int vm = 0; vm < noOfVMs; vm++)
            {
                Vm VM = vmList.get(vm);

                
                // aco (01) - et + er
                if (VM.getHost() == null)
                    arr[vm] = (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getMips()) + cloudlet.getCloudletFileSize()/VM.getBw());
                else
                    arr[vm] = (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getHost().getTotalAllocatedMipsForVm(VM)) + cloudlet.getCloudletFileSize()/VM.getBw());
            }

            this.executionTimeList.add(arr);
        }
    }

    private void CalculateComputingCapacity(List<? extends Vm> vmList)
    {
        double cc = 0.0, denomenator = 0.0;
        for (Vm vm : vmList) 
        {
            cc = vm.getNumberOfPes()*vm.getMips() + vm.getBw();

            denomenator += cc;

            this.computingCapacityList.add(cc);
        }

        if (cc / denomenator * this.noOfVMs == 1)
            this.resourcesAreIdentical = true;
    }

    public List<double[]> GetExecutionTime()
    {
        return this.executionTimeList;
    }

    public List<Double> GetComputingCapacity()
    {
        return this.computingCapacityList;
    }

    public int GetNoOfTasks()
    {
        return this.noOfTasks;
    }

    public boolean AreVmsIdentical()
    {
        return this.resourcesAreIdentical;
    }

    public int GetNoOfVMs()
    {
        return this.noOfVMs;
    }
}