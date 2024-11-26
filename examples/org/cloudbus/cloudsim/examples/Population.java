package org.cloudbus.cloudsim.examples;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Population {
    public Individual population[];

    public Population(int populationSize) {
        this.population = new Individual[populationSize];
    }

    public Population(int populationSize, int chromosomeLength, int dataCenterIterator) {
        this.population = new Individual[populationSize];

        for (int individualCount = 0; individualCount < populationSize; individualCount++) {
            Individual individual = new Individual(chromosomeLength, dataCenterIterator);
            this.population[individualCount] = individual;
        }
    }

    public Individual[] getIndividuals() {
        return this.population;
    }

    public int size() {
        return this.population.length;
    }

    public Individual setIndividual(int offset, Individual individual) {
        return population[offset] = individual;
    }

    public Individual getIndividual(int offset) {
        return population[offset];
    }

    public void shuffle() {
        Random rnd = new Random();
        for (int i = population.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            Individual a = population[index];
            population[index] = population[i];
            population[i] = a;
        }
    }
}
