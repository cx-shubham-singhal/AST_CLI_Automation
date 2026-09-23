
// TC_13: plugins.sbt detection
// TC_20: addSbtPlugin() parsing — parser must detect these
addSbtPlugin("com.typesafe.sbt"   % "sbt-native-packager" % "1.9.9")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"        % "2.4.6")
addSbtPlugin("com.github.sbt"     % "sbt-release"         % "1.1.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"       % "1.9.3")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"        % "1.2.0")
addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph" % "0.10.0")

// vulnerable dependencies for SCA test coverage
libraryDependencies += "log4j" % "log4j" % "1.2.17"
libraryDependencies += "commons-collections" % "commons-collections" % "3.2.1"
