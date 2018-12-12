package com.github.joshelser.zookeeper.impl;

public interface PathGenerator {

  void configure(String[] args);

  /**
   * Generates the name of a znode to provide to an operation.
   */
  String generatePath();
}
