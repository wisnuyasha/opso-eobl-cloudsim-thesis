package org.cloudbus.cloudsim.examples.draft;

// import java.util.Arrays;
import java.util.Random;

public class Individual {
  private int[] chromosome;
  private double fitness = -1;

  public Individual(int[] chromosome) {
    this.chromosome = chromosome;
  }

  public Individual(int chromosomeLength, int dataCenterIterator) {
    this.chromosome = new int[chromosomeLength];
    dataCenterIterator = dataCenterIterator - 1;
    int max = 8 + 9 * dataCenterIterator;
    int min = 0 + 9 * dataCenterIterator;
    int range = max - min + 1;

    Random random = new Random();

    for (int gene = 0; gene < chromosomeLength; gene++) {
      int rand = random.nextInt(range) + min;
      setGene(gene, rand);
    }
    // for (int gene = 0; gene < chromosomeLength; gene++) { //opposite
    // int opposite = min + max - getGene(gene);
    // setGene(gene, opposite);
    // }
  }

  public int[] getChromosome() {
    return this.chromosome;
  }

  public int getChromosomeLength() {
    return this.chromosome.length;
  }

  public void setGene(int offset, int gene) {
    this.chromosome[offset] = gene;
  }

  public int getGene(int offset) {
    return this.chromosome[offset];
  }

  public void setFitness(double fitness) {
    this.fitness = fitness;
  }

  public double getFitness() {
    return this.fitness;
  }

  public String toString() {
    StringBuilder output = new StringBuilder();
    for (int gene = 0; gene < this.chromosome.length; gene++) {
      output.append(this.chromosome[gene]);
    }
    return output.toString();
  }
}