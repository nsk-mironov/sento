package io.sento.compiler.generator

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPublic
import io.sento.compiler.model.ListenerTargetSpec
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER

internal class ListenerBindingContentGenerator(private val target: ListenerTargetSpec) : ContentGenerator {
  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(GeneratedContent.from(target.type, mapOf(), environment.newClass {
      val parent = if (target.listener.listener.access.isInterface) Types.OBJECT else target.listener.listener.type
      val interfaces = if (target.listener.listener.access.isInterface) arrayOf(target.listener.listener.type) else emptyArray()

      visit(ACC_PUBLIC + ACC_SUPER, target.type, null, parent, interfaces)
      visitField(ACC_PRIVATE + ACC_FINAL, "target", target.clazz.type)

      newMethod(ACC_PUBLIC, Methods.getConstructor(target.clazz)) {
        loadThis()
        invokeConstructor(parent, Methods.getConstructor())

        loadThis()
        loadArg(0)
        putField(target.type, "target", target.clazz.type)
      }

      newMethod(ACC_PUBLIC, target.listener.callback) {
        loadThis()
        getField(target.type, "target", target.clazz.type)

        target.arguments.forEach {
          loadArg(it.index).apply {
            if (!Types.isPrimitive(it.type)) {
              checkCast(it.type)
            }
          }
        }

        if (!target.method.access.isPublic) {
          invokeStatic(target.clazz, environment.naming.getSyntheticAccessor(target.clazz, target.method))
        } else {
          invokeVirtual(target.clazz, target.method)
        }

        if (target.listener.callback.returns == Types.BOOLEAN && target.method.returns == Types.VOID) {
          push(false)
        }
      }

      environment.registry.listPublicMethods(target.listener.listener).filter {
        it.access.isAbstract && !Methods.equalsByJavaDeclaration(it, target.listener.callback)
      }.forEach {
        newMethod(ACC_PUBLIC, it) {
          if (it.returns == Types.BOOLEAN) {
            push(false)
          }
        }
      }
    }))
  }
}
