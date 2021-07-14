package example

import cats.effect._
import sttp.tapir._

import sttp.tapir.server.http4s.Http4sServerInterpreter.toRoutes
import org.http4s.HttpRoutes

import cats.syntax.all._

import scala.concurrent.ExecutionContext
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.syntax.kleisli._

import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.ocadotechnology.sttp.oauth2.AuthorizationCodeProvider
import sttp.client3.SttpBackend
import sttp.model.Uri
import com.ocadotechnology.sttp.oauth2.Secret

object Main extends IOApp {

  val helloWorld: Endpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  def endpointLogic(name: String) = IO(s"Hello, $name!".asRight[Unit])

  val helloWorldRoutes: HttpRoutes[IO] =
    toRoutes(helloWorld)(endpointLogic _)

  def serverResource(httpConfig: Config.Server, routes: HttpRoutes[IO])(ec: ExecutionContext) =
    BlazeServerBuilder[IO](ec)
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(Router("/" -> routes).orNotFound)
      .resource

  def authorizationCodeProviderInstance(
    config: Config
  )(
    implicit backend: SttpBackend[IO, Any]
  ): AuthorizationCodeProvider[Uri, IO] =
    AuthorizationCodeProvider.uriInstance[IO](
      baseUrl = config.oauth2.providerBaseUrl,
      redirectUri = Uri.unsafeParse(s"http://${config.server.host}:${config.server.port}/api/post-login"),
      clientId = config.oauth2.appId,
      clientSecret = Secret(config.oauth2.appSecret),
      pathsConfig = AuthorizationCodeProvider.Config.GitHub
    )

  def appRoutes(oAuth2Router: OAuth2Router): HttpRoutes[IO] =
    List(
      toRoutes(helloWorld)(endpointLogic _),
      toRoutes(OAuthEndpoints.loginRedirect)(_ => oAuth2Router.loginRedirect.map(_.asRight[Unit])),
      toRoutes(OAuthEndpoints.postLogin)(
        (oAuth2Router.handleLogin _).tupled.andThen(_.map(_.asRight[Unit]))
      )
    ).reduceLeft(_ <+> _)

  override def run(args: List[String]): IO[ExitCode] = for {
    sttpBackend <- AsyncHttpClientCatsBackend[IO]()
    appConfig   <- ConfigReader.read
    authorizationCodeProvider = authorizationCodeProviderInstance(appConfig)(sttpBackend)
    github = Github.instance(sttpBackend)
    oAuth2Router = OAuth2Router.instance(authorizationCodeProvider, github)
    routes = appRoutes(oAuth2Router)
    _           <- serverResource(appConfig.server, routes)(executionContext).use(_ => IO.never)
  } yield ExitCode.Success

}
