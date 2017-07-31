name := "MOPNews"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= {
  val akkaStreamVersion = "2.5.3"
  val akkaVersion = "2.5.3"
  val slickVersion = "3.2.1"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http" % "10.0.9",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.9",
    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.7",
    "org.postgresql" %  "postgresql" % "9.4-1201-jdbc41",
    "joda-time" % "joda-time" % "2.9.9"
  )
}