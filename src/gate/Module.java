/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import circuit.Circuit;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author caoyuan9642
 */
public abstract class Module {
    public String name;
    protected Map<String, Integer> node_map;
    protected List<Node> nodes;
    protected List<Node> input_nodes;
    protected List<Node> output_nodes;
    protected List<Object> devices;
    protected double last_leakage;
        
    public Module(String name){
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
    
    protected Node name_to_node(String name){
        if(!node_map.containsKey(name)){
            node_map.put(name, nodes.size());
            nodes.add(new Node(nodes.size(), name));
        }
        return nodes.get(node_map.get(name));
    }
    
    public void addInput(String name){
        Node n = name_to_node(name);
        if(input_nodes.size() < getMaxInputs())
            input_nodes.add(n);
        else
            throw new UnsupportedOperationException("Too many inputs in gate " + name);
    }
    
    public void addOutput(String name){
        Node n = name_to_node(name);
        if(output_nodes.size() < getMaxOutputs())
            output_nodes.add(n);
        else
            throw new UnsupportedOperationException("Too many outputs in gate " + name);
    }
    
    public void reset(){
        nodes.stream().forEach((n) -> {
            n.logic = Logic.UNKNOWN;
            n.retries = 0;
        });
        nodes.get(0).logic = Logic.LOW;     //GND
        nodes.get(1).logic = Logic.HIGH;    //VDD
        
        last_leakage = 0;
    }
    
    public int getInputSize(){
        return input_nodes.size();
    }
    
    public int getOutputSize(){
        return output_nodes.size();
    }
    
    public int getInputNodeIndex(String name){
        if(!node_map.containsKey(name)){
            return -1;
        }
        Node n = nodes.get(node_map.get(name));
        return input_nodes.indexOf(n);
    }
    
    public int getOutputNodeIndex(String name){
        if(!node_map.containsKey(name)){
            return -1;
        }
        Node n = nodes.get(node_map.get(name));
        return output_nodes.indexOf(n);
    }
    
    public boolean isInputNode(String n){
        return node_map.containsKey(n) && input_nodes.contains(name_to_node(n));
    }
    
    public boolean isOutputNode(String n){
        return node_map.containsKey(n) && output_nodes.contains(name_to_node(n));
    }
    
    @Override
    public String toString(){
        String s = "", s1 = "";
        s += getClass().getSimpleName() + ": " + name + "\n";
        s += "Nodes: \n";
        s = nodes.stream().map((node) -> " - " + node.toString() + "\n").reduce(s, String::concat);
        s += "Input Nodes: \n";
        s = input_nodes.stream().map((node) -> " - " + node.toString() + "\n").reduce(s, String::concat);
        s += "Output Nodes: \n";
        s = output_nodes.stream().map((node) -> " - " + node.toString() + "\n").reduce(s, String::concat);
        
        s += "Devices: \n";
        s1 = devices.stream().map((device) -> device.toString() + "\n").reduce(s1, String::concat);
        s1 = s1.replaceAll("\n", "\n\t");
        s1 = "\t" + s1.substring(0,s1.length()-1);
        s += s1;
        return s;
    }
    
    public void printLogic(){
        nodes.stream().forEachOrdered((n) -> {
            System.out.println(n.toString() + " " + n.logic.toString());
        });
    }
    
    public void printOutput(){
        System.out.println(name + " output: " +
                Arrays.asList(output_nodes.stream().map(node -> node.alias).toArray()) + "->" + 
                Arrays.asList(output_nodes.stream().map(node -> node.logic).toArray()));
    }
    
    public void printInput(){
        System.out.println(name + " input: " + 
                Arrays.asList(input_nodes.stream().map(node -> node.alias).toArray()) + "->" +
                Arrays.asList(input_nodes.stream().map(node -> node.logic).toArray()));
    }

    protected int getMaxInputs() {
        return 256;
    }

    protected int getMaxOutputs() {
        return 256;
    }
    
    /**
     * Evaluates leakage and outputs for the given input
     * @param input
     * @return
     */
    public abstract Logic[] evaluateOutput(Logic []input);
    public double collectLeakage(){
        return last_leakage;
    }
    
    public static Module loadFile(String filename) throws Exception{
        if(filename.endsWith(".circ"))
            return Circuit.loadFile(filename);
        else if(filename.endsWith(".gate"))
            return Gate.loadFile(filename);
        else 
            return null;
    }
}
