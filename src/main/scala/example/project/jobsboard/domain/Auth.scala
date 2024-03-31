package example.project.jobsboard.domain

object Auth:
  final case class LoginInfo(email: String, password: String)

  final case class NewPasswordInfo(oldPassword: String, newPassword: String)
