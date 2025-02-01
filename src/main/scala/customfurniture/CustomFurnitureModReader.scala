package quincyjo.stardew
package customfurniture

import customfurniture.models.CustomFurniturePack
import encoding.ModReader._
import encoding.{JsonReader, ModReader}
import shared.models.TileSheet

import cats.syntax.traverse._
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.io._
import scala.util.chaining.scalaUtilChainingOps

object CustomFurnitureModReader extends ModReader[CustomFurnitureMod] {

  override final val logger: Logger =
    LoggerFactory.getLogger("CustomFurnitureModReader")

  def readMod(from: Directory): ModReaderResult[CustomFurnitureMod] =
    for {
      manifest <- readManifestFrom(from)
      pack <- readPack(from)
      tilesheets <- readTilesheets(from)
    } yield CustomFurnitureMod(manifest, pack, tilesheets.toVector)

  def readTilesheets(from: Directory): ModReaderResult[Iterable[TileSheet]] = {
    logger.debug(s"Looking for tilesheets in ${from.name}")
    from.files
      .filter(PngFilter)
      .toSeq
      .tap { files =>
        logger.debug(
          s"Found the following tile sheets: ${files.map(file => from.relativize(file)).mkString(", ")}"
        )
      }
      .traverse { file =>
        logger.info(s"Reading tilesheet from ${from.relativize(file.path)}")
        TileSheet
          .fromFile(file)
          .left
          .map { message =>
            FailureToReadFile(
              s"Failed to read tilesheet from ${file.path}: $message"
            )
          }
      }
  }

  def findPacks(in: Directory): Seq[File] = {
    logger.debug(s"Looking for custom furniture pack candidates in ${in.name}")
    in.files
      .filter(JsonFilter & !ManifestFilter)
      .toSeq
      .tap { files =>
        logger.debug(
          s"Found the following JSONs as candidates for custom furniture packs: ${files
            .map(file => in.relativize(file))
            .mkString(", ")}"
        )
      }
  }

  def readPack(from: Directory): ModReaderResult[CustomFurniturePack] = {
    val candidateFiles = findPacks(from)
    candidateFiles.headOption
      .toRight(FileNotFoundException(s"No pack file could be found"))
      .filterOrElse(
        _ => candidateFiles.size < 2,
        FileNotFoundException(
          s"Found ${candidateFiles.size} JSONs, but expecting exactly one other than the manifest in ${from}"
        )
      )
      .flatMap { file =>
        logger.info(
          s"Reading custom furniture pack from ${from.relativize(file.path)}",
          ")}"
        )
        JsonReader(file).decode[CustomFurniturePack].left.map { ex =>
          FailureToReadFile(
            s"Failed to read the custom furniture pack from ${file.path}",
            Some(ex)
          )
        }
      }
  }

}
