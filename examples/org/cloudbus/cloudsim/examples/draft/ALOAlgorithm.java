package org.cloudbus.cloudsim.examples.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

// PERSONAL NOTE (COULD BE WRONG)
//cloudlet = task
//chromosomelenght = solution length
//clouditerator = berapa kali jalan algoritma
//datacenteriterator = jumlah datacenter
//chromosome = solusi
//gene = komponen dalam array solusi/chromosome (might be vm?)

/* contoh isi chromosome  
 * misal task ada 9, vm ada 3
 * task 1,4,7 ke vm 0
 * task 2,5,8 ke vm 1
 * task 3,6,9 ke vm 2
 * 
 * Bentuk chromosome: [0,1,2,0,1,2,0,1,2]
*/

//todo
// Initialization: Random initialization of ant and antlion positions (solutions). //DONE
// Random Walks: Ants perform random walks influenced by antlions.  //DONE
// Trapping Mechanism: Antlions update their positions based on the ants' positions. //DONE
// Fitness Evaluation: Evaluate the fitness of each solution.// DONE (calcfitness)
// Elitism: Maintain the best solution found so far. 
// Termination: Repeat until a stopping criterion is met (like a maximum number of iterations).

public class ALOAlgorithm {
  // private int populationSize;
  private int antPopulationSize;
  private int antlionPopulationSize;
  private List<Cloudlet> cloudletList;
  private List<Vm> vmList;
  // private double mutationRate;
  private List<Integer> vmAllocation;
  private List<Integer> cloudletAllocation;
  private double localBestFitness;
  private List<Integer> bestVmAllocation;
  private List<Integer> bestCloudletAllocation;
  private double globalBestFitness;

  public ALOAlgorithm(int antPopulationSize, int antlionPopulationSize, List<Cloudlet> cloudletList, List<Vm> vmList) {
    // this.populationSize = populationSize;

    // ALO ONLY
    this.antPopulationSize = antPopulationSize;
    this.antlionPopulationSize = antlionPopulationSize;
    // ALO ONLY

    // this.mutationRate = mutationRate;
    this.cloudletList = cloudletList;
    this.vmList = vmList;
    this.localBestFitness = 0;
    this.globalBestFitness = 0;
  }

  // step 1 : populate the population
  public Population initPopulationAnt(int chromosomeLength, int dataCenterIterator) {
    Population population = new Population(this.antPopulationSize, chromosomeLength, dataCenterIterator);
    return population;
  }

  public Population initPopulationAntlion(int chromosomeLength, int dataCenterIterator) {
    Population population = new Population(this.antlionPopulationSize, chromosomeLength, dataCenterIterator);
    return population;
  }

  public void evalPopulation(Population population, int dataCenterIterator, int cloudletIteration) {
    for (Individual individual : population.getIndividuals()) {
      double individualFitness = calcFitness(individual, dataCenterIterator, cloudletIteration);
      individual.setFitness(individualFitness);
    }
  }

  // normal randomwalk
  // public void randomWalk(Individual individual, int lowerBound, int upperBound)
  //   {
  //   Random random = new Random();
  //   for (int i = 0; i < individual.getChromosomeLength(); i++) {
  //   // Randomly decide whether to increase or decrease the gene value
  //   if (random.nextBoolean()) {
  //   int newValue = individual.getGene(i) + 1;
  //   // Check if the new value is within the upper bound
  //   if (newValue <= upperBound) {
  //   individual.setGene(i, newValue);
  //   }
  //   } else {
  //   int newValue = individual.getGene(i) - 1;
  //   // Check if the new value is within the lower bound
  //   if (newValue >= lowerBound) {
  //   individual.setGene(i, newValue);
  //   }
  //   }
  //   }
  // }

  // random stepsize randomwalk
  public void randomWalk(Individual individual, int lowerBound, int upperBound) {
    Random random = new Random();
    for (int i = 0; i < individual.getChromosomeLength(); i++) {
      // Randomly decide whether to increase or decrease the gene value
      if (random.nextBoolean()) {
        int stepSize = random.nextInt(upperBound - lowerBound) + 1;
        int newValue = individual.getGene(i) + stepSize;
        // Check if the new value is within the upper bound
        if (newValue <= upperBound) {
          individual.setGene(i, newValue);
        }
      } else {
        int stepSize = random.nextInt(upperBound - lowerBound) + 1;
        int newValue = individual.getGene(i) - stepSize;
        // Check if the new value is within the lower bound
        if (newValue >= lowerBound) {
          individual.setGene(i, newValue);
        }
      }
    }
  }

  public void trapAnts(Population antPopulation, Population antlionPopulation, int dataCenterIterator,
      int cloudletIteration) {
    for (Individual ant : antPopulation.getIndividuals()) {
      // Calculate the fitness of the ant
      double antFitness = calcFitness(ant, dataCenterIterator, cloudletIteration);
      ant.setFitness(antFitness);

      // Skip this ant if it has not completed all cloudlets
      if (antFitness < 0) { // Assuming that the fitness is negative when the task
        // is not complete
        continue;
      }

      for (Individual antlion : antlionPopulation.getIndividuals()) {
        if (ant.getFitness() < antlion.getFitness()) {
          // The ant is trapped, update its position to the antlion's position
          for (int i = 0; i < ant.getChromosomeLength(); i++) {
            ant.setGene(i, antlion.getGene(i));
          }
          break;
        }
      }
    }
  }

  public void updateAntlionPopulation(Population antPopulation, Population antlionPopulation) {
    // Update the antlion population
    // elitism start here
    Individual bestAnt = antPopulation.getFittest(0);
    int worstAntlion = antlionPopulation.getIndexOfLeastFit();
    antlionPopulation.setIndividual(worstAntlion, bestAnt);// ceplak
  }

  public void updateAntlionPopulationEOBL(Population antPopulation, Population antlionPopulation,
      int dataCenterIterator) {
    // Update the antlion population
    // elitism start here
    Individual bestAnt = antPopulation.getFittest(0);
    int worstAntlionIndex = antlionPopulation.getIndexOfLeastFit();
    Individual worstAntlion = antlionPopulation.getIndividual(worstAntlionIndex);

    int maxGene = ((dataCenterIterator) * 9) - 1;

    for (int i = 0; i < bestAnt.getChromosomeLength(); i++) { // ganti manual per gen cz making new indiv is pain-peko
      int oppositeGene = maxGene - bestAnt.getGene(i);
      worstAntlion.setGene(i, oppositeGene);
    }
  }

  public double calcFitness(Individual individual, int dataCenterIterator, int cloudletIteration) {
    double totalExecutionTime = 0;
    double totalCost = 0;
    int iterator = 0;
    dataCenterIterator = dataCenterIterator - 1;

    for (int i = 0 + dataCenterIterator * 9 + cloudletIteration * 54; i < 9 + dataCenterIterator * 9
        + cloudletIteration * 54; i++) {
      int gene = individual.getGene(iterator);
      double mips = calculateMips(gene % 9);

      totalExecutionTime += cloudletList.get(i).getCloudletLength() / mips;
      totalCost += calculateCost(vmList.get(gene % 9), cloudletList.get(i));
      iterator++;
    }

    // Modify the fitness calculation to prioritize makespan optimization
    double makespanFitness = calculateMakespanFitness(totalExecutionTime);
    double costFitness = calculateCostFitness(totalCost);
    double fitness = makespanFitness + costFitness;

    individual.setFitness(fitness);
    return fitness;
  }

  private double calculateMips(int vmIndex) {
    double mips = 0;
    if (vmIndex % 9 == 0 || vmIndex % 9 == 3 || vmIndex % 9 == 6) {
      mips = 400;
    } else if (vmIndex % 9 == 1 || vmIndex % 9 == 4 || vmIndex % 9 == 7) {
      mips = 500;
    } else if (vmIndex % 9 == 2 || vmIndex % 9 == 5 || vmIndex % 9 == 8) {
      mips = 600;
    }
    return mips;
  }

  private double calculateCost(Vm vm, Cloudlet cloudlet) {
    double costPerMips = vm.getCostPerMips();
    double cloudletLength = cloudlet.getCloudletLength();
    double mips = vm.getMips();
    double executionTime = cloudletLength / mips;
    return costPerMips * executionTime;
  }

  private double calculateMakespanFitness(double totalExecutionTime) {
    // The higher the makespan, the lower the fitness
    // makin kamu pinter, maka makin cepet kamu ngerjain tugas :D
    return 1.0 / totalExecutionTime;
  }

  private double calculateCostFitness(double totalCost) {
    // The lower the cost, the higher the fitness
    return 1.0 / totalCost;
  }

  public void updateLocalBest(double fitness) {
    if (fitness > localBestFitness) {
      localBestFitness = fitness;
      bestVmAllocation = new ArrayList<>(vmAllocation);
      bestCloudletAllocation = new ArrayList<>(cloudletAllocation);
    }
  }

  public void updateGlobalBest(double fitness, List<Integer> vmAllocation, List<Integer> cloudletAllocation) {
    if (fitness > globalBestFitness) {
      globalBestFitness = fitness;
      bestVmAllocation = new ArrayList<>(vmAllocation);
      bestCloudletAllocation = new ArrayList<>(cloudletAllocation);
    }
  }

  public List<Integer> getBestVmAllocation() {
    return bestVmAllocation;
  }

  public List<Integer> getBestCloudletAllocation() {
    return bestCloudletAllocation;
  }
}