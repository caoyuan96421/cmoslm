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
public class TechGPDK90 extends Technology{

    private static final double kn = 140e-6;
    private static final double kp = -60e-6;
    private static final double Vdsatn = 0.2;
    private static final double Vdsatp = -0.3;
    private static final double vt = 0.2;
    private static final double lambda = 0.1;
    private static final double leffd = 0.03e-6;
    
    private static final double id0n = 145e-9;
    private static final double id1n = 225e-9;
    private static final double w0n = 240e-9;
    private static final double w1n = 480e-9;
    
    private static final double id0p = 22e-9;
    private static final double id1p = 34e-9;
    private static final double w0p = 240e-9;
    private static final double w1p = 480e-9;
    
    
    /**Subthreshold conductance coefficient at RT*/
    private static final double sn = 0.098/Math.log(10)/300;
    private static final double sp = -0.067/Math.log(10)/300;
    
    /**DIBL effect coefficient at RT*/
    private static final double dn = 0.75/Math.log(10)/300;
    private static final double dp = 0.85/Math.log(10)/300;
    
    /**Channel length coefficient at RT*/
    private static final double ln = 11.4e-9/Math.log(2)/300;
    private static final double lp = 16e-9/Math.log(2)/300;
    
    private static final double gn = 0.1;
    private static final double gp = -0.15;
    private static final double phn = 0.6;
    private static final double php = -0.6;
    
    static final double wmin = 120e-9;
    static final double lmin = 100e-9;
    
    public TechGPDK90(double T){
        super(T);
    }

    @Override
    public double Id_N(double vgs, double vds, MOSFET mos) {
        double vgt = vgs - vt;
        double id;
        if(mos.w >= w1n){
            id = id1n * mos.w / w1n;
        }
        else if(mos.w >= w0n){
            id = (id1n - id0n) / (w1n - w0n) * (mos.w - w0n) + id0n; // Linear combination
        }
        else{
            id = id0n;
        }
        id = id * Math.exp((Math.min(vgt,0)/sn + Math.abs(vds)/dn - (mos.l - lmin)/ln) / this.T) * (1 - Math.exp(-Math.abs(vds)/Vth()))* Math.signum(vds);
        //System.out.println("Subthreshold id: " + id);
        if(vgt >= 0){// Lin/Sat/Vel.sat
            double C = kn * mos.w / (mos.l - leffd);
            if(vgt <= Math.abs(vds) && vgt <= Vdsatn){
                System.out.println("Sat");
                /*Saturated*/
                return C * 0.5 * vgt * vgt * (1 + lambda * Math.abs(vds)) * Math.signum(vds) + id;
            }
            else if(Vdsatn <= Math.abs(vds) && Vdsatn <= vgs){
                /*Vel. Saturated*/
                System.out.println("Vel. Sat");
                return C * (vgt * Vdsatn - 0.5 * Vdsatn * Vdsatn) * (1 + lambda * Math.abs(vds)) * Math.signum(vds) + id;
            }
            else{
                /*Linear*/
                System.out.println("Lin");
                return C * (vgt * vds - 0.5 * vds * Math.abs(vds)) * (1 + lambda * Math.abs(vds)) + id;
            }
        }
        else{
            /*Subthreshold conducting and DIBL effect*/
            return id;
        }
    }

    @Override
    public double Id_P(double vgs, double vds, MOSFET mos) {
        double vgt = vgs + vt;
        double id;
        if(mos.w >= w1p){
            id = id1p * mos.w / w1p;
        }
        else if(mos.w >= w0p){
            id = (id1p - id0p) / (w1p - w0p) * (mos.w - w0p) + id0p; // Linear combination
        }
        else{
            id = id0p;
        }
        id = id * Math.exp((Math.max(vgt,0)/sp + Math.abs(vds)/dp - (mos.l - lmin)/lp) / this.T) * (1 - Math.exp(-Math.abs(vds)/Vth())) * Math.signum(vds);
        //System.out.println("Subthreshold id: " + id);
        if(vgt <= 0){// Lin/Sat/Vel.sat
            double C = kp * mos.w / (mos.l - leffd);
            if(vgt >= -Math.abs(vds) && vgt >= Vdsatp){
                System.out.println("Sat");
                /*Saturated*/
                return C * 0.5 * vgt * vgt * (1 + lambda * Math.abs(vds)) * Math.signum(-vds) + id;
            }
            else if(Vdsatp >= -Math.abs(vds) && Vdsatn >= vgs){
                /*Vel. Saturated*/
                System.out.println("Vel. Sat");
                return C * (vgt * Vdsatp - 0.5 * Vdsatn * Vdsatn) * (1 + lambda * Math.abs(vds)) * Math.signum(-vds) + id;
            }
            else{
                /*Linear*/
                System.out.println("Lin");
                return C * (vgt * vds - 0.5 * vds * Math.abs(vds)) * (1 + lambda * Math.abs(vds)) + id;
            }
        }
        else{
            /*Subthreshold conducting and DIBL effect*/
            return id;
        }
    }
    
    @Override
    public String toString(){
        return "GPDK90";
    }
    
    /*public static void main(String args[]){
        double vgs = -0.5;
        double vds = 0.1;
        NMOS mos1 = new NMOS(360e-9,130e-9,Technology.Tech_GPDK90);
        System.out.println(mos1.Id_leak(vgs, vds));
        PMOS mos2 = new PMOS(480e-9,100e-9,Technology.Tech_GPDK90);
        System.out.println(mos2.Id_leak(-vgs, -vds));
    } */   

    @Override
    public double VT_N(double vsb) {
        return vt + gn * (Math.sqrt(Math.abs(vsb + phn)) - Math.sqrt(Math.abs(phn)));
    }

    @Override
    public double VT_P(double vsb) {
        return -vt + gp * (Math.sqrt(Math.abs(vsb + php)) - Math.sqrt(Math.abs(php)));
    }

    @Override
    public double VT0() {
        return vt;
    }
    
}
