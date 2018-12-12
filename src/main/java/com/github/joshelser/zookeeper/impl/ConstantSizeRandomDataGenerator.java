package com.github.joshelser.zookeeper.impl;

import java.util.Random;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class ConstantSizeRandomDataGenerator implements DataGenerator {

  public static class ConstantSizeRandomDataGeneratorOpts {
    @Parameter(names = {"-s", "--size"}, description = "The size in bytes of each data randomly generated",
        validateWith = PositiveInteger.class)
    public int sizeInBytes = 1024;
  }

  private int sizeInBytes = -1;
  private Random random = null;

  @Override
  public synchronized void configure(String[] args) {
    if (sizeInBytes != -1 || random != null) {
      throw new IllegalStateException("Configure was already called");
    }
    ConstantSizeRandomDataGeneratorOpts opts = new ConstantSizeRandomDataGeneratorOpts();
    JCommander jcommander = new JCommander();
    jcommander.addObject(opts);
    jcommander.parse(args);

    this.sizeInBytes = opts.sizeInBytes;
    this.random = new Random();
  }

  @Override
  public byte[] generateData() {
    if (sizeInBytes < 1) {
      throw new IllegalStateException("Configure must be called prior to generateData");
    }
    byte[] data = new byte[sizeInBytes];
    // This has the potential to become a bottleneck with multiple threads
    random.nextBytes(data);
    return data;
  }

}
