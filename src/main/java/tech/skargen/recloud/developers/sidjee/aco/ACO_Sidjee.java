package tech.skargen.recloud.developers.sidjee.aco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import tech.skargen.recloud.components.cloudsim.IReBroker;
import tech.skargen.recloud.components.simulation.ASimulation;

/**
 * <pre>
 * Code_By: Sidjee.
 * </pre>
 * Github: https://github.com/sidjee/Ant-Colony-Optimization-Framework
 * </p>
 * Implementation based on unkown papers/books:
 */
public class ACO_Sidjee extends ASimulation {
  /** Nmber of ants to be used. */
  protected int numAnts;
  /** Number of iterations. */
  protected int numIterations;
  /** Initial pheromone for each VM. */
  protected double initPheromone;
  /** Q or total amount of pheromone left on trail by each Ant. */
  protected double totalPheromone;
  /** Alpha or controls the pheromone importance. */
  protected double alpha;
  /** Beta or controls the distance priority; should be greater than alpha. */
  protected double beta;
  /**
   * rho or vapourization constant for pheromone trail in each iteration. (0.0 = no
   * evaporation, 1.0 = full evaporation)
   */
  protected double rho;

  /**
   * Constructor.
   *
   * @param ants         No of ants to be used.
   * @param iterations   No of iterations.
   * @param initialPhero Initial pheromone for each VM.
   * @param alpha        Controls the pheromone importance
   * @param beta         Controls the distance priority
   *                     </p>
   *                     This parameter should be greater than alpha for the best
   *                     results.
   * @param q            Total amount of pheromone left on trail by each Ant.
   * @param rho          Vapourization constant for pheromone trail in each
   *                     iteration.
   */
  public ACO_Sidjee(
      int ants, int iterations, double initialPhero, int alpha, int beta, int q, double rho) {
    this.numAnts = ants;
    this.numIterations = iterations;
    this.initPheromone = initialPhero;
    this.alpha = alpha;
    this.beta = beta;
    this.totalPheromone = q;
    this.rho = rho;
  }

  @Override
  public String getAlgorithmName() {
    return "ACO";
  }

  @Override
  public String getAdditionalInfo() {
    return "none";
  }

  @Override
  public String getDeveloper() {
    return "sidjee";
  }

  @Override
  public void startEntity(IReBroker rebroker) {}

  @Override
  public void shutdownEntity(IReBroker rebroker) {}

  @Override
  public void processCloudletsSubmit(IReBroker rebroker) {
    List<Cloudlet> cloudlets = rebroker.getEntity().getCloudletList();
    List<Vm> vms = rebroker.getEntity().getVmsCreatedList();

    rebroker.updateProgressMax(cloudlets.size());
    rebroker.updateProgressMessage("Ant Colony Optimization By github.com/Sidjee");
    Map<Integer, Integer> map = this.allocateTasks(cloudlets, vms, rebroker);

    // for (Map.Entry<Integer, Integer> kvp : map.entrySet())
    for (int j = 0; j < cloudlets.size(); j++) {
      Cloudlet cloudlet = cloudlets.get(j);
      cloudlet.setVmId(vms.get(map.get(j)).getId());
      rebroker.submitCloudlet(cloudlet);
    }

    map = null;
    cloudlets = null;
    vms = null;
  }

  @Override
  public <T extends Cloudlet> void processCloudletReturn(IReBroker rebroker, T task) {}

  /**
   * Map tasks to certain virtual machines .
   * @param taskList Tasks to be assigned.
   * @param vmList Virtual machines to assign tasks to.
   * @return Mapped tasks.
   */
  public Map<Integer, Integer> allocateTasks(
      List<Cloudlet> taskList, List<Vm> vmList, IReBroker rebroker) {
    int n = vmList.size();
    Map<Integer, Integer> allocatedtasks = new HashMap<>();

    for (int i = 0; i < (int) taskList.size() / (n - 1); i++) {
      Map<Integer, Integer> at =
          implement(taskList.subList(i * (n - 1), (i + 1) * (n - 1)), vmList);

      for (int j = 0; j < at.size(); j++) {
        allocatedtasks.put(j + i * (n - 1), at.get(j));
        rebroker.updateProgress(1);
      }
    }

    Map<Integer, Integer> at =
        implement(taskList.subList((taskList.size() / (n - 1)) * (n - 1), taskList.size()), vmList);

    for (int j = 0; j < at.size(); j++) {
      allocatedtasks.put(j + (taskList.size() / (n - 1)) * (n - 1), at.get(j));
    }
    return allocatedtasks;
  }

  protected Map<Integer, Integer> implement(List<Cloudlet> taskList, List<Vm> vmList) {
    int tasks = taskList.size();
    int vms = vmList.size();
    List<Integer> newVmList = IntStream.range(0, vms).boxed().collect(Collectors.toList());
    // Map<char,int> []edges = new HashMap<char,int>()[tasks];
    List<Double> lengths = new ArrayList<>();
    List<Map<Integer, Integer>> tabu = new ArrayList<>();
    Map<Integer, Map<Integer, Double>> execTimes;
    execTimes = new HashMap<>();

    for (int i = 0; i < tasks; i++) {
      Map<Integer, Double> x = new HashMap<>();
      for (int j = 0; j < vms; j++) {
        double t = getExecutionTime(vmList.get(j), taskList.get(i));
        x.put(j, t);
      }
      execTimes.put(i, x);
    }

    Map<Integer, Map<Integer, Double>> pheromones = initializePheromone(tasks, vms);
    int kmin = 0;
    for (int t = 1; t <= this.numIterations; t++) {
      tabu = new ArrayList<>();

      Collections.shuffle(newVmList);

      for (int k = 0; k < this.numAnts; k++) {
        tabu.add(k, new HashMap<Integer, Integer>());
        tabu.get(k).put(-1, newVmList.get(k % vms));
        double max = 0;

        for (int task = 0; task < tasks; task++) {
          int vmIndexChosen = chooseVM(execTimes.get(task), pheromones.get(task), tabu.get(k));
          tabu.get(k).put(task, vmIndexChosen);
          double time = execTimes.get(task).get(vmIndexChosen);
          max = (max < time) ? time : max;
        }

        lengths.add(k, max);
      }

      double min = lengths.get(0);
      kmin = 0;

      for (int k = 1; k < this.numAnts; k++) {
        min = (min > lengths.get(k)) ? lengths.get(k) : min;
        kmin = (min > lengths.get(k)) ? k : kmin;
      }

      updatePheromones(pheromones, lengths, tabu);
      globalUpdatePheromones(pheromones, min, tabu.get(kmin));
    }

    return tabu.get(kmin);
  }

  protected int chooseVM(
      Map<Integer, Double> execTimes, Map<Integer, Double> pheromones, Map<Integer, Integer> tabu) {
    Map<Integer, Double> probab = new HashMap<>();
    double denominator = 0;

    for (int i = 0; i < pheromones.size(); i++) {
      if (!tabu.containsValue(i)) {
        double exec = execTimes.get(i);
        double pher = pheromones.get(i);
        double p = Math.pow(1 / exec, this.beta) * Math.pow(pher, this.alpha);
        probab.put(i, p);
        denominator += p;
      } else {
        probab.put(i, 0.0);
      }
    }

    double max = 0;
    int maxvm = -1;

    for (int i = 0; i < pheromones.size(); i++) {
      double p = probab.get(i) / denominator;
      if (max < p) {
        max = p;
        maxvm = i;
      }
    }
    return maxvm;
  }

  protected Map<Integer, Map<Integer, Double>> initializePheromone(int tasks, int vms) {
    Map<Integer, Map<Integer, Double>> pheromones = new HashMap<>();
    for (int i = 0; i < tasks; i++) {
      Map<Integer, Double> x = new HashMap<>();
      for (int j = 0; j < vms; j++) {
        x.put(j, this.initPheromone);
      }
      pheromones.put(i, x);
    }
    return pheromones;
  }

  protected void updatePheromones(Map<Integer, Map<Integer, Double>> pheromones,
      List<Double> length, List<Map<Integer, Integer>> tabu) {
    Map<Integer, Map<Integer, Double>> updatep = new HashMap<>();

    for (int i = 0; i < pheromones.size(); i++) {
      Map<Integer, Double> v = new HashMap<>();
      for (int j = 0; j < pheromones.get(i).size(); j++) {
        v.put(j, 0.0);
      }
      updatep.put(i, v);
    }

    for (int k = 0; k < tabu.size(); k++) {
      double updateValue = this.totalPheromone / length.get(k);
      Map<Integer, Integer> tour = new HashMap<>();
      tour.putAll(tabu.get(k));
      tour.remove(-1);
      // for(int i=0;i<tabu.get(k).size()-1;i++){
      // Map<Integer,Double> v = new HashMap<>();
      // v.put(tabu.get(k).get(i), updateValue);
      // updatep.put(i,v);
      // }
      for (int i = 0; i < pheromones.size(); i++) {
        Map<Integer, Double> v = new HashMap<>();
        for (int j = 0; j < pheromones.get(i).size(); j++) {
          if (tour.containsValue(j)) {
            v.put(j, updatep.get(i).get(j) + updateValue);
          } else {
            v.put(j, updatep.get(i).get(j));
          }
        }
        updatep.put(i, v);
      }
    }
    for (int i = 0; i < pheromones.size(); i++) {
      Map<Integer, Double> x = pheromones.get(i);

      for (int j = 0; j < pheromones.get(i).size(); j++) {
        x.put(j, (1 - this.rho) * x.get(j) + updatep.get(i).get(j));
      }
      pheromones.put(i, x);
    }
  }

  protected void globalUpdatePheromones(
      Map<Integer, Map<Integer, Double>> pheromones, double length, Map<Integer, Integer> tabu) {
    double updateValue = this.totalPheromone / length;
    for (int i = 0; i < tabu.size() - 1; i++) {
      Map<Integer, Double> v = pheromones.get(i);
      v.put(tabu.get(i), v.get(tabu.get(i)) + updateValue);
      pheromones.put(i, v);
    }
  }

  protected double getExecutionTime(Vm vm, Cloudlet cloudlet) {
    return (cloudlet.getCloudletLength() / (vm.getNumberOfPes() * vm.getMips())
        + cloudlet.getCloudletFileSize() / vm.getBw());
  }
}