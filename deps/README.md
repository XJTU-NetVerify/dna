### Generate JARs

- Batfish

Get the source code of [Batfish](https://github.com/batfish/batfish/tree/v2019.11.20), use [shade plugin](https://maven.apache.org/plugins/maven-shade-plugin/) of maven to package module `batfish` and `batfish-common-protocol` with all dependencies.

```
// Add the following lines to `batfish/projects/batfish/pom.xml` and `batfish/projects/batfish-common-protocol/pom.xml` and then build
<build>
  // ...
  <plugins>
    // ...
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
<build>
```

- DDlog

The `ddloapi.jar` can be generated following [the doc of DDlog](https://github.com/vmware/differential-datalog/blob/master/doc/java_api.md#compile-ddlog-java-bindings).
