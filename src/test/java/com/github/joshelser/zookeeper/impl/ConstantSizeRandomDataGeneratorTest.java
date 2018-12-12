package com.github.joshelser.zookeeper.impl;

import org.junit.Test;

import com.github.joshelser.zookeeper.impl.ConstantSizeRandomDataGenerator.ConstantSizeRandomDataGeneratorOpts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

public class ConstantSizeRandomDataGeneratorTest {

  @Test
  public void testDataGeneration() {
    ConstantSizeRandomDataGeneratorOpts opts = new ConstantSizeRandomDataGeneratorOpts();
    ConstantSizeRandomDataGenerator gen = new ConstantSizeRandomDataGenerator();

    gen.initialize(opts);
    byte[] data = gen.generateData();
    assertEquals(opts.sizeInBytes, data.length);

    // Random data should be different each time.
    assertFalse(Arrays.equals(gen.generateData(), data));
  }

  @Test
  public void testDataGenerationLength() {
    ConstantSizeRandomDataGeneratorOpts opts = new ConstantSizeRandomDataGeneratorOpts();
    opts.sizeInBytes = 10;
    ConstantSizeRandomDataGenerator gen = new ConstantSizeRandomDataGenerator();
    gen.initialize(opts);
    byte[] data = gen.generateData();
    assertEquals(10, data.length);
  }
}
