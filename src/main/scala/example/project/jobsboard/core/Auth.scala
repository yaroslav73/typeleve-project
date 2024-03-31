package example.project.jobsboard.core

import tsec.authentication.AugmentedJWT
import tsec.mac.jca.HMACSHA256
import example.project.jobsboard.domain.Auth.NewPasswordInfo
import example.project.jobsboard.domain.User
import example.project.jobsboard.domain.User.New
import example.project.jobsboard.domain.Aliases.JwtToken
import example.project.jobsboard.domain.Aliases.Authenticator

trait Auth[F[_]]:
  def login(email: String, password: String): F[Option[JwtToken]]
  def signUp(user: User.New): F[Option[User]]
  def changePassword(email: String, newPassword: NewPasswordInfo): F[Either[String, Option[User]]]

object Auth:
  def make[F[_]](users: Users[F], authenticator: Authenticator[F]): Auth[F] = new Auth[F] {
    def login(email: String, password: String): F[Option[JwtToken]] = ???

    def signUp(user: New): F[Option[User]] = ???

    def changePassword(email: String, newPassword: NewPasswordInfo): F[Either[String, Option[User]]] = ???
  }
