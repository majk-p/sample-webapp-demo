package example

import cats.effect.IO
import cats.implicits._
import sttp.model.Uri

object ConfigReader {

  val appIdEnvVariable = "APP_ID"
  val appSecretEnvVariable = "APP_SECRET"

  private def readFromEnv(variableName: String): IO[String] =
    IO.delay(sys.env(variableName))

  private def readOAuth2Config: IO[Config.OAuth2] = for {
    id     <- readFromEnv(appIdEnvVariable)
    secret <- readFromEnv(appSecretEnvVariable)
    url = Uri.unsafeParse("https://github.com/")
  } yield Config.OAuth2(url, id, secret)

  private val serverConfig = Config.Server("localhost", 8080)

  def read: IO[Config] = for {
    oAuth2Config <- readOAuth2Config
  } yield Config(
    oAuth2Config,
    serverConfig
  )

}
