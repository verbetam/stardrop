package quincyjo.stardew
package converters

import content.models.FurnitureData
import content.models.FurnitureType.{FurnitureFrontType, _}
import customfurniture.models.CustomFurniture

import cats.data.NonEmptySeq

object CustomFurnitureMatcher {

  final case class Matcher(matcher: (CustomFurniture, FurnitureData) => Boolean)
      extends ((CustomFurniture, FurnitureData) => Boolean) {

    def apply(pair: (CustomFurniture, FurnitureData)): Boolean

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
  final val MatchesType = Matcher {
    case (cf, data) =>
      cf.`type` == data.furnitureType
  }
  final val MatchesSpriteSize = Matcher {
    case (cf, data) =>
      cf.width == data.tilesheetWidth &&
        cf.height == data.tilesheetHeight
  }
  final val MatchesBoxSize = Matcher {
    case (cf, data) =>
      cf.boxWidth == data.boundingBoxWidth &&
        cf.boxHeight == data.boundingBoxHeight
  }

  def findMatches(customFurniture: CustomFurniture,
                  targets: Iterable[FurnitureData]): Iterable[FurnitureData] = {
    val baseMatches = findMatchingAlternates(customFurniture, targets)
    val strongMatches = baseMatches.filter(
      furnitureData =>
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
        case _: FurnitureFrontType | Painting | _: BedLike => true
        case _ =>
          customFurniture.boxWidth == data.boundingBoxWidth &&
            customFurniture.boxHeight == data.boundingBoxHeight
      })
    // TODO: Testing extra match requirements
    }

  def matchesEitherSpriteOrBox(customFurniture: CustomFurniture,
                               data: FurnitureData): Boolean = {
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
