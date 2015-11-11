package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal class BindArrayBindingGenerator : FieldBindingGenerator {
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

    if (field.type.sort != Type.ARRAY) {
      environment.fatal("@BindArray should be used only with arrays")
    }

    when (field.type.elementType) {
      Types.TYPE_INT -> {
        adapter.invokeVirtual(Types.TYPE_RESOURCES, Method.getMethod("int[] getIntArray(int)"))
        adapter.putField(clazz.type, field.name, field.type)
      }

      Types.TYPE_STRING -> {
        adapter.invokeVirtual(Types.TYPE_RESOURCES, Method.getMethod("String[] getStringArray(int)"))
        adapter.putField(clazz.type, field.name, field.type)
      }

      Types.TYPE_CHAR_SEQUENCE -> {
        adapter.invokeVirtual(Types.TYPE_RESOURCES, Method.getMethod("CharSequence[] getTextArray(int)"))
        adapter.putField(clazz.type, field.name, field.type)
      }

      else -> {
        environment.fatal("Unsupported filed type \"${field.type.className}\" for @BindArray")
      }
    }

    return emptyList()
  }
}
