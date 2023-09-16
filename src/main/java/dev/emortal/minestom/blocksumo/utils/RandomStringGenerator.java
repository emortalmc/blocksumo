package dev.emortal.minestom.blocksumo.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomStringGenerator {
    private static final int MIN = 'a';
    private static final int MAX = 'z';

    public static @NotNull String generate(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) ThreadLocalRandom.current().nextInt(MIN, MAX);
            result.append(randomChar);
        }
        return result.toString();
    }

    private RandomStringGenerator() {
    }
}
