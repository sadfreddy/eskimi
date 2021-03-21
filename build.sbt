name := "Eskimi"

version := "0.1"

scalaVersion := "2.13.5"

val akkaVersion      = "2.6.8"
val akkaHttpVersion  = "10.2.4"
val scalaTestVersion = "3.2.5"
val monocleVersion   = "2.1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream"              % akkaVersion,
  "com.typesafe.akka"          %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-spray-json"     % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-testkit"        % akkaHttpVersion  % Test,
  "com.typesafe.akka"          %% "akka-actor-testkit-typed" % akkaVersion      % Test,
  "com.typesafe.akka"          %% "akka-testkit"             % akkaVersion      % Test,
  "org.scalatest"              %% "scalatest"                % scalaTestVersion % Test,
  "com.github.julien-truffaut" %% "monocle-macro"            % monocleVersion
)