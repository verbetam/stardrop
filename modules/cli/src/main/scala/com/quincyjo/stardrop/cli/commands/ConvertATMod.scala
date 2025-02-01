/*
 * Copyright 2023 Quincy Jo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quincyjo.stardrop.cli.commands

import cats.implicits.*
import com.quincyjo.stardrop.cli.implicits.*
import com.monovore.decline.*
import com.quincyjo.stardrop.alternativetextures.AlternativeTexturesModWriter
import com.quincyjo.stardrop.cli.models.ModType
import com.quincyjo.stardrop.content.models.FurnitureData
import com.quincyjo.stardrop.customfurniture.CustomFurnitureModReader
import org.slf4j.{Logger, LoggerFactory}
import com.quincyjo.stardrop.converters.AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import com.quincyjo.stardrop.converters.{
  AlternativeTexturesConverter,
  AlternativeTexturesSpriteConverter,
  CustomFurnitureMatcher,
  CustomFurnitureSpriteExtractor
}
import com.quincyjo.stardrop.converters.CustomFurnitureSpriteExtractor.SpriteExtractorOptions
import com.quincyjo.stardrop.encoding.JsonReader

import scala.reflect.io.{Directory, Path}
import scala.util.chaining.scalaUtilChainingOps

final case class ConvertATMod(
    target: Directory,
    unpackedContent: Directory,
    outputTo: Path,
    spriteConversionOptions: AlternativeTexturesSpriteConverterOptions,
    spriteExtractionOptions: SpriteExtractorOptions,
    analyzeOnly: Boolean
) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[ConvertATMod])

  def execute(): Unit = {

    val vanillaData = JsonReader(
      unpackedContent.resolve("Data").resolve("Furniture.json").toFile
    ).decode[Seq[FurnitureData]].toTry.get

    val mod =
      CustomFurnitureModReader.read(target).toTry.get

    if (analyzeOnly) {
      val alternates = vanillaData
        .filterNot(data => data.name.contains(':') || data.name.startsWith("'"))

      //.distinctBy(fd => (fd.furnitureType, fd.tilesheetSize, fd.boundingBoxSize))
      def ofInterest(matches: Seq[FurnitureData]): String = {
        matches
          .distinctBy(fd =>
            (fd.furnitureType, fd.tilesheetSize, fd.boundingBoxSize)
          )
          .take(3)
          .map(_.name)
          .mkString(", ")
      }

      mod.pack.furniture.map { cf =>
        println(s"Looking for matches for ${cf.id}: ${cf.name}")
        val spriteMatches = CustomFurnitureMatcher
          .findMatchingAlternatesBySprite(cf, alternates)
          .tap {
            _.fold(println(s"No sprite matches found")) { spriteMatches =>
              println(
                s"Found the following sprite matches: ${ofInterest(spriteMatches.toSeq)}"
              )
            }
          }
          .map(_.toSeq)
          .getOrElse(Seq.empty)
        val boxMatches = CustomFurnitureMatcher
          .findMatchingAlternatesByBox(cf, alternates)
          .tap {
            _.fold(println(s"No sprite matches found")) { boxMatches =>
              println(
                s"Found the following box matches: ${ofInterest(boxMatches.toSeq)}"
              )
            }
          }
          .map(_.toSeq)
          .getOrElse(Seq.empty)
        val lost = spriteMatches.toSet.removedAll(boxMatches)
        val gained = boxMatches.toSet.removedAll(spriteMatches)
        if (lost.nonEmpty || gained.nonEmpty) {
          println(
            s"Changing to box based matching loses: ${lost.map(_.name).mkString(", ")}"
          )
          println(s"and gains: ${gained.map(_.name).mkString(", ")}")
        }
        println()
      }
      println(s"Done analyzing matches")
    } else {
      val customFurnitureSpriteExtractor =
        CustomFurnitureSpriteExtractor(spriteExtractionOptions)
      val alternativeTexturesSpriteConverter =
        AlternativeTexturesSpriteConverter(spriteConversionOptions)
      val converter =
        AlternativeTexturesConverter(
          customFurnitureSpriteExtractor,
          alternativeTexturesSpriteConverter
        )
      val convertedMod =
        converter.createAlternateTexturesMod(mod, vanillaData)
      AlternativeTexturesModWriter(convertedMod).writeTo(outputTo)
    }
  }
}

object ConvertATMod {

  private val targetMod = Opts.argument[Directory](metavar = "mod")
  private val maybeUnpackedContent = Opts
    .option[Directory](
      long = "content",
      short = "c",
      help =
        "Unpacked game content location. By default this will be found relative to the target mod assuming it is in the game's mods directory."
    )
    .orNone
  private val targetModType = Opts
    .option[ModType](
      long = "to",
      short = "t",
      help = "what mode type to convert the target mod to"
    )
    .withDefault(ModType.AlternativeTextures)
  private val outputDir = Opts
    .option[Path](
      long = "output-directory",
      short = "o",
      help = "set the output location."
    )
    .orNone
  private val analyzeMatchesFlag = Opts
    .flag(
      long = "analyze-matches",
      short = "am",
      help = "Only analyze potential CF to AT conversion matches"
    )
    .orFalse

  final val opts: Opts[ConvertATMod] =
    (
      targetMod,
      maybeUnpackedContent,
      outputDir,
      targetModType,
      optsForATSpriteConverterOptions,
      optsForCFSpriteExtractorOptions,
      analyzeMatchesFlag
    ).mapN {
      (
          targetMod,
          maybeUnpackedContent,
          outputDir,
          targetModType,
          spriteConversionOptions,
          spriteExtractionOptions,
          analyzeMatchesFlag
      ) =>
        ConvertATMod(
          targetMod,
          maybeUnpackedContent.getOrElse(
            Path(
              "C:/Program Files (x86)/Steam/steamapps/common/Stardew Valley/Content (unpacked)"
            ).toDirectory
          ),
          outputDir.getOrElse {
            val x = if (targetMod.name.startsWith(s"[CF]")) {
              targetMod.name
                .replace(s"[CF]", s"[${targetModType.abbreviation}]")
            } else s"[${targetModType.abbreviation}] ${targetMod.name}"
            targetMod.resolve(x.appendedAll(" Conversion"))
            //targetMod.resolve(s"[${targetModType.abbreviation}] conversion")
          },
          spriteConversionOptions,
          spriteExtractionOptions,
          analyzeMatchesFlag
        )
    }

  final val command: Command[Unit] = Command(
    name = "convert",
    header = "Convert a Custom Furniture mod to an Alternative Textures mod."
  )(opts.map(_.execute()))
}
