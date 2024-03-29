package example.project.jobsboard.utils

final case class Pagination private (limit: Int, offset: Int)

object Pagination:
  private val DefaultLimit  = 10
  private val DefaultOffset = 0

  val Default = Pagination(DefaultLimit, DefaultOffset)

  def apply(limit: Option[Int], offset: Option[Int]): Pagination =
    new Pagination(limit.getOrElse(DefaultLimit), offset.getOrElse(DefaultOffset))
