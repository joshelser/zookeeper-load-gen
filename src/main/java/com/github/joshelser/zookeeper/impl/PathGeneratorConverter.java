package com.github.joshelser.zookeeper.impl;

import com.beust.jcommander.IStringConverter;

public class PathGeneratorConverter implements IStringConverter<PathGenerator> {

  @Override
  public PathGenerator convert(String value) {
    try {
      Class<?> clz = Class.forName(value);
      if (!PathGenerator.class.isAssignableFrom(clz)) {
        throw new IllegalArgumentException(clz + " is not an instance of " + PathGenerator.class);
      }
      return (PathGenerator) clz.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

}
