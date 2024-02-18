package example.project.jobsboard.http.validations

import example.project.jobsboard.domain.Job.JobInfo
import cats.data.ValidatedNel
import cats.syntax.validated.catsSyntaxValidatedId
import cats.syntax.apply.catsSyntaxTuple5Semigroupal
import example.project.jobsboard.http.validations.Validator.ValidationFailure
import example.project.jobsboard.http.validations.Validator.ValidationResult
import java.net.URL
import scala.util.Try
import scala.util.Failure
import scala.util.Success

trait Validator[A]:
  def validate(value: A): ValidationResult[A]

object Validator:
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  sealed trait ValidationFailure(val error: String)
  final case class EmptyField(fieldName: String) extends ValidationFailure(s"$fieldName is empty")
  final case class InvalidUrl(fieldName: String) extends ValidationFailure(s"$fieldName invalid URL")

  private def validateRequired[A](value: A, fieldName: String)(required: A => Boolean): ValidationResult[A] =
    if required(value) then value.validNel
    else EmptyField(fieldName).invalidNel

  private def validateUrl(value: String, fieldName: String): ValidationResult[String] =
    Try(URL(value).toURI()) match
      case Success(_) => value.validNel
      case Failure(_) => InvalidUrl(fieldName).invalidNel

  given jobInfoValidator: Validator[JobInfo] = (jobInfo: JobInfo) => {
    val JobInfo(
      company,
      title,
      description,
      externalUrl,
      location,
      remote,
      salary,
      currency,
      country,
      tags,
      image,
      seniority,
      other,
    ) = jobInfo

    val validCompany     = validateRequired(company, "company")(_.nonEmpty)
    val validTitle       = validateRequired(title, "title")(_.nonEmpty)
    val validDescription = validateRequired(description, "description")(_.nonEmpty)
    val validExternalUrl = validateUrl(externalUrl, "externalUrl")
    val validLocation    = validateRequired(location, "location")(_.nonEmpty)

    (validCompany, validTitle, validDescription, validExternalUrl, validLocation).mapN(
      (company, title, description, externalUrl, location) =>
        JobInfo(
          company,
          title,
          description,
          externalUrl,
          location,
          remote,
          salary,
          currency,
          country,
          tags,
          image,
          seniority,
          other,
        )
    )
  }
