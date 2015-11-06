package io.sento.compiler.api

import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassRef
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.Type
import java.util.ArrayList

internal class ClassRegistry(
    public val classes: Collection<ClassSpec>,
    public val references: Collection<ClassRef>
) {
  private val lookupSpecs = classes.toMapBy {
    it.type
  }

  private val lookupRefs = references.toMapBy {
    it.type
  }

  public class Builder() {
    private val references = ArrayList<ClassRef>()
    private val classes = ArrayList<ClassSpec>()

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun reference(clazz: ClassRef): Builder = apply {
      references.add(clazz)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(classes, references)
    }
  }

  public fun spec(type: Type): ClassSpec? {
    return lookupSpecs[type]
  }

  public fun ref(type: Type): ClassRef? {
    return lookupRefs[type]
  }

  public fun isSubclassOf(child: Type, parent: Type): Boolean {
    if (child == Types.TYPE_OBJECT && parent != Types.TYPE_OBJECT) {
      return false
    }

    if (child == parent) {
      return true
    }

    if (lookupRefs[child] == null) {
      return false
    }

    return isSubclassOf(lookupRefs[child]!!.parent, parent)
  }
}
