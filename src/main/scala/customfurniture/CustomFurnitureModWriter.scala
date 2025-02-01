package quincyjo.stardew
package customfurniture

import encoding.ModWriter

import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.io.Directory

class CustomFurnitureModWriter(mod: CustomFurnitureMod)
    extends ModWriter[CustomFurnitureMod] {

  override val logger: Logger =
    LoggerFactory.getLogger("CustomFurnitureModWriter")

  def writeManifest(in: Directory): Unit =
    writeAsJson(in, "manifest", mod.manifest)

  override def writeTo(root: Directory): Unit = {
    if (!root.exists) root.createDirectory()
    writeManifest(root)
    writeAsJson(
      root,
      "content",
      mod.pack.copy(furniture = mod.pack.furniture.sortBy(_.id))
    )
    mod.tileSheets.foreach { tilesheet =>
      writeImage(root, tilesheet.name, tilesheet.image)
    }
  }
}

object CustomFurnitureModWriter {
  def apply(mod: CustomFurnitureMod): CustomFurnitureModWriter =
    new CustomFurnitureModWriter(mod)
}
