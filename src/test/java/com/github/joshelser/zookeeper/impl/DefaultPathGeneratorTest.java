package com.github.joshelser.zookeeper.impl;

import java.util.ArrayList;

import org.junit.Test;

import com.github.joshelser.zookeeper.impl.DefaultPathGenerator.DefaultPathGeneratorOpts;

import static org.junit.Assert.assertEquals;

public class DefaultPathGeneratorTest {

  @Test public void testSimplePathGeneration() {
    DefaultPathGeneratorOpts opts = new DefaultPathGeneratorOpts();
    PathGenerator gen = new DefaultPathGenerator();
    gen.configure(new String[] {});

    ArrayList<String> expectedPaths = new ArrayList<>();
    ArrayList<String> actualPaths = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      expectedPaths.add(opts.rootZNode + "/0000/0000000" + i);
      actualPaths.add(gen.generatePath());
    }
    assertEquals(expectedPaths, actualPaths);
  }

  @Test public void testNonStandardRootZNode() {
    DefaultPathGeneratorOpts opts = new DefaultPathGeneratorOpts();
    opts.rootZNode = "/foo";
    DefaultPathGenerator gen = new DefaultPathGenerator();
    gen.configure(new String[] {});
    gen.setOpts(opts);

    assertEquals("/foo/0000/00000000", gen.generatePath());
  }

  @Test public void testNodeRollOver() {
    DefaultPathGeneratorOpts opts = new DefaultPathGeneratorOpts();
    // Maximum of 5 nodes at the "leaf" level
    opts.maxSecondLevelChildren = 5;
    DefaultPathGenerator gen = new DefaultPathGenerator();
    gen.configure(new String[] {});
    gen.setOpts(opts);

    // Generate the expected paths
    ArrayList<String> expectedPaths = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      expectedPaths.add(opts.rootZNode + "/0000/0000000" + i);
    }
    expectedPaths.add(opts.rootZNode + "/0001/00000000");

    // Get the actual paths generated
    ArrayList<String> actualPaths = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      actualPaths.add(gen.generatePath());
    }

    assertEquals(expectedPaths, actualPaths);
  }

  @Test public void testTotalMaxNodes() {
    // Create a config that only allows us to create 10 nodes at maximum
    DefaultPathGeneratorOpts opts = new DefaultPathGeneratorOpts();
    // Maximum of 5 nodes at the "leaf" level
    opts.maxSecondLevelChildren = 5;
    // Only allowed to have 2 root nodes
    opts.maxTopLevelChildren = 2;
    DefaultPathGenerator gen = new DefaultPathGenerator();
    gen.configure(new String[] {});
    gen.setOpts(opts);

    // Generate the expected paths
    ArrayList<String> expectedPaths = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      expectedPaths.add(opts.rootZNode + "/0000/0000000" + i);
    }
    for (int i = 0; i < 5; i++) {
      expectedPaths.add(opts.rootZNode + "/0001/0000000" + i);
    }

    ArrayList<String> actualPaths = new ArrayList<>();
    int observedNulls = 0;
    for (int i = 0; i < 12; i++) {
      String path = gen.generatePath();
      if (path != null) {
        actualPaths.add(path);
      } else {
        observedNulls++;
      }
    }

    assertEquals(expectedPaths, actualPaths);
    assertEquals(2, observedNulls);
  }
}
