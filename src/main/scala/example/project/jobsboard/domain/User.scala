package example.project.jobsboard.domain

import example.project.jobsboard.domain.User.Role
import java.util.UUID

final case class User(
  id: UUID,
  email: String, // TODO: Update with Email case class
  hashedPassword: String,
  firstName: Option[String],
  lastName: Option[String],
  company: Option[String],
  role: Role,
)

object User:
  enum Role:
    case ADMIN, RECRUITTER

  final case class New private (
    email: String,
    hashedPassword: String,
    firstName: Option[String],
    lastName: Option[String],
    company: Option[String],
    role: Role,
  )

  object New:
    def admin(
      email: String,
      hashedPassword: String,
      firstName: Option[String],
      lastName: Option[String],
      company: Option[String]
    ): New =
      New(email, hashedPassword, firstName, lastName, company, Role.ADMIN)

    def recruiter(
      email: String,
      hashedPassword: String,
      firstName: Option[String],
      lastName: Option[String],
      company: Option[String]
    ): New =
      New(email, hashedPassword, firstName, lastName, company, Role.RECRUITTER)
