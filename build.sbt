val sparkVersion = "3.0.1"

name := "spark-pipelines"
publishArtifact := false  // prevent publishing of root project
scalaVersion := "2.12.10"

lazy val commonSettings = Seq(
  scalaVersion := "2.12.10",

  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-Xlint:-unused"
  ),

  //Switch off unnecessary dependency security checks
  dependencyCheckNuspecAnalyzerEnabled := Option(false),
  dependencyCheckAssemblyAnalyzerEnabled := Option(false),

  libraryDependencies ++= Seq(

    // Keep this using the same versions as in scala-common. This is a temporary
    // workaround for our fat jars.

    "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.11.0-RC1",
    "com.google.protobuf" % "protobuf-java" % "3.7.1" % "provided",

    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.scalamock" %% "scalamock" % "4.1.0" % Test,

    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided",

    "com.databricks" %% "dbutils-api" % "0.0.5" % "provided",
    "io.delta" %% "delta-core" % "0.7.0" % "provided",
    "com.bugsnag" % "bugsnag" % "3.5.1"
  ),

  assemblyMergeStrategy in assembly := {
    case PathList("google","protobuf", xs @ _*) => MergeStrategy.first
    case PathList("mozilla","public-suffix-list.txt") => MergeStrategy.first
    case PathList("org","apache","httpclient", xs @ _*) => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },

  dependencyOverrides ++= Seq(
    "org.scala-lang" % "scala-library" % scalaVersion.value
  ),

  isSnapshot := !version.value.matches("^\\d+\\.\\d+\\.\\d+$"),

  printArtifactName := println((artifactPath in (Compile, packageBin)).value.getName),

  // Publish assembly
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.withClassifier(Some("assembly"))
  },

  // Skip tests on assembly
  test in assembly := {},

  // Add timing output to each test
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),

  // Increase memory allocated to the JVM
  fork in Test := true,
  javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:+CMSClassUnloadingEnabled"),

  // Because holden (https://github.com/holdenk/spark-testing-base#special-considerations) says so:
  parallelExecution in Test := false,

  // Uncomment to allow tests to wait for input (helpful for debugging Spark test app)
  connectInput in Test := true,

  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
)

lazy val printArtifactName = taskKey[Unit]("Get the artifact name")

//make sure compilation is dependent on style checks

lazy val pipelines = (project in file("modules/pipelines"))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    commonSettings,
    name := "spark-pipelines-pipelines",
    addArtifact(artifact in (Compile, assembly), assembly)
  )
