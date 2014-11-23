/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    
    /**Test function*/
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
    }
}
