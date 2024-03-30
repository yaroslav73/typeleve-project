package example.project.jobsboard.fixtures

import example.project.jobsboard.domain.User
import java.util.UUID

trait UserFixture {
  val NotFoundUserId = UUID.fromString("6ea79557-3112-4c84-a8f5-1d1e2c300948")

  val NotFoundUserEmail = "not_found_test@email.com"

  val john = User(
    id             = UUID.fromString("843df718-ec6e-4d49-9289-f799c0f40073"),
    email          = "john_test@email.com",
    hashedPassword = "hashedpassword",
    firstName      = Some("John"),
    lastName       = Some("Wick"),
    company        = Some("Continental"),
    role           = User.Role.ADMIN
  )

  val anna = User(
    id             = UUID.fromString("843df718-ec6e-4d49-9289-f799c0f40074"),
    email          = "anna_test@email.com",
    hashedPassword = "hashedpassword",
    firstName      = Some("Anna"),
    lastName       = Some("Belle"),
    company        = Some("Continental"),
    role           = User.Role.RECRUITTER
  )
}
