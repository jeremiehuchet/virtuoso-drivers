# Virtuoso maven repository

Maven repository distributing Openlink Virtuoso opensource Java libraries.

| Module          | Latest version |
| --------------- | -------------- |
| virtuoso-jdbc   | [42.3.112](https://github.com/jeremiehuchet/virtuoso-drivers/releases/virtuoso-jdbc-3.112) |

## Getting started

You can browse available packages and versions through the [Bintray web UI](https://bintray.com/jeremiehuchet/virtuoso-drivers) section or the [raw repository view](https://dl.bintray.com/jeremiehuchet/virtuoso-drivers)

### maven

Add the repository to your `pom.xml`:

```xml
<repository>
    <id>virtuoso</id>
    <name>Openlink Virtuoso drivers packages</name>
    <url>https://dl.bintray.com/jeremiehuchet/virtuoso-drivers</url>
</repository>
```

And the dependency:

```xml
<dependency>
    <groupId>com.openlink</groupId>
    <artifactId>virtuoso-jdbc</artifactId>
    <version>42.3.112</version>
</dependency>
```

### gradle

Add the repository to your `build.gradle`:

```groovy
repositories {
    maven {
        url = uri("https://dl.bintray.com/jeremiehuchet/virtuoso-drivers")
    }
}
```

And the dependency:

```groovy
dependencies {
    implementation "com.openlink:virtuoso-jdbc:42.3.112"
}
```

## JDBC version naming

I used a convention inspired by the [Postgresql JDBC driver](https://search.maven.org/artifact/org.postgresql/postgresql) : `API.MAJOR.MINOR[.JRE]`.

- `API` is the version of the API implemented without dots : JDBC 4.1 → 41, JDBC 4.2 → 42
- `MAJOR` is the driver implementation major version number
- `MINOR` is the driver implementation minor version number
- `JRE` is optional and specified for old Java runtimes

For instance, `41.3.112.jre7` is:

- a JDBC 4.1 driver
- version 3.112
- compatible with JRE 7+

And `42.3.112` is the same but for latest JRE (8+).

## About

I opened an issue some time ago to get virtuoso JDBC driver published to maven central ([virtuoso-opensource#249](https://github.com/openlink/virtuoso-opensource/issues/249)).

So... no, it's not yet into maven central 😢  
But it's easier to use in maven projects 🎉

## Hacking

### Generating JDBC driver sources files

Currently, the original driver source contains _C preprocessor_ instructions (see for instance [virtuoso.jdbc.Driver](https://github.com/openlink/virtuoso-opensource/blob/97d31f7c3818fffec849258f2c2e932949e7c6ba/libsrc/JDBCDriverType4/virtuoso/jdbc/Driver.java#L541)). It makes the build toolchain somewhat complex for java focused developers used to maven conventions.

I used (mostly for fun) nix package manager to set up the required tools with the right version. After installing nix, you should be able to run `nix-build virtuoso-java-sources.nix` to generate an output with the Virtuoso JDBC drivers rouces files.

Finally, I rearranged files to suit maven standard project layout, but **I didn't modify them**. This way I was able to publish libraries easily to a maven repository.

### Building

Take a look a [main.yml github action](https://github.com/jeremiehuchet/virtuoso-drivers/blob/main/.github/workflows/main.yml). You should _just_ have to run `nix-shell --run "mvn package"` to build the libraries.
