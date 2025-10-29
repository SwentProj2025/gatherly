package com.android.gatherly.model.map

/**
 * This is an interface to unite ToDos and Events by the fact that they both have an optional
 * Location. This is done to ease the displaying of the map
 */
interface DisplayedMapElement {
  val location: Location?
}
