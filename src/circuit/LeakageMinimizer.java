/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import gate.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    
    public void setModule(Module m){
        module[0] = m;
    }
    
    public void loadModule(String filename) throws Exception{
        for(int i=0;i<getParallelism();i++){
            /*Complete independent*/
            Module m = Module.loadFile(filename);
            if(m.vdd == 0){
                m.setVDD(1.2);
            }
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
    
    /**
     * Minimize leakage using class specific method
     */
    public void minimizeLeakage() {
        int n = module[0].getInputSize();
        BigInteger N = BigInteger.valueOf(2).pow(n);
        if(n > 24){
            throw new UnsupportedOperationException("Number of input vectors " + N.toString() +" is very large. Be careful.");
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
        }
        System.out.println();
        System.out.println("Best leakage: " + best_leakage);
        System.out.println("Best input: " + Arrays.asList(best_input));
    }
    
    public void saveLeakageTable(String filename) throws FileNotFoundException, IOException{
        File file = new File(filename);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        
        int n = module[0].getInputSize();
        BigInteger N = BigInteger.valueOf(2).pow(n);
        if(n > 24){
            throw new UnsupportedOperationException("Number of input vectors " + N.toString() +" is very large. Be careful.");
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
            bos.write((i.toString() + "\t" + leakage + "\n").getBytes());
            if(leakage < best_leakage){
                best_leakage = leakage;
                best_input = input;
            }
        }
        bos.close();
        System.out.println();
        System.out.println("Best leakage: " + best_leakage);
        System.out.println("Best input: " + Arrays.asList(best_input));
    }
    
    public static void main(String args[]) throws Exception{
        MonteCarloLeakageMinimizer lm = new MonteCarloLeakageMinimizer();
        //LeakageMinimizer lm = new ParallelLeakageMinimizer(8);
        //LeakageMinimizer lm = new LeakageMinimizer();
        lm.loadModule("FA/FA16.circ");
        //lm.minimizeLeakage();
        //lm.saveLeakageTable("FA/FA4.txt");
        lm.minimizeLeakageWithOutput("FA/FA16.dat");
    }
}

