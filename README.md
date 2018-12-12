&copy; Josh Elser 2018

## Apache ZooKeeper Load Generation Tool

This is a framework which intends to provide an outline to exerting a load (e.g. creating znodes) onto
ZooKeeper given some parameters.

The tool is written to run an "Operation" against a ZooKeeper quorum. Today, the only operation is one that
creates ZNodes.

`com.github.joshelser.zookeeper.LoadGeneration` instantiates and invokes an `Operation` a number of times against
a ZooKeeper quorum.

### Create Operation

`com.github.joshelser.zookeeper.impl.CreateOperation` is a class which generates random data of a certain size
and a unique path to create beneath a parent node. Both data generation and path generation are pluggable.

The path generation generates a "tree" of depth two. That is, from a parent znode of `/test/`, you will observe
the following:

```
/test/
/test/0000
/test/0000/00000000
/test/0000/00000001
/test/0000/00000002
/test/0000/00000003
...
/test/0000/00009999
/test/0001/00000000
...
```

The "leaf" znodes are create sequentially (e.g. 0, 1, 2, etc) as persistent ZNodes with the `OPEN_ACL_UNSAFE` ACL (global
access). The number of "leaf" nodes to create is governed by the property `--second-level-children` (`-sc`) and defaults
to `10000` by default. The number of first-level children (e.g. "0000", "0001") is governed by the property `--top-level-children`
(`-tc`) and defaults to 10.

The LoadGeneration tool accepts a maximum number of operations to execute via the option `--num-ops` (`-n`). If this
operation exceeds the number of allowed nodes (per `-sc` and `tc`), then execution will stop early.

## Example

First, ensure the code is compiled.

```
$ mvn package
```

We can run a simple load generation of 1000 znodes to create via the command:

```
$ mvn exec:java \
    -Dexec.mainClass=com.github.joshelser.zookeeper.LoadGenerator \
    -Dexec.args="-n 1000 -o com.github.joshelser.zookeeper.impl.CreateOperation -q server1.domain.com,server1.domain.com,server3.domain.com:2181"
```

We can also run a large load generation of 100000 znodes in a custom, root ZNode by:

```
$ mvn exec:java \
    -Dexec.mainClass=com.github.joshelser.zookeeper.LoadGenerator \
    -Dexec.args="-n 100000 -o com.github.joshelser.zookeeper.impl.CreateOperation -q server1.domain.com,server1.domain.com,server3.domain.com:2181 -r /custom"
```
