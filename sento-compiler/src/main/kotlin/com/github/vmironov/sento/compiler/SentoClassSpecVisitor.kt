package com.github.vmironov.sento.compiler

import com.github.vmironov.sento.Bind
import com.github.vmironov.sento.OnClick
import com.github.vmironov.sento.compiler.specs.AnnotationSpec
import com.github.vmironov.sento.compiler.specs.ClassSpec
import com.github.vmironov.sento.compiler.specs.FieldSpec
import com.github.vmironov.sento.compiler.specs.MethodSpec
import com.github.vmironov.sento.compiler.visitors.ClassSpecVisitor
import org.objectweb.asm.Type

public class SentoClassSpecVisitor(type: Type, parent: Type, action: (ClassSpec) -> Unit) : ClassSpecVisitor(type, parent, action) {
  private val annotations = listOf<Type>(
      Type.getType(Bind::class.java),
      Type.getType(OnClick::class.java)
  )

  override fun shouldAcceptAnnotation(annotation: AnnotationSpec): Boolean {
    return false
  }

  override fun shouldAcceptField(field: FieldSpec): Boolean {
    return field.annotations.any {
      annotations.contains(it.type)
    }
  }

  override fun shouldAcceptMethod(method: MethodSpec): Boolean {
    return method.annotations.any {
      annotations.contains(it.type)
    }
  }

  override fun shouldAcceptClass(clazz: ClassSpec): Boolean {
    return !clazz.fields.isEmpty() || !clazz.methods.isEmpty()
  }
}
