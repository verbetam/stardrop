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

import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import com.quincyjo.stardrop.encoding.ModReader._
import com.quincyjo.stardrop.smapi.models.SmapiManifest
import org.slf4j.Logger

import scala.reflect.io._
import scala.util.control.NoStackTrace

/** Trait defining a reader for a SMAPI mod.
  * @tparam ModType
  *   The type of the mod.
  */
trait ModReader[ModType] {

  def logger: Logger

  /** Finds and reads the manifest file from a directory. If it cannot find a
    * manifest file or fails to read it, then a
    * [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]] is
    * returned.
    * @param path
    *   The directory to read from.
    * @return
    *   Either a [[com.quincyjo.stardrop.smapi.models.SmapiManifest]] or a
    *   [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]].
    */
  protected def readManifestFrom[F[_]: Async](
      path: Directory
  ): F[ModReaderResult[SmapiManifest]] = (for {
    path <- EitherT(findManifest(path))
    manifest <- EitherT(readManifest(path))
  } yield manifest).value

  /** Reads the mod from the provided directory. This is left to be implemented
    * by the specific mod reader.
    * @param from
    *   The director to read the mod from.
    * @return
    *   Either a <pre>ModType</pre> or a
    *   [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]].
    */
  protected def readMod[F[_]: Async](
      from: Directory
  ): F[ModReaderResult[ModType]]

  /** Reads the mod from the provided path.
    * @param from
    *   The path to read the mod from.
    * @return
    *   Either a <pre>ModType</pre> or a
    *   [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]].
    */
  def read[F[_]: Async](from: Path): F[ModReaderResult[ModType]] = {
    logger.info(s"Reading mod from $from")
    readMod(from.toDirectory)
  }

  /** Reads the mod from the provided path.
    * @param jPath
    *   The path to read the mod from.
    * @return
    *   Either a <pre>ModType</pre> or a
    *   [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]].
    */
  def read[F[_]: Async](
      jPath: java.nio.file.Path
  ): F[ModReaderResult[ModType]] =
    read(Path(jPath.toFile))

  /** Attempts to find a file candidate for a SMAPI manifest in the given
    * directory.
    * @param in
    *   The directory to search.
    * @return
    *   Either a [[File]] or a
    *   [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]].
    */
  private def findManifest[F[_]: Async](
      in: Directory
  ): F[ModReaderResult[File]] = {
    logger.debug(s"Looking for manifest in ${in.name}")
    Async[F].blocking {
      in.files
        .filter(ManifestFilter)
        .nextOption()
        .toRight(
          FileNotFoundException(
            s"Could not find a manifest.json file in ${in.name}"
          )
        )
    }
  }

  /** Attempt to read the given file as a SMAPI manifest.
    * @param file
    *   The file to read.
    * @return
    *   Either a [[SmapiManifest]] or a
    *   [[com.quincyjo.stardrop.encoding.ModReader.ModReaderException]].
    */
  private def readManifest[F[_]: Async](
      file: File
  ): F[ModReaderResult[SmapiManifest]] = {
    logger.info(s"Reading manifest from ${file.name}")
    JsonReader[F](file)
      .decode[SmapiManifest]
      .map(_.left.map { ex =>
        FailureToReadFile(
          s"Failed to read manifest file ${file.name}",
          Some(ex)
        )
      })
  }
}

object ModReader {

  /** A composable filter for files. Used to build filters to matching target
    * files.
    * @param fileNamePredicate
    *   The predicate to apply to the file name. This is passed the lowercased
    *   filename, including the extension.
    */
  final case class FilenameFilter(fileNamePredicate: String => Boolean)
      extends java.io.FilenameFilter
      with (File => Boolean) {

    /** Implementation [[java.io.FilenameFilter.accept]] by leveraging the
      * [[fileNamePredicate]].
      * @param dir
      *   The directory containing the file.
      * @param name
      *   The name of the file.
      * @return
      *   True if the file should be accepted.
      */
    override def accept(dir: java.io.File, name: String): Boolean =
      fileNamePredicate(name.toLowerCase)

    /** Applies this filter to a file.
      * @param file
      *   The file to filter.
      * @return
      *   True if the file should be accepted.
      */
    def apply(file: File): Boolean =
      accept(file.parent.jfile, file.name)

    /** Composes this filter with <pre>that</pre> filter where both must be
      * true.
      * @param that
      *   The filter to compose with.
      * @return
      *   The composed filter.
      */
    def and(that: FilenameFilter): FilenameFilter =
      FilenameFilter(n => fileNamePredicate(n) && that.fileNamePredicate(n))

    /** Alias for
      * [[com.quincyjo.stardrop.encoding.ModReader.FilenameFilter.and]].
      * @param that
      *   The filter to compose with.
      * @see
      *   [[com.quincyjo.stardrop.encoding.ModReader.FilenameFilter.and]]
      * @return
      *   The composed filter.
      */
    def &(that: FilenameFilter): FilenameFilter = this and that

    /** Composes this filter with <pre>that</pre> filter where either must be
      * true.
      * @param that
      *   The filter to compose with.
      * @return
      *   The composed filter.
      */
    def or(that: FilenameFilter): FilenameFilter =
      FilenameFilter(n => fileNamePredicate(n) || that.fileNamePredicate(n))

    /** Alias for
      * [[com.quincyjo.stardrop.encoding.ModReader.FilenameFilter.or]].
      * @param that
      *   The filter to compose with.
      * @see
      *   [[com.quincyjo.stardrop.encoding.ModReader.FilenameFilter.or]]
      * @return
      *   The composed filter.
      */
    def |(that: FilenameFilter): FilenameFilter = this or that

    /** Returns the inverse of this filter.
      * @return
      *   The inverse of this filter.
      */
    def not: FilenameFilter = FilenameFilter(!fileNamePredicate(_))

    /** Alias for
      * [[com.quincyjo.stardrop.encoding.ModReader.FilenameFilter.not]].
      * @see
      *   [[com.quincyjo.stardrop.encoding.ModReader.FilenameFilter.not]]
      * @return
      *   The inverse of this filter.
      */
    def unary_! : FilenameFilter = not
  }

  object FilenameFilter {

    /** Creates a filter which matches files with the given extension.
      * @param extension
      *   The extension to match.
      * @return
      *   A filter matching files of the given extension.
      */
    def byExtension(extension: String): FilenameFilter =
      FilenameFilter(_.endsWith(s".$extension"))
  }

  /** A filter for JSON files.
    */
  final val JsonFilter = FilenameFilter.byExtension("json")

  /** A filter for PNG files.
    */
  final val PngFilter = FilenameFilter.byExtension("png")

  /** A filter for SMAPI manifest files.
    */
  final val ManifestFilter = FilenameFilter(_ == "manifest.json")

  type ModReaderResult[T] = Either[ModReaderException, T]

  /** Describes a failure to read a mod.
    * @param message
    *   A message describing the failure.
    * @param cause
    *   The underlying cause of the failure, if defined.
    */
  sealed abstract class ModReaderException(
      message: String,
      cause: Option[Throwable] = None
  ) extends Exception(message, cause.orNull)
      with NoStackTrace

  /** Describes a failure to find a file.
    * @param message
    *   A message describing the failure.
    * @param cause
    *   The underlying cause of the failure, if defined.
    */
  final case class FileNotFoundException(
      message: String,
      cause: Option[Throwable] = None
  ) extends ModReaderException(message, cause)

  /** Describes a failure to read a file such as it not being a valid JSON or
    * otherwise malformed.
    * @param message
    *   A message describing the failure.
    * @param cause
    *   The underlying cause of the failure, if defined.
    */
  final case class FailureToReadFile(
      message: String,
      cause: Option[Throwable] = None
  ) extends ModReaderException(message, cause)

  /** Describes a failure caused by a given directory path not being directory,
    * but rather a file.
    * @param message
    *   A message describing the failure.
    * @param cause
    *   The underlying cause of the failure, if defined.
    */
  final case class NotADirectory(
      message: String,
      cause: Option[Throwable] = None
  ) extends ModReaderException(message, cause)

  /** Describes a failure caused by a given file path not being a file.
    * @param message
    *   A message describing the failure.
    * @param cause
    *   The underlying cause of the failure, if defined.
    */
  final case class NotAFile(message: String, cause: Option[Throwable] = None)
      extends ModReaderException(message, cause)
}
