/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;
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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.stream.IntStream;
import tech.*;
/**
 *
 * @author caoyuan9642
 */
public class Gate extends Module{
    
    public int output_table[];
    public double leakage_table[];
    
    private Deque<Node> q;
    private static final int MAX_RETRY=5;
    
    public Gate(String name){
        super(name);
    }
    
    public void addMOS(MOSFET model, String g_name, String d_name, String s_name){
        Node g = name_to_node(g_name);
        Node d = name_to_node(d_name);
        Node s = name_to_node(s_name);
        MOSDevice dev = new MOSDevice(model, g, d, s);
        devices.add(dev);
    }
    
    protected int getMaxInputs(){
        return 16;
    }
    
    protected int getMaxOutputs(){
        return 8;
    }
    
    private void init_logic(Object input[]){
        q = new ArrayDeque<>();
        q.addLast(nodes.get(0));
        q.addLast(nodes.get(1));
        for(int i=0;i<input.length;i++){
            input_nodes.get(i).logic = (Logic)input[i];
            q.addLast(input_nodes.get(i));
        }
    }
    
    private void update_logic(){
        while(!q.isEmpty()){
            Node node = q.pollFirst();
            System.out.println("Updating: " + node.toString());
            boolean retry = false;
            for (Device device : (List<Device>)node.devices) {
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
    
    private double sum_leakage(double vdd){
        double sum=0;
        for(Object device : devices){
            if(device instanceof MOSDevice){
                MOSDevice mos = (MOSDevice) device;
                if(mos.D.logic != Logic.UNKNOWN && mos.S.logic != Logic.UNKNOWN && mos.D.logic != mos.S.logic){
                    /*Calculate leak thru the MOSFET*/
                    double leakage = mos.model.Id_leak(0, vdd);
                    System.out.println("Leakage on device 0x" + Integer.toHexString(mos.hashCode()) + ": " + leakage);
                    sum += leakage;
                }
            }
            else{
                throw new UnsupportedOperationException("Only MOSDevice is supported at this moment.");
            }
        }
        return sum;
    }
    
    public void calcLeakageTable(double vdd){
        int l = input_nodes.size();
        int N = (1 << l);
        
        output_table = new int[N];
        leakage_table = new double[N];
        for(int input = 0; input < N; input++){
            List<Logic> list = new ArrayList<>();
            String.format("%" + l + "s", Integer.toBinaryString(input)).replace(' ', '0').chars().forEachOrdered((ch) -> {
                if(ch == '0')
                    list.add(Logic.LOW);
                else
                    list.add(Logic.HIGH);
            });
            System.out.println("Input: " + list);
            reset();
            init_logic(list.toArray());
            update_logic();
            int output = 0;
            for(Node node : output_nodes){
                Logic logic = node.logic;
                if(logic == Logic.UNKNOWN){
                    throw new UnsupportedOperationException(node.toString() + "has unknown logic.");
                }
                output = (output << 1) | (node.logic == Logic.HIGH ? 1 : 0);
            }
            output_table[input] = output;
            leakage_table[input] = sum_leakage(vdd);
            System.out.println("Output: " + String.format("%" + output_nodes.size() + "s", Integer.toBinaryString(output)).replace(' ', '0'));
            System.out.println("Leakage: " + leakage_table[input]);
        }
    }
    
    public void printTable(){
        if(output_table == null || leakage_table == null){
            System.err.println(name + ": table not calculated yet.");
            return;
        }
        List<String> input_names = new ArrayList();
        List<String> output_names = new ArrayList();
        input_nodes.stream().forEachOrdered(node -> {
            input_names.add(node.alias);
        });
        output_nodes.stream().forEachOrdered(node -> {
            output_names.add(node.alias);
        });
        
        for(int i=0;i<output_table.length;i++){
            List<Logic> input_list = new ArrayList<>();
            String.format("%" + input_nodes.size() + "s", Integer.toBinaryString(i)).replace(' ', '0').chars().forEachOrdered((ch) -> {
                if(ch == '0')
                    input_list.add(Logic.LOW);
                else
                    input_list.add(Logic.HIGH);
            });
            
            List<Logic> output_list = new ArrayList<>();
            String.format("%" + output_nodes.size() + "s", Integer.toBinaryString(output_table[i])).replace(' ', '0').chars().forEachOrdered((ch) -> {
                if(ch == '0')
                    output_list.add(Logic.LOW);
                else
                    output_list.add(Logic.HIGH);
            });
            System.out.println(input_names + " -> " + input_list + ": " + output_names + " = " + output_list + ", leak=" + leakage_table[i]);
            
        }
    }
    
    public static Module loadFile(String filename) throws Exception{
        File file = new File(filename);
        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
        Map<String, MOSFET> model_map = new TreeMap<>();
        Gate gate = new Gate(file.getName().substring(0,file.getName().lastIndexOf('.')));
        st.commentChar('#');/*Comment by '#'*/
        st.wordChars('_','_');
        st.lowerCaseMode(false);/*Case sensitive*/
        st.eolIsSignificant(true);
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
            String model_name;
            MOSFET model = null;
            switch(st.sval){
                case "model":
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    model_name = st.sval;
                    if(st.nextToken() != StreamTokenizer.TT_NUMBER){/*Read width*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    double width = st.nval * 1e-9;
                    if(st.nextToken() != StreamTokenizer.TT_NUMBER){/*Read length*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    double length = st.nval * 1e-9;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read Type*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    if(st.sval.startsWith("n")){
                        /*NMOS Model*/
                        model = new NMOS(width, length, Technology.Tech_GPDK90);
                    }
                    else if(st.sval.startsWith("p")){
                        /*PMOS Model*/
                        model = new PMOS(width, length, Technology.Tech_GPDK90);
                    }
                    else{
                        throw new UnsupportedOperationException("Unknown model type at line " + st.lineno());
                    }
                    model_map.put(model_name, model);
                    System.out.println(gate.name + ": Add model " + model_name + ", w=" + width + ", l=" + length + ", " + st.sval);
                    break;
                case "device":
                    /*Adds a device*/
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    model_name = st.sval;
                    if(!model_map.containsKey(model_name)){
                        throw new UnsupportedOperationException("Undefined model at line " + st.lineno());
                    }
                    model = model_map.get(model_name);
                    String g_name, d_name, s_name;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    g_name = st.sval;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    d_name = st.sval;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    s_name = st.sval;
                    gate.addMOS(model, g_name, d_name, s_name);
                    System.out.println(gate.name + ": Add device " + model_name + ", G=" + g_name + ", D=" + d_name + ", S=" + s_name);
                    break;
                case "input":
                    /*Adds an input*/
                    String input_name;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    input_name = st.sval;
                    gate.addInput(input_name);
                    System.out.println(gate.name + ": Add input " + input_name);
                    break;
                case "output":
                    /*Adds an input*/
                    String output_name;
                    if(st.nextToken() != StreamTokenizer.TT_WORD){/*Read model name*/
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    output_name = st.sval;
                    gate.addOutput(output_name);
                    System.out.println(gate.name + ": Add output " + output_name);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown command at line " + st.lineno());
            }
            while(st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF){/*Ignore everything until EOL or EOF*/
                st.nextToken();
            }
        }
        
        return gate;
    }

    @Override
    public Logic[] evaluateOutput(Logic[] input) {
        int index = 0;
        for(int i=0;i<input.length;i++)
            index = index << 1 | (input[i] == Logic.HIGH ? 1 : 0);
        last_leakage = leakage_table[index];
        //System.out.println(name + ": leak: " + last_leakage);
        int out = output_table[index];
        Logic []output = new Logic[output_nodes.size()];
        for(int i=output.length-1;i>=0;i--){
            output[i] = (out & 1) != 0 ? Logic.HIGH : Logic.LOW ;
            out >>= 1;
        }
        return output;
    }
    
}
