package io.sento.compiler

import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassReference
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.Type
import java.util.ArrayList

internal class ClassRegistry(
    public val classes: Collection<ClassSpec>,
    public val references: Collection<ClassReference>
) {
  private val lookupSpecs = classes.toMapBy {
    it.type
  }

  private val lookupRefs = references.toMapBy {
    it.type
  }

  public class Builder() {
    private val references = ArrayList<ClassReference>()
    private val classes = ArrayList<ClassSpec>()

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun reference(clazz: ClassReference): Builder = apply {
      references.add(clazz)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(classes, references)
    }
  }

  public fun spec(type: Type): ClassSpec? {
    return lookupSpecs[type]
  }

  public fun reference(type: Type): ClassReference? {
    return lookupRefs[type]
  }

  public fun isSubclassOf(type: Type, parent: Type): Boolean {
    if (type == Types.TYPE_OBJECT && parent != Types.TYPE_OBJECT) {
      return false
    }

    if (type == parent) {
      return true
    }

    if (lookupRefs[type] == null) {
      return false
    }

    return isSubclassOf(lookupRefs[type]!!.parent, parent)
  }

  public fun isInterface(type: Type): Boolean {
    return lookupRefs[type]?.isInterface ?: false
  }
}
