package io.sento.compiler.common

import org.objectweb.asm.Type
import java.util.HashMap
import java.util.HashSet
import java.util.IdentityHashMap

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

  private val PRIMITIVES = HashMap<String, String>().apply {
    put("byte", "B")
    put("char", "C")
    put("double", "D")
    put("float", "F")
    put("int", "I")
    put("long", "J")
    put("short", "S")
    put("boolean", "Z")
    put("void", "V")
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
  public val VOID = Type.VOID_TYPE

  public val MAP = Type.getType(Map::class.java)
  public val IDENTITY_MAP = Type.getType(IdentityHashMap::class.java)

  public val VIEW = Type.getObjectType("android/view/View")
  public val RESOURCES = Type.getObjectType("android/content/res/Resources")

  public val BINDING = Type.getObjectType("io/sento/Binding")
  public val FINDER = Type.getObjectType("io/sento/Finder")
  public val FACTORY = Type.getObjectType("io/sento/SentoFactory")

  public inline fun <reified T : Any> get(): Type {
    return Type.getType(T::class.java)
  }

  public fun getArrayType(type: Type): Type {
    return Type.getType("[${type.descriptor}")
  }

  public fun getComponentTypeOrSelf(type: Type): Type {
    return if (type.sort == Type.ARRAY) {
      getComponentTypeOrSelf(type.elementType)
    } else {
      type
    }
  }

  public fun getClassFilePath(type: Type): String {
    return "${type.internalName}.class"
  }

  public fun isSystemClass(type: Type): Boolean {
    return type.className != null && arrayOf("android.", "java.", "kotlin.", "dalvik.").any {
      type.className.startsWith(it)
    }
  }

  public fun isPrimitive(type: Type): Boolean {
    return type in PRIMITIVE_TYPES
  }
}
