package io.sento.compiler.common

import org.objectweb.asm.Type
import java.util.HashMap
import java.util.IdentityHashMap

internal object Types {
  private val PRIMITIVES = HashMap<String, String>().apply {
    put("byte", "B")
    put("char", "C")
    put("double", "D")
    put("float", "F")
    put("int", "I")
    put("long", "J")
    put("short", "S")
    put("boolean", "Z")
  }

  public val TYPE_OBJECT = Type.getType(Any::class.java)

  public val TYPE_MAP = Type.getType(Map::class.java)
  public val TYPE_IDENTITY_MAP = Type.getType(IdentityHashMap::class.java)

  public val TYPE_VIEW = Type.getObjectType("android/view/View")
  public val TYPE_RESOURCES = Type.getObjectType("android/content/res/Resources")

  public val TYPE_BINDING = Type.getObjectType("io/sento/Binding")
  public val TYPE_FINDER = Type.getObjectType("io/sento/Finder")
  public val TYPE_FACTORY = Type.getObjectType("io/sento/SentoFactory")

  public inline fun <reified T : Any> get(): Type {
    return Type.getType(T::class.java)
  }

  public fun getClassType(name: String): Type {
    val mapping = PRIMITIVES.withDefault {
      "L$it;"
    }

    return if (name.endsWith("[]")) {
      Type.getType("[${mapping.getOrImplicitDefault(name.substring(0, name.length - 2).replace('.', '/'))}")
    } else {
      Type.getType("${mapping.getOrImplicitDefault(name.replace('.', '/'))}")
    }
  }

  public fun isSystemClass(type: Type): Boolean {
    return type.className != null && arrayOf("android.", "java.", "kotlin.", "dalvik.").any {
      type.className.startsWith(it)
    }
  }
}
