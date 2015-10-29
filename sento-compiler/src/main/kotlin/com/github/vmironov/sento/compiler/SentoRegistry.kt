package com.github.vmironov.sento.compiler

import com.github.vmironov.sento.compiler.specs.ClassSpec
import java.util.ArrayList

public class SentoRegistry(val classes: List<ClassSpec>) {
  public class Builder() {
    private val classes = ArrayList<ClassSpec>()

    public fun spec(clazz: ClassSpec): Builder = apply {
      classes.add(clazz)
    }

    public fun build(): SentoRegistry {
      return SentoRegistry(classes)
    }
  }
}
