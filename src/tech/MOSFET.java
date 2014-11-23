/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech;

/**
 *
 * @author caoyuan9642
 */
public abstract class MOSFET{
    /**
     * Width and Length of MOSFET
     */
    public double w, l;

    /**
     * Technology parameters for the MOSFET
     */
    public final Technology tech;
    
    /**
     * Constructor
     * @param w width of MOSFET
     * @param l length of MOSFET
     * @param tech technology used for MOSFET
     */
    public MOSFET(double w, double l, Technology tech){
        this.w = w;
        this.l = l;
        this.tech = tech;
    }
    
    /**
     * Calculate leak current for specified $V_{gs}$ and $V_{ds}$
     * @param vgs
     * @param vds
     * @return
     */
    public abstract double Id_leak(double vgs, double vds);
}
