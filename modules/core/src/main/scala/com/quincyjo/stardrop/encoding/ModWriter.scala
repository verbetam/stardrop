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

package com.quincyjo.stardrop.encoding

import com.quincyjo.stardrop.smapi.models.SmapiManifest
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json, Printer}
import org.slf4j.Logger

import java.awt.image.BufferedImage
import java.io.FileOutputStream
import javax.imageio.ImageIO
import scala.reflect.io.{Directory, File, Path}

trait ModWriter[T] {

  def logger: Logger

  final val jsonPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)

  def writeManifest(in: Directory, manifest: SmapiManifest): Unit =
    writeAsJson(in, "manifest", manifest)

  def writeJson(file: File, json: Json): Unit = {
    logger.debug(s"Writing ${file.name}")
    new FileOutputStream(file.jfile).getChannel
      .write(jsonPrinter.printToByteBuffer(json.deepDropNullValues))
  }

  def writeJson(path: Path, json: Json): Unit =
    writeJson(ensureExtension(path, "json").createFile(), json)

  def writeJson(in: Directory, name: String, json: Json): Unit =
    writeJson(in / name, json)

  def writeAsJson[T: Encoder](path: Directory, name: String, t: T): Unit =
    writeJson(path, name, t.asJson)

  def writeImage(path: Path, image: BufferedImage, format: String): Unit = {
    logger.debug(s"Writing ${path.name}")
    ImageIO.write(image, format, sanitizePath(path).createFile().jfile)
  }

  def writeImage(
      in: Directory,
      name: String,
      image: BufferedImage,
      format: String = "png"
  ): Unit =
    writeImage(ensureExtension(in / name, format), image, format)

  def sanitizePath(path: Path): Path =
    Path(path.segments match {
      case drive :: tail if path.isAbsolute =>
        tail
          .map(_.replaceAll("[<>:\"/|?*\\\\]", ""))
          .prepended(drive)
          .mkString(path.separatorStr)
      case segments =>
        segments
          .map(_.replaceAll("[<>:\"/|?*\\\\]", ""))
          .mkString(path.separatorStr)
    })

  def writeTo(root: Directory): Unit

  def writeTo(path: Path): Unit = writeTo(path.createDirectory())

  def writeTo(jpath: java.nio.file.Path): Unit = writeTo(Path(jpath.toFile))

  def ensureExtension(path: Path, extension: String): Path =
    if (path.hasExtension(extension)) path else path.addExtension(extension)
}
