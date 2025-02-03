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

package com.quincyjo.stardrop.alternativetextures

import cats.effect.Async
import cats.implicits._
import com.quincyjo.stardrop.alternativetextures.models.{
  AlternativeTexturesMod,
  Texture
}
import com.quincyjo.stardrop.encoding.ModWriter
import com.quincyjo.stardrop.shared.models.Sprite
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.io.Directory

class AlternativeTexturesModWriter(mod: AlternativeTexturesMod)
    extends ModWriter[AlternativeTexturesMod] {

  override protected val logger: Logger =
    LoggerFactory.getLogger("AlternativeTexturesModWriter")

  def writeTo[F[_]: Async](root: Directory): F[Unit] =
    writeManifest[F](root, mod.manifest) >>
      createTexturesDirectory[F](root) >>=
      writeTextures[F]

  private def createTexturesDirectory[F[_]: Async](
      root: Directory
  ): F[Directory] =
    createDirectory[F](root.resolve("Textures"))

  private def writeTextures[F[_]: Async](directory: Directory): F[Unit] =
    mod.textures.toSeq.traverse { case (texture, sprites) =>
      writeTexture(directory, texture, sprites)
    }.void

  private def writeTexture[F[_]: Async](
      textures: Directory,
      texture: Texture,
      sprites: Iterable[Sprite]
  ): F[Unit] =
    for {
      directory <- createDirectory(
        textures /
          texture.textureType.toString /
          textureDirectoryName(texture)
      )
      _ = logger.info(
        s"Writing texture ${textures.relativize(directory.path)}"
      )
      result <- writeAsJson[Texture, F](directory, "texture", texture) >>
        sprites.zipWithIndex.toSeq.traverse { case (sprite, i) =>
          writeImage[F](directory, s"texture_$i", sprite.image)
        }.void
    } yield result

  private def textureDirectoryName(texture: Texture): String =
    texture.itemName
      .replaceAll(" ", "_")
      .appendedAll {
        texture.seasons.fold("")(_.map(_.toString).mkString("_"))
      }
}

object AlternativeTexturesModWriter {

  def apply(mod: AlternativeTexturesMod): AlternativeTexturesModWriter =
    new AlternativeTexturesModWriter(mod)
}
