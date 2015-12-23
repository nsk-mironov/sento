package io.sento;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class Sento {
  private static final Map<Class<?>, Binding> BINDINGS = new IdentityHashMap<>();

  public static void bind(final Object target, final Activity activity) {
    findOrCreateBinding(target.getClass()).bind(target, activity, ACTIVITY_FINDER);
  }

  public static void bind(final Object target, final View view) {
    findOrCreateBinding(target.getClass()).bind(target, view, VIEW_FINDER);
  }

  public static void bind(final Object target, final Object source, final Finder finder) {
    findOrCreateBinding(target.getClass()).bind(target, source, finder);
  }

  public static void unbind(final Object target) {
    findOrCreateBinding(target.getClass()).unbind(target);
  }

  private static Binding findOrCreateBinding(final Class<?> clazz) {
    if (isSystemClass(clazz)) {
      return DEFAULT_BINDING;
    }

    if (!BINDINGS.containsKey(clazz)) {
      BINDINGS.put(clazz, createBinding(clazz));
    }

    return BINDINGS.get(clazz);
  }

  private static Binding createBinding(final Class<?> clazz) {
    final List<Binding> bindings = new ArrayList<>();

    for (Class current = clazz; current != null && !isSystemClass(current); current = current.getSuperclass()) {
      final Binding binding = SentoFactory.createBinding(current);

      if (binding != null) {
        bindings.add(binding);
      }
    }

    if (bindings.isEmpty()) {
      return DEFAULT_BINDING;
    }

    if (bindings.size() == 1) {
      return bindings.get(0);
    }

    return new CompositeBinding(bindings);
  }

  private static boolean isSystemClass(final Class<?> clazz) {
    return clazz.getName().startsWith("android.") || clazz.getName().startsWith("java.") || clazz.getName().startsWith("kotlin.");
  }

  private static String asResourceName(final int id, final Resources resources) {
    return "R." + resources.getResourceTypeName(id) + "." + resources.getResourceEntryName(id);
  }

  private static final class CompositeBinding implements Binding {
    private final List<Binding> bindings;

    private CompositeBinding(final List<Binding> bindings) {
      this.bindings = bindings;
    }

    @Override
    public void bind(final Object target, final Object source, final Finder finder) {
      for (int i = 0, size = bindings.size(); i < size; i++) {
        bindings.get(i).bind(target, source, finder);
      }
    }

    @Override
    public void unbind(final Object target) {
      for (int i = 0, size = bindings.size(); i < size; i++) {
        bindings.get(i).unbind(target);
      }
    }
  }

  private static final Binding DEFAULT_BINDING = new Binding() {
    @Override
    public void bind(final Object target, final Object source, final Finder finder) {
      // nothing to do
    }

    @Override
    public void unbind(final Object target) {
      // nothing to do
    }
  };

  private static final Finder ACTIVITY_FINDER = new Finder() {
    @Override
    public View find(final int id, final Object source) {
      return ((Activity) source).findViewById(id);
    }

    @Override
    public void require(final int id, final View view, final Object source, final String message) {
      if (view == null) {
        throw new IllegalStateException("Unable to find a required view with id " + asResourceName(id, resources(source)) + " for " + message);
      }
    }

    @Override
    public Resources resources(final Object source) {
      return ((Activity) source).getResources();
    }
  };

  private static final Finder VIEW_FINDER = new Finder() {
    @Override
    public View find(final int id, final Object source) {
      return ((View) source).findViewById(id);
    }

    @Override
    public void require(final int id, final View view, final Object source, final String message) {
      if (view == null) {
        throw new IllegalStateException("Unable to find a required view with id " + asResourceName(id, resources(source)) + " for " + message);
      }
    }

    @Override
    public Resources resources(final Object source) {
      return ((View) source).getResources();
    }
  };
}
