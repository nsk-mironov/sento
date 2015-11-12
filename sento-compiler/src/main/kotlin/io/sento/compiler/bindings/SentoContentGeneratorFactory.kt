package io.sento.compiler.bindings

import io.sento.Bind
import io.sento.BindArray
import io.sento.BindBool
import io.sento.BindColor
import io.sento.BindDimen
import io.sento.BindDrawable
import io.sento.BindInteger
import io.sento.BindString
import io.sento.ListenerBinding
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.fields.FieldBindingGenerator
import io.sento.compiler.bindings.fields.ResourceBindingGenerator
import io.sento.compiler.bindings.fields.ViewBindingGenerator
import io.sento.compiler.bindings.methods.MethodBindingGenerator
import io.sento.compiler.bindings.methods.ListenerBindingGenerator
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAnnotation
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.ResourceBindingSpec
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
        put(Type.getType(Bind::class.java), ViewBindingGenerator())

        put(Type.getType(BindArray::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Types.TYPE_INT_ARRAY, "getIntArray"),
            ResourceBindingSpec(Types.TYPE_CHAR_SEQUENCE_ARRAY, "getTextArray"),
            ResourceBindingSpec(Types.TYPE_STRING_ARRAY, "getStringArray")
        )))

        put(Type.getType(BindBool::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Type.BOOLEAN_TYPE, "getBoolean")
        )))

        put(Type.getType(BindColor::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Types.TYPE_COLOR_STATE_LIST, "getColorStateList"),
            ResourceBindingSpec(Type.INT_TYPE, "getColor")
        )))

        put(Type.getType(BindDimen::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Type.INT_TYPE, "getDimensionPixelSize"),
            ResourceBindingSpec(Type.FLOAT_TYPE, "getDimension")
        )))

        put(Type.getType(BindDrawable::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Types.TYPE_DRAWABLE, "getDrawable")
        )))

        put(Type.getType(BindInteger::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Type.INT_TYPE, "getInteger")
        )))

        put(Type.getType(BindString::class.java), ResourceBindingGenerator(listOf(
            ResourceBindingSpec(Types.TYPE_CHAR_SEQUENCE, "getText"),
            ResourceBindingSpec(Types.TYPE_STRING, "getString")
        )))
      }
    }

    private fun createMethodBindings(environment: GenerationEnvironment): Map<Type, MethodBindingGenerator> {
      return HashMap<Type, MethodBindingGenerator>().apply {
        environment.registry.references.forEach {
          if (it.access.isAnnotation && !Types.isSystemClass(it.type)) {
            val spec = environment.registry.resolve(it)
            val binding = spec.getAnnotation<ListenerBinding>()

            if (binding != null) {
              put(it.type, ListenerBindingGenerator(ListenerBindingSpec.create(spec, binding, environment.registry)))
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
