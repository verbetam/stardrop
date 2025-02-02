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

/** Describes a size for a piece of furniture in a measurement of tiles.
  */
sealed trait FurnitureSize {

  /** The string representation of this size, either -1 if the default size of
    * the furniture type or the width and height separated by a space.
    * @return
    */
  def value: String

  /** Returns this [[com.quincyjo.stardrop.content.models.FurnitureSize]] as an
    * option of a [[com.quincyjo.stardrop.content.models.FurnitureSize.Size]] if
    * it is such, or None otherwise.
    * @return
    */
  def asSize: Option[FurnitureSize.Size]

  override final def toString: String = value
}

object FurnitureSize {

  /** A value denoting that the size of the furniture is inherited as the
    * default size based on its type.
    */
  final case object Default extends FurnitureSize {

    val value: String = "-1"

    val asSize: Option[Size] = None
  }

  /** Represents a size for a piece of furniture in a measurement of tiles.
    * @param width
    *   The width in tiles of a piece of furniture.
    * @param height
    *   The height in tiles of a piece of furniture.
    */
  final case class Size(width: Int, height: Int) extends FurnitureSize {

    val value = s"$width $height"

    /** The inverse of this
      * [[com.quincyjo.stardrop.content.models.FurnitureSize.Size]] by swapping
      * the width and height values.
      * @return
      *   The inverse of this size.
      */
    def inverse: Size = Size(height, width)

    def asSize: Option[Size] = Some(this)
  }

  /** Parses the given string into a
    * [[com.quincyjo.stardrop.content.models.FurnitureSize]] if it is valid.
    * Else, None is returned.
    * @param string
    *   The string to parse.
    * @return
    *   An option of a [[com.quincyjo.stardrop.content.models.FurnitureSize]].
    */
  def fromString(string: String): Option[FurnitureSize] = string match {
    case Default.value => Some(Default)
    case s"$width $height" =>
      width.toIntOption zip height.toIntOption map { case (w, h) =>
        Size(w, h)
      }
    case _ => None
  }
}
