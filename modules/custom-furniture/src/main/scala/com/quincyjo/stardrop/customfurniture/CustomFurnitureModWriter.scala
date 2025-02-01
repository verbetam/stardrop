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

package com.quincyjo.stardrop.customfurniture

import com.quincyjo.stardrop.encoding.ModWriter
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
