package tech.cypherskar.cloudex.components.algorithms.ipso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import tech.cypherskar.cloudex.components.algorithms.pso.PSO.Inertia;
import tech.cypherskar.cloudex.components.algorithms.pso.PSO.Position;
import tech.cypherskar.cloudex.components.algorithms.standard.StandardSim;
import tech.cypherskar.cloudex.components.cloudsim.ABroker;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;

// (14), inertia(24), (32)
public class IPSO extends StandardSim
{
    public int NumParticles; 
    public double NumIterations;
    public double C1; // Local Weight
    public double C2; // Global Weight
    public double Min_W; 
    public double Max_W;
    public int K; //Constriction Factor,
    public Inertia Inertia_Method;
    public Position Position_Method;

    protected int bestParticle;
    protected FitnessFunction ff;
    protected Particle[] particles;
    protected double[][] runTimes;
    public Random rand;

    private List<? extends Cloudlet> tasks;
    private List<? extends Vm> vms;
    private int noOfBatches;
    protected int rangeStart;
    protected int rangeEnd;

    public IPSO(int particles, int iterations, double wMin, double wMax, double c1, 
    double c2, int k, Inertia wUpdate, Position posUpdate)
    {
        this.NumParticles = particles;//100;
        this.NumIterations = iterations;//1000;
        this.C1 = c1;//1.49445;
        this.C2 = c2;//1.49445;
        this.Min_W = wMin;// 0.1;
        this.Max_W = wMax;// 0.9;
        this.K = k;// 5;
        this.Inertia_Method = wUpdate;
        this.Position_Method = posUpdate;
	}
	
	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		this.SetProgressMax((int)this.NumIterations * brokers.length);
		this.SetProgressMessage("IPSO Is Iterating");
	}

	@Override
	public void Destroy() 
	{
	}

    @Override
    public void ProcessCloudletSubmit(ABroker broker) 
    {
		this.BenchmarkStart();

        this.tasks = broker.getCloudletList();
        this.vms = broker.getVmsCreatedList();
        this.noOfBatches = 5;
    
        this.bestParticle = 0;
        this.particles = new Particle[this.NumParticles];
        this.rand = new Random();
        this.ff = new FitnessFunction();
		this.runTimes = this.ff.GetRunTimeMatrix(tasks, vms);
		
		this.BenchmarkEnd();

        int[] result = this.FindSolution();

        for(int i = 0; i < result.length; i++)
        {
            broker.getCloudletList().get(i).setVmId(broker.getVmsCreatedList().get(result[i]).getId());
        }


        super.ProcessCloudletSubmit(broker);

        // Free memory
        this.tasks = null;
        this.vms = null;
        this.particles = null;
        this.rand = null;
        this.ff = null;
        this.runTimes = null;
	}
	
	@Override
	public <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task) 
	{
	}

    protected void InitializeParticles() 
    {
        this.rangeStart = 0;
        this.rangeEnd = this.tasks.size();

        for(int p = 0; p < this.particles.length; p++)
        {
			this.BenchmarkStart();
			this.particles[p] = new Particle(this.tasks.size(), this.vms.size());
			
			this.particles[p].Initialize(this.rand);
			
			this.UpdatePersonalBest(this.particles[p]);
			this.BenchmarkEnd();
        }
    }
    
    // Calculate batch limit according to IPSO paper.
    // mu : resource utilization indicator.
    public double CalculateBatchLimit(List<? extends Vm> vms)
    {
        int noOfVMs = vms.size();
        double batchLimit = 0;

        for (int vm = 0; vm < noOfVMs; vm++)
        {
            Vm VM = vms.get(vm);

            if (VM.getHost() == null)
                batchLimit += VM.getMips();
            else
                batchLimit += VM.getHost().getTotalAllocatedMipsForVm(VM);
        }

        return batchLimit / noOfVMs;
    }

    public int[] FindSolution()
    {
        this.InitializeParticles();

        /*final double batchLimit = this.CalculateBatchLimit(this.vms);
        double batchLength = 0;
        int[] result = new int[tasks.size()];
        int q = 0;
        double tasksSize = this.tasks.size();

        for (this.rangeStart = 0, this.rangeEnd = 0; this.rangeStart < tasks.size(); this.rangeEnd++)
        {
        if (this.rangeEnd < tasks.size() )//&& this.noOfBatches > 0)
        {
            batchLength += tasks.get(this.rangeEnd).getCloudletLength();

            if (batchLength <= batchLimit)
            continue;
        }
        else
        {
            this.rangeEnd = this.tasks.size();
        }

        this.FindPSOSolution(tasksSize);

        this.AppendSolution(result);
        
        this.RebalanceFinalSolution(result);

        batchLength = 0;
        this.noOfBatches--;
        this.rangeStart = this.rangeEnd;
        }*/

        int[] result = new int[tasks.size()];

        this.FindPSOSolution(this.tasks.size());

        //this.RebalanceFinalSolution();

        //this.AppendSolution(result);
        result = this.GetCloudletsToVMsPositions();

        this.RebalanceFinalSolution(result);

        return result;
    }

    private void FindPSOSolution(double tasksSize)
    {
        for (int iteration = 0; iteration < this.NumIterations; iteration++)
        {
			this.BenchmarkStart();
            for (int p = 0; p < this.particles.length; p++)
            {
				this.particles[p].Update(this, iteration);
				
                this.UpdatePersonalBest(this.particles[p]);
				
                this.UpdateGlobalBest(this.particles[p].GetBestFitness(), p);
            }
			this.BenchmarkEnd();
			
			this.UpdateProgress(1);
        }
    }

    public void AppendSolution(int[] result)
    {
		this.BenchmarkStart();
        for(int i = 0 ; i < this.vms.size() ; i++)
        {
			for(int j = this.rangeStart ; j < this.rangeEnd; j++)
			{
				if (this.particles[this.bestParticle].GetBestPositions()[i][j] == 1)
				{
					result[j] = i;
				}
			}
        }
		this.BenchmarkEnd();
    }

    protected int[] GetCloudletsToVMsPositions()
    {
		this.BenchmarkStart();
        int[][] positions = this.particles[this.bestParticle].GetBestPositions();
        final int noOfTasks = positions[0].length;
        final int noOfVMs = positions.length;

        int[] result = new int[noOfTasks];

        for(int i = 0; i < noOfVMs ; i++)
        {
			for(int j = 0; j < noOfTasks ; j++)
			{
				if (positions[i][j] == 1)
				{
					result[j] = i;
				}
			}
		}
		this.BenchmarkEnd();
		

        return result;
    }

    private void RebalanceFinalSolution()
    {
        int noOfVms = this.vms.size();
        int noOfTasks = this.tasks.size();
        int[][] bestPos = this.particles[this.bestParticle].GetBestPositions();
        

        // Completion time array
        List<Double> completionTimes = new ArrayList<>();

        for (int i = 0; i < noOfVms; i++)
        {
        double ct = 0.0;
        for (int j = 0; j < noOfTasks; j++)
        {
            if (bestPos[i][j] == 1)
            {
            ct += this.runTimes[i][j];
            }
        }
        completionTimes.add(ct);
        }
        
        while(completionTimes.size() > 1)
        {
        int heaviestVMIdx = 0;

        // Finding heaviest loaded machines.
        for(int vmIdx = 1; vmIdx < completionTimes.size(); vmIdx++)
        {
            if(completionTimes.get(heaviestVMIdx) < completionTimes.get(vmIdx))
            {
            heaviestVMIdx = vmIdx;
            }
        }
        
        // Looking for tasks in heavy loaded vm.
        for(int j = 0; j < noOfTasks; j++)
        {
            if (bestPos[heaviestVMIdx][j] == 1)
            {
            int lightestVMIdx = 0;
            
            // Finding lightest loaded machines.
            for(int vmIdx = 0; vmIdx < completionTimes.size(); vmIdx++)
            {
                if(completionTimes.get(lightestVMIdx) > completionTimes.get(vmIdx))
                {
                lightestVMIdx = vmIdx;
                }
            }
            
            double heaviestVMAfterTaskMoved = completionTimes.get(heaviestVMIdx) - 1;//this.runTimes[heaviestVMIdx][j];
            double lightestVMAfterTaskMoved = completionTimes.get(lightestVMIdx) + 1;//this.runTimes[heaviestVMIdx][j];
            
            
            
            // Does swapping improve makespan? if not look for next task under heaviest vm
            if (heaviestVMAfterTaskMoved < lightestVMAfterTaskMoved)
            {
                // Swapping task machines.
                bestPos[heaviestVMIdx][j] = 0;
                bestPos[lightestVMIdx][j] = 1;
                
                completionTimes.set(heaviestVMIdx, heaviestVMAfterTaskMoved);
                completionTimes.set(lightestVMIdx, lightestVMAfterTaskMoved);
            }
            }
        }

        completionTimes.remove(heaviestVMIdx);
        }
    }

    // This function will re-balance the solution found by PSO for better solutions
    private void RebalanceFinalSolution(int[] result)
    {
		this.BenchmarkStart();
        int noOfTasks = this.tasks.size();
        int noOfVms = this.vms.size();

        // Completion time array
        HashSet<Integer> checkedResources = new HashSet<>();
        double[] completionTimes = new double[noOfVms];
        for (int j = 0; j < result.length; j++)
        {
       		completionTimes[result[j]] += this.runTimes[result[j]][j];
        }
        
        while(checkedResources.size() < noOfVms-2)
        {
			int heaviestVmIdx = -1;

			// Finding heaviest loaded machines.
			for(int vmIdx = 0; vmIdx < completionTimes.length; vmIdx++)
			{
				if (checkedResources.contains(vmIdx))
					continue;
				else
				if (heaviestVmIdx == -1)
					heaviestVmIdx = vmIdx;

				if(completionTimes[heaviestVmIdx] < completionTimes[vmIdx])
				{
					heaviestVmIdx = vmIdx;
				}
			}

			
			
			// Swapping task machines.
			/*for(int j = 0; j < result.length; j++)
			{
				if (result[j] == heaviestVMIdx)
				{
				result[j] = lightestVMIdx;
				
				completionTimes[heaviestVMIdx] -=  this.runTimes[heaviestVMIdx][j];
				completionTimes[lightestVMIdx] +=  this.runTimes[lightestVMIdx][j];

				if(completionTimes[heaviestVMIdx] <= completionTimes[lightestVMIdx])
					break;
				}
			}*/

			// Looking for tasks in heavy loaded vm.
			for(int j = 0; j < noOfTasks; j++)
			{
				if (result[j] == heaviestVmIdx)
				{
					int lightestVmIdx = 0;
					
					// Finding lightest loaded machines.
					for(int vmIdx = 0; vmIdx < completionTimes.length; vmIdx++)
					{
						if (checkedResources.contains(vmIdx)) continue;

						if(completionTimes[lightestVmIdx] > completionTimes[vmIdx] && vmIdx != heaviestVmIdx)
						{
							lightestVmIdx = vmIdx;
						}
					}
					
					double heaviestVmAfterTaskMoved = completionTimes[heaviestVmIdx] - this.runTimes[heaviestVmIdx][j];
					double lightestVmAfterTaskMoved = completionTimes[lightestVmIdx] + this.runTimes[lightestVmIdx][j];
					// Swapping task machines.
					result[j] = lightestVmIdx;
					
					completionTimes[heaviestVmIdx] = heaviestVmAfterTaskMoved;
					completionTimes[lightestVmIdx] = lightestVmAfterTaskMoved;
					
					// Does swapping improve makespan? if not look for next task under heaviest vm
					if (heaviestVmAfterTaskMoved < lightestVmAfterTaskMoved)
					{
						break;
					}
				}
			}

			//noOfVms--;
			checkedResources.add(heaviestVmIdx);
		}
		this.BenchmarkEnd();
    }

    public double CalculateInertia(int iteration, double bestFitness, List<Double> allFitness)
    {
        switch(this.Inertia_Method)
        {
        case RIW_LDIW_1:
        case RIW_LDIW_2:
        case RIW_LDIW_3:
        
        if (iteration % this.K == 0 && iteration != 0)
        {
            return this.RandomInertiaWeight(iteration, bestFitness, allFitness); // RIW
        }
        
        
        default:
            return this.LinearlyDecreasingInertiaWeight(iteration); // LDIW
        }
    }

    /**
     * will calculate the RIW according to 
     * (A new particle swarm optimization algorithm with random inertia weight and evolution strategy: paper)
     * 
     * @param particleNumber: The particle's number; one of the possible solutions
     * @param iterationNumber: The move number when searching the space
     * @return double value of the inertia weight
     */	
    private double RandomInertiaWeight(final int iteration, final double bestFitness, final List<Double> avgFitness)
    {
        //annealing probability
        double p = 0;
            
        // [last] = currentFitness || [last - k] = previousFitness
        final int lastAvgFitness = avgFitness.size() - 1;
        final double currentFitness = avgFitness.get(lastAvgFitness);
        final double previousFitness = avgFitness.get(lastAvgFitness - this.K);
        
        if(previousFitness <= currentFitness)
        {
        p = 1;
        }
        else {
        //annealing temperature
        double coolingTemp_Tt = 0.0;
        
        double ParticleFitnessAverage = 0;
        
        int counter = 0;
        for(final Double d : avgFitness)
        {
            if(d > 0)
            {
            ParticleFitnessAverage += d;
            counter++;
            }
        }
        
        ParticleFitnessAverage = ParticleFitnessAverage/counter;
        
        coolingTemp_Tt = (ParticleFitnessAverage / bestFitness) - 1;
        
        p = Math.exp(-(previousFitness - currentFitness)/coolingTemp_Tt);
        
        }	
        
        final int random = new Random().nextInt(2);
        
        //new inertia weight
        if(p >= random)
        {
        return 1 + random/2;				
        }
        else {
        return 0 + random/2;
        }   
    }

    // (24) -> (29)
    protected double LinearlyDecreasingInertiaWeight(final int iteration)
    {
        switch(this.Inertia_Method)
        {
        case LDIW_1:
        return this.Max_W - ((this.Max_W - this.Min_W) * (this.NumIterations - iteration) / this.NumIterations);

        case LDIW_2:
        return this.Max_W + ((this.Max_W - this.Min_W) * (this.NumIterations - iteration) / this.NumIterations);

        default:
        return this.Max_W - ((this.Max_W - this.Min_W) * iteration / this.NumIterations);
        }
    }


    protected void UpdatePersonalBest(Particle particle)
    {
        double fitness = 
        this.ff.CalculateFitnessForRange(particle.GetPositions(), this.runTimes, 
        this.rangeStart, this.rangeEnd);

        particle.UpdatePersonalBest(fitness, this.Inertia_Method);
    }

    protected void UpdateGlobalBest(double fitness, int idx) 
    {
        if (fitness < this.particles[this.bestParticle].GetBestFitness())
        this.bestParticle = idx;

    }

    public int GetBestPosition(int vm, int task)
    {
        return this.particles[this.bestParticle].GetBestPositions()[vm][task];
	}
}