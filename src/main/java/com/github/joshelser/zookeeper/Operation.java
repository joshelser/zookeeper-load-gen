package com.github.joshelser.zookeeper;

import org.apache.zookeeper.ZooKeeper;

/**
 * An state-modification action to take against ZooKeeper, e.g. create a znode.
 */
public interface Operation {

  /**
   * Initialize this operation with the given configuration.
   */
  void initialize(String[] args);

  /**
   * Invokes the business-logic of this operation against the provided ZooKeeper.
   */
  void call(ZooKeeper zk) throws InterruptedException;
}
