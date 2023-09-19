package com.example.datingapp.Original;

public final class SSGRTCUtils {
    private SSGRTCUtils() {
    }

    public static void assertIsTrue(boolean z) {
        if (!z) {
            throw new AssertionError("Expected condition to be true");
        }
    }
}
