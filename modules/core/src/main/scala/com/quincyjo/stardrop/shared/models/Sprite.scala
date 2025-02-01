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

package com.quincyjo.stardrop.shared.models

import TileSheet.{TILE_HEIGHT, TILE_WIDTH}

import java.awt.image.BufferedImage

case class Sprite(image: BufferedImage) {

  val widthInPixels: Int = image.getWidth
  val heightInPixels: Int = image.getHeight
  val widthInTiles: Int = widthInPixels / TILE_WIDTH
  val heightInTiles: Int = heightInPixels / TILE_HEIGHT
}

object Sprite {}
