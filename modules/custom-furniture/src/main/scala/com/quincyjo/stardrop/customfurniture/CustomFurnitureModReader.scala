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

import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import com.quincyjo.stardrop.customfurniture.models.CustomFurniturePack
import com.quincyjo.stardrop.encoding.ModReader._
import com.quincyjo.stardrop.encoding.{JsonReader, ModReader}
import com.quincyjo.stardrop.shared.models.TileSheet
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.io._
import scala.util.chaining.scalaUtilChainingOps

object CustomFurnitureModReader extends ModReader[CustomFurnitureMod] {

  override final val logger: Logger =
    LoggerFactory.getLogger("CustomFurnitureModReader")

  override protected def readMod[F[_]: Async](
      from: Directory
  ): F[ModReaderResult[CustomFurnitureMod]] =
    (for {
      manifest <- EitherT(readManifestFrom(from))
      pack <- EitherT(readPack(from))
      tilesheets <- EitherT(readTilesheets(from))
    } yield CustomFurnitureMod(manifest, pack, tilesheets.toVector)).value

  def readTilesheets[F[_]: Async](
      from: Directory
  ): F[ModReaderResult[Seq[TileSheet]]] = {
    logger.debug(s"Looking for tilesheets in ${from.name}")
    Async[F]
      .blocking {
        from.files
          .filter(PngFilter)
          .toSeq
          .tap { files =>
            logger.debug(
              s"Found the following tile sheets: ${files.map(file => from.relativize(file)).mkString(", ")}"
            )
          }
      }
      .flatMap { files =>
        files.traverse { file =>
          logger.info(s"Reading tilesheet from ${from.relativize(file.path)}")
          EitherT(
            TileSheet
              .fromFile[F](file)
              .map[ModReaderResult[TileSheet]](Right.apply)
              .recover { ex =>
                Left(
                  FailureToReadFile(
                    s"Failed to read tilesheet from ${file.path}",
                    Some(ex)
                  )
                )
              }
          )
        }.value
      }
  }

  def findPacks[F[_]: Async](in: Directory): F[ModReaderResult[Seq[File]]] =
    Async[F].blocking {
      logger.debug(
        s"Looking for custom furniture pack candidates in ${in.name}"
      )
      Right(
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
      )
    }

  def readPack[F[_]: Async](
      from: Directory
  ): F[ModReaderResult[CustomFurniturePack]] = (for {
    candidates <- EitherT(findPacks[F](from))
    file <- EitherT.fromEither[F](
      candidates.headOption
        .toRight[ModReaderException](
          FileNotFoundException(s"No pack file could be found")
        )
        .filterOrElse(
          _ => candidates.size < 2,
          FileNotFoundException(
            s"Found ${candidates.size} JSONs, but expecting exactly one other than the manifest in ${from}"
          )
        )
    )
    pack <- EitherT(JsonReader[F](file).decode[CustomFurniturePack])
      .leftMap[ModReaderException] { ex =>
        FailureToReadFile(
          s"Failed to read the custom furniture pack from ${file.path}",
          Some(ex)
        )
      }
  } yield pack).value

}
