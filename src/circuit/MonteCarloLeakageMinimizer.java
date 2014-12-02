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
import java.util.Calendar;
import java.util.Random;
/**
 *
 * @author caoyuan9642
 */
public class MonteCarloLeakageMinimizer extends LeakageMinimizer{
    
    private static Random random = new Random(Calendar.getInstance().getTimeInMillis());
    public MonteCarloLeakageMinimizer(){
    }
    
    private static final int PASS_PER_TEMP = 10;
    private static final double T0 = 10e-6;
    private static final double COOLING_RATE = 0.98;
    private static final double T1 = 1e-9;
    private static final int DOUBLE_FLIP_FREQ = 10;
    
    @Override
    public void minimizeLeakage(){
        int n = module[0].getInputSize();
        Logic []input = new Logic[n];
        for(int i=0;i<n;i++){
            input[i] = Logic.random();
        }
        best_input = input.clone();
        best_leakage = leakage(0, input);
        double current_leakage = best_leakage;
        for(double T=current_leakage/5;T>T1;T*=COOLING_RATE){
            System.out.println("T = " + T);
            for(int pass=PASS_PER_TEMP * n;pass>0;pass--){
                int index = random.nextInt(n);
                input[index] = input[index].invert();
                double leakage = leakage(0, input);
                if(leakage < current_leakage){
                    current_leakage = leakage;
                    if(leakage < best_leakage){
                        best_leakage = leakage;
                        best_input = input.clone();
                    }
                    //System.out.println(best_leakage + " " + Arrays.asList(best_input));
                }
                else{
                    /*Monte Carlo*/
                    double th = Math.exp(-(leakage - best_leakage) / T);
                    if(random.nextDouble() < th){
                        current_leakage = leakage;
                        //System.out.println(best_leakage + " " + Arrays.asList(best_input));
                    }
                    else{
                        /*Restore*/
                        input[index] = input[index].invert();
                    }
                }
            }
        }
        System.out.println("Best leakage: " + best_leakage);
        System.out.println("Best input: " + Arrays.asList(best_input));
        System.out.println("Output: " + Arrays.asList(module[0].evaluateOutput(best_input)));
    }
    
    public void minimizeLeakageWithOutput(String filename) throws FileNotFoundException, IOException{
        File file = new File(filename);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        
        int n = module[0].getInputSize();
        Logic []input = new Logic[n];
        for(int i=0;i<n;i++){
            input[i] = Logic.random();
        }
        best_input = input.clone();
        best_leakage = leakage(0, input);
        double current_leakage = best_leakage;
        int count = 0;
        for(double T=current_leakage/5;T>T1;T*=COOLING_RATE){
            System.out.println("T = " + T);
            input = best_input.clone();
            current_leakage = best_leakage;
            for(int pass=PASS_PER_TEMP * n;pass>0;pass--){
                int index = random.nextInt(n);
                input[index] = input[index].invert();
                double leakage = leakage(0, input);
                if(leakage < current_leakage){
                    current_leakage = leakage;
                    if(leakage < best_leakage){
                        best_leakage = leakage;
                        best_input = input.clone();
                    }
                    //System.out.println(best_leakage + " " + Arrays.asList(best_input));
                    /*long i=0;
                    for(int j=0;j<best_input.length;j++)
                        i = (i << 1L) | (best_input[j] == Logic.HIGH ? 1L : 0L);
                    bos.write((T + "\t" + i + "\t" + best_leakage + "\n").getBytes());*/
                }
                else{
                    /*Monte Carlo*/
                    double th = Math.exp(-(leakage - best_leakage) / T);
                    if(random.nextDouble() < th){
                        current_leakage = leakage;
                        /*long i=0;
                        for(int j=0;j<best_input.length;j++)
                            i = (i << 1L) | (best_input[j] == Logic.HIGH ? 1L : 0L);
                        bos.write((T + "\t" + i + "\t" + best_leakage + "\n").getBytes());*/
                        //System.out.println(best_leakage + " " + Arrays.asList(best_input));
                    }
                    else{
                        /*Restore*/
                        input[index] = input[index].invert();
                    }
                }
            }
            
            if(++count == DOUBLE_FLIP_FREQ){
                count = 0;
                System.out.println("Double flipping optimization");
                for(int i=0;i<n;i++){
                    for(int j=i+1;j<n;j++){
                        best_input[i].invert();
                        best_input[j].invert();
                        double leakage = leakage(0, best_input);
                        if(leakage < best_leakage){
                            continue;
                        }
                        best_input[i].invert();
                        best_input[j].invert();
                    }
                }
            }
            
            long i=0;
            for(int j=0;j<best_input.length;j++)
                i = (i << 1L) | (best_input[j] == Logic.HIGH ? 1L : 0L);
            bos.write((T + "\t" + i + "\t" + best_leakage + "\n").getBytes());
            
        }
        
        bos.close();
        System.out.println("Best leakage: " + best_leakage);
        System.out.println("Best input: " + Arrays.asList(best_input));
        System.out.println("Output: " + Arrays.asList(module[0].evaluateOutput(best_input)));
    }
}
