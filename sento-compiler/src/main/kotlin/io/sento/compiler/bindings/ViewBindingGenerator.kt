package io.sento.compiler.bindings

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.id
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal class ViewBindingGenerator {
  private val logger = LoggerFactory.getLogger(ViewBindingGenerator::class.java)

  public fun bind(context: ViewBindingContext, environment: GenerationEnvironment) {
    logger.info("Generating @{} binding for '{}' field",
        context.annotation.type.simpleName, context.field.name)

    if (context.field.type.sort == Type.ARRAY) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
          context.annotation.type.simpleName, context.clazz.type.className, context.field.name, context.field.type.className)
    }

    val isView = environment.registry.isSubclassOf(context.field.type, Types.VIEW)
    val isInterface = environment.registry.reference(context.field.type).access.isInterface

    if (!isInterface && !isView) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
          context.annotation.type.simpleName, context.clazz.type.className, context.field.name, Types.VIEW.className, context.field.type.className)
    }

    context.adapter.apply {
      loadLocal(context.variable("target"))
      loadLocal(context.variable("view${context.annotation.id}"))

      if (context.field.type != Types.VIEW) {
        checkCast(context.field.type)
      }

      putField(context.clazz.type, context.field.name, context.field.type)
    }
  }

  public fun unbind(context: ViewBindingContext, environment: GenerationEnvironment) {
    context.adapter.loadLocal(context.variable("target"))
    context.adapter.visitInsn(Opcodes.ACONST_NULL)
    context.adapter.putField(context.clazz.type, context.field.name, context.field.type)
  }
}
