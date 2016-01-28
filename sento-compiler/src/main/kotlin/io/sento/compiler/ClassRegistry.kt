package io.sento.compiler

import io.sento.compiler.annotations.ListenerClass
import io.sento.compiler.common.Types
import io.sento.compiler.common.isPublic
import io.sento.compiler.model.ListenerClassSpec
import io.sento.compiler.reflect.AnnotationSpec
import io.sento.compiler.reflect.ClassReference
import io.sento.compiler.reflect.ClassSpec
import io.sento.compiler.reflect.MethodSpec
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashSet

internal class ClassRegistry(
    val references: Collection<ClassReference>,
    val inputs: Collection<ClassReference>
) {
  private val refs = HashMap<Type, ClassReference>(references.size)
  private val listeners = HashMap<Type, ListenerClassSpec?>()
  private val specs = HashMap<Type, ClassSpec>()

  init {
    references.forEach {
      refs.put(it.type, it)
    }

    inputs.forEach {
      refs.put(it.type, it)
    }
  }

  internal class Builder() {
    private val references = LinkedHashSet<ClassReference>()
    private val inputs = LinkedHashSet<ClassReference>()

    fun references(values: Collection<ClassReference>): Builder = apply {
      references.addAll(values)
    }

    fun inputs(values: Collection<ClassReference>): Builder = apply {
      references.addAll(values)
      inputs.addAll(values)
    }

    fun build(): ClassRegistry {
      return ClassRegistry(references, inputs)
    }
  }

  fun contains(type: Type): Boolean {
    return type in refs
  }

  fun reference(type: Type): ClassReference {
    return refs[type] ?: throw SentoException("Unable to find a class \"${type.className}\". Make sure it is present in application classpath.")
  }

  fun resolve(reference: ClassReference, cacheable: Boolean = true): ClassSpec {
    return resolve(reference.type, cacheable)
  }

  fun resolve(type: Type, cacheable: Boolean = true): ClassSpec {
    return if (cacheable) {
      specs.getOrPut(type) {
        reference(type).resolve()
      }
    } else {
      specs.getOrElse(type) {
        reference(type).resolve()
      }
    }
  }

  fun resolveListenerClassSpec(annotation: AnnotationSpec): ListenerClassSpec? {
    return listeners.getOrPut(annotation.type) {
      val spec = resolve(annotation.type)
      val listener = resolve(annotation.type).getAnnotation<ListenerClass>()

      listener?.let {
        ListenerClassSpec.create(spec, it, this)
      }
    }
  }

  fun isSubclassOf(type: Type, parent: Type): Boolean {
    if (type.sort == Type.METHOD) {
      throw SentoException("Invalid argument type = $type. Types with ''sort'' == Type.METHOD are not allowed.")
    }

    if (parent.sort == Type.METHOD) {
      throw SentoException("Invalid argument parent = $parent. Types with ''sort'' == Type.METHOD are not allowed.")
    }

    if (type == parent) {
      return true
    }

    if (Types.isPrimitive(type) || Types.isPrimitive(parent)) {
      return type == parent
    }

    if (type == Types.OBJECT) {
      return parent == Types.OBJECT
    }

    if (parent == Types.OBJECT) {
      return true
    }

    if (type.sort == Type.ARRAY && parent.sort == Type.ARRAY) {
      return isSubclassOf(type.elementType, parent.elementType)
    }

    if (type.sort == Type.ARRAY || parent.sort == Type.ARRAY) {
      return false
    }

    reference(type).interfaces.forEach {
      if (isSubclassOf(it, parent)) {
        return true
      }
    }

    return isSubclassOf(reference(type).parent, parent)
  }

  fun isCastableFromTo(type: Type, target: Type): Boolean {
    return isSubclassOf(type, target) || isSubclassOf(target, type)
  }

  fun listPublicMethods(clazz: ClassSpec): Collection<MethodSpec> {
    val result = ArrayList<MethodSpec>()

    clazz.interfaces.forEach {
      result.addAll(listPublicMethods(resolve(it)).filter {
        clazz.getDeclaredMethod(it.name, it.type.descriptor) == null
      })
    }

    if (clazz.type != Types.OBJECT) {
      result.addAll(listPublicMethods(resolve(clazz.parent)).filter {
        clazz.getDeclaredMethod(it.name, it.type.descriptor) == null
      })
    }

    return clazz.methods.filterTo(result) {
      it.isPublic
    }
  }
}
