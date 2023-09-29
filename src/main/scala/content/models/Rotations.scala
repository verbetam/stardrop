package quincyjo.stardew
package content.models

sealed trait Rotations {

  def value: Int

  override final def toString: String = value.toString
}
object Rotations {
  final case object One extends Rotations { val value = 1 }
  final case object Two extends Rotations { val value = 2 }
  final case object Four extends Rotations { val value = 4 }

  def fromInt(int: Int): Option[Rotations] = int match {
    case 1 => Some(One)
    case 2 => Some(Two)
    case 4 => Some(Four)
    case _ => None
  }

  def fromString(string: String): Option[Rotations] =
    string.toIntOption.flatMap(fromInt)
}
