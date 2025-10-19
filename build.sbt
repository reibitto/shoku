import sbt.Keys._
import sbt._
import sbtwelcome._

ThisBuild / scalaVersion := "2.13.17"
ThisBuild / version      := "0.1.0"

lazy val root = project
  .in(file("."))
  .settings(
    name                := "shoku",
    addCommandAlias("fmt", "all scalafmtSbt scalafmtAll"),
    addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll"),
    logo                :=
      s"""
         |蝕 Shoku
         |
         |""".stripMargin,
    usefulTasks         := Seq(
      UsefulTask("a", "run", "Run Shoku (opens window)"),
      UsefulTask("b", "~compile", "Compile with file-watch enabled"),
      UsefulTask("c", "fmt", "Run scalafmt on the entire project"),
      UsefulTask("d", "assembly", "Create an executable JAR")
    ),
    fork                := true,
    run / baseDirectory := file(".")
  )
