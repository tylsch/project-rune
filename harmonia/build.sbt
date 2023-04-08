import AkkaAppDependencies._

inThisBuild(
  List(
    organization := "com.rune.harmonia"
  )
)

lazy val exchange = (project in file("exchange"))
  .settings(
    name := "harmonia-exchange",
    scalaVersion := "2.13.10",
    version := "0.1.0-SNAPSHOT",
    Compile / unmanagedResourceDirectories += sourceDirectory.value / "protobuf"
  )

lazy val cart =
  appModule("harmonia-cart", "cart")
    .dependsOn(exchange)
