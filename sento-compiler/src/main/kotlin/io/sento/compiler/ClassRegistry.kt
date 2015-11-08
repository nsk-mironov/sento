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
  private val lookupReferences = references.toMapBy {
    it.type
  }

  private val lookupAnnotations = annotations.toMapBy {
    it.type
  }

  private val lookupSpecs = classes.toMapBy {
    it.type
  }

  public class Builder() {
    private val references = ArrayList<ClassReference>()

    private val annotations = ArrayList<ClassSpec>()
    private val classes = ArrayList<ClassSpec>()

    public fun reference(clazz: ClassReference): Builder = apply {
      references.add(clazz)
    }

    public fun references(values: Collection<ClassReference>): Builder = apply {
      references.addAll(values)
    }

    public fun annotation(clazz: ClassSpec): Builder = apply {
      annotations.add(clazz)
    }

    public fun annotations(values: Collection<ClassSpec>): Builder = apply {
      annotations.addAll(values)
    }

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun specs(values: Collection<ClassSpec>): Builder = apply {
      classes.addAll(values)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(references, annotations, classes)
    }
  }

  public fun reference(type: Type): ClassReference? {
    return lookupReferences[type]
  }

  public fun annotation(type: Type): ClassSpec? {
    return lookupAnnotations[type]
  }

  public fun spec(type: Type): ClassSpec? {
    return lookupSpecs[type]
  }

  public fun isSubclassOf(type: Type, parent: Type): Boolean {
    if (type == Types.TYPE_OBJECT && parent != Types.TYPE_OBJECT) {
      return false
    }

    if (type == parent) {
      return true
    }

    if (lookupReferences[type] == null) {
      return false
    }

    return isSubclassOf(lookupReferences[type]!!.parent, parent)
  }

  public fun isInterface(type: Type): Boolean {
    return lookupReferences[type]?.isInterface ?: false
  }
}
