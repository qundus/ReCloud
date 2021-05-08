package tech.cypherskar.cloudex.components.algorithms.ipso;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

// (09) (13)
public class FitnessFunction
{
    /**
     * will calculate the fitness value of the current particle's solution
     * binary style
     * @param runTime: the list of execution times of all cloudlets on all VMs 
     * @param positionsArrList: the positions matrix found by current particle
     * 
     * @return double fitness value
     */	
    public double CalculateFitness(int[][] positions, double[][] runTimes) 
    {
        double[] completionTime = new double[runTimes.length];
        int resultIdx = 0;

        for (int i = 0; i < runTimes.length; i++) 
        {
            for (int j = 0; j < runTimes[i].length; j++)
            {
                // Get computing time of particle on each vm
                completionTime[i] += runTimes[i][j] * positions[i][j]; // No need to multiply
            }

            // will find the highest execution time among all VMs
            if (completionTime[resultIdx] < completionTime[i])
            {
                resultIdx = i;
            }
        }
        
        return completionTime[resultIdx];
    }

    public double CalculateFitnessForRange(int[][] positions, double[][] runTimes, int rangeStart, int rangeEnd) 
    {
        double[] completionTime = new double[runTimes.length];
        double result = 0;

        for (int i = 0; i < runTimes.length; i++) 
        {
            for (int j = rangeStart; j < rangeEnd; j++)
            {
                // Get computing time of particle on each vm
                if (positions[i][j] == 1)
                {
                    //completionTime[i] += this.tasksRunTime[i][j] * positions[i][j]; // No need to multiply
                    completionTime[i] += runTimes[i][j];
                }
            }

            // will find the highest execution time among all VMs
            if (result < completionTime[i])
            {
                result = completionTime[i];
            }
        }
        
        return result;
    }

    public double[][] GetRunTimeMatrix(List<? extends Cloudlet> tasks, List<? extends Vm> vms)
    {
        double[][] result = new double[vms.size()][tasks.size()];

        for (int i = 0; i < vms.size(); i++)
        {
            Vm vm = vms.get(i);

            for (int j = 0; j < tasks.size(); j++)
            {
                Cloudlet task = tasks.get(j);

                if (vm.getHost() == null)
                    result[i][j] = task.getCloudletLength() / vm.getMips();
                else
                    result[i][j] = task.getCloudletLength() / vm.getHost().getTotalAllocatedMipsForVm(vm);
            }
        }
        
        return result;
    }
    
    /*

    if (VM.getHost() == null)
        arr[task] = (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getMips()) +
        cloudlet.getCloudletFileSize()/VM.getBw());
    else
        arr[task] = (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getHost().getTotalAllocatedMipsForVm(VM)) +
        cloudlet.getCloudletFileSize()/VM.getBw());*/
}