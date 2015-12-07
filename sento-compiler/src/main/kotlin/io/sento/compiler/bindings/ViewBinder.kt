package io.sento.compiler.bindings

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.id
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import io.sento.compiler.model.BindTargetSpec
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal class ViewBinder {
  private val logger = LoggerFactory.getLogger(ViewBinder::class.java)

  public fun bind(target: BindTargetSpec, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    logger.info("Generating @{} binder for '{}' field",
        target.annotation.type.simpleName, target.field.name)

    if (target.field.type.sort == Type.ARRAY) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
          target.annotation.type.simpleName, target.clazz.type.className, target.field.name, target.field.type.className)
    }

    val isView = environment.registry.isSubclassOf(target.field.type, Types.VIEW)
    val isInterface = environment.registry.reference(target.field.type).access.isInterface

    if (!isInterface && !isView) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
          target.annotation.type.simpleName, target.clazz.type.className, target.field.name, Types.VIEW.className, target.field.type.className)
    }

    adapter.loadLocal(variables.target())
    adapter.loadLocal(variables.view(target.annotation.id))

    if (target.field.type != Types.VIEW) {
      adapter.checkCast(target.field.type)
    }

    adapter.putField(target.clazz, target.field)
  }

  public fun unbind(target: BindTargetSpec, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    logger.info("Generating @{} unbinder for '{}' field",
        target.annotation.type.simpleName, target.field.name)

    adapter.loadLocal(variables.target())
    adapter.pushNull()
    adapter.putField(target.clazz, target.field)
  }
}
