---
---

# Use r:trån as Library (experimental)

r:trån is a collection of software components for spatio-semantic road space models, as described in the [architecture](https://rtron.io/architecture/).
To use its functionality in another Kotlin or Java project, add the dependency to the respective component using Gradle:

```text
dependencies {
  implementation("io.rtron:rtron-main:1.2.0")
  implementation("io.rtron:rtron-readerwriter:1.2.0")
}
```

To add a dependency using Maven:
```text
<dependency>
  <groupId>io.rtron</groupId>
  <artifactId>rtron-main</artifactId>
  <version>1.2.0</version>
</dependency>
```
