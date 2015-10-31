package io.sento.asmifier;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class SentoAsmifier {
  public static void main(final String[] args) throws Exception {
    final ClassReader reader = new ClassReader(TestBinding.class.getName());
    final PrintWriter writer = new PrintWriter(System.out);
    final ClassVisitor visitor = new TraceClassVisitor(null, new ASMifier(), writer);

    reader.accept(visitor, ClassReader.SKIP_DEBUG);
  }
}
