/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import tech.*;
/**
 *
 * @author caoyuan9642
 */
public class Gate {
    public String name;
    Map<String, Integer> node_map;
    List<Node> nodes;
    List<Node> input_nodes;
    List<Node> output_nodes;
    List<Device> devices;
    
    private Deque<Node> q;
    private static final int MAX_RETRY= 5;
    
    public Gate(String name){
        this.name = name;
        node_map = new TreeMap<>();
        nodes = new ArrayList<>();
        input_nodes = new ArrayList<>();
        output_nodes = new ArrayList<>();
        devices = new ArrayList<>();
        
        node_map.put("GND", 0);
        node_map.put("VDD", 1);
        nodes.add(new Node(0, "GND"));
        nodes.add(new Node(1, "VDD"));
    }
    
    public static final int INTERNAL=0;
    public static final int INPUT=1;
    public static final int OUTPUT=2;
    
    private Node name_to_node(String name){
        if(!node_map.containsKey(name)){
            node_map.put(name, nodes.size());
            nodes.add(new Node(nodes.size(), name));
        }
        return nodes.get(node_map.get(name));
    }
    
    public void addMOS(MOSFET model, String g_name, String d_name, String s_name){
        Node g = name_to_node(g_name);
        Node d = name_to_node(d_name);
        Node s = name_to_node(s_name);
        MOSDevice dev = new MOSDevice(this, model, g, d, s);
        devices.add(dev);
    }
    
    public void addInput(String name){
        Node n = name_to_node(name);
        input_nodes.add(n);
    }
    
    public void addOutput(String name){
        Node n = name_to_node(name);
        output_nodes.add(n);
    }
    
    private void reset_all(){
        for (Node n : nodes) {
            n.logic = Logic.UNKNOWN;
            n.retries = 0;
        }
    }
    
    private void init_logic(Logic input[]){
        q = new ArrayDeque<>();
        nodes.get(0).logic = Logic.LOW;     //GND
        nodes.get(1).logic = Logic.HIGH;    //VDD
        q.addLast(nodes.get(0));
        q.addLast(nodes.get(1));
        for(int i=0;i<input.length;i++){
            input_nodes.get(i).logic = input[i];
            q.addLast(input_nodes.get(i));
        }
    }
    
    private void update_logic(){
        while(!q.isEmpty()){
            Node node = q.pollFirst();
            System.out.println("Updating: " + node.toString());
            boolean retry = false;
            for (Device device : node.devices) {
                if(device instanceof MOSDevice){
                    MOSDevice mos = (MOSDevice) device;
                    if(node == mos.G){
                        /*Don't need do anything*/
                        /*Wait until D or S is updated*/
                        continue;
                    }
                    switch(mos.model.type){
                        case MOSFET.NMOS:
                            if(mos.G.logic == Logic.HIGH){
                                if(mos.D.logic != Logic.UNKNOWN && mos.S.logic != Logic.UNKNOWN){
                                    if(mos.D.logic != mos.S.logic){
                                        throw new UnsupportedOperationException("Short circuit detected. Aborting");
                                    }
                                    else{
                                        continue;
                                    }
                                }
                                System.out.println(" - " + mos.toString());
                                if(node == mos.D){
                                    mos.S.logic = node.logic;
                                    q.addLast(mos.S);
                                }
                                else{
                                    mos.D.logic = node.logic;
                                    q.addLast(mos.D);
                                }
                            }
                            else if(mos.G.logic == Logic.UNKNOWN){
                                /*Gate not updated yet, postpone*/
                                node.retries++;
                                if(node.retries > MAX_RETRY){
                                    throw new UnsupportedOperationException("Floating gate detected. Unsupported yet.");
                                }
                                System.out.println("Updating " + node.toString() + " postponed.");
                                retry = true;
                            }
                            break;
                        case MOSFET.PMOS:
                            if(mos.G.logic == Logic.LOW){
                                if(mos.D.logic != Logic.UNKNOWN && mos.S.logic != Logic.UNKNOWN){
                                    if(mos.D.logic != mos.S.logic){
                                        throw new UnsupportedOperationException("Short circuit detected. Aborting");
                                    }
                                    else{
                                        continue;
                                    }
                                }
                                System.out.println(" - " + mos.toString());
                                if(node == mos.D){
                                    mos.S.logic = node.logic;
                                    q.addLast(mos.S);
                                }
                                else{
                                    mos.D.logic = node.logic;
                                    q.addLast(mos.D);
                                }
                            }
                            else if(mos.G.logic == Logic.UNKNOWN){
                                /*Gate not updated yet, postpone*/
                                node.retries++;
                                if(node.retries > MAX_RETRY){
                                    throw new UnsupportedOperationException("Floating gate detected. Unsupported yet.");
                                }
                                retry = true;
                            }
                            break;
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
    }
    
    public boolean updateLogic(Logic ... logic){
        reset_all();
        init_logic(logic);
        try{
            update_logic();
        }catch(Exception e){
            return true;
        }
        return false;
    }
    
    public double sumLeakage(double vdd){
        double sum=0;
        for(Device device : devices){
            if(device instanceof MOSDevice){
                MOSDevice mos = (MOSDevice) device;
                if(mos.D.logic != Logic.UNKNOWN && mos.S.logic != Logic.UNKNOWN && mos.D.logic != mos.S.logic){
                    /*Calculate leak thru the MOSFET*/
                    double leakage = mos.model.Id_leak(0, vdd);
                    sum += leakage;
                }
            }
            else{
                throw new UnsupportedOperationException("Only MOSDevice is supported at this moment.");
            }
        }
        return sum;
    }
    
    @Override
    public String toString(){
        String s = "";
        s += "Gate: " + name + "\n";
        s += "Nodes: \n";
        for(Iterator<Node> it = nodes.iterator();it.hasNext();){
            s += " - " + it.next().toString() + "\n";
        }
        s += "Input Nodes: \n";
        for(Iterator<Node> it = input_nodes.iterator();it.hasNext();){
            s += " - " + it.next().toString() + "\n";
        }
        s += "Output Nodes: \n";
        for(Iterator<Node> it = output_nodes.iterator();it.hasNext();){
            s += " - " + it.next().toString() + "\n";
        }
        
        s += "Devices: \n";
        for(Iterator<Device> it = devices.iterator();it.hasNext();){
            s += " - " + it.next().toString() + "\n";
        }
        return s;
    }
    
    public void printLogic(){
        for(Node n: nodes){
            System.out.println(n.toString() + " " + n.logic.toString());
        }
    }
    
    public static void main(String args[]){
        Gate g = new Gate("NAND");
        NMOS nmos = new NMOS(240e-9, 100e-9, Technology.Tech_GPDK90);
        PMOS pmos = new PMOS(480e-9, 100e-9, Technology.Tech_GPDK90);
        
        g.addMOS(pmos, "A", "OUT", "VDD");
        g.addMOS(pmos, "B", "OUT", "VDD");
        g.addMOS(nmos, "A", "OUT", "INT");
        g.addMOS(nmos, "B", "INT", "GND");
        g.addInput("A");
        g.addInput("B");
        g.addOutput("OUT");
        
        System.out.println(g.toString());
        
        g.updateLogic(Logic.HIGH, Logic.LOW);
        g.printLogic();
        System.out.println("Leakage: " + g.sumLeakage(1.2));
    }
}
