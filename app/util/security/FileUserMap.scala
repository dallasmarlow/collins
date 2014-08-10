package util.security

import scala.collection.immutable.MapProxy

import models.UserImpl

// This exists because a Guava cache loader is invariant in type K, this is an easy work around
case class FileUserMap(override val self: Map[String, UserImpl]) extends MapProxy[String, UserImpl]
