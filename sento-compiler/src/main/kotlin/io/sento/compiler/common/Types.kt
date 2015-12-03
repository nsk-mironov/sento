package io.sento.compiler.common

import io.sento.compiler.annotations.AnnotationDelegate
import org.objectweb.asm.Type
import java.util.HashSet
import java.util.IdentityHashMap
import kotlin.jvm.internal.KotlinClass

internal object Types {
  private val PRIMITIVE_TYPES = HashSet<Type>().apply {
    add(Type.BYTE_TYPE)
    add(Type.CHAR_TYPE)
    add(Type.DOUBLE_TYPE)
    add(Type.FLOAT_TYPE)
    add(Type.INT_TYPE)
    add(Type.LONG_TYPE)
    add(Type.SHORT_TYPE)
    add(Type.BOOLEAN_TYPE)
    add(Type.VOID_TYPE)
  }

  public val OBJECT = Type.getType(Any::class.java)
  public val CLASS = Type.getType(Class::class.java)

  public val BYTE = Type.BYTE_TYPE
  public val CHAR = Type.CHAR_TYPE
  public val DOUBLE = Type.DOUBLE_TYPE
  public val FLOAT = Type.FLOAT_TYPE
  public val INT = Type.INT_TYPE
  public val LONG = Type.LONG_TYPE
  public val SHORT = Type.SHORT_TYPE
  public val BOOLEAN = Type.BOOLEAN_TYPE
  public val STRING = Type.getType(String::class.java)
  public val VOID = Type.VOID_TYPE

  public val MAP = Type.getType(Map::class.java)
  public val IDENTITY_MAP = Type.getType(IdentityHashMap::class.java)
  public val VIEW = Type.getObjectType("android/view/View")

  public val BINDING = Type.getObjectType("io/sento/Binding")
  public val FINDER = Type.getObjectType("io/sento/Finder")
  public val FACTORY = Type.getObjectType("io/sento/SentoFactory")
  public val OPTIONAL = Type.getObjectType("io/sento/annotations/Optional")

  public inline fun <reified T : Any> get(): Type {
    return Type.getType(T::class.java)
  }

  public fun getAnnotationType(clazz: Class<*>): Type {
    return clazz.getAnnotation(AnnotationDelegate::class.java)?.run {
      Type.getObjectType(value.replace('.', '/'))
    } ?: Type.getType(clazz)
  }

  public fun getClassFilePath(type: Type): String {
    return "${type.internalName}.class"
  }

  public fun isSystemClass(type: Type): Boolean {
    val name = type.className

    if (name.endsWith(".Nullable")) {
      return false
    }

    if (name.endsWith(".NotNull")) {
      return false
    }

    if (name == KotlinClass::class.qualifiedName) {
      return false
    }

    return arrayOf("android.", "java.", "kotlin.", "dalvik.").any {
      name.startsWith(it)
    }
  }

  public fun isPrimitive(type: Type): Boolean {
    return type in PRIMITIVE_TYPES
  }
}
