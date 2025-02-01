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

## Running (after local compilation)

See the available commands and their options.

```
java -jar ./stardrop-assembly-pre-release.jar --help
```

### Common flow for converting a CF mod into an AT mod

1. Exploding the Mod

By default, this will output the exploded mod into an `[root director name] exploded` subdirectory of the target mod
root.

```
java -jar ./stardrop-assembly-pre-release.jar explode \
"path/to/mod/root" \
--expand-furniture-front --copy-fourth-rotation
```

2. Convert the exploded MOD to AT (after spot checking sprites, such as the different layers)

```
java -jar ./stardrop-assembly-pre-release.jar convert --to AT \
"path/to/mod/root/[root name] exploded" --read-furniture-front
```
