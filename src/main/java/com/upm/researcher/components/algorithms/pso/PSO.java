package com.upm.researcher.components.algorithms.pso;

import java.util.List;
import java.util.Random;

import com.upm.researcher.components.algorithms.standard.StandardSim;
import com.upm.researcher.components.cloudsim.ABroker;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;

// Papers: (14), inertia(24), (32)
public class PSO extends StandardSim
{
    public enum Inertia
    {
        LDIW_1,
        RIW_LDIW_1,
        
        LDIW_2,
        RIW_LDIW_2,

        LDIW_3,
        RIW_LDIW_3,
    }

    public enum Position
    {
        Standard,
        Sigmoid
    }

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

    public PSO(int particles, int iterations, double wMin, double wMax, double c1, 
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
		this.SetProgressMessage("PSO Is Iterating");
	}

	@Override
	public void Destroy() 
	{
	}

    @Override
    public void ProcessCloudletSubmit(ABroker broker) 
    {
		this.BenchmarkStart();

        this.bestParticle = 0;
        this.particles = new Particle[this.NumParticles];
        this.rand = new Random();
        this.ff = new FitnessFunction();
        this.runTimes = this.ff.GetRunTimeMatrix(broker.getCloudletList(), broker.getVmsCreatedList());
		this.BenchmarkEnd();

        int[] result = this.FindSolution();

        for(int i = 0; i < result.length; i++)
        {
            broker.getCloudletList().get(i).setVmId(broker.getVmsCreatedList().get(result[i]).getId());
        }


        super.ProcessCloudletSubmit(broker);

        // Free memory
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
        for(int p = 0; p < this.particles.length; p++)
        {
			this.BenchmarkStart();

            this.particles[p] = new Particle(this.runTimes[0].length, this.runTimes.length);
			
            this.particles[p].Initialize(this.rand);
			
			this.UpdatePersonalBest(this.particles[p]);
			
			this.BenchmarkEnd();
        }
    }

    public int[] FindSolution()
    {
		this.InitializeParticles();

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

        return this.GetCloudletsToVMsPositions();
    }

    protected int[] GetCloudletsToVMsPositions()
    {
		this.BenchmarkStart();
        int[][] positions = this.particles[this.bestParticle].GetBestPositions();
        final int noOfTasks = positions[0].length;
        final int noOfVMs = positions.length;

        int[] result = new int[noOfTasks];

        switch(this.Inertia_Method)
        {
        case RIW_LDIW_1:
        case LDIW_1:
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
        break;

        default:
        for(int j = 0, vmIdx = 0; j < noOfTasks ; j++, vmIdx = 0)
        {
            for(int i = 1, chosen = positions[0][j]; i < noOfVMs ; i++)
            {
            if (positions[i][j] > chosen)
            {
                vmIdx = i;
                chosen = positions[i][j];
            }
            }
    
            result[j] = vmIdx;
        }
        break;
		}
		
		this.BenchmarkEnd();

        return result;
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

    public int GetBestPosition(int vm, int task)
    {
        return this.particles[this.bestParticle].GetBestPositions()[vm][task];
    }

    protected void UpdatePersonalBest(Particle particle) 
    {
        double fitness = this.ff.CalculateFitness(particle.GetPositions(), this.runTimes);

        particle.UpdatePersonalBest(fitness, this.Inertia_Method);
    }

    protected void UpdateGlobalBest(double fitness, int idx) 
    {
        if (fitness < this.particles[this.bestParticle].GetBestFitness())
        this.bestParticle = idx;
    }

	
}