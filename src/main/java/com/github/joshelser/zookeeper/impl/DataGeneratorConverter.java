package com.github.joshelser.zookeeper.impl;

import com.beust.jcommander.IStringConverter;

/**
 * JCommander converter which converts strings into instances of DataGenerator classes.
 */
public class DataGeneratorConverter implements IStringConverter<DataGenerator<?>> {
  @Override
  public DataGenerator<?> convert(String value) {
    try {
      Class<?> clz = Class.forName(value);
      if (!DataGenerator.class.isAssignableFrom(clz)) {
        throw new IllegalArgumentException(clz + " is not an instance of " + DataGenerator.class);
      }
      return (DataGenerator<?>) clz.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }
}
