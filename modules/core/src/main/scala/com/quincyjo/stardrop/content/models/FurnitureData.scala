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

/** Describes the data associated with a piece of furniture.
  * @param id
  *   The ID of the furniture.
  * @param name
  *   The name of the furniture.
  * @param furnitureType
  *   The type of the furniture
  * @param tilesheetSize
  *   The size of the tilesheet.
  * @param boundingBoxSize
  *   The size of the bounding box.
  * @param rotations
  *   The number of rotations.
  * @param price
  *   The price of the furniture.
  * @param displayName
  *   The display name of the furniture, if defined.
  */
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

  /** Resolves the bounding box width to the defined bounding box width if
    * defined, or the default bounding box width if not.
    * @return
    *   The tile width of the bounding box.
    */
  def boundingBoxWidth: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultBoundingBoxSize.get.width)(_.width)

  /** Resolves the bounding box height to the defined bounding box height if
    * defined, or the default bounding box height if not.
    * @return
    *   The tile height of the bounding box.
    */
  def boundingBoxHeight: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultBoundingBoxSize.get.height)(_.height)

  /** Resolves the tilesheet width to the defined tilesheet width if defined, or
    * the default tilesheet width if not.
    * @return
    *   The tile width of the tilesheet.
    */
  def tilesheetWidth: Int =
    tilesheetSize.asSize
      .fold(furnitureType.defaultTilesheetSize.get.width)(_.width)

  /** Resolves the tilesheet height to the defined tilesheet height if defined,
    * or the default tilesheet height if not.
    * @return
    *   The tile height of the tilesheet.
    */
  def tilesheetHeight: Int =
    tilesheetSize.asSize
      .fold(furnitureType.defaultTilesheetSize.get.height)(_.height)

  /** Resolves the rotated bounding box width to the defined rotated bounding
    * box width if defined, or the default rotated bounding box width if not.
    * @return
    *   The tile width of the rotated bounding box.
    */
  def rotatedBoundingBoxWidth: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultRotatedBoundingBoxSize.get.width)(_.height)

  /** Resolves the rotated bounding box height to the defined rotated bounding
    * box height if defined, or the default rotated bounding box height if not.
    * @return
    *   The tile height of the rotated bounding box.
    */
  def rotatedBoundingBoxHeight: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultRotatedBoundingBoxSize.get.height)(_.width)
}

object FurnitureData {

  /** Attempts to parse furniture data from a string. Returns None if the string
    * is not a valid furniture data encoding.
    * @param id
    *   The ID of the furniture to parse.
    * @param string
    *   The string to parse.
    * @return
    *   The parsed furniture data or None if it is not valid.
    */
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
