package tech.skargen.recloud.developers.cypherskar.pso;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/** A particle data to be used with PSO algorithm. */
public class Particle {
  /** best solutions */
  public double bestFitness;
  public int[][] bestPositions;

  /** Fitness tracker */
  public DoubleArrayList fitnessList;
  /** Positions equivalent to x-Values and/or solution */
  public int[][] positions;
  /** Velocities */
  public double[][] velocities;

  public Particle(int noOfTasks, int noOfVMs) {
    this.positions = new int[noOfVMs][noOfTasks];
    this.velocities = new double[noOfVMs][noOfTasks];
    this.fitnessList = new DoubleArrayList();
    this.bestFitness = Double.POSITIVE_INFINITY;
  }
}
