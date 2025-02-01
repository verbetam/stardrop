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

sealed trait FurnitureSize {

  def value: String

  def asSize: Option[FurnitureSize.Size]

  override final def toString: String = value
}

object FurnitureSize {

  final case object Default extends FurnitureSize {
    val value: String = "-1"
    val asSize: Option[Size] = None
  }

  final case class Size(width: Int, height: Int) extends FurnitureSize {
    val value = s"$width $height"

    def inverse: Size = Size(height, width)

    def asSize: Option[Size] = Some(this)
  }

  def fromString(string: String): Option[FurnitureSize] = string match {
    case Default.value => Some(Default)
    case s"$width $height" =>
      width.toIntOption zip height.toIntOption map { case (w, h) =>
        Size(w, h)
      }
    case _ => None
  }
}
