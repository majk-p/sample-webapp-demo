import sbt._

object Versions {
  val Cats = "2.3.0"
  val CatsEffect = "2.5.1"
  val Circe = "0.14.1"
  val Tapir = "0.18.0-M15"
  val Sttp = "3.3.6"
  val SttpOAuth2 = "0.10.0"
  val GithubGraphQL = "0.9.5-2"
}

object Dependencies {

  private val cats = Seq(
    "org.typelevel" %% "cats-core" % Versions.Cats
  )

  private val catsEffect = Seq(
    "org.typelevel" %% "cats-effect" % Versions.CatsEffect
  )

  private val circe = Seq(
    "io.circe" %% "circe-generic" % Versions.Circe
  )

  private val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core" % Versions.Tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.Tapir 
  )

  private val sttp = Seq(
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats-ce2" % Versions.Sttp
  )

  private val sttpOAuth2 = Seq(
    "com.ocadotechnology" %% "sttp-oauth2" % Versions.SttpOAuth2
  )

  val appDependencies = 
    cats ++ catsEffect ++ circe ++ tapir ++ sttp ++ sttpOAuth2
}
