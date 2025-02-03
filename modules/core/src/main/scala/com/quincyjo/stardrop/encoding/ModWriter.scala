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

import cats.effect.{Async, Resource}
import com.quincyjo.stardrop.smapi.models.SmapiManifest
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json, Printer}
import org.slf4j.Logger

import java.awt.image.BufferedImage
import java.io.FileOutputStream
import javax.imageio.ImageIO
import scala.reflect.io.{Directory, File, Path}
import scala.util.chaining.scalaUtilChainingOps

/** Trait defining a writer for a SMAPI mod.
  * @tparam ModType
  *   The type of the mod.
  */
trait ModWriter[ModType] {

  protected def logger: Logger

  /** Writes the mod to the provided directory.
    * @param root
    *   The directory to write the mod to.
    */
  def writeTo[F[_]: Async](root: Directory): F[Unit]

  /** Writes the mod to the provided path, creating a directory if necessary.
    * @param path
    *   The path to write the mod to.
    */
  def writeTo[F[_]: Async](path: Path): F[Unit] =
    writeTo[F](path.createDirectory())

  /** Writes the mod to the provided java.nio.file.Path, creating a directory if
    * necessary.
    * @param jpath
    *   The [[java.nio.file.Path]] to write the mod to.
    */
  def writeTo[F[_]: Async](jpath: java.nio.file.Path): F[Unit] =
    writeTo[F](Path(jpath.toFile))

  /** A writer configured to drop null values from JSON.
    */
  private final val jsonPrinter: Printer =
    Printer.spaces2.copy(dropNullValues = true)

  /** Writes the given manifest to the given directory.
    * @param in
    *   The director to write the manifest to.
    * @param manifest
    *   The manifest to write.
    * @return
    *   The number of bytes written, possibly zero.
    */
  protected def writeManifest[F[_]: Async](
      in: Directory,
      manifest: SmapiManifest
  ): F[Int] =
    writeAsJson(in, "manifest", manifest)

  /** Writes the given JSON to the given file.
    * @param file
    *   The file to write to.
    * @param json
    *   The JSON to write.
    * @return
    *   The number of bytes written, possibly zero.
    */
  protected def writeJson[F[_]: Async](file: File, json: Json): F[Int] = {
    logger.debug(s"Writing ${file.name}")
    Resource
      .make[F, FileOutputStream](
        Async[F].blocking(new FileOutputStream(file.jfile))
      )(x => Async[F].blocking(x.close()))
      .use { outputStream =>
        Async[F].blocking {
          outputStream.getChannel
            .write(jsonPrinter.printToByteBuffer(json.deepDropNullValues))
            .tap { bytesWritten =>
              logger.debug(
                s"Successfully Wrote $bytesWritten bytes to ${file.name}"
              )
            }
        }
      }
  }

  /** Writes the given JSON to the given path.
    * @param path
    *   The path to write to.
    * @param json
    *   The JSON to write.
    * @return
    *   The number of bytes written, possibly zero.
    */
  protected def writeJson[F[_]: Async](path: Path, json: Json): F[Int] =
    writeJson[F](ensureExtension(path, "json").createFile(), json)

  /** Writes the given JSON to the given director with the provided filename.
    * @param in
    *   The directory to write to.
    * @param name
    *   The name of the file to write.
    * @param json
    *   The JSON to write.
    * @return
    *   The number of bytes written, possibly zero.
    */
  protected def writeJson[F[_]: Async](
      in: Directory,
      name: String,
      json: Json
  ): F[Int] =
    writeJson[F](in / name, json)

  /** Writes the given value to the given directory with the provided filename
    * as a JSON using the provided encoder.
    * @param path
    *   The directory to write to.
    * @param name
    *   The name of the file to write.
    * @param t
    *   The value to write.
    * @return
    *   The number of bytes written, possibly zero.
    */
  protected def writeAsJson[T: Encoder, F[_]: Async](
      path: Directory,
      name: String,
      t: T
  ): F[Int] =
    writeJson[F](path, name, t.asJson)

  /** Writes the given image to the given path.
    * @param path
    *   The path to write to.
    * @param image
    *   The image to write.
    * @param format
    *   The format to write the image in.
    * @return
    *   True if the image was written, false otherwise.
    */
  protected def writeImage[F[_]: Async](
      path: Path,
      image: BufferedImage,
      format: String
  ): F[Boolean] = Async[F].blocking {
    logger.debug(s"Writing ${path.name}")
    ImageIO.write(image, format, sanitizePath(path).createFile().jfile)
  }

  /** Writes the given image to the given directory with the provided filename.
    * @param in
    *   The directory to write to.
    * @param name
    *   The name of the file to write.
    * @param image
    *   The image to write.
    * @param format
    *   The format to write the image in.
    * @return
    *   True if the image was written, false otherwise.
    */
  protected def writeImage[F[_]: Async](
      in: Directory,
      name: String,
      image: BufferedImage,
      format: String = "png"
  ): F[Boolean] =
    writeImage[F](ensureExtension(in / name, format), image, format)

  /** Sanitizes the path by removing invalid characters.
    * @param path
    *   The path to sanitize.
    * @return
    *   The sanitized path.
    */
  private def sanitizePath(path: Path): Path =
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

  protected def createDirectory[F[_]: Async](path: Path): F[Directory] =
    Async[F].blocking {
      path.createDirectory()
    }

  /** Ensures that the path has the given extension.
    * @param path
    *   The path to ensure.
    * @param extension
    *   The extension to ensure.
    * @return
    *   The path with the extension.
    */
  private def ensureExtension(path: Path, extension: String): Path =
    if (path.hasExtension(extension)) path else path.addExtension(extension)
}
