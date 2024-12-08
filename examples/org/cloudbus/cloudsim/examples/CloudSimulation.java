package org.cloudbus.cloudsim.examples;

import java.util.Locale;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.DoubleStream;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


public class CloudSimulation {
    private static PowerDatacenter datacenter1, datacenter2, datacenter3, datacenter4, datacenter5, datacenter6;
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;
    private static int bot = 1;

    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        Log.printLine("Starting Cloud Simulation with OPSO...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            BufferedWriter outputWriter = null;
            outputWriter = new BufferedWriter(new FileWriter("filename.txt"));

            CloudSim.init(num_user, calendar, trace_flag);

            int hostId = 0;

            datacenter1 = createDatacenter("DataCenter_1", hostId);
            hostId = 3;
            datacenter2 = createDatacenter("DataCenter_2", hostId);
            hostId = 6;
            datacenter3 = createDatacenter("DataCenter_3", hostId);
            hostId = 9;
            datacenter4 = createDatacenter("DataCenter_4", hostId);
            hostId = 12;
            datacenter5 = createDatacenter("DataCenter_5", hostId);
            hostId = 15;
            datacenter6 = createDatacenter("DataCenter_6", hostId);

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();
            int vmNumber = 54;
//             int cloudletNumber = 7395;
  		      int cloudletNumber = bot*1000;

            vmlist = createVM(brokerId, vmNumber);
            cloudletList = createCloudlet(brokerId, cloudletNumber);

            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            int cloudletLoopingNumber = cloudletNumber / vmNumber - 1;

            for (int cloudletIterator = 0; cloudletIterator <= cloudletLoopingNumber; cloudletIterator++) {
                System.out.println("Cloudlet Iteration Number " + cloudletIterator);

                for (int dataCenterIterator = 1; dataCenterIterator <= 6; dataCenterIterator++) {
                    
                    // Parameters for DAPDP
                    int Imax = 5;
                    int populationSize = 30;
                    double wMax = 0.6;
                    double wMin = 0.3;
                    double l1 = 2.0;
                    double l2 = 2.0;
                    
                    // Static inertia weight for PSO
                    double w = 0.6;

//                   // OPSO
//                   OPSO OPSO = new OPSO(Imax, populationSize, wMax, wMin, l1, l2, cloudletList, vmlist, cloudletNumber);
//
//                   // Initialize population
//                   System.out.println("Datacenter " + dataCenterIterator + " Population Initialization");
//
//                   Population population = OPSO.initPopulation(cloudletNumber, dataCenterIterator);
//
//                   // Evaluate initial fitness
//                   OPSO.evaluateFitness(population, dataCenterIterator, cloudletIterator);
//
//                   // Iteration loop
//                   int iteration = 1;
//                   while (iteration <= Imax) {
//                       OPSO.updateVelocitiesAndPositions(population, iteration, dataCenterIterator);
//                       OPSO.evaluateFitness(population, dataCenterIterator, cloudletIterator);
//
//                       System.out.println("Iteration " + iteration + " Best Fitness: " + OPSO.getBestFitness());
//
//                       iteration++;
//                   }
//
//
//                   // Get the best solution
//                   int[] bestSolution = OPSO.getBestVmAllocation();


                    // PSO
                    PSO PSO = new PSO(Imax, populationSize, w, l1, l2, cloudletList, vmlist, cloudletNumber);

                    // Initialize population
                    System.out.println("Datacenter " + dataCenterIterator + " Population Initialization");

                    PopulationPSO population = PSO.initPopulation(cloudletNumber, dataCenterIterator);

                    // Evaluate initial fitness
                    PSO.evaluateFitness(population, dataCenterIterator, cloudletIterator);

                    // Iteration loop
                    int iteration = 1;
                    while (iteration <= Imax) {
                        PSO.updateVelocitiesAndPositions(population, iteration, dataCenterIterator);
                        PSO.evaluateFitness(population, dataCenterIterator, cloudletIterator);

                        System.out.println("Iteration " + iteration + " Best Fitness: " + PSO.getBestFitness());

                        iteration++;
                    }

                    // Get the best solution
                    int[] bestSolution = PSO.getBestVmAllocation();

                    
//                     Assign tasks to VMs based on bestSolution
                    for (int assigner = 0 + (dataCenterIterator - 1) * 9 + cloudletIterator * 54;
                         assigner < 9 + (dataCenterIterator - 1) * 9 + cloudletIterator * 54; assigner++) {
                        int vmId = bestSolution[assigner - (dataCenterIterator - 1) * 9 - cloudletIterator * 54];
                        //System.out.println("Assigner: " + assigner + " vmId: " + vmId + " best solution length: " + bestSolution.length);
                        broker.bindCloudletToVm(assigner, vmId);
                    }
                }
            }

            // Start simulation and print results as in Lampiran 4
            CloudSim.startSimulation();

            outputWriter.flush();
            outputWriter.close();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("Cloud Simulation with OPSO finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Simulation terminated due to an error");
        }
    }


  private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
    ArrayList<Double> randomSeed = getSeedValue(cloudlets);

    LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

    long fileSize = 300; 
    long outputSize = 300;
    int pesNumber = 1; 
    UtilizationModel utilizationModel = new UtilizationModelFull();

    for (int i = 0; i < cloudlets; i++) {
      long length = 0;

      if (randomSeed.size() > i) {
        length = Double.valueOf(randomSeed.get(i)).longValue();
      }

      Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
      cloudlet.setUserId(userId);
      list.add(cloudlet);
    }
    Collections.shuffle(list);

    return list;
  }

  private static List<Vm> createVM(int userId, int vms) {
    LinkedList<Vm> list = new LinkedList<Vm>();

    long size = 10000;
    int[] ram = { 512, 1024, 2048 }; 
    int[] mips = { 400, 500, 600 }; 
    long bw = 1000; 
    int pesNumber = 1; 
    String vmm = "Xen"; 

    Vm[] vm = new Vm[vms];

    for (int i = 0; i < vms; i++) {
      vm[i] = new Vm(i, userId, mips[i % 3], pesNumber, ram[i % 3], bw, size, vmm, new CloudletSchedulerSpaceShared());
      list.add(vm[i]);
    }

    return list;
  }

  private static ArrayList<Double> getSeedValue(int cloudletcount) {
    ArrayList<Double> seed = new ArrayList<Double>();
    try {
         File fobj = new File(System.getProperty("user.dir") + "/cloudsim-3.0.3/datasets/randomSimple/RandSimple"+bot+"000.txt");
//         File fobj = new File(System.getProperty("user.dir") + "/cloudsim-3.0.3/datasets/randomStratified/RandStratified"+bot+"000.txt");
//      File fobj = new File(System.getProperty("user.dir") + "/cloudsim-3.0.3/datasets/SDSC/SDSC7395.txt");
      java.util.Scanner readFile = new java.util.Scanner(fobj);

      while (readFile.hasNextLine() && cloudletcount > 0) {
        seed.add(readFile.nextDouble());
        cloudletcount--;
      }
      readFile.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return seed;
  }

  private static PowerDatacenter createDatacenter(String name, int hostId) {

    List<PowerHost> hostList = new ArrayList<PowerHost>();

    List<Pe> peList1 = new ArrayList<Pe>();
    List<Pe> peList2 = new ArrayList<Pe>();
    List<Pe> peList3 = new ArrayList<Pe>();

    int mipsunused = 300; 
    int mips1 = 400; 
    int mips2 = 500;
    int mips3 = 600;

    peList1.add(new Pe(0, new PeProvisionerSimple(mips1))); 
    peList1.add(new Pe(1, new PeProvisionerSimple(mips1)));
    peList1.add(new Pe(2, new PeProvisionerSimple(mips1)));
    peList1.add(new Pe(3, new PeProvisionerSimple(mipsunused)));
    peList2.add(new Pe(4, new PeProvisionerSimple(mips2)));
    peList2.add(new Pe(5, new PeProvisionerSimple(mips2)));
    peList2.add(new Pe(6, new PeProvisionerSimple(mips2)));
    peList2.add(new Pe(7, new PeProvisionerSimple(mipsunused)));
    peList3.add(new Pe(8, new PeProvisionerSimple(mips3)));
    peList3.add(new Pe(9, new PeProvisionerSimple(mips3)));
    peList3.add(new Pe(10, new PeProvisionerSimple(mips3)));
    peList3.add(new Pe(11, new PeProvisionerSimple(mipsunused)));

    int ram = 128000;
    long storage = 1000000;
    int bw = 10000;
    int maxpower = 117; 
    int staticPowerPercentage = 50; 

    hostList.add(
        new PowerHostUtilizationHistory(
            hostId, new RamProvisionerSimple(ram),
            new BwProvisionerSimple(bw),
            storage,
            peList1,
            new VmSchedulerTimeShared(peList1),
            new PowerModelLinear(maxpower, staticPowerPercentage)));
    hostId++;

    hostList.add(
        new PowerHostUtilizationHistory(
            hostId, new RamProvisionerSimple(ram),
            new BwProvisionerSimple(bw),
            storage,
            peList2,
            new VmSchedulerTimeShared(peList2),
            new PowerModelLinear(maxpower, staticPowerPercentage)));
    hostId++;

    hostList.add(
        new PowerHostUtilizationHistory(
            hostId, new RamProvisionerSimple(ram),
            new BwProvisionerSimple(bw),
            storage,
            peList3,
            new VmSchedulerTimeShared(peList3),
            new PowerModelLinear(maxpower, staticPowerPercentage)));

    String arch = "x86"; 
    String os = "Linux"; 
    String vmm = "Xen"; 
    double time_zone = 10.0; 
    double cost = 3.0; 
    double costPerMem = 0.05; 
    double costPerStorage = 0.1; 
    double costPerBw = 0.1; 
    LinkedList<Storage> storageList = new LinkedList<Storage>();

    DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
        arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

    PowerDatacenter datacenter = null;
    try {
      datacenter = new PowerDatacenter(name, characteristics, new PowerVmAllocationPolicySimple(hostList), storageList, 9); 
    } catch (Exception e) {
      e.printStackTrace();
    }

    return datacenter;
  }

  private static DatacenterBroker createBroker() {

    DatacenterBroker broker = null;
    try {
      broker = new DatacenterBroker("Broker");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return broker;
  }

  private static void printCloudletList(List<Cloudlet> list) throws FileNotFoundException {

    // Initializing the printed output to zero
    int size = list.size();
    Cloudlet cloudlet = null;

    String indent = "    ";
    Log.printLine();
    Log.printLine("========== OUTPUT ==========");
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
        "Data center ID" + indent + "VM ID" + indent + "Time"
        + indent + "Start Time" + indent + "Finish Time" + indent + "Waiting Time");

    double waitTimeSum = 0.0;
    double CPUTimeSum = 0.0;
    int totalValues = 0;
    DecimalFormat dft = new DecimalFormat("###,##");

    double response_time[] = new double[size];

    // Printing all the status of the Cloudlets
    for (int i = 0; i < size; i++) {
      cloudlet = list.get(i);
      Log.print(cloudlet.getCloudletId() + indent + indent);

      if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
        Log.print("SUCCESS");
        CPUTimeSum = CPUTimeSum + cloudlet.getActualCPUTime();
        waitTimeSum = waitTimeSum + cloudlet.getWaitingTime();
        Log.printLine(
            indent + indent + indent + (cloudlet.getResourceId() - 1) + indent + indent + indent + cloudlet.getVmId() +
                indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
                + dft.format(cloudlet.getExecStartTime()) +
                indent + indent + dft.format(cloudlet.getFinishTime()) + indent + indent + indent
                + dft.format(cloudlet.getWaitingTime()));
        totalValues++;

        response_time[i] = cloudlet.getActualCPUTime();
      }
    }
    DoubleSummaryStatistics stats = DoubleStream.of(response_time).summaryStatistics();

    Log.printLine();
    System.out.println(String.format("min = %,6f",stats.getMin()));
    System.out.println(String.format("Response_Time: %,6f",CPUTimeSum / totalValues));

    Log.printLine();
    Log.printLine(String.format("TotalCPUTime : %,6f",CPUTimeSum));
    Log.printLine("TotalWaitTime : "+waitTimeSum);
    Log.printLine("TotalCloudletsFinished : "+totalValues);

    // Average Cloudlets Finished
    Log.printLine(String.format("AverageCloudletsFinished : %,6f",(CPUTimeSum / totalValues)));

    // Average Start Time
    double totalStartTime = 0.0;
    for (int i = 0; i < size; i++) {
      totalStartTime += cloudletList.get(i).getExecStartTime();
    }
    double avgStartTime = totalStartTime / size;
    System.out.println(String.format("Average StartTime: %,6f",avgStartTime));

    // Average Execution Time
    double ExecTime = 0.0;
    for (int i = 0; i < size; i++) {
      ExecTime += cloudletList.get(i).getActualCPUTime();
    }
    double avgExecTime = ExecTime / size;
    System.out.println(String.format("Average Execution Time: %,6f",avgExecTime));

    // Average Finish Time
    double totalTime = 0.0;
    for (int i = 0; i < size; i++) {
      totalTime += cloudletList.get(i).getFinishTime();
    }
    double avgTAT = totalTime / size;
    System.out.println(String.format("Average FinishTime: %,6f",avgTAT));

    // Average Waiting Time
    double avgWT = cloudlet.getWaitingTime() / size;
    System.out.println(String.format("Average Waiting time: %,6f",avgWT));

    // Throughput
    double maxFT = 0.0;
    for (int i = 0; i < size; i++) {
      double currentFT = cloudletList.get(i).getFinishTime();
      if (currentFT > maxFT) {
        maxFT = currentFT;
      }
    }
    double throughput = size / maxFT;
    System.out.println(String.format("Throughput: %,9f",throughput));

    // Makespan
    double makespan = 0.0;
    double makespan_total = makespan + cloudlet.getFinishTime();
    System.out.println(String.format("Makespan: %,f",makespan_total));

    // Imbalance Degree
    double degree_of_imbalance = (stats.getMax() - stats.getMin()) / (CPUTimeSum / totalValues);
    System.out.println(String.format("Imbalance Degree: %,3f",degree_of_imbalance));

    // Scheduling Length
    double scheduling_length = waitTimeSum + makespan_total;
    Log.printLine(String.format("Total Scheduling Length: %,f", scheduling_length));

    // CPU Resource Utilization
    double resource_utilization = (CPUTimeSum / (makespan_total * 54)) * 100;
    Log.printLine(String.format("Resouce Utilization: %,f",resource_utilization));

    // Energy Consumption
    Log.printLine(String.format("Total Energy Consumption: %,2f  kWh",
        (datacenter1.getPower() + datacenter2.getPower() + datacenter3.getPower() + datacenter4.getPower()
            + datacenter5.getPower() + datacenter6.getPower()) / (3600 * 1000)));
  }

}
