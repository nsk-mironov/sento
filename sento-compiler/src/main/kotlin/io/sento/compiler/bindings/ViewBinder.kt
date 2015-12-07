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

  public fun bind(targets: Collection<BindTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    targets.forEach {
      logger.info("Generating @{} binder for '{}' field",
          it.annotation.type.simpleName, it.field.name)

      if (it.field.type.sort == Type.ARRAY) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
            it.annotation.type.simpleName, it.clazz.type.className, it.field.name, it.field.type.className)
      }

      val isView = environment.registry.isSubclassOf(it.field.type, Types.VIEW)
      val isInterface = environment.registry.reference(it.field.type).access.isInterface

      if (!isInterface && !isView) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
            it.annotation.type.simpleName, it.clazz.type.className, it.field.name, Types.VIEW.className, it.field.type.className)
      }

      adapter.loadLocal(variables.target())
      adapter.loadLocal(variables.view(it.annotation.id))

      if (it.field.type != Types.VIEW) {
        adapter.checkCast(it.field.type)
      }

      adapter.putField(it.clazz, it.field)
    }
  }

  public fun unbind(targets: Collection<BindTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    targets.forEach {
      logger.info("Generating @{} unbinder for '{}' field",
          it.annotation.type.simpleName, it.field.name)

      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(it.clazz, it.field)
    }
  }
}
