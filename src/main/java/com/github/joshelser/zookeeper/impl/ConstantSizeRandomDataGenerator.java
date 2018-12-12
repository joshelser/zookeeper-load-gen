package com.github.joshelser.zookeeper.impl;

import static java.util.Objects.requireNonNull;

import java.util.Random;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import com.github.joshelser.zookeeper.impl.ConstantSizeRandomDataGenerator.ConstantSizeRandomDataGeneratorOpts;

public class ConstantSizeRandomDataGenerator implements DataGenerator<ConstantSizeRandomDataGeneratorOpts> {

  public static class ConstantSizeRandomDataGeneratorOpts {
    @Parameter(names = {"-s", "--size"}, description = "The size in bytes of each data randomly generated",
        validateWith = PositiveInteger.class)
    public int sizeInBytes = 1024;
  }

  private ConstantSizeRandomDataGeneratorOpts opts = null;
  private Random random = null;

  @Override
  public ConstantSizeRandomDataGeneratorOpts configure(JCommander parser) {
    ConstantSizeRandomDataGeneratorOpts opts = new ConstantSizeRandomDataGeneratorOpts();
    parser.addObject(opts);
    return opts;
  }

  // Visible for testing, to be used instead of configure()
  public synchronized void initialize(ConstantSizeRandomDataGeneratorOpts opts) {
    if (this.opts != null || random != null) {
      throw new IllegalStateException("Configure was already called");
    }
    this.opts = requireNonNull(opts);
    this.random = new Random();
  }

  @Override
  public byte[] generateData() {
    if (opts == null) {
      throw new IllegalStateException("Configure must be called prior to generateData");
    }
    byte[] data = new byte[opts.sizeInBytes];
    // This has the potential to become a bottleneck with multiple threads
    random.nextBytes(data);
    return data;
  }

}
