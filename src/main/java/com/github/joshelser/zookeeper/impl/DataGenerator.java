package com.github.joshelser.zookeeper.impl;

public interface DataGenerator {

  void configure(String[] args);

  /**
   * Generates a ZNode's data
   */
  byte[] generateData();
}
