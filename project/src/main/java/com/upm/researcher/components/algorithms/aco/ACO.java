package com.upm.researcher.components.algorithms.aco;

import java.util.*;
import java.util.stream.*;

import com.upm.researcher.components.algorithms.standard.StandardSim;
import com.upm.researcher.components.cloudsim.ABroker;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;


public class ACO extends StandardSim
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
    /** rho || Vapourization constant for pheromone trail in each iteration (0.0 = no evaporation, 1.0 = full evaporation) */
    public double Evaporation_Rate; //(0.01);
    
	public ACO(int ants, int iterations, double initialPhero, int alpha, int beta, int q, double rho) 
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
		this.SetProgressMax(this.GetTasksTarget());
		this.SetProgressMessage("Ant Colony Optimization");
	}

	@Override
	public void Destroy() 
	{
	}

	@Override
    public void ProcessCloudletSubmit(ABroker broker) 
    {
		Map<Integer, Integer> map =
		this.allocateTasks(broker.getCloudletList(), broker.getVmsCreatedList());
		
		//for (Map.Entry<Integer, Integer> kvp : map.entrySet())
		for (int j = 0; j < broker.getCloudletList().size(); j++)
		{
			broker.getCloudletList().get(j).setVmId(map.get(j));
		}
		
        super.ProcessCloudletSubmit(broker);
	}
	
	@Override
	public <T extends Cloudlet> void ProcessCloudletsReturn(ABroker broker, T task) 
	{
	}

	public Map<Integer,Integer> allocateTasks(List<Cloudlet> taskList,List<Vm> vmList)
	{
		int n = vmList.size();
		Map<Integer,Integer> allocatedtasks = new HashMap<>();
		
		for(int i=0;i<(int)taskList.size()/(n-1);i++){
			Map<Integer,Integer> at = implement(taskList.subList(i*(n-1),(i+1)*(n-1)),vmList);
			for(int j=0;j<at.size();j++){
				allocatedtasks.put(j+i*(n-1),at.get(j));
				this.UpdateProgress(1);
			}
		}
		
		Map<Integer,Integer> at = implement(taskList.subList((taskList.size()/(n-1))*(n-1),taskList.size()),vmList);
		
		for(int j=0;j<at.size();j++){
			allocatedtasks.put(j+(taskList.size()/(n-1))*(n-1),at.get(j));
		}
		return allocatedtasks;
	}

	protected Map<Integer,Integer> implement(List<Cloudlet> taskList,List<Vm> vmList)
	{
		this.BenchmarkStart();
		int tasks = taskList.size();
		int vms = vmList.size();
		List<Integer> newVmList = IntStream.range(0,vms).boxed().collect(Collectors.toList());
		// Map<char,int> []edges = new HashMap<char,int>()[tasks];
		List<Double> lengths = new ArrayList<>();
		List<Map<Integer,Integer>> tabu = new ArrayList<>();
		Map<Integer, Map<Integer,Double> > execTimes;
		execTimes = new HashMap<>();

		for(int i=0;i<tasks;i++){
			Map<Integer,Double> x = new HashMap<>();
			for (int j=0; j<vms ; j++) {
				double t = getExecutionTime(vmList.get(j),taskList.get(i));
				x.put(j,t);
			}
			execTimes.put(i,x);
		}
		
		Map<Integer, Map<Integer,Double> > pheromones = initializePheromone(tasks,vms);
		int kmin=0;
		for(int t=1;t<=this.NumIterations;t++)
		{
			tabu = new ArrayList<>();

			Collections.shuffle(newVmList);

			for(int k=0;k<this.NumAnts;k++){
				tabu.add(k,new HashMap<Integer,Integer>());
				tabu.get(k).put(-1,newVmList.get(k % vms));
				double max = 0;

				for(int task=0;task<tasks;task++){
					int vmIndexChosen = chooseVM(execTimes.get(task),pheromones.get(task),tabu.get(k));
					tabu.get(k).put(task,vmIndexChosen);
					double time = execTimes.get(task).get(vmIndexChosen);
					max = (max<time)?time:max;
				}

				lengths.add(k,max);
			}

			double min = lengths.get(0);
			kmin = 0;

			for(int k=1;k<this.NumAnts;k++){
				min = (min>lengths.get(k))?lengths.get(k):min;
				kmin = (min>lengths.get(k))?k:kmin;
			}

			updatePheromones(pheromones,lengths,tabu);
			globalUpdatePheromones(pheromones,min,tabu.get(kmin));
		}

		this.BenchmarkEnd();

		return tabu.get(kmin);
	}


	protected int 
	chooseVM(Map<Integer,Double> execTimes, Map<Integer,Double> pheromones, Map<Integer,Integer> tabu){
		
		Map<Integer,Double> probab = new HashMap<>();
		double denominator = 0;
		
		for(int i=0;i<pheromones.size();i++){
			if(!tabu.containsValue(i)){
				double exec = execTimes.get(i), pher = pheromones.get(i);
				double p = Math.pow(1/exec,this.Distance_Priority)*Math.pow(pher,this.Pheromone_Importance);
				probab.put(i,p);
				denominator+=p;
			}
			else
				probab.put(i,0.0);
		}
		
		double max = 0;
		int maxvm = -1;
		
		for(int i=0;i<pheromones.size();i++){
			double p = probab.get(i)/denominator;
			if(max<p){
				max = p;
				maxvm = i;
			}
		}
		return maxvm;
	}

	protected Map<Integer, Map<Integer,Double> > initializePheromone(int tasks, int vms){
		Map<Integer, Map<Integer,Double> > pheromones = new HashMap<>();
		for(int i=0;i<tasks;i++){
			Map<Integer,Double> x = new HashMap<>();
			for (int j=0; j<vms ; j++) {
				x.put(j,this.Initial_Pheromone);
			}
			pheromones.put(i,x);
		}
		return pheromones;
	}

	protected void updatePheromones(Map<Integer, Map<Integer,Double> > pheromones, List<Double> length, 
		List<Map<Integer,Integer>> tabu){
		Map<Integer, Map<Integer,Double> > updatep = new HashMap<>();

		for(int i=0;i<pheromones.size();i++){
			Map<Integer,Double> v = new HashMap<>();
			for(int j=0;j<pheromones.get(i).size();j++){
				v.put(j,0.0);
			}
			updatep.put(i,v);
		}

		for(int k=0;k<tabu.size();k++){
			double updateValue = this.Pheromone_Total/length.get(k);
			Map<Integer,Integer> tour = new HashMap<>();
			tour.putAll(tabu.get(k));
			tour.remove(-1);
			// for(int i=0;i<tabu.get(k).size()-1;i++){
			// 	Map<Integer,Double> v = new HashMap<>();
			// 	v.put(tabu.get(k).get(i), updateValue);
			// 	updatep.put(i,v);
			// }
			for(int i=0;i<pheromones.size();i++){
				Map<Integer,Double> v = new HashMap<>();
				for(int j=0;j<pheromones.get(i).size();j++){
					if(tour.containsValue(j)){
						v.put(j,updatep.get(i).get(j)+updateValue);
					}
					else
						v.put(j,updatep.get(i).get(j));
				}
				updatep.put(i,v);
			}
		}
		for(int i=0;i<pheromones.size();i++){
			Map<Integer,Double> x = pheromones.get(i);
		
			for (int j=0; j<pheromones.get(i).size() ; j++) {
				x.put(j,(1-this.Evaporation_Rate)*x.get(j)+updatep.get(i).get(j));
			}
			pheromones.put(i,x);
		}
	}

	protected void globalUpdatePheromones(Map<Integer, Map<Integer,Double> > pheromones, double length, Map<Integer,Integer> tabu){
		double updateValue = this.Pheromone_Total/length;
		for(int i=0;i<tabu.size()-1;i++){
			Map<Integer,Double> v = pheromones.get(i);
			v.put(tabu.get(i),v.get(tabu.get(i))+updateValue);
			pheromones.put(i,v);
		}
	}

	protected double getExecutionTime(Vm VM, Cloudlet cloudlet){
		return (cloudlet.getCloudletLength()/(VM.getNumberOfPes()*VM.getMips()) + cloudlet.getCloudletFileSize()/VM.getBw());
	}
}
