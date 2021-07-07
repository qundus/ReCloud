package tech.skargen.recloud.developers.cypherskar.aco;

import java.util.ArrayList;
import java.util.List;

public class Ant implements Cloneable {
  /**
   * Depends on visited VMs only, no connection to the tasks.
   */
  protected List<Integer> visited;

  /**
   * Phermone trail left by scouting ants.
   */
  protected int[] trail;

  /**
   * Length of the phermone trauil.
   */
  protected double trailLength;

  /**
   * Constructor.
   *
   * @param totalTasks    Total number of tasks.
   * @param startTourAtVm Virtual machine to start tour.
   */
  public Ant(int totalTasks, int startTourAtVm) {
    this.trailLength = 0.0;
    this.trail = new int[totalTasks];
    this.visited = new ArrayList<Integer>();

    this.visited.add(startTourAtVm);
  }

  public int[] getTrail() {
    return this.trail;
  }

  public double getTrailLength() {
    return this.trailLength;
  }

  public void clearTrailLength() {
    this.trailLength = 0.0;
    // this.trail = new HashMap<>();
  }

  /**
   * Clone this instance of the object.
   */
  public Ant clone() {
    try {
      return (Ant) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  protected void visitVm(int vm, int task) {
    this.trail[task] = vm;
    this.visited.add(vm);
  }

  protected void checkVisited(int[] randomVms, boolean allVmsAreIdentical) {
    // if (task != 0 && task % randomVms.length == 0)
    if (this.visited.size() >= randomVms.length) {
      int lastVisited = this.visited.get(this.visited.size() - 1);
      this.visited = new ArrayList<Integer>(this.visited.size());
      // this.visited.add((task / (random.nextInt(randomVms.length) + 1)) %
      // randomVms.length);

      if (allVmsAreIdentical) {
        this.visited.add(randomVms[lastVisited]);
      } else {
        this.visited.add(lastVisited);
      }
    }
  }

  protected boolean visited(int vm) {
    return this.visited.contains(vm);
  }

  protected void updateTrailLength(double pheromone) {
    if (pheromone > this.trailLength) {
      this.trailLength = pheromone;
    }
  }
}