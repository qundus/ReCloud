package com.upm.researcher.components.algorithms.aco;

import java.util.*;

public class AAnt implements Cloneable
{
    protected List<Integer> visited; // Depends on visited VMs only, no connection to the tasks
    protected int[] trail;
    protected double trailLength;

    public AAnt(int totalTasks, int startTourAtVm)
    {
        this.trailLength = 0.0;
        this.trail = new int[totalTasks];
        this.visited = new ArrayList<Integer>();

        this.visited.add(startTourAtVm);
    }
    
    protected void VisitVm(int vm, int task)
    {
        this.trail[task] = vm;
        this.visited.add(vm);
    }

    protected void CheckVisited(int[] randomVms, boolean allVmsAreIdentical)
    {
        //if (task != 0 && task % randomVms.length == 0)
        if (this.visited.size() >= randomVms.length)
        {
            int lastVisited = this.visited.get(this.visited.size()-1);
            this.visited = new ArrayList<Integer>(this.visited.size());
            //this.visited.add((task / (random.nextInt(randomVms.length) + 1)) % randomVms.length);

            if (allVmsAreIdentical)
                this.visited.add(randomVms[lastVisited]);
            else
                this.visited.add(lastVisited);
        }
    }

    protected boolean Visited(int vm)
    {
        return this.visited.contains(vm);
    }

    protected void UpdateTrailLength(double pheromone)
    {
        if (pheromone > this.trailLength)
            this.trailLength = pheromone;
    }

    public int[] GetTrail()
    {
        return this.trail;
    }

    public double GetTrailLength()
    {
        return this.trailLength;
    }

    public void ClearTrailLength()
    {
        this.trailLength = 0.0;
        //this.trail = new HashMap<>();
    }

    public AAnt Clone()
    {
        try
        {
            return (AAnt) super.clone();
        }
        catch(CloneNotSupportedException e) {
            return null;
        }
    }
}