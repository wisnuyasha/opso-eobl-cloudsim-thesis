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
    private double l1; // Initial Learning factor l1
    private double l2; // Initial Learning factor l2
    private double d;

    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    private int numberOfDataCenters = 6;
    private double[] globalBestFitnesses;
    private int[][] globalBestPositions;

    public OPSO(int Imax, int populationSize, double wMax, double wMin, double l1, double l2, double d,
                         List<Cloudlet> cloudletList, List<Vm> vmList, int chromosomeLength) {
        this.Imax = Imax;
        this.populationSize = populationSize;
        this.wMax = wMax;
        this.wMin = wMin;
        this.l1 = l1;
        this.l2 = l2;
        this.d = d;
        this.cloudletList = cloudletList;
        this.vmList = vmList;

        globalBestFitnesses = new double[numberOfDataCenters];
        globalBestPositions = new int[numberOfDataCenters][];

        for (int i = 0; i < numberOfDataCenters; i++) {
            globalBestFitnesses[i] = Double.NEGATIVE_INFINITY;
            globalBestPositions[i] = null;
        }
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
            int dcIndex = dataCenterIterator - 1;
            if (fitness > globalBestFitnesses[dcIndex]) {
                globalBestFitnesses[dcIndex] = fitness;
                globalBestPositions[dcIndex] = individual.getChromosome().clone();
            }
        }
        
        applyEOBL(population, dataCenterIterator, cloudletIteration);
    }

    public void applyEOBL(Population population, int dataCenterIterator, int cloudletIteration) {
        int dcIndex = dataCenterIterator - 1;

        int maxGene = ((dataCenterIterator) * 9) - 1;
        int minGene = (dataCenterIterator - 1) * 9;
        
        Random random = new Random();

        int[] currentGlobalBestPosition = globalBestPositions[dcIndex];
        double currentGlobalBestFitness = globalBestFitnesses[dcIndex];

        int[] oppositeSolution = new int[currentGlobalBestPosition.length];

        int range = maxGene - minGene + 1;

        for (int i = 0; i < currentGlobalBestPosition.length; i++) {
            int candidate = (int) ((minGene + maxGene) * d) - currentGlobalBestPosition[i];

            if (candidate < minGene || candidate > maxGene) {
                candidate = minGene + random.nextInt(range);
            }

            oppositeSolution[i] = candidate;
//             System.out.println("-----EOBL DEBUG: " + minGene + maxGene + " Before :" + currentGlobalBestPosition[i] + "After :" + oppositeSolution[i]);
        }
        
        Individual oppositeIndividual = new Individual(oppositeSolution);

        double oppositeFitness = calcFitness(oppositeIndividual, dataCenterIterator, cloudletIteration);
        oppositeIndividual.setFitness(oppositeFitness);


        if (oppositeFitness > currentGlobalBestFitness) {
            System.out.println("-----EOBL SUCCESS: " + oppositeFitness + " ----- Before :" + currentGlobalBestFitness);

            globalBestFitnesses[dcIndex] = oppositeFitness;
            globalBestPositions[dcIndex] = oppositeIndividual.getChromosome().clone();
        }
    }

    // Step 7: Update velocities and positions
    public void updateVelocitiesAndPositions(Population population, int iteration, int dataCenterIterator) {
        double w = wMax - (wMax - wMin) * Math.pow((double) iteration / Imax, 2);
        // System.out.println("----------- Iteration: " + iteration + ", Inertia Weight (w): " + w);
        
        Random random = new Random();
        int dcIndex = dataCenterIterator - 1;
        
        int[] currentGlobalBestPosition = globalBestPositions[dcIndex];

        for (Individual particle : population.getIndividuals()) {
            for (int i = 0; i < particle.getChromosomeLength(); i++) {
                int vPrev = particle.getVelocity()[i];
                int pBest = particle.getPersonalBestPosition()[i];
                int gBest = currentGlobalBestPosition[i];
                int currentPosition = particle.getGene(i);

                double r1 = random.nextDouble(); // r1 antara 0.0 hingga 1.0
                double r2 = random.nextDouble(); // r2 antara 0.0 hingga 1.0

                // Update velocity
                int newVelocity = (int) Math.round(
                    w * vPrev
                    + l1 * r1 * (pBest - currentPosition)
                    + l2 * r2 * (gBest - currentPosition)
                );
//                System.out.println("-----Iteration: " + i + "-----");
//                System.out.println("Previous Velocity (vPrev): " + vPrev);
//                System.out.println("-New Velocity (raw): " + newVelocity);

                // Apply position bounds
                int vmSize = (int) ((double) vmList.size() / 6.0 - 1.0);
                int Vmax = (int) (vmSize * 0.5);
                int velocityMin = -Vmax;
                int velocityMax = Vmax;

                // Batasi velocity
                if (newVelocity < velocityMin) {
                    newVelocity = velocityMin;
                } else if (newVelocity > velocityMax) {
                    newVelocity = velocityMax;
                }

                // Update position
                int newPosition = currentPosition + newVelocity;
//                System.out.println("Current Position: " + currentPosition);
//                System.out.println("-New Position (raw): " + newPosition);

                // Apply position bounds
                int minPosition = (dataCenterIterator - 1) * 9;
                int maxPosition = ((dataCenterIterator) * 9) - 1;
                // int minPosition = 0;
                // int maxPosition = vmList.size() - 1;

                // If the position is invalid
                if (newPosition < minPosition) {
                    newPosition = minPosition;
                } else if (newPosition > maxPosition) {
                    newPosition = maxPosition;
                }

                // Update particle
                particle.setVelocity(i, newVelocity);
                particle.setGene(i, newPosition);

//                System.out.println("pBest: " + pBest + ", gBest: " + gBest);
//                System.out.println("r1: " + r1 + ", r2: " + r2);
//                System.out.println("--New Velocity (bounded): " + newVelocity);
//                System.out.println("--New Position (bounded): " + newPosition);
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

        double makespanFitness = calculateMakespanFitness(totalExecutionTime);
        double costFitness = calculateCostFitness(totalCost);
        // System.out.println("--Total Execution Time (Makespan Fitness) (after): " + makespanFitness);
        // System.out.println("--Total Cost (Cost Fitness) (after): " + costFitness);
        
        double fitness = makespanFitness + costFitness;

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
      return 1.0 / totalExecutionTime;
    }

    private double calculateCostFitness(double totalCost) {
      // The lower the cost, the higher the fitness
      return 1.0 / totalCost;
    }

    public int[] getBestVmAllocationForDatacenter(int dataCenterIterator) {
        return globalBestPositions[dataCenterIterator - 1];
    }

    public double getBestFitnessForDatacenter(int dataCenterIterator) {
        return globalBestFitnesses[dataCenterIterator - 1];
    }
}