package com.github.joshelser.zookeeper.impl;

import com.beust.jcommander.JCommander;

public interface DataGenerator<T> {

  /**
   * Configures this generator from CLI arguments
   */
  T configure(JCommander parser);

  /**
   * Initializes this generator with the given configuration
   */
  void initialize(T opts);

  /**
   * Generates a ZNode's data
   */
  byte[] generateData();
}
