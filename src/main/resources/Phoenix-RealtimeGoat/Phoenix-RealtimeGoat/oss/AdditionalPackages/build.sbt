
// TC_12: build.sbt detection — PackageManager should be 'sbt'
// TC_18: val / lazy val version resolution
name := "vuln-scala-app"
organization := "com.example"
scalaVersion := "2.13.8"

// TC_18: val version variables — parser must resolve these
val akkaVersion        = "2.6.20"
val akkaHttpVersion    = "10.2.9"
lazy val log4jVersion  = "2.14.1"    // CVE-2021-44228
lazy val jacksonVersion = "2.12.3"   // CVE-2020-36518
val snakeYamlVersion   = "1.29"      // CVE-2022-1471
val xstreamVersion     = "1.4.15"    // CVE-2021-29505
val shiroVersion       = "1.7.0"     // CVE-2020-17523
val hibernateVersion   = "5.4.32.Final" // CVE-2019-14900
val nettyVersion       = "4.1.59.Final" // CVE-2021-21290
val commonsTextVersion = "1.9"       // CVE-2022-42889

// TC_19: libraryDependencies using Seq block
// TC_15: % operator (Java artifact)
// TC_16: %% operator (Scala artifact)
libraryDependencies ++= Seq(
  // TC_16: Scala %% operator
  "com.typesafe.akka"  %% "akka-actor"         % akkaVersion,
  "com.typesafe.akka"  %% "akka-http"           % akkaHttpVersion,
  "com.typesafe.akka"  %% "akka-stream"         % akkaVersion,
  "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,

  // TC_15: Java % operator
  "org.apache.logging.log4j"  % "log4j-core"     % log4jVersion,   // CVE-2021-44228
  "org.apache.logging.log4j"  % "log4j-api"      % log4jVersion,
  "org.slf4j"                 % "slf4j-api"       % "1.7.36",
  "ch.qos.logback"            % "logback-classic" % "1.2.10",       // CVE-2021-42550

  // More TC_15 Java deps
  "com.fasterxml.jackson.core" % "jackson-databind"     % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-core"         % jacksonVersion,
  "org.yaml"                   % "snakeyaml"            % snakeYamlVersion,
  "com.thoughtworks.xstream"   % "xstream"              % xstreamVersion,
  "org.apache.shiro"           % "shiro-core"           % shiroVersion,
  "io.netty"                   % "netty-all"            % nettyVersion,
  "org.hibernate"              % "hibernate-core"       % hibernateVersion,
  "org.apache.commons"         % "commons-text"         % commonsTextVersion,
  "commons-collections"        % "commons-collections"  % "3.2.1",   // CVE-2015-6420
  "com.google.guava"           % "guava"                % "29.0-jre",
  "com.h2database"             % "h2"                   % "1.4.200",  // CVE-2021-42392
  "org.bouncycastle"           % "bcprov-jdk15on"       % "1.64",     // CVE-2020-15522
  "io.jsonwebtoken"            % "jjwt"                 % "0.9.1",    // CVE-2022-21449
  "com.alibaba"                % "fastjson"             % "1.2.62",   // CVE-2019-17558
  "net.minidev"                % "json-smart"           % "2.3",      // CVE-2021-31684
  "xerces"                     % "xercesImpl"           % "2.12.0",
  "org.apache.velocity"        % "velocity"             % "1.7",      // CVE-2020-13936
  "org.apache.struts"          % "struts2-core"         % "2.5.26",   // CVE-2021-31805

  // TC_21: dependency modifiers — exclude(), withSources(), classifier() stripped by parser
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
    exclude("com.fasterxml.jackson.core", "jackson-core"),
  "org.apache.commons" % "commons-compress" % "1.20"
    withSources()
    withJavadoc(),
  "com.nimbusds" % "nimbus-jose-jwt" % "8.20"
    classifier "tests",

  // Spray JSON for Akka HTTP
  "io.spray" %% "spray-json" % "1.3.6",

  // Test
  "org.scalatest"  %% "scalatest"  % "3.2.11" % Test,
  "junit"           % "junit"      % "4.13.1" % Test   // CVE-2020-15250
)

// TC_21: addSbtPlugin-like intransitive modifier
"org.apache.commons" % "commons-lang3" % "3.9" intransitive()

enablePlugins(JavaAppPackaging)
