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

package com.quincyjo.stardrop.converters

import cats.data.NonEmptySeq
import com.quincyjo.stardrop.content.models.FurnitureData
import com.quincyjo.stardrop.content.models.FurnitureType.{
  BedLike,
  HasFrontLayer,
  Painting
}
import com.quincyjo.stardrop.customfurniture.models.CustomFurniture

object CustomFurnitureMatcher {

  final case class Matcher(
      matcher: ((CustomFurniture, FurnitureData)) => Boolean
  ) extends ((CustomFurniture, FurnitureData) => Boolean) {

    def apply(pair: (CustomFurniture, FurnitureData)): Boolean =
      matcher(pair)

    def apply(cf: CustomFurniture, data: FurnitureData): Boolean =
      matcher((cf, data))

    def and(that: Matcher): Matcher =
      Matcher(n => matcher(n) && that.matcher(n))

    def &(that: Matcher): Matcher =
      this and that

    def or(that: Matcher): Matcher =
      Matcher(n => matcher(n) || that.matcher(n))

    def |(that: Matcher): Matcher =
      this or that

    def not: Matcher =
      Matcher(!matcher(_))

    def unary_! : Matcher =
      not
  }

  final val MatchesType = Matcher { case (cf, data) =>
    cf.`type` == data.furnitureType
  }
  final val MatchesSpriteSize = Matcher { case (cf, data) =>
    cf.width == data.tilesheetWidth &&
      cf.height == data.tilesheetHeight
  }
  final val MatchesBoxSize = Matcher { case (cf, data) =>
    cf.boxWidth == data.boundingBoxWidth &&
      cf.boxHeight == data.boundingBoxHeight
  }

  def findMatches(
      customFurniture: CustomFurniture,
      targets: Iterable[FurnitureData]
  ): Iterable[FurnitureData] = {
    val baseMatches = findMatchingAlternates(customFurniture, targets)
    val strongMatches = baseMatches.filter(furnitureData =>
      guessKeywords(customFurniture)
        .exists(furnitureData.name.toLowerCase.contains)
    )
    if (strongMatches.nonEmpty) strongMatches else baseMatches
  }

  def findMatchingAlternates(
      customFurniture: CustomFurniture,
      alternatesTo: Iterable[FurnitureData]
  ): Iterable[FurnitureData] =
    alternatesTo.filter { data =>
      MatchesType & (MatchesSpriteSize | MatchesBoxSize)
      data.furnitureType == customFurniture.`type` &&
      customFurniture.width == data.tilesheetWidth &&
      customFurniture.height == data.tilesheetHeight &&
      (customFurniture.`type` match {
        case _: HasFrontLayer | Painting | _: BedLike => true
        case _ =>
          customFurniture.boxWidth == data.boundingBoxWidth &&
            customFurniture.boxHeight == data.boundingBoxHeight
      })
    // TODO: Testing extra match requirements
    }

  def matchesEitherSpriteOrBox(
      customFurniture: CustomFurniture,
      data: FurnitureData
  ): Boolean = {
    customFurniture.width == data.tilesheetWidth &&
    customFurniture.height == data.tilesheetHeight ||
    customFurniture.boxWidth == data.boundingBoxWidth &&
    customFurniture.boxHeight == data.boundingBoxHeight
  }

  def findMatchingAlternatesBySprite(
      customFurniture: CustomFurniture,
      alternatesTo: Iterable[FurnitureData]
  ): Option[NonEmptySeq[FurnitureData]] =
    NonEmptySeq.fromSeq(alternatesTo.filter { data =>
      data.furnitureType == customFurniture.`type` &&
      customFurniture.width == data.tilesheetWidth &&
      customFurniture.height == data.tilesheetHeight
    }.toSeq)

  def findMatchingAlternatesByBox(
      customFurniture: CustomFurniture,
      alternatesTo: Iterable[FurnitureData]
  ): Option[NonEmptySeq[FurnitureData]] =
    NonEmptySeq.fromSeq(alternatesTo.filter { data =>
      data.furnitureType == customFurniture.`type` &&
      customFurniture.boxWidth == data.boundingBoxWidth &&
      customFurniture.boxHeight == data.boundingBoxHeight &&
      customFurniture.width <= data.tilesheetWidth &&
      customFurniture.height <= data.tilesheetHeight
    }.toSeq)

  def findExactSizeMatches(
      customFurniture: CustomFurniture,
      alternatesTo: Iterable[FurnitureData]
  ): Iterable[FurnitureData] =
    alternatesTo
      //.filter(_.furnitureType.defaultTilesheetSize.isDefined)
      .filter { data =>
        customFurniture.width == data.tilesheetWidth &&
        customFurniture.height == data.tilesheetHeight &&
        customFurniture.boxWidth == data.boundingBoxWidth &&
        customFurniture.boxHeight == data.boundingBoxHeight
      }

  def findSpriteSizeMatches(
      customFurniture: CustomFurniture,
      alternatesTo: Iterable[FurnitureData]
  ): Iterable[FurnitureData] =
    alternatesTo
      .filter { data =>
        customFurniture.width == data.tilesheetWidth &&
        customFurniture.height == data.tilesheetHeight
      }

  def findBoundingBoxSizeMatches(
      customFurniture: CustomFurniture,
      alternatesTo: Iterable[FurnitureData]
  ): Iterable[FurnitureData] =
    alternatesTo
      .filter { data =>
        customFurniture.boxWidth == data.boundingBoxWidth &&
        customFurniture.boxHeight == data.boundingBoxHeight
      }

  def guessKeywords(customFurniture: CustomFurniture): Set[String] =
    guessKeywords(customFurniture.name)

  def guessKeywords(string: String): Set[String] =
    string.toLowerCase
      .split("[ ,-]")
      .map(_.replaceAll("[^a-z1-9]", ""))
      .toSet
      .diff(omittedKeywords)

  private val omittedKeywords = Set("a", "an", "of", "the", "and", "with")
}
