ThisBuild / scalaVersion := "3.8.4"

ThisBuild / scalacOptions ++= Seq(
  "-language:strictEquality",
  "-Yexplicit-nulls",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Werror"
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "mazesolver-imperative",
    libraryDependencies ++= Seq(
      "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
    )
  )
