name := "PlayApiTest"

version := "1.0"

scalaVersion := "2.11.7"

mainClass := Some("PlayApiTest")

libraryDependencies ++= Seq(
    "org.json4s"                  %%  "json4s-native"          % "3.2.11",
    "org.apache.httpcomponents"   %   "httpclient"             % "4.3.6"
)