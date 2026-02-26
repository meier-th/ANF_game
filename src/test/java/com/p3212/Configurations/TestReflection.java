package com.p3212.Configurations;

import java.lang.reflect.Field;

/**
 * Small helper for tests to inject dependencies into classes
 * that are normally wired by Spring.
 *
 * This is intentionally minimal and kept in test scope only.
 */
public final class TestReflection {

    private TestReflection() {
        // utility
    }

    public static void setField(Object target, String fieldName, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to set field '" + fieldName + "'", e);
            }
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' not found on " + target.getClass());
    }
}

