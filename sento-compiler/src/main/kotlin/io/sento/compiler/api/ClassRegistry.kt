package io.sento.compiler.api

import io.sento.compiler.model.ClassRef
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.Type
import java.util.ArrayList

internal class ClassRegistry(
    public val classes: Collection<ClassSpec>,
    public val references: Collection<ClassRef>
) {
  private val lookup = classes.toMapBy {
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

  public fun lookup(type: Type): ClassSpec? {
    return lookup[type]
  }

  public fun isSubclassOf(child: Type, parent: Type): Boolean {
    return child == parent
  }
}
