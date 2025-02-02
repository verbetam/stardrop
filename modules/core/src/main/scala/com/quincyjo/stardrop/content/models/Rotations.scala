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

/** Describes the number of rotations for a piece of furniture. This can be one,
  * where it cannot be rotated. 2, where it can be flipped, or 4, where it can
  * be rotated 360 degrees by 90 degrees increments.
  */
sealed trait Rotations {

  /** The number of rotations.
    * @return
    *   The number of rotations as an integer.
    */
  def value: Int

  /** The string representation of the number of rotations as stored in game
    * data.
    * @return
    *   The string representation of the number of rotations.
    */
  override final def toString: String = value.toString
}

object Rotations {

  /** A single rotation, IE, the furniture cannot be rotated.
    */
  final case object One extends Rotations {

    override val value = 1
  }

  /** Two rotations, IE, the furniture can be flipped.
    */
  final case object Two extends Rotations {
    override val value = 2
  }

  /** Four rotations, IE, the furniture can be rotated 360 degrees by 90 degree
    * increments.
    */
  final case object Four extends Rotations {
    override val value = 4
  }

  /** Converts an integer to a
    * [[com.quincyjo.stardrop.content.models.Rotations]], if possible.
    * @param int
    *   The integer to convert.
    * @return
    *   The [[com.quincyjo.stardrop.content.models.Rotations]] if valid or None
    *   otherwise.
    */
  def fromInt(int: Int): Option[Rotations] = int match {
    case 1 => Some(One)
    case 2 => Some(Two)
    case 4 => Some(Four)
    case _ => None
  }

  /** Converts a string to a [[com.quincyjo.stardrop.content.models.Rotations]],
    * if possible.
    * @param string
    *   The string to convert.
    * @return
    *   The [[com.quincyjo.stardrop.content.models.Rotations]] if valid or None
    *   otherwise.
    */
  def fromString(string: String): Option[Rotations] =
    string.toIntOption.flatMap(fromInt)
}
