package com.github.joshelser.zookeeper.impl;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.github.joshelser.zookeeper.Operation;
import com.github.joshelser.zookeeper.impl.ConstantSizeRandomDataGenerator.ConstantSizeRandomDataGeneratorOpts;
import com.github.joshelser.zookeeper.impl.DefaultPathGenerator.DefaultPathGeneratorOpts;

public class CreateOperation implements Operation {
  private static final Logger LOG = LoggerFactory.getLogger(CreateOperation.class);

  private ConstantSizeRandomDataGenerator dataGenerator = null;
  private DefaultPathGenerator pathGenerator = null;

  @Override
  public synchronized void initialize(String[] args) {
    if (dataGenerator != null || pathGenerator != null) {
      throw new IllegalStateException("Configure was called on CreateOperationOpts multiple times");
    }

    // Setup a parser for the data generator and path generator
    // TODO figure out a way to do this parsing better. Can't get the DataGenerator we want
    // to use until after we parsed the args. Maybe CreateOperation is just hard-coded to specific
    // data generators and path generator.
    JCommander parser = new JCommander();
    parser.setAcceptUnknownOptions(true);

    // Get and configure the DataGenerator
    this.dataGenerator = new ConstantSizeRandomDataGenerator();
    ConstantSizeRandomDataGeneratorOpts dataGenOpts = this.dataGenerator.configure(parser);
    dataGenerator.initialize(dataGenOpts);

    // Get and configure the PathGenerator
    this.pathGenerator = new DefaultPathGenerator();
    DefaultPathGeneratorOpts pathGenOpts = this.pathGenerator.configure(parser);
    pathGenerator.initialize(pathGenOpts);

    parser.parse(args);
  }

  @Override
  public void call(ZooKeeper zk) throws InterruptedException {
    final String rootZNode = pathGenerator.getRootZNode();
    try {
      if (zk.exists(rootZNode, false) == null) {
        zk.create(rootZNode, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      } // else we have a node there, hope for the best and move forward.
    } catch (KeeperException e) {
      LOG.error("Could not ensure rootZNode existed", e);
    }
    String path = pathGenerator.generatePath(zk);
    byte[] data = dataGenerator.generateData();
    for (int i = 0; i < 5; i++) {
      try {
        LOG.trace("Creating {} with {} (attempt #{})", path, data, i+1);
        zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        return;
      } catch (KeeperException.NodeExistsException e) {
        throw new IllegalStateException("The path to create already exists: " + path);
      } catch (KeeperException e) {
        // continue
        LOG.debug("Retrying on non-fatal ZK exception", e);
      }
    }
    throw new RuntimeException("Failed to create node at " + path + " after 5 attempts");
  }
}
