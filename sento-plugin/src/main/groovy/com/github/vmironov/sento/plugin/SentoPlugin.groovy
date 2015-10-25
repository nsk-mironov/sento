package com.github.vmironov.sento.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class SentoPlugin implements Plugin<Project> {
  @Override
  public void apply(final Project project) {
    project.dependencies.add("compile", "com.github.vmironov.sento:sento-core:0.10.0")
    project.dependencies.add("compile", "com.github.vmironov.sento:sento-runtime:0.10.0")
  }
}
