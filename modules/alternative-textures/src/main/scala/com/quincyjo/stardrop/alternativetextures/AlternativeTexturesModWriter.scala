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

  def writeTo(root: Directory): Unit = {
    writeManifest(root, mod.manifest)
    val texturesDir = root.resolve("Textures").createDirectory()
    mod.textures.foreach { case (texture, sprites) =>
      writeTexture(texturesDir, texture, sprites)
    }
  }

  def writeTexture(
      textures: Directory,
      texture: Texture,
      sprites: Iterable[Sprite]
  ): Unit = {
    val thisTextureDir =
      (textures /
        texture.textureType.toString /
        textureDirectoryName(texture))
        .createDirectory()
    logger.info(s"Writing texture ${textures.relativize(thisTextureDir.path)}")
    writeAsJson(thisTextureDir, "texture", texture)
    sprites.zipWithIndex.foreach { case (sprite, i) =>
      writeImage(thisTextureDir, s"texture_$i", sprite.image)
    }
  }

  def textureDirectoryName(texture: Texture): String =
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
