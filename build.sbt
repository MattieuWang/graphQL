name := """graphqlTest"""

lazy val commonSettings = Seq(
  organization := "com.graphqlTest",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.3",
  libraryDependencies ++= (default ++ deps_db)
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .aggregate(user, experience, enterprise)
  .dependsOn(user, experience, enterprise)

val `user` = (project in file("modules/user"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)

val `experience` = (project in file("modules/experience"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .dependsOn(user)

val enterprise = (project in file("modules/enterprise"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .dependsOn(user)

lazy val default = Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

lazy val deps_db = Seq(
  evolutions,
  "org.sangria-graphql" %% "sangria-play-json" % "2.0.1",
  "org.sangria-graphql" %% "sangria" % "2.0.0",
  "org.sangria-graphql" %% "sangria-slowlog" % "2.0.0-M1",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "org.postgresql" % "postgresql" % "42.2.16",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
)
