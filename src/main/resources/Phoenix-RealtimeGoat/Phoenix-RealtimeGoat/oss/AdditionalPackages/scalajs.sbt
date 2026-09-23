
// TC_17: Scala.js %%% operator — parser must handle without error
enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  // TC_17: %%% Scala.js artifact operator
  "org.scala-js" %%% "scalajs-dom" % "2.1.0",
  "com.raquo"    %%% "laminar"     % "0.14.2"
)

// vulnerable dependency for SCA test coverage
libraryDependencies += "org.yaml" % "snakeyaml" % "1.26"
