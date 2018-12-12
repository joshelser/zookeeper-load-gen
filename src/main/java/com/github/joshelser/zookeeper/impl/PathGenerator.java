package com.github.joshelser.zookeeper.impl;

import org.apache.zookeeper.ZooKeeper;

import com.beust.jcommander.JCommander;

public interface PathGenerator<T> {

  /**
   * Configures this generator from CLI arguments.
   */
  T configure(JCommander parser);

  /**
   * Initializes this generator with the given options
   */
  void initialize(T opts);

  /**
   * Generates the name of a znode to provide to an operation.
   */
  String generatePath(ZooKeeper zk);
}
