package io.sento.compiler

import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassReference
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.HashMap

internal class ClassRegistry(
    public val references: Collection<ClassReference>,
    public val classes: Collection<ClassSpec>
) {
  private val refs = HashMap<Type, ClassReference>(references.size)
  private val specs = HashMap<Type, ClassSpec>(classes.size)

  init {
    references.forEach {
      refs.put(it.type, it)
    }

    classes.forEach {
      specs.put(it.type, it)
    }
  }

  public class Builder() {
    private val references = ArrayList<ClassReference>()
    private val classes = ArrayList<ClassSpec>()

    public fun reference(clazz: ClassReference): Builder = apply {
      references.add(clazz)
    }

    public fun references(values: Collection<ClassReference>): Builder = apply {
      references.addAll(values)
    }

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun specs(values: Collection<ClassSpec>): Builder = apply {
      classes.addAll(values)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(references, classes)
    }
  }

  public fun resolve(reference: ClassReference): ClassSpec {
    return resolve(reference.type)
  }

  public fun resolve(type: Type): ClassSpec {
    return specs.getOrPut(type) {
      refs.getOrImplicitDefault(type).resolve()
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
    return refs[type]?.isInterface ?: false
  }
}
