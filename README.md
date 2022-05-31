## Differential Network Analysis.

This is a prototype implementation of the following [paper](https://nskeylab.xjtu.edu.cn/people/pzhang/files/2021/09/dna_nsdi22.pdf):
> Peng Zhang, Aaron Gember-Jacobson, Yueshang Zuo, Yuhao Huang, Xu Liu, and Hao Li. "Differential Network Analysis", NSDI'22

## Structure

The implementation of DNA consists of the following four modules.
- config-parser: first leverage [Batfish](https://github.com/batfish/batfish/tree/v2019.11.20) to parse configurations into vendor-independent representations, then convert them into [DDlog](https://github.com/vmware/differential-datalog) (a dialog of Datalog) facts.

- dp-generator: read changes of DDlog facts and output changes of forwarding rules. This is achieved by modeling the control plane in DDlog and compile the DDlog program into differential Dataflow computation. The model (`projects/dp-generator/routing.dl`) supports BGP and OSPF.

- dp-verifier: read changes of forwarding rules and output changes of properties. This module includes two stages, i.e., differential dataplane modeling and differential property checking. The module uses a modified version of APKeep, which has been published in NSDI'20.

- dna: combine the above modules, provide an end-to-end workflow.

The main stages of DNA (except the parser of Batfish) are completely incremental that each of them consumes differences and produces differences.
You can also run each module independently to fully investigate each stage of DNA.

# Setup

Make sure you have Java and Rust toolchains first, the implementation has been tested in Linux environment.

1. Install dependent jars to local maven repository (The building instructions are in the folder `deps/README.md`).

```
$ cd deps; ./install-jars.sh
```

2. Generate the runtime dynamic library libddlogapi.so from the Datalog program `routing.dl`.

```
# Get a binary release DDlog (https://github.com/vmware/differential-datalog#installing-ddlog-from-a-binary-release, DNA currently uses v1.2.3).

# Compile the datalog program to get the libddlogapi.so. (https://github.com/vmware/differential-datalog/blob/master/doc/java_api.md#compiling-ddlog-programs-with-java-api-enabled)
$ cd projects/dp-generator
$ ddlog -i routing.dl -j
$ cd routing_ddlog; cargo build --features=flatbuf --release
$ cc -shared -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/${JDK_OS} -I. -I${DDLOG_HOME}/lib ${DDLOG_HOME}/java/ddlogapi.c -Ltarget/release/ -lrouting_ddlog -o libddlogapi.so
```

## Run

```
# Prepare the environment as `Setup` and then compile the project.
$ cd projects; mvn package

# Run the example network in CLI.
$ java -Djava.library.path=<PATH of libddlogapi.so> -jar dna/target/dna-0.1.0.jar
Differential Network Analysis
  Usage:
    init <snapshot>                    initialize with a network snapshot
    update [<snapshot>]                push changes on the init or provide an updated snapshot
    dump topo | fib | policy           dump topology, fibs or policies

DNA> init ../networks/example-network/base/
DNA> dump policy
+ a->e: [1.1.2.0/24, 1.1.1.0/24]
+ b->e: [1.2.0.0/16, 1.1.2.0/24, 1.1.1.0/24]
DNA> update ../networks/example-network/update # you can update with another snapshot or modify the original configs and push an update
DNA> dump policy
- a->e: [1.1.2.0/24, 1.1.1.0/24]
DNA>
```

## Evaluation

- We provide some of the experimental networks in the folder `networks/`.
- We provide the main evaluation scripts of our NSDI 2022 paper in the folder `projects/dna/src/main/java/org/ants/exp`.

## Contact

- Peng Zhang (p-zhang@xjtu.edu.cn)
- Aaron Gember-Jacobson (agemberjacobson@colgate.edu)
- Yueshang Zuo (yueshangzuo@outlook.com)

## License

MIT License, see [LICENSE](LICENSE).
