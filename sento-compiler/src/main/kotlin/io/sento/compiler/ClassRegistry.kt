package io.sento.compiler

import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.model.ClassReference
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.HashMap

internal class ClassRegistry(
    public val references: Collection<ClassReference>,
    public val inputs: Collection<ClassReference>
) {
  private val refs = HashMap<Type, ClassReference>(references.size)
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
    private val references = ArrayList<ClassReference>()
    private val inputs = ArrayList<ClassReference>()

    public fun references(values: Collection<ClassReference>): Builder = apply {
      references.addAll(values)
    }

    public fun inputs(values: Collection<ClassReference>): Builder = apply {
      inputs.addAll(values)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(references, inputs)
    }
  }

  public fun resolve(reference: ClassReference, cacheable: Boolean = false): ClassSpec {
    return resolve(reference.type, cacheable)
  }

  public fun resolve(type: Type, cacheable: Boolean = false): ClassSpec {
    return if (cacheable) {
      specs.getOrPut(type) {
        refs.getOrImplicitDefault(type).resolve()
      }
    } else {
      specs.getOrElse(type) {
        refs.getOrImplicitDefault(type).resolve()
      }
    }
  }

  public fun isSubclassOf(type: Type, parent: Type): Boolean {
    if (type == Types.TYPE_OBJECT && parent != Types.TYPE_OBJECT) {
      return false
    }

    if (type == parent) {
      return true
    }

    if (refs[type] == null) {
      return false
    }

    return isSubclassOf(refs[type]!!.parent, parent)
  }

  public fun isInterface(type: Type): Boolean {
    return refs[type]?.access?.isInterface ?: false
  }
}
