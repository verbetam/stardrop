package quincyjo.stardew
package alternativetextures

import alternativetextures.models.{AlternativeTexturesMod, Texture}
import encoding.ModWriter
import shared.models.Sprite

import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.io.Directory

class AlternativeTexturesModWriter(mod: AlternativeTexturesMod)
    extends ModWriter[AlternativeTexturesMod] {

  override val logger: Logger =
    LoggerFactory.getLogger("AlternativeTexturesModWriter")

  def writeTo(root: Directory): Unit = {
    writeManifest(root, mod.manifest)
    val texturesDir = root.resolve("Textures").createDirectory()
    mod.textures.foreach {
      case (texture, sprites) =>
        writeTexture(texturesDir, texture, sprites)
    }
  }

  def writeTexture(textures: Directory,
                   texture: Texture,
                   sprites: Iterable[Sprite]): Unit = {
    val thisTextureDir =
      (textures /
        texture.textureType.toString /
        textureDirectoryName(texture))
        .createDirectory()
    logger.info(s"Writing texture ${textures.relativize(thisTextureDir.path)}")
    writeAsJson(thisTextureDir, "texture", texture)
    sprites.zipWithIndex.foreach {
      case (sprite, i) =>
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
