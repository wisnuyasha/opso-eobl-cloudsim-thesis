package org.cloudbus.cloudsim.examples;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class PopulationPSO {
  public IndividualPSO population[];

  public PopulationPSO(int populationSize) {
      this.population = new IndividualPSO[populationSize];
  }

  public PopulationPSO(int populationSize, int chromosomeLength, int dataCenterIterator) {
      this.population = new IndividualPSO[populationSize];

      for (int individualCount = 0; individualCount < populationSize; individualCount++) {
          IndividualPSO individual = new IndividualPSO(chromosomeLength, dataCenterIterator);
          this.population[individualCount] = individual;
      }
  }

  public IndividualPSO[] getIndividuals() {
      return this.population;
  }

  public IndividualPSO getFittest(int offset) {
    Arrays.sort(this.population, new Comparator<IndividualPSO>() {
      @Override
      public int compare(IndividualPSO o1, IndividualPSO o2) {
        if (o1.getFitness() < o2.getFitness()) {
          return 1;
        } else if (o1.getFitness() > o2.getFitness()) {
          return -1;
        }
        return 0;
      }
    });
    return this.population[offset];
  }

  public int getIndexOfLeastFit() {
      Arrays.sort(this.population, new Comparator<IndividualPSO>() {
        @Override
        public int compare(IndividualPSO o1, IndividualPSO o2) {
          if (o1.getFitness() < o2.getFitness()) {
            return -1;
          } else if (o1.getFitness() > o2.getFitness()) {
            return 1;
          }
          return 0;
        }
      });

      return 0;
  }

  public int size() {
      return this.population.length;
  }

  public IndividualPSO setIndividual(int offset, IndividualPSO individual) {
      return population[offset] = individual;
  }

  public IndividualPSO getIndividual(int offset) {
      return population[offset];
  }

  public void shuffle() {
      Random rnd = new Random();
      for (int i = population.length - 1; i > 0; i--) {
          int index = rnd.nextInt(i + 1);
          IndividualPSO a = population[index];
          population[index] = population[i];
          population[i] = a;
      }
  }
}
