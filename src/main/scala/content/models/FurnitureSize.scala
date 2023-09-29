package quincyjo.stardew
package content.models

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
      width.toIntOption zip height.toIntOption map {
        case (w, h) =>
          Size(w, h)
      }
    case _ => None
  }
}
