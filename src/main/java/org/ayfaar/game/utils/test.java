package org.ayfaar.game.utils;

import org.junit.Test;

import java.util.Random;

public class test {
    @Test
    public void main() {
        Random random = new Random();
        for(int i=0; i < 100; i++) {
            System.out.println(random.nextDouble() <= (double)3/7);
        }
    }
}
