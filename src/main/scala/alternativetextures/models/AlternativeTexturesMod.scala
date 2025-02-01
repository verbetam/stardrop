package quincyjo.stardew
package alternativetextures.models

import shared.models.Sprite
import smapi.models.SmapiManifest

case class AlternativeTexturesMod(manifest: SmapiManifest,
                                  textures: Map[Texture, Vector[Sprite]])
