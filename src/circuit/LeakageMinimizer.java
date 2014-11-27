/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import gate.*;
import java.math.BigInteger;
import java.util.Arrays;
/**
 *
 * @author caoyuan9642
 */
public class LeakageMinimizer {
    public Module module[];
    
    public LeakageMinimizer(){
        module = new Module[1];
    }
    
    public LeakageMinimizer(Module module){
        this();
        this.module = new Module[]{module};
    }
    
    public int getParallelism(){
        return 1;
    }
    
    public void loadModule(String filename) throws Exception{
        for(int i=0;i<getParallelism();i++){
            /*Complete independent*/
            Module m = Module.loadFile(filename);
            this.module[i] = m;
        }
    }
    
    
    /**
     * Evaluate leakage using i-th parallel module
     * @param i parallelism index
     * @param input input vector
     * @return Leakage current
     */
    public double leakage(int i, Logic []input){
        module[i].evaluateOutput(input);
        return module[i].collectLeakage();
    }
    
    protected Logic[] best_input;
    protected double best_leakage;
    
    public void minimizeLeakage() {
        int n = module[0].getInputSize();
        BigInteger N = BigInteger.valueOf(2).pow(n);
        if(n > 24){
            System.err.println("Number of input vectors " + N.toString() +" is very large. Be careful.");
        }
        best_leakage = Double.MAX_VALUE;
        best_input = null;
        for(BigInteger i = BigInteger.ZERO; i.subtract(N).signum() < 0; i = i.add(BigInteger.ONE)){
            Logic []input = new Logic[n];
            for(int j=0;j<n;j++){
                input[j] = i.testBit(n-j-1) ? Logic.HIGH : Logic.LOW;
            }
            //System.out.println("Trying input: " + Arrays.asList(input));
            double leakage = leakage(0, input);
            //System.out.println("Leakage: " + leakage);
            if(leakage < best_leakage){
                best_leakage = leakage;
                best_input = input;
            }
            if(i.remainder(N.divide(BigInteger.valueOf(100))).intValue() == 0){
                System.out.print("#");
            }
        }
        System.out.println();
        System.out.println("Best leakage: " + best_leakage);
        System.out.println("Best input: " + Arrays.asList(best_input));
    }
    
    public static void main(String args[]) throws Exception{
        LeakageMinimizer lm = new ParallelLeakageMinimizer(8);
        lm.loadModule("FA12.circ");
        lm.minimizeLeakage();
    }
}
