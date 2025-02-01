package quincyjo.stardew
package cli.implicits

import converters.AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import converters.CustomFurnitureSpriteExtractor.SpriteExtractorOptions

import cats.implicits.catsSyntaxTuple3Semigroupal
import com.monovore.decline._

trait StardropImplicits {

  implicit val optsForATSpriteConverterOptions
    : Opts[AlternativeTexturesSpriteConverterOptions] =
    (
      Opts
        .flag(
          long = "expand-furniture-front",
          help = "Expand CF sprites to have a furniture front."
        )
        .orFalse,
      Opts
        .flag(
          long = "copy-fourth-rotation",
          help =
            "If expanding sprites with the furniture front layer, copy the fourth rotation sprite into the furniture front rotation."
        )
        .orFalse,
      Opts
        .flag(
          long = "expand-rotations",
          help =
            "When converting a 1 rotation CF sprite to a rotating sprite, create rotated sprites by copying the original."
        )
        .orFalse
    ).mapN {
      case (expandFurnitureFront, copyFourthRotationToFront, expandRotations) =>
        AlternativeTexturesSpriteConverterOptions(
          expandFurnitureFront,
          copyFourthRotationToFront,
          expandRotations
        )
    }

  implicit val optsForCFSpriteExtractorOptions: Opts[SpriteExtractorOptions] =
    Opts
      .flag(
        long = "read-furniture-front",
        help =
          "Read furniture front from CF tilesheets. Note that CF does not support furniture front sprites, so this is rarely desirable."
      )
      .orFalse
      .map { readFurnitureFront =>
        SpriteExtractorOptions(readFurnitureFront)
      }
}
