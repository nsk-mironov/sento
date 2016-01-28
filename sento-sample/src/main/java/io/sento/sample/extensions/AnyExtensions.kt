package io.sento.sample.extensions

fun <T> Any?.notNull(): T {
  return null as T
}

fun Any?.asTrue(): Boolean {
  return true
}

fun Any?.asFalse(): Boolean {
  return false
}
