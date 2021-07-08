<!-- 
Useful links
code inspector          | https://code-inspector.com 
create shields          | https://shields.io
markdown ref style      | https://markdownguide.org/basic-syntax/#reference-style-links 
-->

<!-- variable definition -->
<!-- shields -->
[contributors-shield]: https://img.shields.io/github/contributors/cypherskar/ReCLoud?style=flat-square
[contributors-url]: https://github.com/cypherskar/ReCloud/graphs/contributors
[downloads-shield]: https://img.shields.io/github/downloads/cypherskar/ReCloud/total?style=flat-square
[vulnerabilities-shield]: https://img.shields.io/snyk/vulnerabilities/github/cypherskar/recloud?style=flat-square
[code-grade]: https://www.code-inspector.com/project/24753/status/svg
[code-quality]: https://www.code-inspector.com/project/24753/score/svg
<!-- references -->
[Cloudsim]: https://github.com/Cloudslab/cloudsim/tree/cloudsim-4.0


<!-- entry -->
# Researchers Cloud 
![code-grade]
![code-quality]
![vulnerabilities-shield]
![downloads-shield]
[![contributors-shield]][contributors-url]

<!-- description -->
A simulation library based on [Cloudsim] that aims to offer many methods to automate replication of various simulation scenarios.

### Why not use Cloudsim directly?
> I faced alot of difficulties during my masters studies and alot of time was consumed by the process of re-implementating the research papers' algorithm/s instead of focusing on the actual improvments that could be done on any algorithm. Therefore, i created this tool to help me replicate all of those various experimentation scenarios with few lines of code as possible and as scalable and flexible as i can make them.

### Features Include:
> - Simplify process of creating, modifying and deleting cloudsim entities.
> - Create random attributes and automate enitity instantiation.
> - Control the distribution of created cloudlets over brokers, such as: Even or Random.
> - Integrate scheduling algorithms and control cloudsim process easily.
> - Packed with implementations of several famous cloud algorithms such as: Honey Bee and PSO.
> - Display experiment outcomes in form of tables, charts and log files.

_Help me expand base of implemented cloud algorithms_

## Simplest example
Create a `ReCloud` instance to start configuring experiment fields, then launch instance through `Recloud.launch()`:
```java
// Create a ReCloud instance.
ReCloud recloud = new ReCloud();

// Create a datacenters/server.
recloud.servers().newServer().make();

// Create a host.
recloud.servers().newHost().make();

// Create a broker.
recloud.jobs().newBroker().make();

// Create a virtual machine.
recloud.jobs().newVm().make();

// Create a cloudlet (Task).
recloud.jobs().newTask().make();

// Add wanted simulations to experiment.
recloud.experiment().newSimulations(new CloudsimSimulation());

// Add goal for number of cloudlets created.
recloud.experiment().taskTargets(100);

// Launch.
ReCloud.launch(recloud);
```
<img style="float: right;" height="500" width="100%" src="https://user-images.githubusercontent.com/72963129/124991815-d1faac00-e04a-11eb-9273-094d708f80b3.gif">

## Intermediate example
Configure each cloud entity separatly and run multiple scheduling algorithms with the same setup environment:
```java
// Create a ReCloud instance.
ReCloud recloud = new ReCloud();

// Create and configure datacenters/server.
recloud.servers().newServer().name("Server").environment("x86", "Linux", "Xen").timeZone(10.0)
.secCost(3.0).memCost(0.05).storageCost(0.001).bwCost(0.0).intervals(0).clones(1).make();

// Create and configure host.
recloud.servers().newHost().on("Server").mips(177730).pes(6).ram(16000).bw(15000)
.storage(4000000).clones(2).make();

// Create and configure broker.
recloud.jobs().newBroker().name("koala").make();

// Create and configure virtual machine.
recloud.jobs().newVm().mips(9726).pes(1).ram2(9).bw(1000).image(10000).vmm("Xen").clones(5).make();

// Create and configure a cloudlet (Task).
recloud.jobs().newTask().randomStyle(RandomStyle.Fixed_Pace).length(10000, 20000).pes(1)
.filesize(1).outpusize(1).make();

// Add wanted simulations to experiment.
recloud.experiment().newSimulations(
 new CloudsimSimulation(), 
 new Bullet_CS(GunType.Magnum),
 new SJF_CS(), 
 new HoneyBee_CS(0.1)
);

// Add goal for number of cloudlets created.
recloud.experiment().taskTargets(100);

// Launch.
ReCloud.launch(recloud);
```
<img style="float: right;" height="500" width="100%" src="https://user-images.githubusercontent.com/72963129/124992936-526ddc80-e04c-11eb-8c3a-0cd15a8f1c86.gif">

## Custom example
The difficulty of more complex examples rise with the experiment parameters, 
say we need the following setup
> - 1 server named `server 1` with 2 hosts named `host 1` & `host 2` repsectivley.
> - 1 server named `server 2` with 2 hosts, `host 1` from `server 1` & `host 3`
> - 2 brokers named `broker 1` & `broker 2`.
> - 2 virtual machines on `broker 1` named `vm 1` and `vm 2`.
> - 1 virtual machine on `broker 2` named `vm 2`.
> - 1 fixed task-type* cloudlets.
> - 1 random task-type* cloudlets.
> 
> task-types*: are cloudlets templates that will generate cloudsim cloudlets/tasks in runtime according to their setup.
> 
And with this setup we need to configure the experiment as following:
> - Create cloudlets and ditribute them over brokers `randomly`.
> - Test `CloudsimSimulation`, `Honey Bee and Particle` and `Swarm Optimization algorithms`.
> - Experiment with `100, 200, 300` tasks.


