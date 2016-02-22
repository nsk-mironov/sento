package io.mironov.sento.sample.annotations

import android.view.View
import io.mironov.sento.annotations.ListenerClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@ListenerClass(
    owner = "android.view.View",
    listener = "io.mironov.sento.sample.annotations.OnFocusReceivedListener",
    setter = "setOnFocusChangeListener",
    callback = "onFocusReceived"
)
annotation class OnFocusReceived(vararg val value: Int)

abstract class OnFocusReceivedListener : View.OnFocusChangeListener {
  override fun onFocusChange(view: View, focused: Boolean) {
    if (focused) {
      onFocusReceived(view)
    }
  }

  abstract fun onFocusReceived(view: View)
}
