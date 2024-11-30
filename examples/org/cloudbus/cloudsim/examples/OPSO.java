package org.cloudbus.cloudsim.examples;

import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class OPSO {
    // Parameters
    private int Imax; // Maximum number of iterations
    private int populationSize; // Population size P
    private double wMax; // Maximum inertia weight
    private double wMin; // Minimum inertia weight
    private double l1; // Learning factor
    private double l2; // Learning factor

    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    private double globalBestFitness = Double.NEGATIVE_INFINITY;
    private int[] globalBestPosition;

    public OPSO(int Imax, int populationSize, double wMax, double wMin, double l1, double l2,
                         List<Cloudlet> cloudletList, List<Vm> vmList, int chromosomeLength) {
        this.Imax = Imax;
        this.populationSize = populationSize;
        this.wMax = wMax;
        this.wMin = wMin;
        this.l1 = l1;
        this.l2 = l2;
        this.cloudletList = cloudletList;
        this.vmList = vmList;

// 
        //  this.globalBestPosition = new int[vmList.size()];
        //  for (int i = 0; i < chromosomeLength; i++) {
        //      this.globalBestPosition[i] = 0;
        //  }
    }

    // Step 3: Initialize population
    public Population initPopulation(int chromosomeLength, int dataCenterIterator) {
        Population population = new Population(this.populationSize, chromosomeLength, dataCenterIterator);
        return population;
    }

    // Step 4: Evaluate fitness
    public void evaluateFitness(Population population, int dataCenterIterator, int cloudletIteration) {
        for (Individual individual : population.getIndividuals()) {
            double fitness = calcFitness(individual, dataCenterIterator, cloudletIteration);
            individual.setFitness(fitness);

            // Step 5: Update personal best
            if (fitness > individual.getPersonalBestFitness()) {
                individual.setPersonalBestFitness(fitness);
                individual.setPersonalBestPosition(individual.getChromosome());
            }

            // Step 6: Update global best
            if (fitness > globalBestFitness) {
                globalBestFitness = fitness;
                globalBestPosition = individual.getChromosome().clone();
            }

            // eobl 
            // nyari posisi terburuk
            // reverse dari best position individual (personal)
            // didalam iterasi atau diluar
        }
    }

    // Step 7: Update velocities and positions
    public void updateVelocitiesAndPositions(Population population, int iteration) {
        double w = wMax - ((wMax - wMin) * iteration) / Imax; // Update inertia weight
        // System.out.println("----------- Iteration: " + iteration + ", Inertia Weight (w): " + w);

        Random random = new Random();

        for (Individual particle : population.getIndividuals()) {
            for (int i = 0; i < particle.getChromosomeLength(); i++) {
                double vPrev = particle.getVelocity()[i]; // v_{i-1}
                double pBest = particle.getPersonalBestPosition()[i];
                double gBest = globalBestPosition[i];
                int currentPosition = particle.getGene(i);

                // double r1 = 0.5 + (random.nextDouble() * 0.5); // r1 antara 0.5 hingga 1.0
                // double r2 = 0.5 + (random.nextDouble() * 0.5); // r2 antara 0.5 hingga 1.0
                double r1 = random.nextDouble(); // r1 antara 0.5 hingga 1.0
                double r2 = random.nextDouble(); // r2 antara 0.5 hingga 1.0

                // Update velocity
                double newVelocity = w * vPrev
                        + l1 * r1 * (pBest - vPrev)
                        + l2 * r2 * (gBest - vPrev);
                // System.out.println("-----Previous Velocity (vPrev): " + vPrev);
                // System.out.println("--New Velocity: " + newVelocity);

                // Apply position bounds
                double vmSize = 54.0 - 1.0;
                double Vmax = vmSize * 0.5;
                double velocityMin = -Vmax;
                double velocityMax = Vmax;

                // Batasi velocity
                if (newVelocity < velocityMin) {
                    newVelocity = velocityMin;
                } else if (newVelocity > velocityMax) {
                    newVelocity = velocityMax;
                }

                // Update position
                int newPosition = (int) Math.round(newVelocity);

                // Apply position bounds
                int minPosition = 0;
                int maxPosition = vmList.size() - 1;

                // If the position is invalid
                if (newPosition < minPosition) {
                    newPosition = minPosition;
                } else if (newPosition > maxPosition) {
                    newPosition = maxPosition;
                }
                // System.out.println("--Current Position: " + currentPosition);
                // System.out.println("--New Position (bounded): " + newPosition);

                // Update particle
                particle.setVelocity(i, newVelocity);
                particle.setGene(i, newPosition);
                // System.out.println("Personal Best (pBest): " + pBest);
                // System.out.println("Global Best (gBest): " + gBest);
                // System.out.println("Random r1: " + r1 + ", Random r2: " + r2);

                // System.out.println("--Updated Velocity: " + particle.getVelocity()[i]);
                // System.out.println("--Updated Gene: " + particle.getGene(i));
                
            }
        }
    }

    // Calculate fitness based on the system fairness (Equation 1)
    public double calcFitness(Individual individual, int dataCenterIterator, int cloudletIteration) {
        double totalExecutionTime = 0;
        double totalCost = 0;
        int iterator = 0;
        dataCenterIterator = dataCenterIterator - 1;

        for (int i = 0 + dataCenterIterator * 9 + cloudletIteration * 54;
             i < 9 + dataCenterIterator * 9 + cloudletIteration * 54; i++) {
            int gene = individual.getGene(iterator);
            double mips = calculateMips(gene % 9);

            totalExecutionTime += cloudletList.get(i).getCloudletLength() / mips;
            totalCost += calculateCost(vmList.get(gene % 9), cloudletList.get(i));
            iterator++;
        }

        // // System Fairness F (Equation 1)
        // double fitness = -totalExecutionTime; // Negative for minimization
        // double fitness = - (totalExecutionTime + totalCost);

        // Calculate makespan and cost fitness components
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

    public int[] getBestVmAllocation() {
        return globalBestPosition;
    }

    public double getBestFitness() {
        return globalBestFitness;
    }
}