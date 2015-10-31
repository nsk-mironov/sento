package io.sento.asmifier;

import android.graphics.drawable.Drawable;
import io.sento.Binding;
import io.sento.Finder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class SentoAsmifier {
  private static class TestBinding implements Binding<Test> {
    @Override
    public <S> void bind(Test target, S source, Finder<? super S> finder) {
      target.background = finder.resources(source).getDrawable(456789);
      target.padding = finder.resources(source).getDimension(456789);
      target.enabled = finder.resources(source).getBoolean(456789);
      target.title = finder.resources(source).getString(456789);
    }

    @Override
    public void unbind(Test target) {

    }
  }

  private static class Test {
    public Drawable background = null;
    public boolean enabled = false;

    public String title = null;
    public float padding = 0;
  }

  public static void main(final String[] args) throws Exception {
    final ClassReader reader = new ClassReader(TestBinding.class.getName());
    final PrintWriter writer = new PrintWriter(System.out);
    final ClassVisitor visitor = new TraceClassVisitor(null, new ASMifier(), writer);

    reader.accept(visitor, ClassReader.SKIP_DEBUG);
  }
}
