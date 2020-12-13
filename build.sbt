import sbtassembly.MergeStrategy

name := "SparkTransactions"

version := "0.1"

scalaVersion := "2.11.12"


val sparkVersion = "2.3.4"
lazy val excludeJpountz = ExclusionRule(organization = "net.jpountz.lz4", name = "lz4")
libraryDependencies += "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % sparkVersion  excludeAll(excludeJpountz)  // add more exclusions here
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % sparkVersion   excludeAll(excludeJpountz) // add more exclusions here
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.2.1"
libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.2.1"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.2.1"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion


dependencyOverrides ++= {
  Seq(
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.1",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.1"
  )
}


assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
  case x => (assemblyMergeStrategy in assembly).value(x)
}

