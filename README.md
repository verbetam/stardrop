# Stardrop

Stardrop is a modding tool for Stardew Valley mods. Specifically it is a tool for converting different types of custom
entities and custom textures mods between the different type. This was originally started because I wanted to convert
Custom Furniture [CF] to other mod types that I preferred, such as Alternative Textures [AT].

It also has capability to split and restitch sprite sheets across different styles, as well as re-adjusting the JSON
info for the different mod types to use the different sprite sheet approaches. Reformatting a given mod that uses
combined spritesheets (large sprite sheets with many different sprites) into individual sprite sheets is a process which
I have labeled as "exploding" the mod. The end result is the same in game, but the file structure is different and can
be easier to work with.

The current state of this project is as a personal project which I used locally, but I am taking steps to formalizing it
and providing a more user friendly release. At present, this will take some effort to get familiar with and use. I plan
and hopefully making some nice quality of life improvements and expanding this readme in the future.

## Modules

This project contains several modules that published and available for use within your own project.

### Core

Core module contains the core models and utilities of the project. This includes base models for Stardrop entities and
serialization tools.

```scala
libraryDependencies += "com.quincyjo" %% "stardrop-core" % version
```

### Alternative Textures

This module contains the models and serialization tools for the Alternative Textures (AT) mod type.
``

```scala
libraryDependencies += "com.quincyjo" %% "stardrop-alternative-textures" % version
```

### Custom Furniture

This module contains the models and serialization tools for the Custom Furniture (CF) mod type.

```scala
libraryDependencies += "com.quincyjo" %% "stardrop-custom-furniture" % version
```

### Converters

Finally, the `stardrop-converters` module contains tools for converting between different mod types.

```scala
libraryDependencies += "com.quincyjo" %% "stardrop-converters" % version
```

### Cli

The Cli module contains the command line interface for Stardrop and is not available as a dependency. Check
the [releases](https://github.com/quincyjo/stardrop/releases)
page for the latest version or [built it from source](#building-cli-from-source) yourself.

## [Building CLI From Source]

The SBT project comes with the [sbt-assembly](https://github.com/sbt/sbt-assembly) plugin which allows for the building
of a fat JAR file.

This can be built by running the following SBT command from within the project directory. The resulting JAR file will be
located at `./modules/cli/target/scala-2.13/stardrop.jar` assuming the CWD is the project root directory.

```
sbt cli/assembly
```

## Running the CLI JAR

See the available commands and their options.

```
java -jar ./stardrop.jar --help
```

### Common flow for converting a CF mod into an AT mod

1. Exploding the Mod

By default, this will output the exploded mod into an `[root director name] exploded` subdirectory of the target mod
root.

```
java -jar ./stardrop.jar explode \
"path/to/mod/root" \
--expand-furniture-front --copy-fourth-rotation
```

2. Convert the exploded MOD to AT (after spot checking sprites, such as the different layers)

```
java -jar ./stardrop.jar convert --to AT \
"path/to/mod/root/[root name] exploded" --read-furniture-front
```
