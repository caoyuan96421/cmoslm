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
public class NMOS extends MOSFET{
    
    /**
     *
     * @param w
     * @param l
     * @param tech
     */
    public NMOS(double w, double l, Technology tech){
        super(w,l,tech);
        if(this.w < tech.wmin){
            this.w = tech.wmin;
        }
        if(this.l < tech.lmin){
            this.l = tech.lmin;
        }
    }

    /**
     *
     * @param vgs
     * @param vds
     * @return
     */
    @Override
    public double Id_leak(double vgs, double vds) {
        return tech.Id_N(vgs, vds, this);
    }
    
    
}
