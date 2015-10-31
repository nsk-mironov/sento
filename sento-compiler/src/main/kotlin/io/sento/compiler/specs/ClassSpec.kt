package io.sento.compiler.specs

import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.io.File
import java.util.ArrayList

internal data class ClassSpec(
    public val file: File,
    public val type: Type,
    public val parent: Type,
    public val annotations: List<AnnotationSpec>,
    public val fields: List<FieldSpec>,
    public val methods: List<MethodSpec>
) {
  public class Builder(val file: File, val type: Type, val parent: Type) {
    private val annotations = ArrayList<AnnotationSpec>()
    private val fields = ArrayList<FieldSpec>()
    private val methods = ArrayList<MethodSpec>()

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun field(field: FieldSpec): Builder = apply {
      fields.add(field)
    }

    public fun method(method: MethodSpec): Builder = apply {
      methods.add(method)
    }

    public fun build(): ClassSpec {
      return ClassSpec(file, type, parent, annotations, fields, methods)
    }
  }

  public fun field(name: String): FieldSpec? {
    return fields.firstOrNull {
      it.name == name
    }
  }

  public fun method(name: String): MethodSpec? {
    return methods.firstOrNull {
      it.name == name
    }
  }

  public fun toClassReader(): ClassReader {
    return ClassReader(FileUtils.readFileToByteArray(file))
  }
}
