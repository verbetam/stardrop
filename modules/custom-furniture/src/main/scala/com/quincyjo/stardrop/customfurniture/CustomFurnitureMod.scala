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

import com.quincyjo.stardrop.customfurniture.models.CustomFurniturePack
import com.quincyjo.stardrop.shared.models.TileSheet
import com.quincyjo.stardrop.smapi.models.SmapiManifest

final case class CustomFurnitureMod(
    manifest: SmapiManifest,
    pack: CustomFurniturePack,
    tileSheets: Vector[TileSheet]
) {

  /** Returns a copy of the pack with the furniture sorted by ID.
    * @return
    *   A copy of the pack with the furniture sorted by ID
    */
  def sortPack: CustomFurniturePack =
    pack.copy(furniture = pack.furniture.sortBy(_.id))
}
