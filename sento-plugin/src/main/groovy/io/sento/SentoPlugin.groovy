package io.sento

import org.gradle.api.Plugin
import org.gradle.api.Project

public class SentoPlugin implements Plugin<Project> {
  @Override
  public void apply(final Project project) {
    onPrepareDependencies(project)
    onPrepareTransforms(project)
  }

  private static void onPrepareDependencies(final Project project) {
    project.dependencies.add("compile", "io.sento:sento-runtime:0.10.2@aar")
  }

  private static void onPrepareTransforms(final Project project) {
    project.android.registerTransform(new SentoTransform(project))
  }
}
