/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gate;

import static gate.Gate.MAX_RETRY;
import java.util.ArrayDeque;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import tech.MOSFET;
/**
 * 
 * @author caoyuan9642
 */
@Deprecated
public class RealisticGate extends Gate{

    public RealisticGate(String name) {
        super(name);
    }
    
    @Override
    protected void init_logic(Object input[]){
        q = new ArrayDeque<>();
        q.addLast(nodes.get(0));
        q.addLast(nodes.get(1));
        for(int i=0;i<input.length;i++){
            input_nodes.get(i).logic = (Logic)input[i];
            switch((Logic)input[i]){
                case LOW:
                    input_nodes.get(i).voltage = 0;
                    break;
                case HIGH:
                    input_nodes.get(i).voltage = vdd;
                    break;
            }
            q.addLast(input_nodes.get(i));
        }
    }
    
    @Override
    protected void update_logic(){
        while(!q.isEmpty()){
            Node node = q.pollFirst();
            System.out.println("Updating: " + node.toString() + " v=" + node.voltage + " " + node.devices.size());
            boolean retry = false;
            for (Device device : (List<Device>)node.devices) {
                if(device instanceof MOSDevice){
                    MOSDevice mos = (MOSDevice) device;
                    if(node == mos.G){
                        /*Don't need do anything*/
                        /*Wait until D or S is updated*/
                        continue;
                    }
                    if(!Double.isNaN(mos.G.voltage)){
                        System.out.println(" - " + mos.toString() + " Vt = " + mos.model.getVT(0));
                        DoubleBinaryOperator extremal = (mos.model.type == MOSFET.NMOS ? Math::min : Math::max);
                        if(!Double.isNaN(mos.S.voltage) && !Double.isNaN(mos.D.voltage)){
                            
                            if(node == mos.S){
                                if(mos.model.type == MOSFET.NMOS){
                                    if(mos.G.voltage - mos.S.voltage < mos.model.getVT(mos.S.voltage)){
                                        System.out.println("NMOS cutoff consistent: " + " " + mos.G.toString() + " G=" + mos.G.voltage + " " + mos.D.toString() + " D=" + mos.D.voltage + " " + mos.S.toString() + " S=" + mos.S.voltage);
                                        continue;
                                    }
                                    
                                    if(mos.D.voltage > mos.S.voltage){
                                        mos.D.voltage = mos.S.voltage;
                                        q.addLast(mos.D);
                                        System.out.println(mos.S.toString() + " G=" + mos.G.voltage +", S=" + mos.S.voltage + " -> D=" + mos.D.voltage + " " + mos.D.toString());
                                    }
                                }
                                else if(mos.model.type == MOSFET.PMOS){
                                    if(mos.G.voltage - mos.S.voltage > mos.model.getVT(mos.S.voltage)){
                                        System.out.println("PMOS cutoff consistent: " + " " + mos.G.toString() + " G=" + mos.G.voltage + " " + mos.D.toString() + " D=" + mos.D.voltage + " " + mos.S.toString() + " S=" + mos.S.voltage);
                                        continue;
                                    }
                                    
                                    if(mos.D.voltage < mos.S.voltage){
                                        mos.D.voltage = mos.S.voltage;
                                        q.addLast(mos.D);
                                        System.out.println(mos.S.toString() + " G=" + mos.G.voltage +", S=" + mos.S.voltage + " -> D=" + mos.D.voltage + " " + mos.D.toString());
                                    }
                                    
                                }
                            }
                            else if(node == mos.D){
                                if(mos.model.type == MOSFET.NMOS){
                                    if(mos.G.voltage - mos.D.voltage < mos.model.getVT(mos.D.voltage)){
                                        System.out.println("NMOS cutoff consistent: " + " " + mos.G.toString() + " G=" + mos.G.voltage + " " + mos.D.toString() + " D=" + mos.D.voltage + " " + mos.S.toString() + " S=" + mos.S.voltage);
                                        continue;
                                    }
                                    
                                    if(mos.S.voltage > mos.D.voltage){
                                        mos.S.voltage = mos.D.voltage;
                                        q.addLast(mos.S);
                                        System.out.println(mos.D.toString() + " G=" + mos.G.voltage +", D=" + mos.D.voltage + " -> S=" + mos.S.voltage + " " + mos.S.toString());
                                    }
                                }
                                else if(mos.model.type == MOSFET.PMOS){
                                    if(mos.G.voltage - mos.D.voltage > mos.model.getVT(mos.D.voltage)){
                                        System.out.println("PMOS cutoff consistent: " + " " + mos.G.toString() + " G=" + mos.G.voltage + " " + mos.D.toString() + " D=" + mos.D.voltage + " " + mos.S.toString() + " S=" + mos.S.voltage);
                                        continue;
                                    }
                                    
                                    if(mos.S.voltage < mos.D.voltage){
                                        mos.S.voltage = mos.D.voltage;
                                        q.addLast(mos.S);
                                        System.out.println(mos.D.toString() + " G=" + mos.G.voltage +", D=" + mos.D.voltage + " -> S=" + mos.S.voltage + " " + mos.S.toString());
                                    }
                                    
                                }
                            }
                            
                            System.out.println("Consistent: " + " " + mos.G.toString() + " G=" + mos.G.voltage + " " + mos.D.toString() + " D=" + mos.D.voltage + " " + mos.S.toString() + " S=" + mos.S.voltage);
                            
                        }
                        else if(!Double.isNaN(mos.S.voltage)){
                            /*update D voltage*/
                            double v = extremal.applyAsDouble(mos.S.voltage, mos.G.voltage - mos.model.getVT0());
                            int count = 0;
                            /*Iterate to get better V_D*/
                            do{
                                mos.D.voltage = v;
                                v = extremal.applyAsDouble(mos.S.voltage, mos.G.voltage - mos.model.getVT(mos.D.voltage));
                                count++;
                            }while(Math.abs(mos.D.voltage - v) > vdd * 0.01 && count <= 10);
                            if(mos.D.voltage < 0 || mos.D.voltage > vdd){
                                /*Voltage cannot be out of range. The MOS must already be off*/
                                mos.D.voltage = Double.NaN;
                                System.out.println("CUTOFF");
                                continue;
                            }
                            if(count > 10){
                                throw new UnsupportedOperationException(name + ": voltage failed to converge at " + node.toString());
                            }
                            q.addLast(mos.D);
                            System.out.println(mos.S.toString() + " G=" + mos.G.voltage +", S=" + mos.S.voltage + " -> D=" + mos.D.voltage + " " + mos.D.toString());
                        }
                        else if(!Double.isNaN(mos.D.voltage)){
                            /*update S voltage*/
                            double v = extremal.applyAsDouble(mos.D.voltage, mos.G.voltage - mos.model.getVT0());
                            int count = 0;
                            /*Iterate to get better V_S*/
                            do{
                                mos.S.voltage = v;
                                v = extremal.applyAsDouble(mos.D.voltage, mos.G.voltage - mos.model.getVT(mos.S.voltage));
                                count++;
                            }while(Math.abs(mos.S.voltage - v) > vdd * 0.01 && count <= 10);
                            if(mos.S.voltage < 0 || mos.S.voltage > vdd){
                                /*Voltage cannot be out of range. The MOS must already be off*/
                                mos.S.voltage = Double.NaN;
                                System.out.println("CUTOFF");
                                continue;
                            }
                            if(count > 10){
                                throw new UnsupportedOperationException(name + ": voltage failed to converge at " + node.toString());
                            }
                            q.addLast(mos.S);
                            System.out.println(mos.D.toString() + " G=" + mos.G.voltage +", D=" + mos.D.voltage + " -> S=" + mos.S.voltage + " " + mos.S.toString());
                        }
                    }
                    else{
                        /*Gate unknown. Postpone enumeration*/
                        node.retries++;
                        if(node.retries > MAX_RETRY){
                            throw new UnsupportedOperationException("Floating gate detected. Unsupported yet.");
                        }
                        System.out.println("Updating " + node.toString() + " postponed.");
                        retry = true;
                    }
                }
                else{
                    throw new UnsupportedOperationException("Device other than MOSFET is not implemented yet");
                }
            }
            if(retry){
                q.addLast(node);
            }
        }
        nodes.stream().forEachOrdered((n) -> {
            System.out.println(name + ": " + n.toString() + " v=" + n.voltage);
        });
        output_nodes.stream().forEach(node -> {
            if(node.voltage < vdd * 0.01){
                node.logic = Logic.LOW;
            }
            else if(node.voltage > vdd * (1 - 0.01)){
                node.logic = Logic.HIGH;
            }
            else{
                throw new UnsupportedOperationException(name + ": " + node.toString() + " is floating, v=" + node.voltage);
            }
        });
    }
    
    @Override
    protected double sum_leakage(){
        double sum=0;
        if(vdd == 0){
            throw new UnsupportedOperationException(name + ": VDD not set");
        }
        for(Object device : devices){
            if(device instanceof MOSDevice){
                MOSDevice mos = (MOSDevice) device;
                if(!Double.isNaN(mos.G.voltage) && !Double.isNaN(mos.D.voltage) && !Double.isNaN(mos.S.voltage)){
                    /*Calculate leak thru the MOSFET*/
                    double leakage = Math.abs(mos.model.Id_leak(mos.G.voltage - mos.S.voltage, mos.D.voltage - mos.S.voltage));
                    System.out.println("Leakage on device 0x" + Integer.toHexString(mos.hashCode()) + " G " + mos.G.toString() + " D " + mos.D.toString() + " S " + mos.S.toString() + " : " + leakage);
                    sum += leakage;
                }
            }
            else{
                throw new UnsupportedOperationException("Only MOSDevice is supported at this moment.");
            }
        }
        return sum;
    }
}
