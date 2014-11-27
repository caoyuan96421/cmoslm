/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;

import java.util.Random;

/**
 *
 * @author caoyuan9642
 */
public enum Logic {
    HIGH,
    LOW,
    UNKNOWN;

    private static Random random = new Random();
    public static Logic random() {
        if(random.nextBoolean())
            return Logic.HIGH;
        else
            return Logic.LOW;
    }
    public Logic invert() {
        if(this == Logic.HIGH)
            return Logic.LOW;
        else if(this == Logic.LOW)
            return Logic.HIGH;
        else
            return Logic.UNKNOWN;
    }
}
