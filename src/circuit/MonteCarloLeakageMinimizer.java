/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import gate.*;
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
    
    private static final int PASS_PER_TEMP = 1;
    private static final double T0 = 10e-6;
    private static final double COOLING_RATE = 0.9;
    private static final double T1 = 1e-12;
    
    @Override
    public void minimizeLeakage(){
        int n = module[0].getInputSize();
        Logic []input = new Logic[n];
        for(int i=0;i<n;i++){
            input[i] = Logic.random();
        }
        best_input = input.clone();
        best_leakage = leakage(0, input);
        for(double T=T0;T>T1;T*=COOLING_RATE){
            System.out.println("T = " + T);
            for(int pass=PASS_PER_TEMP * n;pass>0;pass--){
                int index = random.nextInt(n);
                input[index] = input[index].invert();
                double leakage = leakage(0, input);
                if(leakage < best_leakage){
                    best_leakage = leakage;
                    best_input = input.clone();
                    //System.out.println(best_leakage + " " + Arrays.asList(best_input));
                }
                else{
                    /*Monte Carlo*/
                    double th = Math.exp(-(leakage - best_leakage) / T);
                    if(random.nextDouble() < th){
                        best_leakage = leakage;
                        best_input = input.clone();
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
    
}
