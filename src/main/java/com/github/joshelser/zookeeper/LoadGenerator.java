package com.github.joshelser.zookeeper;

import static java.util.Objects.requireNonNull;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

public class LoadGenerator implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(LoadGenerator.class);

  private final LoadGeneratorOpts opts;
  private final JCommander parser;

  public LoadGenerator(LoadGeneratorOpts opts, JCommander parser) {
    this.opts = requireNonNull(opts);
    this.parser = requireNonNull(parser);
  }

  public void run() {
    try {
      runWithExceptions();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException("Caught exception during execution", e);
    }
  }

  void runWithExceptions() throws Exception {
    final ZooKeeper zk = new ZooKeeper(opts.getZooKeeperQuorum(), opts.getConnectionTimeout(), null);
    try {
      final Operation op = opts.getOperation();
      final long numOps = opts.getNumOperations();

      // Pass configuration down into the Operation
      op.configure(parser);

      for (long i = 0; i < numOps; i++) {
        if (i % 1000 == 0) {
          LOG.debug("Executing operation {}", i);
        }
        try {
          op.call(zk);
        } catch (InterruptedException e) {
          LOG.warn("Operation {} was interrupted, exiting", i + 1);
          Thread.currentThread().interrupt();
          break;
        }
      }
    } finally {
      zk.close();
    }
  }

  public static void main(String[] args) {
    final LoadGeneratorOpts opts = new LoadGeneratorOpts();
    JCommander jcommander = new JCommander();
    jcommander.addObject(opts);
    LoadGenerator generator = new LoadGenerator(opts, jcommander);
    generator.run();
  }

}
