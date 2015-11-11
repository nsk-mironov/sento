package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import org.objectweb.asm.commons.Method

internal class BindDimenBindingGenerator : FieldBindingGenerator {
  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val adapter = context.adapter
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    adapter.loadArg(context.variable("target"))
    adapter.loadArg(context.variable("finder"))
    adapter.loadArg(context.variable("source"))

    adapter.invokeInterface(Types.TYPE_FINDER, Method.getMethod("android.content.res.Resources resources(Object))"))
    adapter.push(Annotations.id(annotation))

    when (field.type) {
      Types.TYPE_FLOAT -> {
        adapter.invokeVirtual(Types.TYPE_RESOURCES, Method.getMethod("float getDimension(int)"))
        adapter.putField(clazz.type, field.name, field.type)
      }

      Types.TYPE_INT -> {
        adapter.invokeVirtual(Types.TYPE_RESOURCES, Method.getMethod("int getDimensionPixelSize(int)"))
        adapter.putField(clazz.type, field.name, field.type)
      }

      else -> {
        environment.fatal("Unsupported type \"${field.type.className}\" for @BindDimen")
      }
    }

    return emptyList()
  }
}
