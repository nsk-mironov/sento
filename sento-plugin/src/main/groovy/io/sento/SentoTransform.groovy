package io.sento

import com.android.build.transform.api.AsInputTransform
import com.android.build.transform.api.Context
import com.android.build.transform.api.ScopedContent
import com.android.build.transform.api.Transform
import com.android.build.transform.api.TransformException
import com.android.build.transform.api.TransformInput
import com.android.build.transform.api.TransformOutput
import com.google.common.collect.Iterables
import io.sento.compiler.SentoCompiler
import io.sento.compiler.SentoOptions

public class SentoTransform extends Transform implements AsInputTransform {
  @Override
  public void transform(final Context context, final Map<TransformInput, TransformOutput> inputs, final Collection<TransformInput> references, final boolean incremental) throws IOException, TransformException, InterruptedException {
    final def compiler = new SentoCompiler()

    final def entry = Iterables.getOnlyElement(inputs.keySet())
    final def input = Iterables.getOnlyElement(entry.files)
    final def output = Iterables.getOnlyElement(inputs.values()).outFile

    final def builder = new SentoOptions.Builder(input, output)

    builder.incremental(incremental)
    builder.dryRun(true)

    compiler.compile(builder.build())
  }

  @Override
  public String getName() {
    return "sento"
  }

  @Override
  public Set<ScopedContent.ContentType> getInputTypes() {
    return Collections.singleton(ScopedContent.ContentType.CLASSES)
  }

  @Override
  public Set<ScopedContent.ContentType> getOutputTypes() {
    return EnumSet.of(ScopedContent.ContentType.CLASSES)
  }

  @Override
  public Set<ScopedContent.Scope> getScopes() {
    return EnumSet.of(ScopedContent.Scope.PROJECT)
  }

  @Override
  public Set<ScopedContent.Scope> getReferencedScopes() {
    return Collections.emptySet()
  }

  @Override
  public ScopedContent.Format getOutputFormat() {
    return ScopedContent.Format.SINGLE_FOLDER
  }

  @Override
  public Collection<File> getSecondaryFileInputs() {
    return Collections.emptyList()
  }

  @Override
  public Collection<File> getSecondaryFileOutputs() {
    return Collections.emptyList()
  }

  @Override
  public Collection<File> getSecondaryFolderOutputs() {
    return Collections.emptyList()
  }

  @Override
  public Map<String, Object> getParameterInputs() {
    return Collections.emptyMap()
  }

  @Override
  public boolean isIncremental() {
    return false
  }
}
