package tech.cypherskar.cloudex.components.algorithms.pso;

import java.util.Random;

import tech.cypherskar.cloudex.components.algorithms.pso.PSO.Inertia;
import tech.cypherskar.cloudex.utils.ArrayUtils;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

public class Particle
{
    /** best solutions */
	protected double pBestFitness;
    protected int[][] pBestpositions;

    /** Fitness tracker */
    protected DoubleArrayList fitnessList;
	/** Positions equivalent to x-Values and/or solution */
    protected int[][] positions;
	/** Velocities */
    protected double[][] velocities;

    public Particle(int noOfTasks, int noOfVMs)
    {
        this.positions = new int[noOfVMs][noOfTasks];
        this.velocities = new double[noOfVMs][noOfTasks];
        this.fitnessList = new DoubleArrayList();
        this.pBestFitness = Double.POSITIVE_INFINITY;
    }

    public void Initialize(Random random)
    {
        int noOfTasks = this.positions[0].length;
        int noOfVMs = this.positions.length;

        for (int i = 0; i < noOfVMs; i++)
        {
            for (int j = 0; j < noOfTasks; j++)
            {
                this.positions[i][j] = random.nextInt(2);
                this.velocities[i][j] = Math.random();
            }
        }

        this.pBestpositions = ArrayUtils.Clone(this.positions);
    }

    public void Update(PSO swarmer, int iteration)
    {
        int noOfTasks = this.velocities[0].length;
        int noOfVMs = this.velocities.length;
        double inertia = swarmer.CalculateInertia(iteration, this.GetBestFitness(), this.fitnessList);

        double r1, r2; // cognitive and social randomizations

		for(int i = 0; i < noOfVMs ; i++)
		{
			for(int j = 0 ; j < noOfTasks; j++)
			{
                r1 = swarmer.rand.nextInt(2);
                r2 = swarmer.rand.nextInt(2);
                
                // Update Velocity
                this.velocities[i][j] = 
                (inertia * this.velocities[i][j]) +
                (swarmer.C1 * r1 * (this.pBestpositions[i][j] - this.positions[i][j])) + 
                (swarmer.C2 * r2 * (swarmer.GetBestPosition(i, j) - this.positions[i][j]));

                switch(swarmer.Inertia_Method)
                {
                    case LDIW_2:
                    case RIW_LDIW_2:
                    if (this.velocities[i][j] < -1) this.velocities[i][j] = -1;
                    if (this.velocities[i][j] > 1) this.velocities[i][j] = 1;
                    break;

                    default:
                    break;
                }

                // Update Position
                switch(swarmer.Position_Method)
                {
                    case Standard:
                    this.positions[i][j] += this.velocities[i][j];
                    break;
                    
                    case Sigmoid:
                    //to calculate sigmoid function
                    this.positions[i][j] = (1 / (1 + Math.exp(-this.velocities[i][j])) > Math.random())? 1 : 0;
                    break;
                }
            }
        }
    }

    public void UpdatePersonalBest(double newFitness, Inertia inertiaMethod)
    {
        if (newFitness < this.pBestFitness)
        {
            this.pBestFitness = newFitness;
            this.pBestpositions = ArrayUtils.Clone(this.positions);
        }
        
        // For the calculation of avearage fitness by inertia function
        //@SuppressWarnings("missing")
        switch (inertiaMethod)
        {
            case RIW_LDIW_1:
            case RIW_LDIW_2:
            case RIW_LDIW_3:
            this.fitnessList.add(newFitness);
            break;
            
            default:
            break;
        } 
    }

    public int[][] GetPositions()
    {
        return this.positions;
    }

    public int[][] GetBestPositions()
    {
        return this.pBestpositions;
    }

    public double GetBestFitness()
    {
        return this.pBestFitness;
    }
}
