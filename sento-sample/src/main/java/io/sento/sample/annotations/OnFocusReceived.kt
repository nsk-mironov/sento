package io.sento.sample.annotations

import android.view.View
import io.sento.annotations.ListenerBinding

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@ListenerBinding(
    owner = View::class,
    listener = OnFocusReceivedListener::class,
    setter = "setOnFocusChangeListener"
)
public annotation class OnFocusReceived(vararg val value: Int)

public abstract class OnFocusReceivedListener : View.OnFocusChangeListener {
  override fun onFocusChange(view: View, focused: Boolean) {
    if (focused) {
      onFocusReceived(view)
    }
  }

  abstract fun onFocusReceived(view: View)
}
