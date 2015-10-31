package io.sento;

import android.app.Activity;
import android.view.View;
import io.sento.Binding;
import io.sento.Finder;

import java.util.IdentityHashMap;
import java.util.Map;

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

    if (BINDINGS.containsKey(clazz)) {
      return BINDINGS.get(clazz);
    }

    final Binding<Object> binding = createBinding(clazz);

    if (binding == null) {
      return findOrCreateBinding(clazz.getSuperclass());
    }

    BINDINGS.put(clazz, binding);
    return binding;
  }

  @SuppressWarnings("unchecked")
  private static Binding<Object> createBinding(final Class<?> clazz) {
    try {
      return (Binding<Object>) Class.forName(clazz.getName() + "$$SentoBinding").newInstance();
    } catch (Exception exception) {
      return null;
    }
  }

  private static boolean isSystemClass(final Class<?> clazz) {
    return clazz.getName().startsWith("android.") || clazz.getName().startsWith("java.");
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
    public <V extends View> V find(final int id, final Activity source) {
      return (V) source.findViewById(id);
    }
  };

  private static Finder<View> VIEW_FINDER = new Finder<View>() {
    @Override
    @SuppressWarnings("unchecked")
    public <V extends View> V find(final int id, final View source) {
      return (V) source.findViewById(id);
    }
  };
}
