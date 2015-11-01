package io.sento;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;

import java.util.*;

public class Sento {
  private static Map<Class<?>, Binding<Object>> BINDINGS = new IdentityHashMap<>();

  public static void bind(final Object target, final Activity activity) {
    findOrCreateBinding(target.getClass()).bind(target, activity, ACTIVITY_FINDER);
  }

  public static void bind(final Object target, final View view) {
    findOrCreateBinding(target.getClass()).bind(target, view, VIEW_FINDER);
  }

  public static <S> void bind(final Object target, final S source, final Finder<? super S> finder) {
    findOrCreateBinding(target.getClass()).bind(target, source, finder);
  }

  public static void unbind(final Object target) {
    findOrCreateBinding(target.getClass()).unbind(target);
  }

  @SuppressWarnings("unchecked")
  private static Binding<Object> findOrCreateBinding(final Class<?> clazz) {
    if (isSystemClass(clazz)) {
      return DEFAULT_BINDING;
    }

    if (!BINDINGS.containsKey(clazz)) {
      BINDINGS.put(clazz, createBinding(clazz));
    }

    return BINDINGS.get(clazz);
  }

  @SuppressWarnings("unchecked")
  private static Binding<Object> createBinding(final Class<?> clazz) {
    final List<Binding<Object>> bindings = new ArrayList<>();

    for (Class parent = clazz; parent != null && !isSystemClass(parent); parent = parent.getSuperclass()) {
      try {
        bindings.add((Binding<Object>) Class.forName(parent.getName() + "$$SentoBinding").newInstance());
      } catch (final Exception exception) {
        // nothing to do. expected
      }
    }

    return new CompositeBinding<>(bindings);
  }

  private static boolean isSystemClass(final Class<?> clazz) {
    return clazz.getName().startsWith("android.") || clazz.getName().startsWith("java.");
  }

  private static String asResourceName(final int id, final Resources resources) {
    return "R." + resources.getResourceTypeName(id) + "." + resources.getResourceEntryName(id);
  }

  private static class CompositeBinding<T> implements Binding<T> {
    private final List<Binding<T>> bindings;

    private CompositeBinding(final List<Binding<T>> bindings) {
      this.bindings = bindings;
    }

    @Override
    public <S> void bind(final T target, final S source, final Finder<? super S> finder) {
      for (int i = 0, size = bindings.size(); i < size; i++) {
        bindings.get(i).bind(target, source, finder);
      }
    }

    @Override
    public void unbind(final T target) {
      for (int i = 0, size = bindings.size(); i < size; i++) {
        bindings.get(i).unbind(target);
      }
    }
  }

  private static Binding<Object> DEFAULT_BINDING = new Binding<Object>() {
    @Override
    public <S> void bind(Object target, S source, Finder<? super S> finder) {
      // nothing to do
    }

    @Override
    public void unbind(Object target) {
      // nothing to do
    }
  };

  private static Finder<Activity> ACTIVITY_FINDER = new Finder<Activity>() {
    @Override
    @SuppressWarnings("unchecked")
    public <V extends View> V find(final int id, final Activity source, final boolean optional) {
      final View result = source.findViewById(id);

      if (result == null && !optional) {
        throw new IllegalStateException("Unable to find a required view with id " + asResourceName(id, resources(source)));
      }

      return (V) result;
    }

    @Override
    public Resources resources(final Activity source) {
      return source.getResources();
    }
  };

  private static Finder<View> VIEW_FINDER = new Finder<View>() {
    @Override
    @SuppressWarnings("unchecked")
    public <V extends View> V find(final int id, final View source, final boolean optional) {
      final View result = source.findViewById(id);

      if (result == null && !optional) {
        throw new IllegalStateException("Unable to find a required view with id " + asResourceName(id, resources(source)));
      }

      return (V) result;
    }

    @Override
    public Resources resources(final View source) {
      return source.getResources();
    }
  };
}
