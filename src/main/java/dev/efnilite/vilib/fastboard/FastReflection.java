package dev.efnilite.vilib.fastboard;

/**
 * Folia 1.21.x 兼容的空操作 FastReflection 替代。
 */
public final class FastReflection {

    private FastReflection() {
        throw new UnsupportedOperationException();
    }

    public static boolean isRepackaged() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T> T enumValueOf(Class<?> clazz, String name, int ordinal) {
        return (T) clazz.getEnumConstants()[ordinal];
    }
}
