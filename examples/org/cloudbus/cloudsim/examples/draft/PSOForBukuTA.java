package org.cloudbus.cloudsim.examples;

import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class PSO {
    // Parameters
    private int Imax; // Maximum number of iterations
    private int populationSize; // Population size P
    private double w; // Inertia weight static 
    private double l1; // Komponen kognitif
    private double l2; // Komponen sosial

    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    private double globalBestFitness = Double.NEGATIVE_INFINITY;
    private int[] globalBestPosition;

    public PSO(int Imax, int populationSize, double w, double l1, double l2,
                         List<Cloudlet> cloudletList, List<Vm> vmList, int chromosomeLength) {
        this.Imax = Imax;
        this.populationSize = populationSize;
        this.w = w;
        this.l1 = l1;
        this.l2 = l2;
        this.cloudletList = cloudletList;
        this.vmList = vmList;
        this.globalBestPosition = new int[chromosomeLength];
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
                // System.out.println("Chromosome: " + individual.toString());
                globalBestPosition = individual.getChromosome().clone();
            }
        }
    }

    // Step 7: Update velocities and positions
    public void updateVelocitiesAndPositions(Population population, int iteration, int dataCenterIterator) {
        Random random = new Random();

        for (Individual particle : population.getIndividuals()) {
            for (int i = 0; i < particle.getChromosomeLength(); i++) {
                double vPrev = particle.getVelocity()[i];
                double pBest = particle.getPersonalBestPosition()[i];
                double gBest = globalBestPosition[i];
                int currentPosition = particle.getGene(i);

                double r1 = random.nextDouble();
                double r2 = random.nextDouble();

                double newVelocity = w * vPrev
                        + l1 * r1 * (pBest - currentPosition)
                        + l2 * r2 * (gBest - currentPosition);

                double vmSize = ((double) vmList.size() / 6.0 ) - 1.0;
                double Vmax = vmSize * 0.5;
                double velocityMin = -Vmax;
                double velocityMax = Vmax;

                if (newVelocity < velocityMin) {
                    newVelocity = velocityMin;
                } else if (newVelocity > velocityMax) {
                    newVelocity = velocityMax;
                }

                int newPosition = currentPosition + (int) Math.round(newVelocity);

                int minPosition = (dataCenterIterator - 1) * 9;
                int maxPosition = ((dataCenterIterator) * 9) - 1;

                if (newPosition < minPosition) {
                    newPosition = minPosition;
                } else if (newPosition > maxPosition) {
                    newPosition = maxPosition;
                }

                particle.setVelocity(i, newVelocity);
                particle.setGene(i, newPosition);
            }
        }
    }

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