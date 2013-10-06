package org.ayfaar.game.utils;

public class ProbabilityUtils {

    public static Boolean will(Double value) {
        return Math.random() <= value;
    }
}
