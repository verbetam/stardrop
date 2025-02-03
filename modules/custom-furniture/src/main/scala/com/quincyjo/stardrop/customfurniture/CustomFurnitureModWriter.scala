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

import cats.effect.Async
import cats.implicits._
import com.quincyjo.stardrop.encoding.ModWriter
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.io.Directory

class CustomFurnitureModWriter(mod: CustomFurnitureMod)
    extends ModWriter[CustomFurnitureMod] {

  override protected val logger: Logger =
    LoggerFactory.getLogger("CustomFurnitureModWriter")

  override def writeTo[F[_]: Async](root: Directory): F[Unit] = {
    logger.info(s"Writing mod ${mod.manifest.name} to ${root.name}")
    writeManifest(root, mod.manifest) >>
      writeContent(root) >>
      writeTileSheets(root)
  }

  private def writeContent[F[_]: Async](root: Directory): F[Int] =
    writeAsJson(root, "content", mod.sortPack)

  private def writeTileSheets[F[_]: Async](root: Directory): F[Unit] =
    mod.tileSheets.traverse { tilesheet =>
      writeImage(root, tilesheet.name, tilesheet.image)
    }.void
}

object CustomFurnitureModWriter {

  def apply(mod: CustomFurnitureMod): CustomFurnitureModWriter =
    new CustomFurnitureModWriter(mod)
}
