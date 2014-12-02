/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import gate.*;
import circuit.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author caoyuan9642
 */
public class ISC89Loader {
    
    public static Circuit loadFile(String filename) throws Exception{
        File file = new File(filename);
        if(!file.exists()){
            System.out.println("Error: file not found");
            return null;
        }
        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
        st.commentChar('#');
        st.ordinaryChars('0', '9');
        st.wordChars('.', '.');
        st.wordChars('0', '9');
        st.lowerCaseMode(true);
        Circuit circ = new Circuit(file.getName().substring(0,file.getName().lastIndexOf('.')));
        Gate gate_not = (Gate) TableGate.loadFile("INV.gate");
        Gate gate_dff = (Gate) TableGate.loadFile("DFF.gate");
        Gate gate_nor2 = (Gate) TableGate.loadFile("NOR2.gate");
        Gate gate_nor3 = (Gate) TableGate.loadFile("NOR3.gate");
        Gate gate_nor4 = (Gate) TableGate.loadFile("NOR4.gate");
        Gate gate_xor2 = (Gate) TableGate.loadFile("XOR2.gate");
        Gate gate_or2 = (Gate) TableGate.loadFile("OR2.gate");
        Gate gate_or3 = (Gate) TableGate.loadFile("OR3.gate");
        Gate gate_or4 = (Gate) TableGate.loadFile("OR4.gate");
        Gate gate_nand2 = (Gate) TableGate.loadFile("NAND2.gate");
        Gate gate_nand3 = (Gate) TableGate.loadFile("NAND3.gate");
        Gate gate_nand4 = (Gate) TableGate.loadFile("NAND4.gate");
        Gate gate_and2 = (Gate) TableGate.loadFile("AND2.gate");
        Gate gate_and3 = (Gate) TableGate.loadFile("AND3.gate");
        Gate gate_and4 = (Gate) TableGate.loadFile("AND4.gate");
        while(true){
            if(st.nextToken() == StreamTokenizer.TT_EOF){
                break;
            }
            if(st.ttype != StreamTokenizer.TT_WORD){
                throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
            }
            switch(st.sval){
                case "input":
                    if(st.nextToken() != (int)'('){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    if(st.nextToken() != StreamTokenizer.TT_WORD){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    String input_name = st.sval;
                    circ.addInput(input_name);
                    System.out.println(circ.name + ": Add input " + input_name);
                    if(st.nextToken() != (int)')'){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    break;
                case "output":
                    if(st.nextToken() != (int)'('){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    if(st.nextToken() != StreamTokenizer.TT_WORD){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    String output_name = st.sval;
                    circ.addOutput(output_name);
                    System.out.println(circ.name + ": Add output " + output_name);
                    if(st.nextToken() != (int)')'){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    break;
                default:
                    /*devices*/
                    String out = st.sval;
                    if(st.nextToken() != (int)'='){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    if(st.nextToken() != StreamTokenizer.TT_WORD){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    String gate_name = st.sval;
                    if(st.nextToken() != (int)'('){
                        throw new UnsupportedOperationException(circ.name + ": input format error at " + st.lineno());
                    }
                    List<String> inputs = new ArrayList();
                    st.nextToken();
                    while(st.ttype != StreamTokenizer.TT_EOF && st.ttype != (int)')'){
                        String input = st.sval;
                        inputs.add(input);
                        st.nextToken();
                        if(st.ttype == (int)',')
                            st.nextToken();
                    }
                    System.out.println(circ.name + ": " + gate_name + " " + inputs + " -> " + out);
                    inputs.add(out);
                    Gate g2 = null;
                    Gate g3 = null;
                    Gate g4 = null;
                    switch(gate_name){
                        case "not":
                            if(inputs.size() != 2){
                                throw new UnsupportedOperationException(circ.name + ": input format error at gate: " + gate_name + " " + inputs + " -> " + out + ", at " + st.lineno());
                            }
                            circ.addModule(gate_not, Arrays.asList(new String[]{"A","OUT"}), inputs);
                            break;
                        case "buff": case "buf":
                            if(inputs.size() != 2){
                                throw new UnsupportedOperationException(circ.name + ": input format error at gate: " + gate_name + " " + inputs + " -> " + out + ", at " + st.lineno());
                            }
                            List<String> intermediate = new ArrayList(inputs);
                            String internal = intermediate.get(0) + "-inter";
                            intermediate.set(1, internal);
                            inputs.set(0, internal);
                            circ.addModule(gate_not, Arrays.asList(new String[]{"A","OUT"}), intermediate);
                            circ.addModule(gate_not, Arrays.asList(new String[]{"A","OUT"}), inputs);
                            break;
                        case "dff":
                            if(inputs.size() != 2){
                                throw new UnsupportedOperationException(circ.name + ": input format error at gate: " + gate_name + " " + inputs + " -> " + out + ", at " + st.lineno());
                            }
                            inputs.remove(1);
                            circ.addInput(out);/*Output of sequential logic must be considered as extra inputs*/
                            circ.addModule(gate_dff, Arrays.asList(new String[]{"D"}), inputs);
                            break;
                        case "and":
                            g2 = gate_and2;
                            g3 = gate_and3;
                            g4 = gate_and4;
                            add_gate(inputs, g2, g3, g4, circ);
                            break;
                        case "or":
                            g2 = gate_or2;
                            g3 = gate_or3;
                            g4 = gate_or4;
                            add_gate(inputs, g2, g3, g4, circ);
                            break;
                        case "xor":
                            g2 = gate_xor2;
                            g3 = null;
                            g4 = null;
                            add_gate(inputs, g2, g3, g4, circ);
                            break;
                        case "nand":
                            g2 = gate_nand2;
                            g3 = gate_nand3;
                            g4 = gate_nand4;
                            add_gate(inputs, g2, g3, g4, circ);
                            break;
                        case "nor":
                            g2 = gate_nor2;
                            g3 = gate_nor3;
                            g4 = gate_nor4;
                            add_gate(inputs, g2, g3, g4, circ);
                            break;
                        default:
                            throw new UnsupportedOperationException(circ.name + ": unsupported gate name " + gate_name + " at " + st.lineno());
                    }
                    break;
            }
        }
        circ.setVDD(1.2);
        
        return circ;
    }
    
    private static void add_gate(List<String> inputs, Gate g2, Gate g3, Gate g4, Circuit circ){
        if(inputs.size() < 3){
            throw new UnsupportedOperationException(circ.name + ": input format error at gate " + inputs);
        }
        if(inputs.size() == 3){
            circ.addModule(g2, Arrays.asList(new String[]{"A","B","OUT"}), inputs);
        }
        else if(inputs.size() == 4){
            circ.addModule(g3, Arrays.asList(new String[]{"A","B","C","OUT"}), inputs);
        }
        else if(inputs.size() == 5){
            circ.addModule(g4, Arrays.asList(new String[]{"A","B","C","D","OUT"}), inputs);
        }
        else if(g2.name.startsWith("AND") || g2.name.startsWith("OR")){
            /*Split*/
            int n = inputs.size() - 1;
            int d = 0;
            Gate g = null;
            Gate gb = null;
            if(n % 4 == 0){
                d = 4;
                g = g4;
            }
            else if(n % 3 == 0){
                d = 3;
                g = g3;
            }
            else if(n == 5){
                d = 3;
                g = g3;
                gb = g2;
            }
            else if(n == 7){
                d = 4;
                g = g4;
                gb = g3;
            }
            List<String> q = new ArrayList();
            for(int i=0;i<n;i+=d){
                if(i + d > n){
                    d--;
                    g = gb;
                }
                List<String> p = new ArrayList(inputs.subList(i, i+d));
                String internal = inputs.get(inputs.size()-1) + "-" + i;
                p.add(internal);
                q.add(internal);
                if(d == 2)
                    circ.addModule(g, Arrays.asList(new String[]{"A","B","OUT"}), p);
                else if(d == 3)
                    circ.addModule(g, Arrays.asList(new String[]{"A","B","C","OUT"}), p);
                else
                    circ.addModule(g, Arrays.asList(new String[]{"A","B","C","D","OUT"}), p);
            }
            q.add(inputs.get(inputs.size()-1));
            switch(q.size()-1){
                case 2:
                    circ.addModule(g2, Arrays.asList(new String[]{"A","B","OUT"}), q);
                    break;
                case 3:
                    circ.addModule(g3, Arrays.asList(new String[]{"A","B","C","OUT"}), q);
                    break;
                case 4:
                    circ.addModule(g4, Arrays.asList(new String[]{"A","B","C","D","OUT"}), q);
                    break;
                                        
            }
        }
    }
    
    public static void main(String args[]) throws Exception{
        Circuit circ = ISC89Loader.loadFile("benchmark/c7552.bench");
        System.out.println(circ.toString());
        
        MonteCarloLeakageMinimizer lm = new MonteCarloLeakageMinimizer();
        //LeakageMinimizer lm = new LeakageMinimizer();
        lm.setModule(circ);
        lm.minimizeLeakageWithOutput("benchmark/out.dat");
    }
}
