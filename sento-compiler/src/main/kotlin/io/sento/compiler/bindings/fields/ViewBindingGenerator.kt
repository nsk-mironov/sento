package io.sento.compiler.bindings.fields

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import org.objectweb.asm.commons.Method

internal class ViewBindingGenerator : FieldBindingGenerator {
  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val adapter = context.adapter
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    val optional = field.getAnnotation<Optional>() != null
    val isInterface = environment.registry.isInterface(field.type)
    val isView = environment.registry.isSubclassOf(field.type, Types.TYPE_VIEW)

    if (!isInterface && !isView) {
      throw RuntimeException("${field.type.className} isn't a subclass of ${Types.TYPE_VIEW.className}")
    }

    adapter.loadArg(context.variable("target"))
    adapter.loadArg(context.variable("finder"))
    adapter.push(Annotations.id(annotation))
    adapter.loadArg(context.variable("source"))
    adapter.push(optional)

    adapter.invokeInterface(Types.TYPE_FINDER, Method.getMethod("android.view.View find (int, Object, boolean)"))
    adapter.checkCast(field.type)
    adapter.putField(clazz.type, field.name, field.type)

    return emptyList()
  }

  override fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val adapter = context.adapter
    val field = context.field
    val clazz = context.clazz

    adapter.loadArg(context.variable("target"))
    adapter.push(null as String?)
    adapter.putField(clazz.type, field.name, field.type)

    return emptyList()
  }
}
