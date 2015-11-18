package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.Optional
import io.sento.compiler.annotations.WithId
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal class ViewBindingGenerator : FieldBindingGenerator {
  private val logger = LoggerFactory.getLogger(ResourceBindingGenerator::class.java)

  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    logger.info("Generating @{} binding for '{}' field",
        context.annotation.type.simpleName, context.field.name)

    if (context.field.type.sort == Type.ARRAY) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
          context.annotation.type.simpleName, context.clazz.type.className, context.field.name, context.field.type.className)
    }

    val isView = environment.registry.isSubclassOf(context.field.type, Types.VIEW)
    val isInterface = environment.registry.reference(context.field.type).access.isInterface
    val optional = context.field.getAnnotation<Optional>() != null

    if (!isInterface && !isView) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
          context.annotation.type.simpleName, context.clazz.type.className, context.field.name, Types.VIEW.className, context.field.type.className)
    }

    context.adapter.apply {
      loadArg(context.variable("target"))
      loadArg(context.variable("finder"))
      push(WithId.resolve(context.annotation).value())

      loadArg(context.variable("source"))
      push(optional)

      invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT, Types.BOOLEAN)).apply {
        if (context.field.type != Types.VIEW) {
          checkCast(context.field.type)
        }
      }

      putField(context.clazz.type, context.field.name, context.field.type)
    }

    return emptyList()
  }

  override fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    context.adapter.loadArg(context.variable("target"))
    context.adapter.visitInsn(Opcodes.ACONST_NULL)
    context.adapter.putField(context.clazz.type, context.field.name, context.field.type)

    return emptyList()
  }
}
