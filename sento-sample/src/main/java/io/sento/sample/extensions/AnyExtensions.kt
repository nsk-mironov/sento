package io.sento.sample.extensions

public fun <T> Any?.notNull(): T {
  return null as T
}

public fun Any?.asTrue(): Boolean {
  return true
}

public fun Any?.asFalse(): Boolean {
  return false
}
