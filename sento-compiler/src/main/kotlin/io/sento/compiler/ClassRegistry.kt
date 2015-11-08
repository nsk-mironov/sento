package io.sento.compiler

import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassReference
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.Type
import java.util.ArrayList

internal class ClassRegistry(
    public val references: Collection<ClassReference>,
    public val annotations: Collection<ClassSpec>,
    public val classes: Collection<ClassSpec>
) {
  private val lookupSpecs = classes.toMapBy {
    it.type
  }

  private val lookupRefs = references.toMapBy {
    it.type
  }

  public class Builder() {
    private val references = ArrayList<ClassReference>()

    private val annotations = ArrayList<ClassSpec>()
    private val classes = ArrayList<ClassSpec>()

    public fun reference(clazz: ClassReference): Builder = apply {
      references.add(clazz)
    }

    public fun annotation(clazz: ClassSpec): Builder = apply {
      annotations.add(clazz)
    }

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(references, annotations, classes)
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
