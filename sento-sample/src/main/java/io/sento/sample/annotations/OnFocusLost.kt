package io.sento.sample.annotations

import android.view.View
import io.sento.annotations.ListenerClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@ListenerClass(
    owner = "android.view.View",
    listener = "io.sento.sample.annotations.OnFocusLostListener",
    setter = "setOnFocusChangeListener",
    callback = "onFocusLost"
)
annotation class OnFocusLost(vararg val value: Int)

abstract class OnFocusLostListener : View.OnFocusChangeListener {
  override fun onFocusChange(view: View, focused: Boolean) {
    if (!focused) {
      onFocusLost(view)
    }
  }

  abstract fun onFocusLost(view: View)
}
