package dev.jaczerob.delfino.login.tools;

import java.util.Random;

public class Randomizer {

    private final static Random rand = new Random();

    public static int nextInt(final int arg0) {
        return rand.nextInt(arg0);
    }

    public static int rand(final int lbound, final int ubound) {
        return ((int) (rand.nextDouble() * (ubound - lbound + 1))) + lbound;
    }
}