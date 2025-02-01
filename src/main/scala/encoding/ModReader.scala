package quincyjo.stardew
package encoding

import encoding.ModReader._
import smapi.models.SmapiManifest

import org.slf4j.Logger

import scala.reflect.io._

trait ModReader[T] {

  def logger: Logger

  def findManifest(in: Directory): ModReaderResult[File] = {
    logger.debug(s"Looking for manifest in ${in.name}")
    in.files
      .filter(ManifestFilter)
      .nextOption()
      .toRight(
        FileNotFoundException(
          s"Could not find a manifest.json file in ${in.name}"
        )
      )
  }

  def readManifest(file: File): ModReaderResult[SmapiManifest] = {
    logger.info(s"Reading manifest from ${file.name}")
    JsonReader(file).decode[SmapiManifest].left.map { ex =>
      FailureToReadFile(s"Failed to read manifest file ${file.name}", Some(ex))
    }
  }

  def readManifestFrom(path: Directory): ModReaderResult[SmapiManifest] =
    findManifest(path).flatMap(readManifest)

  protected def readMod(from: Directory): ModReaderResult[T]

  def read(from: Path): ModReaderResult[T] = {
    logger.info(s"Reading mod from $from")
    readMod(from.toDirectory)
  }

  def read(jPath: java.nio.file.Path): ModReaderResult[T] =
    read(Path(jPath.toFile))
}

object ModReader {

  final case class FilenameFilter(fileNamePredicate: String => Boolean)
      extends java.io.FilenameFilter
      with (File => Boolean) {

    override def accept(dir: java.io.File, name: String): Boolean =
      fileNamePredicate(name.toLowerCase)

    def apply(file: File): Boolean =
      accept(file.parent.jfile, file.name)

    def and(that: FilenameFilter): FilenameFilter =
      FilenameFilter(n => fileNamePredicate(n) && that.fileNamePredicate(n))
    def &(that: FilenameFilter): FilenameFilter = this and that
    def or(that: FilenameFilter): FilenameFilter =
      FilenameFilter(n => fileNamePredicate(n) || that.fileNamePredicate(n))
    def |(that: FilenameFilter): FilenameFilter = this or that
    def not: FilenameFilter = FilenameFilter(!fileNamePredicate(_))
    def unary_! : FilenameFilter = not
  }
  object FilenameFilter {

    def byExtension(extension: String): FilenameFilter =
      FilenameFilter(_.endsWith(s".$extension"))
  }

  final val JsonFilter = FilenameFilter.byExtension("json")
  final val PngFilter = FilenameFilter.byExtension("png")
  final val ManifestFilter = FilenameFilter(_ == "manifest.json")

  type ModReaderResult[T] = Either[ModReaderException, T]

  sealed abstract class ModReaderException(message: String,
                                           cause: Option[Throwable] = None)
      extends Exception(message, cause.orNull)
  final case class FileNotFoundException(message: String,
                                         cause: Option[Throwable] = None)
      extends ModReaderException(message, cause)
  final case class FailureToReadFile(message: String,
                                     cause: Option[Throwable] = None)
      extends ModReaderException(message, cause)
  final case class NotADirectory(message: String,
                                 cause: Option[Throwable] = None)
      extends ModReaderException(message, cause)
  final case class NotAFile(message: String, cause: Option[Throwable] = None)
      extends ModReaderException(message, cause)
}
