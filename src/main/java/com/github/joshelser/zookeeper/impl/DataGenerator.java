package com.github.joshelser.zookeeper.impl;

import com.beust.jcommander.JCommander;

public interface DataGenerator<T> {

  /**
   * Configures this generator from CLI arguments
   */
  void configure(JCommander parser);

  /**
   * Configures this generator with the given options
   */
  void initialize(T opts);

  /**
   * Generates a ZNode's data
   */
  byte[] generateData();
}
