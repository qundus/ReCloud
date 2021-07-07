package tech.skargen.recloud.developers.cypherskar.pso;

import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

/**
 * Particle Swarm Optimization implementation by github.com/cypherskar.
 * Implementatiun based on papers:
 * <ol>
 * <li> "Particle Swarm Optimization: Developments, Applications and Resources" by Russell C.
 * Eberhart and Yuhui Shi, in 2001.
 * <li> "Cloudlet Scheduling with Particle Swarm Optimization" by Hussein S. Al-Olimat, Mansoor
 * Alam, Robert Green and Jong Kwan Lee, in 2015.
 * <li> "A hybrid particle swarm optimization and hill climbing algorithm for task scheduling in the
 * cloud environments" by Negar Dordaie, Nima Jafari Navimipour, in 2017.
 * </ol>
 */
public class PSO_CS extends ASimulation {
  /** Inertia's weight updating approaches. */
  public enum Inertia {
    /** Linearly Decreasing Inertai Weight method 1. */
    LDIW_1,
    /** Random Inertia Weight with method 1. */
    RIW_1,

    /** Linearly Decreasing Inertai Weight method 2. */
    LDIW_2,
    /** Random Inertia Weight with method 2. */
    RIW_2,

    /** Linearly Decreasing Inertai Weight method 3. */
    LDIW_3,
    /** Random Inertia Weight with method 3. */
    RIW_3,
  }
  protected Inertia inertiaApproach;

  /** Particle's position updating approaches. */
  public enum Position { Standard, Sigmoid }
  public Position positionApproach;

  /** Number of particles. */
  protected int numParticles;
  /** Number of iterations. */
  protected double numIterations;
  /** Cognitive/local coefficient. */
  protected double c1;
  /** Social/global coefficient. */
  protected double c2;
  /** Minimum inertia. */
  protected double wMin;
  /** Maximum inertia. */
  protected double wMax;
  /** Constriction factor. */
  protected int K;

  /**
   * Create a Particle Swarm Optimization simulation scheduling algorithm.
   * @param p Number of particles.
   * @param iter Number of iterations.
   * @param min Inertia min.
   * @param max Inertia max.
   * @param c1 Cognitive/local coefficient.
   * @param c2 Social/global coefficient.
   * @param k Contriction factor
   * @param m Method used to update inertia.
   * @param pos Method used to update particle position.
   */
  public PSO_CS(int p, int iter, double min, double max, double c1, double c2, int k, Inertia m,
      Position pos) {
    this.numParticles = p; // 100;
    this.numIterations = iter; // 1000;
    this.c1 = c1; // 1.49445;
    this.c2 = c2; // 1.49445;
    this.wMin = min; // 0.1;
    this.wMax = max; // 0.9;
    this.K = k; // 5;
    this.inertiaApproach = m;
    this.positionApproach = pos;
  }

  @Override
  public String getAlgorithmName() {
    return "PSO";
  }

  @Override
  public String getAdditionalInfo() {
    return this.inertiaApproach.name() + "_" + this.positionApproach.name();
  }

  @Override
  public String getDeveloper() {
    return "cypherskar";
  }

  @Override
  public void startEntity(IReBroker rebroker) {}

  @Override
  public void shutdownEntity(IReBroker rebroker) {}

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    List<Cloudlet> cloudlets = rebroker.getEntity().getCloudletList();
    List<Vm> vms = rebroker.getEntity().getVmsCreatedList();

    rebroker.updateProgressMax((int) this.numIterations);
    rebroker.updateProgressMessage("Particle Swarm Optimization By github.com/cypherskar");

    int[] result = this.findSolution(cloudlets, vms, rebroker);

    for (int i = 0; i < result.length; i++) {
      cloudlets.get(i).setVmId(vms.get(result[i]).getId());
      rebroker.submitCloudlet(cloudlets.get(i));
    }
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}

  /**
   * Use PSO attributes to find the vms suitable for each task/cloudlet.
   * @param cloudlets List of cloudlets.
   * @param vms List of vms.
   * @param rebroker Current Broker.
   * @return 1D array of mapping cloudlets to vms.
   */
  public int[] findSolution(List<Cloudlet> cloudlets, List<Vm> vms, IReBroker rebroker) {
    FitnessFunction ff = new FitnessFunction(cloudlets, vms);
    Particle[] particles =
        this.createParticles(cloudlets.size(), vms.size(), this.numParticles, ff);
    Random rand = new Random();

    Particle bestParticle = particles[0];
    for (int iteration = 0; iteration < this.numIterations; iteration++) {
      for (int p = 0; p < particles.length; p++) {
        this.updateParticle(particles[p], bestParticle, rand, iteration);

        this.updateParticleBest(particles[p], ff);

        bestParticle = this.getSwarmBest(particles[p], bestParticle);
      }
      rebroker.updateProgress(1);
    }

    ff = null;
    particles = null;
    int[] cloudletsToVmMap = this.getCloudletsToVMsPositions(bestParticle);
    bestParticle = null;

    return cloudletsToVmMap;
  }

  /**
   * Retrieve an initialize particles array.
   * @param numTasks Total number of tasks.
   * @param numVms Total number of vms.
   * @param numParticles Number of particles.
   * @param particles Array of particles.
   * @param ff Fitness function.
   */
  public Particle[] createParticles(
      int numTasks, int numVms, int numParticles, FitnessFunction ff) {
    Particle[] particles = new Particle[this.numParticles];
    Random rand = new Random();

    for (int p = 0; p < particles.length; p++) {
      particles[p] = new Particle(numTasks, numVms);

      for (int i = 0; i < numVms; i++) {
        for (int j = 0; j < numTasks; j++) {
          particles[p].positions[i][j] = rand.nextInt(2);
          particles[p].velocities[i][j] = Math.random();
        }
      }

      particles[p].bestPositions = clone(particles[p].positions);

      this.updateParticleBest(particles[p], ff);
    }

    rand = null;
    return particles;
  }

  public void updateParticle(Particle p, Particle bestp, Random rand, int iteration) {
    int noOfTasks = p.velocities[0].length;
    int noOfVMs = p.velocities.length;
    double inertia = this.getInertia(iteration, p.bestFitness, p.fitnessList);

    double r1, r2; // cognitive and social randomizations

    for (int i = 0; i < noOfVMs; i++) {
      for (int j = 0; j < noOfTasks; j++) {
        r1 = rand.nextInt(2);
        r2 = rand.nextInt(2);

        // Update Velocity
        p.velocities[i][j] = (inertia * p.velocities[i][j])
            + (this.c1 * r1 * (p.bestPositions[i][j] - p.positions[i][j]))
            + (this.c2 * r2 * (bestp.bestPositions[i][j] - p.positions[i][j]));

        switch (this.inertiaApproach) {
          case LDIW_2:
          case RIW_2:
            if (p.velocities[i][j] < -1)
              p.velocities[i][j] = -1;
            if (p.velocities[i][j] > 1)
              p.velocities[i][j] = 1;
            break;

          default:
            break;
        }

        // Update Position
        switch (this.positionApproach) {
          case Standard:
            p.positions[i][j] += p.velocities[i][j];
            break;

          case Sigmoid:
            // to calculate sigmoid function
            p.positions[i][j] = (1 / (1 + Math.exp(-p.velocities[i][j])) > Math.random()) ? 1 : 0;
            break;
        }
      }
    }
  }

  /**
   * Decide which inertia calculation method to be used.
   * @param iteration Current iteration.
   * @param bestFitness Best found fitness by all particles.
   * @param allFitness List of best fitness by all particles, used if requested.
   * @return Inertia weight.
   */
  public double getInertia(int iteration, double bestFitness, List<Double> allFitness) {
    switch (this.inertiaApproach) {
      case RIW_1:
      case RIW_2:
      case RIW_3:

        if (iteration % this.K == 0 && iteration != 0) {
          return this.getRandomInertiaWeight(iteration, bestFitness, allFitness); // RIW
        }

      default:
        return this.getLinearlyDecreasingInertiaWeight(iteration); // LDIW
    }
  }

  /**
   * Calculate inertia weight.
   * @param iteration Current iteration.
   * @param bestFitness Best found fitness.
   * @param avgFitness All fitness values.
   * @return Inertia weight
   */
  public double getRandomInertiaWeight(
      final int iteration, final double bestFitness, final List<Double> avgFitness) {
    // annealing probability
    double p = 0;

    // [last] = currentFitness || [last - k] = previousFitness
    final int lastAvgFitness = avgFitness.size() - 1;
    final double currentFitness = avgFitness.get(lastAvgFitness);
    final double previousFitness = avgFitness.get(lastAvgFitness - this.K);

    if (previousFitness <= currentFitness) {
      p = 1;
    } else {
      // annealing temperature
      double coolingTemp_Tt = 0.0;

      double ParticleFitnessAverage = 0;

      int counter = 0;
      for (final Double d : avgFitness) {
        if (d > 0) {
          ParticleFitnessAverage += d;
          counter++;
        }
      }

      ParticleFitnessAverage = ParticleFitnessAverage / counter;

      coolingTemp_Tt = (ParticleFitnessAverage / bestFitness) - 1;

      p = Math.exp(-(previousFitness - currentFitness) / coolingTemp_Tt);
    }

    final int random = new Random().nextInt(2);

    // new inertia weight
    if (p >= random) {
      return 1 + random / 2;
    } else {
      return 0 + random / 2;
    }
  }

  /**
   * Calculate inertia weight.
   * @param iteration Iteration to calculate inertia weight.
   * @return Inertia weight.
   */
  public double getLinearlyDecreasingInertiaWeight(final int iteration) {
    switch (this.inertiaApproach) {
      case LDIW_1:
        return this.wMax
            - ((this.wMax - this.wMin) * (this.numIterations - iteration) / this.numIterations);

      case LDIW_2:
        return this.wMax
            + ((this.wMax - this.wMin) * (this.numIterations - iteration) / this.numIterations);

      default:
        return this.wMax - ((this.wMax - this.wMin) * iteration / this.numIterations);
    }
  }

  protected void updateParticleBest(Particle particle, FitnessFunction ff) {
    double newFitness = ff.calculate(particle.positions);

    if (newFitness < particle.bestFitness) {
      particle.bestFitness = newFitness;
      particle.bestPositions = clone(particle.positions);
    }

    // For the calculation of avearage fitness by inertia function
    // @SuppressWarnings("missing")
    switch (this.inertiaApproach) {
      case RIW_1:
      case RIW_2:
      case RIW_3:
        particle.fitnessList.add(newFitness);
        break;

      default:
        break;
    }
  }

  protected Particle getSwarmBest(Particle p, Particle bestp) {
    if (p.bestFitness < bestp.bestFitness) {
      return p;
    }
    return bestp;
  }

  /**
   * Retrieve best solution for from array of particle based on best particle.
   * @param particle Best particle found.
   * @return 1D array of mapping cloudlets to vms.
   */
  public int[] getCloudletsToVMsPositions(Particle p) {
    int[][] positions = p.bestPositions;
    final int noOfTasks = positions[0].length;
    final int noOfVMs = positions.length;

    int[] result = new int[noOfTasks];

    switch (this.inertiaApproach) {
      case RIW_1:
      case LDIW_1:
        for (int i = 0; i < noOfVMs; i++) {
          for (int j = 0; j < noOfTasks; j++) {
            if (positions[i][j] == 1) {
              result[j] = i;
            }
          }
        }
        break;

      default:
        for (int j = 0, vmIdx = 0; j < noOfTasks; j++, vmIdx = 0) {
          for (int i = 1, chosen = positions[0][j]; i < noOfVMs; i++) {
            if (positions[i][j] > chosen) {
              vmIdx = i;
              chosen = positions[i][j];
            }
          }

          result[j] = vmIdx;
        }
        break;
    }

    return result;
  }

  /**
   * Clone 2D array.
   * @param source Array to be copied.
   * @return
   */
  protected static int[][] clone(int[][] source) {
    int[][] dest = new int[source.length][source[0].length];

    for (int i = 0; i < source.length; i++) {
      for (int j = 0; j < source[i].length; j++) {
        dest[i][j] = source[i][j];
      }
    }

    return dest;
  }
}