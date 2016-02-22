package io.mironov.sento.plugin

import io.sento.plugin.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SentoPlugin implements Plugin<Project> {
  @Override
  public void apply(final Project project) {
    onPrepareDependencies(project)
    onPrepareTransforms(project)
  }

  private static void onPrepareDependencies(final Project project) {
    project.dependencies.add("compile", "io.mironov.sento:sento-runtime:${BuildConfig.VERSION}@aar")
  }

  private static void onPrepareTransforms(final Project project) {
    project.android.registerTransform(new SentoTransform(project))
  }
}
