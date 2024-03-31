package example.project.jobsboard.domain

import tsec.authentication.AugmentedJWT
import tsec.authentication.JWTAuthenticator
import tsec.mac.jca.HMACSHA256

object Aliases {
  type Crypto              = HMACSHA256
  type JwtToken            = AugmentedJWT[Crypto, String]
  type Authenticator[F[_]] = JWTAuthenticator[F, String, User, Crypto]
}
