package io.sento.compiler.bindings

import io.sento.Bind
import io.sento.BindArray
import io.sento.BindBool
import io.sento.BindColor
import io.sento.BindDimen
import io.sento.BindDrawable
import io.sento.BindInteger
import io.sento.BindString
import io.sento.MethodBinding
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.fields.FieldBindingGenerator
import io.sento.compiler.bindings.fields.BindArrayBindingGenerator
import io.sento.compiler.bindings.fields.BindBoolBindingGenerator
import io.sento.compiler.bindings.fields.BindColorBindingGenerator
import io.sento.compiler.bindings.fields.BindDimenBindingGenerator
import io.sento.compiler.bindings.fields.BindDrawableBindingGenerator
import io.sento.compiler.bindings.fields.BindIntegerBindingGenerator
import io.sento.compiler.bindings.fields.BindStringBindingGenerator
import io.sento.compiler.bindings.fields.BindViewBindingGenerator
import io.sento.compiler.bindings.methods.MethodBindingGenerator
import io.sento.compiler.bindings.methods.MethodBindingGeneratorImpl
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAnnotation
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.MethodBindingSpec
import io.sento.compiler.model.SentoBindingSpec
import org.objectweb.asm.Type
import java.util.HashMap

internal class SentoContentGeneratorFactory private constructor(
    private val fields: Map<Type, FieldBindingGenerator>,
    private val methods: Map<Type, MethodBindingGenerator>
) {
  public companion object {
    public fun from(environment: GenerationEnvironment): SentoContentGeneratorFactory {
      return SentoContentGeneratorFactory(createFieldBindings(environment), createMethodBindings(environment))
    }

    private fun createFieldBindings(environment: GenerationEnvironment): Map<Type, FieldBindingGenerator> {
      return HashMap<Type, FieldBindingGenerator>().apply {
        put(Type.getType(Bind::class.java), BindViewBindingGenerator())
        put(Type.getType(BindArray::class.java), BindArrayBindingGenerator())
        put(Type.getType(BindBool::class.java), BindBoolBindingGenerator())
        put(Type.getType(BindColor::class.java), BindColorBindingGenerator())
        put(Type.getType(BindDimen::class.java), BindDimenBindingGenerator())
        put(Type.getType(BindDrawable::class.java), BindDrawableBindingGenerator())
        put(Type.getType(BindInteger::class.java), BindIntegerBindingGenerator())
        put(Type.getType(BindString::class.java), BindStringBindingGenerator())
      }
    }

    private fun createMethodBindings(environment: GenerationEnvironment): Map<Type, MethodBindingGenerator> {
      return HashMap<Type, MethodBindingGenerator>().apply {
        environment.registry.references.forEach {
          if (it.access.isAnnotation && !Types.isSystemClass(it.type)) {
            val spec = environment.registry.resolve(it)
            val binding = spec.getAnnotation<MethodBinding>()

            if (binding != null) {
              put(it.type, MethodBindingGeneratorImpl(MethodBindingSpec.create(spec, binding, environment.registry)))
            }
          }
        }
      }
    }
  }

  public fun createBinding(clazz: ClassSpec): ContentGenerator {
    return SentoBindingContentGenerator(fields, methods, clazz)
  }

  public fun createFactory(bindings: Collection<SentoBindingSpec>): ContentGenerator {
    return SentoFactoryContentGenerator(bindings)
  }
}
