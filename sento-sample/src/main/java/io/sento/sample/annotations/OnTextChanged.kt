package io.sento.sample.annotations

import android.text.TextWatcher
import android.widget.TextView
import io.sento.annotations.ListenerClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@ListenerClass(
    owner = TextView::class,
    listener = TextWatcher::class,
    setter = "addTextChangedListener",
    unsetter = "removeTextChangedListener",
    callback = "onTextChanged"
)
public annotation class OnTextChanged(vararg val value: Int)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@ListenerClass(
    owner = TextView::class,
    listener = TextWatcher::class,
    setter = "addTextChangedListener",
    unsetter = "removeTextChangedListener",
    callback = "afterTextChanged"
)
public annotation class AfterTextChanged(vararg val value: Int)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@ListenerClass(
    owner = TextView::class,
    listener = TextWatcher::class,
    setter = "addTextChangedListener",
    unsetter = "removeTextChangedListener",
    callback = "beforeTextChanged"
)
public annotation class BeforeTextChanged(vararg val value: Int)
