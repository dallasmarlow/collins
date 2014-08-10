package util

import scala.collection.immutable.SortedSet

import util.power.PowerComponent
import util.power.PowerUnit

package object power {
  type PowerComponents = Set[PowerComponent]
  type PowerUnits = SortedSet[PowerUnit]
}
