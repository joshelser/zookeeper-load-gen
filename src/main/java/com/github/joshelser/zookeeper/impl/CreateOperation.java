package com.github.joshelser.zookeeper.impl;

import java.util.Collections;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.joshelser.zookeeper.Operation;

public class CreateOperation implements Operation {
  private static final Logger LOG = LoggerFactory.getLogger(CreateOperation.class);

  private static class CreateOperationOpts {
    @Parameter(names = {"-dg", "--data-generator"}, description = "Name of the class to instantiate for data generation", converter = DataGeneratorConverter.class)
    DataGenerator dataGenerator;

    @Parameter(names = {"-pg", "--path-generator"}, description = "Name of the class to instantiate for path generation", converter = PathGeneratorConverter.class)
    PathGenerator pathGenerator;
  }

  private CreateOperationOpts opts = null;
  private DataGenerator dataGenerator = null;
  private PathGenerator pathGenerator = null;

  @Override
  public synchronized void configure(String[] args) {
    if (opts != null || dataGenerator != null || pathGenerator != null) {
      throw new IllegalStateException("Configure was called on CreateOperationOpts multiple times");
    }
    this.opts = new CreateOperationOpts();
    JCommander jcommander = new JCommander();
    jcommander.addObject(opts);
    jcommander.parse(args);

    // Get and configure the DataGenerator
    this.dataGenerator = opts.dataGenerator;
    this.dataGenerator.configure(args);

    // Get and configure the PathGenerator
    this.pathGenerator = opts.pathGenerator;
    this.pathGenerator.configure(args);
  }

  @Override
  public void call(ZooKeeper zk) throws InterruptedException {
    String path = pathGenerator.generatePath();
    byte[] data = dataGenerator.generateData();
    for (int i = 0; i < 5; i++) {
      try {
        LOG.trace("Creating {} with {} (attempt #{})", path, data, i+1);
        zk.create(path, data, Collections.emptyList(), CreateMode.PERSISTENT);
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
