import scala.language.postfixOps

val mainScalaVersion = "2.11.7"

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
  aggregate(geohexJS, geohexJVM, geohexJts, geohexTesting).
  settings(
    scalaVersion := mainScalaVersion,
    crossScalaVersions := Seq(mainScalaVersion, "2.10.5"),
    publish := {},
    publishLocal := {}
  )

val commonSettings = Seq(
  organization := "net.teralytics",
  version := "0.1." + sys.env.getOrElse("TRAVIS_BUILD_NUMBER", "0-SNAPSHOT"),
  scalaVersion := mainScalaVersion,
  licenses +=("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("teralytics")
)

val libs = new {

  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4"

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.12.5"

  val testingLibs = Seq(
    scalaTest % "test",
    scalaCheck % "test")

  val json = "io.spray" %% "spray-json" % "1.3.2"

  val jts = "com.vividsolutions" % "jts" % "1.13"
}

lazy val geohex = crossProject.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "geohex"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      libs.json % "test",
      libs.jts % "test"),
    libraryDependencies ++= libs.testingLibs
  )
  .jsSettings(
    scalaJSOutputWrapper := jsModuleWrapper,
    npmPublish := {
      "rm -rf npm-tar" #&&
        "mkdir npm-tar" #&&
        "npm install -g npm" #&&
        s"npm version ${version.value} --no-git-tag-version --force" #&&
        "cp package.json README.md js/target/scala-2.11/geohex-opt.js npm-tar" #&&
        "tar -cf npm.tar npm-tar" #&&
        "npm publish npm.tar" !
    }
  )

lazy val geohexJVM = geohex.jvm

lazy val geohexJS = geohex.js

lazy val geohexJts = project.in(file("geohex-jts"))
  .dependsOn(geohexJVM)
  .settings(commonSettings: _*)
  .settings(
    name := "geohex-jts",
    libraryDependencies ++= Seq(
      libs.jts)
  )

lazy val geohexTesting = project.in(file("geohex-testing"))
  .dependsOn(geohexJts)
  .settings(commonSettings: _*)
  .settings(
    name := "geohex-testing",
    libraryDependencies ++= Seq(
      libs.scalaTest,
      libs.scalaCheck)
  )