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
import com.quincyjo.stardrop.cli.implicits._
import com.monovore.decline.*
import com.quincyjo.stardrop.converters.AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import com.quincyjo.stardrop.converters.{
  AlternativeTexturesSpriteConverter,
  CustomFurnitureModExploder,
  CustomFurnitureSpriteExtractor
}
import com.quincyjo.stardrop.converters.CustomFurnitureSpriteExtractor.SpriteExtractorOptions
import com.quincyjo.stardrop.customfurniture.{
  CustomFurnitureModReader,
  CustomFurnitureModWriter
}

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
      (
          targetMod,
          outputDir,
          spriteExtractionOptions,
          spriteConversionOptions
      ) =>
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
