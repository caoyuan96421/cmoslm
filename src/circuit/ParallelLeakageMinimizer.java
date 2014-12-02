/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import gate.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author caoyuan9642
 */
public class ParallelLeakageMinimizer extends LeakageMinimizer {
    
    final int parallelism;
    ExecutorService executor;
    private Lock lock[];
    
    public ParallelLeakageMinimizer(int parallelism){
        this.parallelism = parallelism;
        this.module = new Module[parallelism];
        
        executor = Executors.newFixedThreadPool(parallelism);
        lock = new Lock[parallelism];
        for(int i=0;i<parallelism;i++)
            lock[i] = new ReentrantLock();
    }
    
    @Override
    public int getParallelism(){
        return this.parallelism;
    }
    
    private abstract class RangeRunnable implements Runnable{
        protected long start, end;
        public RangeRunnable(long start, long end){
            this.start = start; 
            this.end = end;
        }
    };
    
    private static final int SLICE_SIZE = 4096;
    @Override
    public void minimizeLeakage() {
        int n = module[0].getInputSize();
        long N = (1L << (long)n);
        System.out.println(n);
        if(n > 24){
            System.err.println("Number of input vectors " + N +" is very large. Be careful.");
        }
        best_leakage = Double.MAX_VALUE;
        best_input = null;
        for(long i=0; i<N; i+= SLICE_SIZE){
            executor.submit(new RangeRunnable(i, Math.min(N, i + SLICE_SIZE)){

                @Override
                public void run() {
                    int p=0;
                    do{
                        if(lock[p].tryLock())
                            break;
                        p++;
                    }while(p < parallelism);
                    try{
                        for(long i = start; i < end; i++){
                            Logic []input = new Logic[n];
                            for(int j=0;j<n;j++){
                                input[j] = ((i & (1L << (n-j-1))) != 0) ? Logic.HIGH : Logic.LOW;
                            }
                            double leakage = leakage(p, input);
                            synchronized (ParallelLeakageMinimizer.this){
                                if(leakage < best_leakage){
                                    best_leakage = leakage;
                                    best_input = input;
                                }
                            }
                        }
                    }finally{
                        lock[p].unlock();
                    }
                    System.out.println("Finish " + start + " - " + end);
                }
                
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(ParallelLeakageMinimizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println();
        System.out.println("Best leakage: " + best_leakage);
        System.out.println("Best input: " + Arrays.asList(best_input));
    }
}
