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

package com.quincyjo.stardrop.alternativetextures

import com.quincyjo.stardrop.content.models.{FurnitureData, Rotations}
import com.quincyjo.stardrop.content.models.FurnitureType.{
  BedLike,
  FurnitureFrontType,
  LampLike
}
import com.quincyjo.stardrop.content.models.Rotations.{Four, One, Two}

object Util {

  def textureWidthFromFurnitureData(data: FurnitureData): Int = {
    data.furnitureType match {
      case _: LampLike | _: BedLike =>
        data.tilesheetWidth * 2 * 16
      case other =>
        (data.rotations match {
          case One => data.tilesheetWidth
          case Two | Four =>
            data.rotations.value / 2 * data.tilesheetWidth + // TODO: One off for couch because of change in bounding box ratio through rotation
              data.rotatedBoundingBoxWidth
        }) * 16
    }
  }

  def textureHeightFromFurnitureData(data: FurnitureData): Int = {
    val layerHeight = (data.rotations match {
      case Rotations.One => data.tilesheetHeight
      case _ => //Vector(data.tilesheetHeight, data.tilesheetWidth).max
        Vector(
          data.tilesheetHeight - data.boundingBoxHeight + data.rotatedBoundingBoxHeight,
          data.tilesheetHeight
        ).max // TODO: Add to rotated bounding box height instead of regular bounding box width
    }) * 16
    data.furnitureType match {
      case f: FurnitureFrontType => layerHeight * 2
      case _                     => layerHeight
    }
  }
}
