/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;

import gate.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author caoyuan9642
 */
public class ModuleInstance {
    public Module module;
    Node inputs[];
    Node outputs[];
    
    public ModuleInstance(Module module){
        this.module = module;
        inputs = new Node[module.getInputSize()];
        outputs = new Node[module.getOutputSize()];
    }
    
    public void setInput(String name, Node node){
        int index = module.getInputNodeIndex(name);
        if(index == -1){
            throw new java.lang.UnsupportedOperationException(module.name + ": Input not found: " + name);
        }
        inputs[index] = node;
    }
    
    public void setOutput(String name, Node node){
        int index = module.getOutputNodeIndex(name);
        if(index == -1){
            throw new java.lang.UnsupportedOperationException(module.name + ": Input not found: " + name);
        }
        outputs[index] = node;
    }
    
    public boolean isInputKnown(){
        for(Node n : inputs)
            if(n.logic == Logic.UNKNOWN)
                return false;
        return true;
    }
    
    public void updateOutput(){
        int input = 0;
        for(Node n : inputs){
            input = input << 1 | (n.logic == Logic.HIGH ? 1 : 0);
        }
        int output = module.evaluateOutput(input);
        for(int i=outputs.length-1, j=1;i>=0;i--, j<<=1){
            outputs[i].logic = ((output & j) != 0 ? Logic.HIGH : Logic.LOW);
        }
    }
    
    public String toString(){
        String s = module.toString();
        s += "Binding: ";
        List<String> inames = new ArrayList();
        List<String> onames = new ArrayList();
        List<String> ibnames = new ArrayList();
        List<String> obnames = new ArrayList();
        module.input_nodes.stream().forEachOrdered(node -> {
            inames.add(node.alias);
        });
        module.output_nodes.stream().forEachOrdered(node -> {
            onames.add(node.alias);
        });
        Arrays.asList(inputs).stream().forEachOrdered(node -> {
            ibnames.add(node.alias);
        });
        Arrays.asList(outputs).stream().forEachOrdered(node -> {
            obnames.add(node.alias);
        });
        s += inames + "->" + ibnames + ", " + onames + "->" + obnames;
        return s;
    }
    
    public String toShortString(){
        String s = module.name + " ";
        List<String> inames = new ArrayList();
        List<String> onames = new ArrayList();
        List<String> ibnames = new ArrayList();
        List<String> obnames = new ArrayList();
        module.input_nodes.stream().forEachOrdered(node -> {
            inames.add(node.alias);
        });
        module.output_nodes.stream().forEachOrdered(node -> {
            onames.add(node.alias);
        });
        Arrays.asList(inputs).stream().forEachOrdered(node -> {
            ibnames.add(node.alias);
        });
        Arrays.asList(outputs).stream().forEachOrdered(node -> {
            obnames.add(node.alias);
        });
        s += inames + "->" + ibnames + ", " + onames + "->" + obnames;
        return s;
    }
}
