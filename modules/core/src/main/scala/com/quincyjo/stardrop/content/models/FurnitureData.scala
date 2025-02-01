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

package com.quincyjo.stardrop.content.models

import cats.syntax.traverse.*
import io.circe.Decoder

case class FurnitureData(
    id: String,
    name: String,
    furnitureType: FurnitureType,
    tilesheetSize: FurnitureSize,
    boundingBoxSize: FurnitureSize,
    rotations: Rotations,
    price: Int,
    displayName: Option[String] = None
) {

  override def toString: String =
    s"$name/$furnitureType/$tilesheetSize/$boundingBoxSize/$rotations/$price"

  def boundingBoxWidth: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultBoundingBoxSize.get.width)(_.width)

  def boundingBoxHeight: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultBoundingBoxSize.get.height)(_.height)

  def tilesheetWidth: Int =
    tilesheetSize.asSize
      .fold(furnitureType.defaultTilesheetSize.get.width)(_.width)

  def tilesheetHeight: Int =
    tilesheetSize.asSize
      .fold(furnitureType.defaultTilesheetSize.get.height)(_.height)

  def rotatedBoundingBoxWidth: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultRotatedBoundingBoxSize.get.width)(_.height)

  def rotatedBoundingBoxHeight: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultRotatedBoundingBoxSize.get.height)(_.width)
}

object FurnitureData {

  def fromString(id: String, string: String): Option[FurnitureData] = {
    val values = string.split('/').toList
    for {
      name <- values.headOption
      furnitureType <- values.lift(1).flatMap(FurnitureType.fromString)
      tilesheetSize <- values.lift(2).flatMap(FurnitureSize.fromString)
      boundingBlockSize <- values.lift(3).flatMap(FurnitureSize.fromString)
      rotations <- values.lift(4).flatMap(Rotations.fromString)
      price <- values.lift(5).flatMap(_.toIntOption)
    } yield FurnitureData(
      id,
      name,
      furnitureType,
      tilesheetSize,
      boundingBlockSize,
      rotations,
      price
    )
  }

  implicit val decodeFurnitureData: Decoder[Seq[FurnitureData]] =
    implicitly[Decoder[Map[String, String]]].emap {
      _.toSeq.traverse { case (key, value) =>
        fromString(key, value).toRight(
          s"$value is not a valid furniture data encoding'"
        )
      }
    }
}
