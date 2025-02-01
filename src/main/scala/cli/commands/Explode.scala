package quincyjo.stardew
package cli.commands

import cli.implicits._
import converters.AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import converters.CustomFurnitureSpriteExtractor.SpriteExtractorOptions
import converters.{
  AlternativeTexturesSpriteConverter,
  CustomFurnitureModExploder,
  CustomFurnitureSpriteExtractor
}
import customfurniture.{CustomFurnitureModReader, CustomFurnitureModWriter}

import cats.implicits.catsSyntaxTuple4Semigroupal
import com.monovore.decline._

import scala.reflect.io.{Directory, Path}

final case class Explode(
  target: Directory,
  outputTo: Path,
  spriteExtractionOptions: SpriteExtractorOptions,
  spriteConverterOptions: AlternativeTexturesSpriteConverterOptions
) {

  def execute(): Unit = {
    val mod = CustomFurnitureModReader.read(target).toTry.get
    val converter = AlternativeTexturesSpriteConverter(spriteConverterOptions)
    val exploder = CustomFurnitureModExploder(
      CustomFurnitureSpriteExtractor(spriteExtractionOptions),
      Some((cf, sprite) => converter.convert(cf, sprite))
    )
    val explodedMod = exploder.explode(mod)
    CustomFurnitureModWriter(explodedMod).writeTo(outputTo)
  }
}

object Explode {

  private val targetMod = Opts
    .argument[Directory](metavar = "mod")
  private val outputDir = Opts
    .option[Path](
      long = "output-directory",
      short = "o",
      help = "set the output location."
    )
    .orNone

  final val opts: Opts[Explode] =
    (
      targetMod,
      outputDir,
      optsForCFSpriteExtractorOptions,
      optsForATSpriteConverterOptions
    ).mapN {
      (targetMod,
       outputDir,
       spriteExtractionOptions,
       spriteConversionOptions) =>
        Explode(
          targetMod,
          outputDir.getOrElse(targetMod.resolve("exploded")),
          spriteExtractionOptions,
          spriteConversionOptions
        )
    }

  final val command: Command[Unit] = Command(
    name = "explode",
    header =
      "Rewrite an Alternative Textures mod with separate tilesheets for each sprite."
  )(opts.map(_.execute()))
}
