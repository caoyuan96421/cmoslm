/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import gate.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tech.MOSFET;
import tech.NMOS;
import tech.PMOS;
import tech.Technology;
/**
 *
 * @author caoyuan9642
 */
public class Circuit extends Module{
    
    private List<ModuleInstance> update_order;
    
    public Circuit(String name){
        super(name);
    }
    
    public void addModule(Module m, List<String> ports, List<String> nodes){
        if(ports.size() != nodes.size() || ports.size() != m.getInputSize() + m.getOutputSize()){
            System.err.println(name + ": input or output size mismatch in " + m.name);
            return;
        }
        
        ModuleInstance gw = new ModuleInstance(m);
        for(int i=0;i<ports.size();i++){
            Node n = name_to_node(nodes.get(i));
            if(m.isInputNode(ports.get(i)))
                gw.setInput(ports.get(i), n);
            else if(m.isOutputNode(ports.get(i)))
                gw.setOutput(ports.get(i), n);
        }
        devices.add(gw);
    }
    
    private void find_update_order(){
        update_order = new ArrayList();
        Deque<ModuleInstance> q = new ArrayDeque(devices);
        reset();
        input_nodes.stream().forEachOrdered(node -> {node.logic = Logic.LOW;});
        int counter=0, last=0;
        while(!q.isEmpty()){
            ModuleInstance m = (ModuleInstance) q.pollFirst();
            counter++;
            if(m.isInputKnown()){
                m.updateOutput();
                update_order.add(m);
                last=counter;
            }
            else{
                if(counter - last >= 2 * q.size() + 2){
                    /*We've gone through the whole queue for two passes, but nothing updated*/
                    throw new UnsupportedOperationException(name + ": Loop detected in circuit.");
                }
                q.addLast(m);
            }
        }
        System.out.println(name + ": update order found: \n" + update_order.stream().map(mod->mod.toShortString()).reduce("", (s1,s2)->{return s1+" ---> "+s2;}));
    }
    
    @Override
    public Logic[] evaluateOutput(Logic input[]) {
        if(update_order == null)
            find_update_order();
        
        reset();
        for (int i=0;i<input.length;i++){
            input_nodes.get(i).logic = input[i];
        }
        
        update_order.stream().forEachOrdered((mod) -> {
            mod.updateOutput();
            last_leakage += mod.module.collectLeakage();
        });
        
        Logic output[] = new Logic[output_nodes.size()];
        for (int i=0;i<output.length;i++){
            Logic logic = output_nodes.get(i).logic;
            if(logic == Logic.UNKNOWN){
                throw new UnsupportedOperationException(name + ": " + output_nodes.get(i).toString() + "floating");
            }
            output[i] = logic;
        }
        return output;
    }
    
    public static Module loadFile(String filename) throws Exception{
        File file = new File(filename);
        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
        Map<String, Module> module_map = new TreeMap<>();
        Circuit circ = new Circuit(file.getName().substring(0,file.getName().lastIndexOf('.')));
        st.commentChar('#');/*Comment by '#'*/
        st.wordChars('_','_');
        st.wordChars('=','=');
        st.lowerCaseMode(false);/*Case sensitive*/
        st.eolIsSignificant(true);
        double vdd = -1;
        while(st.ttype != StreamTokenizer.TT_EOF){
            st.nextToken();
            if(st.ttype == StreamTokenizer.TT_EOF){
                break;
            }
            if(st.ttype == StreamTokenizer.TT_EOL){
                continue;
            }
            if(st.ttype != StreamTokenizer.TT_WORD){/*Read command token in a line*/
                throw new UnsupportedOperationException("Input format error at line " + st.lineno());
            }
            Module module;
            switch(st.sval){
                case "vdd":
                    if(st.nextToken() != StreamTokenizer.TT_NUMBER){
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    vdd = st.nval;
                    System.out.println(circ.name + ": VDD set to " + vdd);
                    break;
                case "include":
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read module name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    String module_name = st.sval;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read file name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    String module_file = st.sval;
                    module = Module.loadFile(module_file);
                    module_map.put(module_name, module);
                    if(module instanceof Gate){
                        Gate g = (Gate) module;
                        if(vdd == -1){
                            throw new UnsupportedOperationException("VDD must be defined first " + st.lineno());
                        }
                        g.calcLeakageTable(vdd);
                        g.printTable();
                    }
                    System.out.println(circ.name + ": Add module " + module_name + " (" + module.name + ")");
                    break;
                case "device":
                    /*Adds a device*/
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    module_name = st.sval;
                    if(!module_map.containsKey(module_name)){
                        throw new UnsupportedOperationException("Undefined module at line " + st.lineno());
                    }
                    module = module_map.get(module_name);
                    List<String> ports = new ArrayList();
                    List<String> nodes = new ArrayList();
                    st.nextToken();
                    do{
                        if(st.ttype != StreamTokenizer.TT_WORD){
                            throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                        }
                        if(st.sval.indexOf('=')==-1)
                            continue;
                        String port_name = st.sval.substring(0,st.sval.indexOf('='));
                        String node_name = st.sval.substring(st.sval.indexOf('=')+1);
                        ports.add(port_name);
                        nodes.add(node_name);
                        st.nextToken();
                    }while(st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF);
                    circ.addModule(module, ports, nodes);
                    System.out.println(circ.name + ": Add module instance of " + module_name + ": " + ports + "->" + nodes);
                    break;
                case "input":
                    /*Adds an input*/
                    String input_name;
                    st.nextToken();
                    do{
                        if(st.ttype != StreamTokenizer.TT_WORD){/*Read model name*/
                            throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                        }
                        input_name = st.sval;
                        circ.addInput(input_name);
                        System.out.println(circ.name + ": Add input " + input_name);
                        st.nextToken();
                    }while(st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF);
                    break;
                case "output":
                    /*Adds an output*/
                    String output_name;
                    st.nextToken();
                    do{
                        if(st.ttype != StreamTokenizer.TT_WORD){/*Read model name*/
                            throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                        }
                        output_name = st.sval;
                        circ.addOutput(output_name);
                        System.out.println(circ.name + ": Add output " + output_name);
                        st.nextToken();
                    }while(st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown command at line " + st.lineno());
            }
            while(st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF){/*Ignore everything until EOL or EOF*/
                st.nextToken();
            }
        }
        
        return circ;
    }
    
    public static void main(String args[]) throws Exception{
        //Circuit circ = (Circuit) Circuit.loadFile("FA.circ");
        Module circ = Module.loadFile("FA16.circ");
        if(circ instanceof Gate)
            ((Gate)circ).calcLeakageTable(1.2);
        System.out.println(circ.toString());
        for(int i=10000000;i>0;i--){
            circ.evaluateOutput(new Logic[]{
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.HIGH,Logic.HIGH,Logic.HIGH,Logic.HIGH,
                Logic.LOW
            });
        }
        System.out.println(circ.collectLeakage());
        circ.printInput();
        circ.printOutput();
        /*Gate xor = Gate.loadFile("XOR.gate");
        xor.calcLeakageTable(1.2);
        xor.printTable();
        Gate nand = Gate.loadFile("NAND.gate");
        nand.calcLeakageTable(1.2);
        nand.printTable();
        Gate inv = Gate.loadFile("Inverter.gate");
        inv.calcLeakageTable(1.2);
        inv.printTable();
        
        Circuit and = new Circuit("AND");
        and.addModule(nand, new String[]{"A","B"}, new String[]{"A","B"}, new String[]{"OUT"}, new String[]{"AND"});
        and.addModule(inv, new String[]{"A"}, new String[]{"AND"}, new String[]{"Y"}, new String[]{"OUT"});
        and.addInput("A");
        and.addInput("B");
        and.addOutput("OUT");
        
        Circuit or = new Circuit("OR");
        or.addModule(inv, new String[]{"A"}, new String[]{"A"}, new String[]{"Y"}, new String[]{"A_"});
        or.addModule(inv, new String[]{"A"}, new String[]{"B"}, new String[]{"Y"}, new String[]{"B_"});
        or.addModule(nand, new String[]{"A","B"}, new String[]{"A_","B_"}, new String[]{"OUT"}, new String[]{"OUT"});
        or.addInput("A");
        or.addInput("B");
        or.addOutput("OUT");
        
        Circuit c = new Circuit("FA");
        c.addModule(xor, new String[]{"A","B"}, new String[]{"A","B"}, new String[]{"OUT"}, new String[]{"P"});
        c.addModule(xor, new String[]{"A","B"}, new String[]{"C","P"}, new String[]{"OUT"}, new String[]{"S"});
        c.addModule(and, new String[]{"A","B"}, new String[]{"A","B"}, new String[]{"OUT"}, new String[]{"G"});
        c.addModule(and, new String[]{"A","B"}, new String[]{"C","P"}, new String[]{"OUT"}, new String[]{"D"});
        c.addModule(or, new String[]{"A","B"}, new String[]{"G","D"}, new String[]{"OUT"}, new String[]{"Cout"});
        
        c.addInput("A");
        c.addInput("B");
        c.addInput("C");
        c.addOutput("S");
        c.addOutput("Cout");
        
        int input = 0x07;
        c.evaluateOutput(input);
        c.printInput();
        c.printOutput();
        System.out.println(c.collectLeakage());
        
        Gate fa = Gate.loadFile("FA.gate");
        fa.calcLeakageTable(1.2);
        fa.printTable();
        
        Circuit d = new Circuit("FA2");
        d.addModule(fa, new String[]{"A","B","C"}, new String[]{"A","B","C"}, new String[]{"S", "Cout"}, new String[]{"S","Cout"});
        d.addInput("A");
        d.addInput("B");
        d.addInput("C");
        d.addOutput("S");
        d.addOutput("Cout");
        d.evaluateOutput(input);
        d.printInput();
        d.printOutput();
        System.out.println(d.collectLeakage());
        d.evaluateOutput(input-1);
        d.printInput();
        d.printOutput();
        System.out.println(d.collectLeakage());*/
        //System.out.println("Leakage: " + g.evaluate(new Logic[]{Logic.HIGH, Logic.HIGH}, 1.2));
    }

}
