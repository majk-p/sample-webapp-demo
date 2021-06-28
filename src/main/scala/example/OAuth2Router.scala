package example

import cats.effect.IO
import com.ocadotechnology.sttp.oauth2.AuthorizationCodeProvider
import com.ocadotechnology.sttp.oauth2.OAuth2TokenResponse
import sttp.model.Uri

import OAuthEndpoints._

trait OAuth2Router {
  def loginRedirect: IO[RedirectUrl]
  def handleLogin(code: AuthorizationCode, state: State): IO[String]
}

object OAuth2Router {

  def instance(
    authorizationCodeProvider: AuthorizationCodeProvider[Uri, IO],
    github: Github
  ): OAuth2Router = new OAuth2Router {

    override def loginRedirect: IO[RedirectUrl] = {
      val uri = authorizationCodeProvider.loginLink()
      val redirectUrl = RedirectUrl(uri.toString())
      IO.pure(redirectUrl)
    }

    override def handleLogin(code: AuthorizationCode, state: State): IO[String] =
      for {
        token    <- authorizationCodeProvider
                      .authCodeToToken[OAuth2TokenResponse](code.value)
        userInfo <- github.userInfo(token.accessToken)
      } yield s"Logged in as $userInfo"

  }

}
