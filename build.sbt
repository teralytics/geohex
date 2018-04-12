import scala.language.postfixOps

val mainScalaVersion = "2.12.1"

lazy val npmPublish = taskKey[Unit]("Publish NPM package")

val jsModuleWrapper =
  (
    """
      |(function (global, factory) {
      |  if (typeof define === "function" && define.amd) {
      |    define(["exports"], factory);
      |  } else if (typeof exports !== "undefined") {
      |    factory(exports);
      |  } else {
      |    var mod = {
      |      exports: {}
      |    };
      |    factory(mod.exports);
      |    global.terahex = mod.exports;
      |  }
      |})(this, function (exports) {
      |
      |  var scalaExports = {};
      |  var __ScalaJSEnv = { exportsNamespace: scalaExports };
    """.stripMargin,
    """
      |  var terahex = scalaExports.terahex();
      |  for (var key in terahex) { exports[key] = terahex[key] }
      |});""".stripMargin)

lazy val root = project.in(file(".")).
  aggregate(geohexJS, geohexJVM).
  settings(
    scalaVersion := mainScalaVersion,
    crossScalaVersions := Seq(mainScalaVersion, "2.11.8", "2.10.6"),
    publish := {},
    publishLocal := {}
  )

lazy val geohex = crossProject.in(file(".")).
  settings(
    organization := "net.teralytics",
    name := "geohex",
    version := "0.1." + sys.env.getOrElse("TRAVIS_BUILD_NUMBER", "0-SNAPSHOT"),
    scalaVersion := mainScalaVersion,
    crossScalaVersions := Seq("2.11.12"),
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "io.spray" %% "spray-json" % "1.3.3" % "test",
      "com.vividsolutions" % "jts" % "1.13" % "test",
      "org.apache.spark" %% "spark-sql" % "2.2.0"
    ),
    bintrayOrganization := Some("teralytics")
  ).
  jsSettings(
    scalaJSOutputWrapper := jsModuleWrapper,
    npmPublish := {
      "rm -rf npm-tar" #&&
        "mkdir npm-tar" #&&
        "npm install -g npm" #&&
        s"npm version ${version.value} --no-git-tag-version --force" #&&
        s"cp package.json README.md js/target/scala-${scalaBinaryVersion.value}/geohex-opt.js npm-tar" #&&
        "tar -cf npm.tar npm-tar" #&&
        "npm publish npm.tar" !
    }
  )

lazy val geohexJVM = geohex.jvm

lazy val geohexJS = geohex.js
