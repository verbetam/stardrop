package quincyjo.stardew
package customfurniture

import customfurniture.models.CustomFurniturePack
import shared.models.TileSheet
import smapi.models.SmapiManifest

case class CustomFurnitureMod(manifest: SmapiManifest,
                              pack: CustomFurniturePack,
                              tileSheets: Vector[TileSheet])
