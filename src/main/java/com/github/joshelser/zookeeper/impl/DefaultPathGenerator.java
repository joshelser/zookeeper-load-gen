package com.github.joshelser.zookeeper.impl;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.github.joshelser.zookeeper.impl.DefaultPathGenerator.DefaultPathGeneratorOpts;

public class DefaultPathGenerator implements PathGenerator<DefaultPathGeneratorOpts> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultPathGenerator.class);

  public static class ZNodePathValidator implements IParameterValidator {
    @Override public void validate(String name, String value) throws ParameterException {
      if (!value.startsWith("/")) {
        throw new ParameterException("Parameter " + name + " does not begin with a slash.");
      }
      if (value.endsWith("/")) {
        throw new ParameterException("Parameter " + name + " should not end with a slash.");
      }
    }
  }

  public static class NonZeroPositiveInteger implements IParameterValidator {
    public void validate(String name, String value) throws ParameterException {
      Integer i = Integer.parseInt(value);
      if (i < 1) {
        throw new ParameterException("Parameter " + name + " should be positive and non-zero");
      }
    }
  }

  public static class DefaultPathGeneratorOpts {
    @Parameter(names = {"-sc", "--second-level-children"}, description = "The maximum number of children at a second level",
        validateWith = NonZeroPositiveInteger.class)
    int maxSecondLevelChildren = 10000;

    @Parameter(names = {"-r", "--root-znode"}, description = "The root znode under which all generated data should be placed",
        validateWith = ZNodePathValidator.class)
    String rootZNode = "/test";

    @Parameter(names = {"-tc", "--top-level-children"}, description = "The maximum number of children at the root level",
        validateWith = NonZeroPositiveInteger.class)
    int maxTopLevelChildren = 10;
  }

  private DefaultPathGeneratorOpts opts = null;
  private Map<String,AtomicLong> topLevelChildren = null;
  private String currentTopLevelChild = null;

  @Override
  public DefaultPathGeneratorOpts configure(JCommander parser) {
    DefaultPathGeneratorOpts opts = new DefaultPathGeneratorOpts();
    parser.addObject(opts);
    return opts;
  }

  public synchronized void initialize(DefaultPathGeneratorOpts opts) {
    if (this.opts != null || topLevelChildren != null || currentTopLevelChild != null) {
      throw new IllegalStateException("Configure was called on DefaultPathGenerator multiple times");
    }
    this.opts = requireNonNull(opts);
    this.topLevelChildren = new HashMap<>();
  }

  @Override
  public String generatePath(ZooKeeper zk) {
    String currentPath = findNextPath(zk);

    // Could not generate a new path
    if (currentPath == null) {
      return null;
    }

    return join(opts.rootZNode, currentPath);
  }

  public String getRootZNode() {
    return opts.rootZNode;
  }

  public String createAndStoreNewTopLevelChild(ZooKeeper zk) {
    int nextChild = topLevelChildren.size();
    if (nextChild >= opts.maxTopLevelChildren) {
      return null;
    }
    String child = String.format("%04d", nextChild);
    topLevelChildren.put(child, new AtomicLong(0L));
    String fullPath = join(opts.rootZNode, child);
    ensureNodeExists(zk, fullPath);
    return child;
  }

  void ensureNodeExists(ZooKeeper zk, String path) {
    try {
      if (zk.exists(path, false) == null) {
        zk.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    } catch (KeeperException e) {
      LOG.error("Failed to create first-level ZNode: {}", path, e);
      throw new RuntimeException(e);
    }
  }

  public String findNextPath(ZooKeeper zk) {
    if (currentTopLevelChild == null) {
      if (topLevelChildren.isEmpty()) {
        currentTopLevelChild = createAndStoreNewTopLevelChild(zk);
      } else {
        // If we have no currentTopLevelChild, that means a previous call here
        // returned null (indicating that no more children should be created).
        return null;
      }
    }
    AtomicLong numChildren = topLevelChildren.get(currentTopLevelChild);
    if (numChildren.longValue() >= opts.maxSecondLevelChildren) {
      // We maxed out the current child. Create the next and reset the number of children
      currentTopLevelChild = createAndStoreNewTopLevelChild(zk);
      if (currentTopLevelChild == null) {
        // We exceeded the number of top leve children, bail out.
        return null;
      }
      numChildren = new AtomicLong(0L);
      topLevelChildren.put(currentTopLevelChild, numChildren);
    }
    return join(currentTopLevelChild, String.format("%08d", numChildren.getAndIncrement()));
  }

  public String join(String a, String b) {
    StringBuilder sb = new StringBuilder(a);
    return sb.append("/").append(b).toString();
  }
}
