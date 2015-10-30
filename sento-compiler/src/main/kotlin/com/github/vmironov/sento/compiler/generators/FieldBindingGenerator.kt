package com.github.vmironov.sento.compiler.generators

public interface FieldBindingGenerator<A : Annotation> {
  public fun bind(context: FieldBindingContext<A>)

  public fun unbind(context: FieldBindingContext<A>)
}
