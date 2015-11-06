package io.sento;

public final class SentoFactory {
  @SuppressWarnings("unchecked")
  public static Binding<Object> createBinding(final Class<?> clazz) {
    try {
      return (Binding<Object>) Class.forName(clazz.getName() + "$$SentoBinding").newInstance();
    } catch (final Exception exception) {
      return null;
    }
  }
}
