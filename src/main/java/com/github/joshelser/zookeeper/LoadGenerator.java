package com.github.joshelser.zookeeper;

import static java.util.Objects.requireNonNull;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

public class LoadGenerator implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(LoadGenerator.class);

  private static class NoOpWatcher implements Watcher {
    @Override public void process(WatchedEvent event) {}
  }

  private final LoadGeneratorOpts opts;
  private final String[] args;

  public LoadGenerator(LoadGeneratorOpts opts, String[] args) {
    this.opts = requireNonNull(opts);
    this.args = requireNonNull(args);
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
    final Operation op = opts.getOperation();
    final long numOps = opts.getNumOperations();

    // Make sure the Operation's state is initialized too
    op.initialize(args);

    final ZooKeeper zk = new ZooKeeper(opts.getZooKeeperQuorum(), opts.getConnectionTimeout(), new NoOpWatcher());
    try {
      for (long i = 0; i < numOps; i++) {
        if (i % 1000 == 0) {
          LOG.info("Executing operation {}", i);
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
    // LoadGenerator doesn't know about the options for the Operation (and its members)
    // so we might see options we don't know how to handle.
    jcommander.setAcceptUnknownOptions(true);
    jcommander.addObject(opts);
    // Need to do a parse to fill out the LoadGeneratorOpts object.
    jcommander.parse(args);
    LoadGenerator generator = new LoadGenerator(opts, args);
    generator.run();
  }

}
