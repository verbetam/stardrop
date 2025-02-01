package quincyjo.stardew
package cli.models

import cats.data.{Validated, ValidatedNel}
import com.monovore.decline._

sealed trait ModType {
  def abbreviation: String
  def friendlyName: String
}

object ModType {

  final case object AlternativeTextures extends ModType {
    override val abbreviation: String = "AT"
    override val friendlyName: String = "Alternative Textures"
  }
  final case object CustomFurniture extends ModType {
    override val abbreviation: String = "CF"
    override val friendlyName: String = "Custom Furniture"
  }
  final case object ContentPatcher extends ModType {
    override val abbreviation: String = "CP"
    override val friendlyName: String = "Content Patcher"
  }

  implicit val readModType: Argument[ModType] = new Argument[ModType] {

    override def read(string: String): ValidatedNel[String, ModType] =
      string.toUpperCase match {
        case AlternativeTextures.abbreviation =>
          Validated.valid(AlternativeTextures)
        case CustomFurniture.abbreviation =>
          Validated.valid(CustomFurniture)
        case ContentPatcher.abbreviation =>
          Validated.valid(ContentPatcher)
        case other =>
          Validated.invalidNel(s"$other is not a known mod type.")
      }

    override def defaultMetavar: String = "AT|CF|CP"
  }
}
