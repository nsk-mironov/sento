package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.id
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal class ViewBindingGenerator : FieldBindingGenerator {
  private val logger = LoggerFactory.getLogger(ViewBindingGenerator::class.java)

  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
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

      if (!context.optional) {
        loadArg(context.argument("finder"))
        push(context.annotation.id)

        loadLocal(context.variable("view${context.annotation.id}"))
        loadArg(context.argument("source"))
        push("field '${context.field.name}'")

        invokeInterface(Types.FINDER, Methods.get("require", Types.VIEW, Types.INT, Types.VIEW, Types.OBJECT, Types.STRING)).apply {
          if (context.field.type != Types.VIEW) {
            checkCast(context.field.type)
          }
        }
      }

      if (context.optional) {
        loadLocal(context.variable("view${context.annotation.id}")).apply {
          if (context.field.type != Types.VIEW) {
            checkCast(context.field.type)
          }
        }
      }

      putField(context.clazz.type, context.field.name, context.field.type)
    }

    return emptyList()
  }

  override fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    context.adapter.loadLocal(context.variable("target"))
    context.adapter.visitInsn(Opcodes.ACONST_NULL)
    context.adapter.putField(context.clazz.type, context.field.name, context.field.type)

    return emptyList()
  }
}
