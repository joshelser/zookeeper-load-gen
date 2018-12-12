package com.github.joshelser.zookeeper;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

/**
 * Command line options for load generation against ZooKeeper.
 */
public class LoadGeneratorOpts {

  private static class OperationConverter implements IStringConverter<Operation> {

    public Operation convert(String value) {
      try {
        Class<?> clz = Class.forName(value);
        if (!Operation.class.isAssignableFrom(clz)) {
          throw new IllegalArgumentException(clz + " is not an instance of " + Operation.class);
        }
        return (Operation) clz.newInstance();
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Parameter(names = {"-n", "--num-ops"}, description = "The number of operations to execute")
  private long numOperations;

  @Parameter(names = {"-o", "--operation"}, description = "The operation to run", converter = OperationConverter.class)
  private Operation operation;

  @Parameter(names = {"-q", "--quorum"}, description = "The comma-separate list of ZooKeeper servers to generate load against")
  private String zooKeeperQuorum;

  @Parameter(names = {"-t", "--timeout"}, description = "The ZooKeeper connection timeout")
  private int connectionTimeout = 30000;

  public long getNumOperations() {
    return numOperations;
  }

  public void setNumOperations(long numOperations) {
    this.numOperations = numOperations;
  }

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  public String getZooKeeperQuorum() {
    return zooKeeperQuorum;
  }

  public void setZooKeeperQuorum(String zooKeeperQuorum) {
    this.zooKeeperQuorum = zooKeeperQuorum;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }
}
