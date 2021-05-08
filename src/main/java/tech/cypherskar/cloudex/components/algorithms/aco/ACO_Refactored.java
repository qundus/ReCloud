package tech.cypherskar.cloudex.components.algorithms.aco;

import java.util.Random;
import java.util.stream.IntStream;

import tech.cypherskar.cloudex.components.algorithms.standard.StandardSim;
import tech.cypherskar.cloudex.components.cloudsim.ABroker;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;


public class ACO_Refactored extends StandardSim
{
    /** No of ants to be used */
    public int NumAnts;
    /** No of iterations */
    public int NumIterations;
    /** Initial pheromone for each VM */
    public double Initial_Pheromone; //(0.05),
    /** Q || Total amount of pheromone left on the trail by each Ant */
    public double Pheromone_Total; //(8),
    /** alpha || Controls the pheromone importance */
    public double Pheromone_Importance; //(3),
    /** beta || Controls the distance priority. 
     *  This parameter should be greater than alpha for the best results */
    public double Distance_Priority; //(2),
    /** rho || Vapourization constant for pheromone trail in 
     * each iteration (0.0 = no evaporation, 1.0 = full evaporation) */
    public double Evaporation_Rate; //(0.01);

    private AAnt[] ants;
    private double[][] trails;
    private FitnessFunction ff;
    private int[] shuffledVms;
    private Random random;

	public ACO_Refactored(int ants, int iterations, double initialPhero, 
	int alpha, int beta, int q, double rho)
    {
        this.NumAnts = ants;//8;
        this.NumIterations = iterations;//50;
        this.Initial_Pheromone = initialPhero;//0.05;
        this.Pheromone_Importance = alpha;//3;
        this.Distance_Priority = beta;//2;
        this.Pheromone_Total = q;//8;
        this.Evaporation_Rate = rho;//0.01;
	}
	
	@Override
	public void Initialize(Datacenter[] datacenters, ABroker[] brokers) 
	{
		this.SetProgressMax(this.NumIterations * brokers.length);
		this.SetProgressMessage("Ant Colony Optimization By Eslam Sharif");
	}

	@Override
	public void Destroy() 
	{
	}

    @Override
    public void ProcessCloudletSubmit(ABroker broker)
    {
        this.ants = new AAnt[this.NumAnts];
        this.trails = new double[broker.getCloudletList().size()][broker.getVmsCreatedList().size()];
        this.ff = new FitnessFunction(broker.getCloudletList(), broker.getVmsCreatedList());
        this.shuffledVms = IntStream.range(0, this.ff.GetNoOfVMs()).toArray();
        this.random = new Random();

        this.GetBestTrails();
    
        int vmForTask = 0;
        for (int task = 0; task < this.trails.length; task++)
        {
            for (int vm = 0; vm < this.trails[task].length; vm++)
            {
                if (trails[task][vm] > trails[task][vmForTask]) // Looping probabilities for chosen vms
                {
                    vmForTask = vm;
                }
            }

            broker.getCloudletList().get(task).setVmId(vmForTask);
        }

        super.ProcessCloudletSubmit(broker);

        this.ants = null;
        this.trails = null;
        this.ff = null;
        this.shuffledVms = null;
        this.random = null;
	}
	
	@Override
	public <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task) 
	{
	}

    /**
     * Use this method to run the main logic
     */
    public void GetBestTrails() 
    {
		this.BenchmarkStart();
        this.InitializeTrailPheromones();

        this.InitializeAnts();
        
		this.BenchmarkEnd();
        for (int iteration = 0; iteration < this.NumIterations; iteration++)
        {
			this.BenchmarkStart();
			
			this.MoveAnts();
			
			this.UpdateTrails();
			
			this.BenchmarkEnd();

            this.UpdateProgress(1);
        }
    }

    /**
     * Prepare initial pheromone matrix
     */
    private void InitializeTrailPheromones()
    {
        for(int task = 0; task < this.ff.GetNoOfTasks(); task++)
			for(int vm = 0; vm < this.ff.GetNoOfVMs(); vm++)
				//this.trails[task][vm] = this.GetEdgeDistance(task, vm); // Choose the pheromone graph not the cost graph
				this.trails[task][vm] = this.Initial_Pheromone; // Choose the pheromone graph not the cost graph
    }

    /**
     * Prepare ants for the simulation (Initial solution)
     */
    private void InitializeAnts()
    {
        for(int ant = 0; ant < this.ants.length; ant++)
        {
            if (ant % this.shuffledVms.length == 0)
                this.ShuffleArray(this.shuffledVms);

            this.ants[ant] = new AAnt(this.ff.GetNoOfTasks(), this.shuffledVms[ant % this.shuffledVms.length]);
        }
    }
    
    /**
     * At each iteration, move ants
     */
    private void MoveAnts() 
    {
        for(int task = 0, vm; task < this.ff.GetNoOfTasks(); task++)
        {
            if (task != 0 && task % this.ff.GetNoOfVMs() == 0)
                this.ShuffleArray(this.shuffledVms);

            for(int ant = 0; ant < this.ants.length; ant++)
            {
                vm = CalculateNextVmForTask(this.ants[ant], task);
                this.ants[ant].VisitVm(vm, task);
                this.ants[ant].CheckVisited(this.shuffledVms, this.ff.AreVmsIdentical());
                this.ants[ant].UpdateTrailLength(this.GetEdgeDistance(task, vm));
            }
        }
    }

    /**
     * Select next city for each ant
     */
    private int CalculateNextVmForTask(AAnt ant, int task)
    {
        double maxVmPheromone = Double.NEGATIVE_INFINITY, 
        minVmPheromone = Double.POSITIVE_INFINITY;
        int maxVm = 0, minVm = 0;

        for (int vm = 0; vm < this.ff.GetNoOfVMs(); vm++) 
        {
            if (!ant.Visited(vm)) 
            {
                double hueristicValue = Math.pow(1 / 
                this.GetEdgeDistance(task, vm), this.Distance_Priority);
                double edgePheromoneValue = Math.pow(this.trails[task][vm], 
                this.Pheromone_Importance);
                
                hueristicValue *= edgePheromoneValue;

                if (minVmPheromone > hueristicValue)
                {
                    minVmPheromone = hueristicValue;
                    minVm = vm;
                }
                
                if (maxVmPheromone < hueristicValue)
                {
                    maxVmPheromone = hueristicValue;
                    maxVm = vm;
                }
            }
        }

        if (this.ff.AreVmsIdentical())
            return minVm;

        return maxVm;
    }

    /**
     * Update trails that ants used
     */
    private void UpdateTrails() 
    {
        AAnt shortestTrailAnt = this.ants[0].Clone();
        double contribution = 0.0;
        int[] trail;

        // Apply evaporation rate on every trail
        for (int task = 0; task < this.trails.length; task++)
        {
            for (int vm = 0; vm < this.trails[task].length; vm++)
            {
                this.trails[task][vm] *= this.Evaporation_Rate;
            }
        }

        // Increase the taken trails by ants pheromones contributions
        for(AAnt ant : this.ants)
        {
            contribution = this.Pheromone_Total / ant.GetTrailLength();
            
            trail = ant.GetTrail();

            for(int task = 0; task < trail.length; task++)
            {
                this.trails[task][trail[task]] += contribution;
            }
            
            if (ant.GetTrailLength() < shortestTrailAnt.GetTrailLength())
            {
                shortestTrailAnt = ant.Clone();
            }


            ant.ClearTrailLength();
        }

        // Reinforce shortest trail ant's path
        contribution = this.Pheromone_Total / shortestTrailAnt.GetTrailLength();
        trail = shortestTrailAnt.GetTrail();
        for(int task = 0; task < trail.length; task++)
        {
            this.trails[task][trail[task]] += contribution;
        }
    }

    // Implementing Fisher–Yates shuffle
    private void ShuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = this.random.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private double GetEdgeDistance(int task, int vm)
    {
        //return this.ff.GetExecutionTime().get(task)[vm] / this.ff.GetAvgExecutionTime()[task];
        return this.ff.GetExecutionTime().get(task)[vm];
    }
}