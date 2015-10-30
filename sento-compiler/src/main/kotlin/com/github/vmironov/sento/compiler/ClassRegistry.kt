package com.github.vmironov.sento.compiler

import com.github.vmironov.sento.compiler.specs.ClassSpec
import java.util.ArrayList

internal class ClassRegistry(val classes: List<ClassSpec>) {
  public class Builder() {
    private val classes = ArrayList<ClassSpec>()

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun build(): ClassRegistry {
      return ClassRegistry(classes)
    }
  }
}
