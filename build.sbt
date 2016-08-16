name := "spot-price-data"

version := "0.1.0"

scalaVersion := "2.11.7"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.github.seratch" %% "awscala" % "0.5.+"
libraryDependencies += "codes.reactive" %% "scala-time" % "0.4.0-SNAPSHOT"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0.RC4"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"
libraryDependencies += "org.scalanlp" %% "breeze" % "0.11.2"
libraryDependencies += "org.scalaz" %% "scalaz-iteratee" % "7.2.0-M2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0-M15" % Test
