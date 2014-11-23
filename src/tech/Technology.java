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
public abstract class Technology {
    static double wmin;
    static double lmin;
    
    protected final double T;
    
    public Technology(double T){
        this.T = T;
    }
    
    public abstract double Id_N(double vgs, double vds, MOSFET mos);
    public abstract double Id_P(double vgs, double vds, MOSFET mos);
    
    /**90 nm Technology at room temperature*/
    public static final Technology Tech_GPDK90 = new TechGPDK90(300);
}
