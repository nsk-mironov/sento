package io.sento.compiler.common

import io.sento.Binding
import io.sento.Finder
import org.objectweb.asm.Type

internal object Types {
  public val TYPE_OBJECT = Type.getType(Any::class.java)

  public val TYPE_INT = Type.INT_TYPE
  public val TYPE_FLOAT = Type.FLOAT_TYPE
  public val TYPE_STRING = Type.getType(String::class.java)
  public val TYPE_CHAR_SEQUENCE = Type.getType(CharSequence::class.java)

  public val TYPE_VIEW = Type.getObjectType("android/view/View")
  public val TYPE_COLOR_STATE_LIST = Type.getObjectType("android/content/res/ColorStateList")
  public val TYPE_RESOURCES = Type.getObjectType("android/content/res/Resources")
  public val TYPE_DRAWABLE = Type.getObjectType("android/graphics/drawable/Drawable")

  public val TYPE_BINDING = Type.getType(Binding::class.java)
  public val TYPE_FINDER = Type.getType(Finder::class.java)

  public fun isSystemClass(type: Type): Boolean {
    return type.className != null && (type.className.startsWith("android.") || type.className.startsWith("java."))
  }
}
