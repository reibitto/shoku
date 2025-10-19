import sbt._
import sbt.Keys._
import sbtwelcome._

ThisBuild / scalaVersion := "2.13.17"
ThisBuild / version := "0.1.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "shoku",
    addCommandAlias("fmt", "all scalafmtSbt scalafmtAll"),
    addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll"),
    logo :=
      s"""
         |蝕 Shoku
         |
         |""".stripMargin,
    usefulTasks := Seq(
      UsefulTask("run", "Run Shoku (opens window)"),
      UsefulTask("~compile", "Compile with file-watch enabled"),
      UsefulTask("fmt", "Run scalafmt on the entire project"),
      UsefulTask("assembly", "Create an executable JAR")
    ),
    fork := true,
    run / baseDirectory := file("."),
    scalacOptions ++= Seq(
      "-Xsource:3",
      "-Ytasty-reader",
      "-Ymacro-annotations",
      "-Ypatmat-exhaust-depth",
      "50",
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      // "-Xcheckinit", // Uncomment this only when debugging initialization order issues since there is a heavy runtime cost for it
      // the backend can now run bytecode serialization, classfile writing and method-local optimizations (-opt:l:method)
      "-Ybackend-parallelism",
      "16",
      "-Wunnamed-boolean-literal"
    )
  )
