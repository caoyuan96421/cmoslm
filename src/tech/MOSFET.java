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
    
    public static final int NMOS=0;
    public static final int PMOS=1;

    public final int type;
    
    /**
     * Constructor
     * @param w width of MOSFET
     * @param l length of MOSFET
     * @param tech technology used for MOSFET
     */
    public MOSFET(double w, double l, Technology tech, int type){
        this.w = w;
        this.l = l;
        this.tech = tech;
        this.type = type;
    }
    
    /**
     * Calculate leak current for specified $V_{gs}$ and $V_{ds}$
     * @param vgs
     * @param vds
     * @return
     */
    public abstract double Id_leak(double vgs, double vds);
    
    public String toString(){
        if(type == NMOS){
            return tech.toString() + " NMOS w=" + String.format("%.0fnm",w*1e9) + " l=" + String.format("%.0fnm",l*1e9);
        }
        else if(type == PMOS){
            return tech.toString() + " PMOS w=" + String.format("%.0fnm",w*1e9) + " l=" + String.format("%.0fnm",l*1e9);
        }
        else{
            return tech.toString();
        }
    }
}
