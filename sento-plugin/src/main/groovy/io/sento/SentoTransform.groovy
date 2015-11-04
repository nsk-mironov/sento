package io.sento

import com.android.build.transform.api.Context
import com.android.build.transform.api.Format
import com.android.build.transform.api.QualifiedContent
import com.android.build.transform.api.Transform
import com.android.build.transform.api.TransformException
import com.android.build.transform.api.TransformInput
import com.android.build.transform.api.TransformOutputProvider
import com.google.common.collect.Iterables
import io.sento.compiler.SentoCompiler
import io.sento.compiler.SentoOptions

public class SentoTransform extends Transform {
  @Override
  public void transform(final Context context, final Collection<TransformInput> inputs, final Collection<TransformInput> references, final TransformOutputProvider provider, final boolean incremental) throws IOException, TransformException, InterruptedException {
    final def compiler = new SentoCompiler()

    final def transformInput = Iterables.getOnlyElement(inputs)
    final def directoryInput = Iterables.getOnlyElement(transformInput.directoryInputs)

    final def output = provider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
    final def input = directoryInput.file

    compiler.compile(new SentoOptions.Builder(input, output)
        .incremental(incremental)
        .dryRun(false)
        .build()
    )
  }

  @Override
  public String getName() {
    return "sento"
  }

  @Override
  public Set<QualifiedContent.Scope> getScopes() {
    return EnumSet.of(QualifiedContent.Scope.PROJECT)
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  public Set<QualifiedContent.ContentType> getOutputTypes() {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  public Set<QualifiedContent.Scope> getReferencedScopes() {
    return Collections.emptySet()
  }

  @Override
  public Collection<File> getSecondaryFileInputs() {
    return Collections.emptySet()
  }

  @Override
  public Collection<File> getSecondaryFileOutputs() {
    return Collections.emptySet()
  }

  @Override
  public Collection<File> getSecondaryDirectoryOutputs() {
    return Collections.emptySet()
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
