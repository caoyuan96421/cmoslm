/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 *
 * @author caoyuan9642
 */
public class TableGate extends Gate{

    
    public TableGate(String name) {
        super(name);
    }
    
    @Override
    public void setVDD(double vdd){
        if(vdd != this.vdd){
            throw new UnsupportedOperationException(name + ": VDD not compatible. Required " + this.vdd + ", provided " + vdd);
        }
        printTable();
    }
    
    public static Module loadFile(String filename) throws Exception{
        File file = new File(filename);
        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
        st.commentChar('#');/*Comment by '#'*/
        st.wordChars('_','_');
        st.lowerCaseMode(false);/*Case sensitive*/
        st.eolIsSignificant(true);
        st.ordinaryChars('0', '9');
        st.ordinaryChar('.');
        st.wordChars('0', '9');
        st.wordChars('.', '.');
        TableGate gate = new TableGate(file.getName().substring(0,file.getName().lastIndexOf('.')));
        gate.vdd = 0;
        while(st.ttype != StreamTokenizer.TT_EOF){
            st.nextToken();
            if(st.ttype == StreamTokenizer.TT_EOF){
                throw new UnsupportedOperationException("Unexpected EOF at line " + st.lineno());
            }
            if(st.ttype == StreamTokenizer.TT_EOL){
                continue;
            }
            if(st.ttype != StreamTokenizer.TT_WORD){/*Read command token in a line*/
                throw new UnsupportedOperationException("Unknown character at line " + st.lineno());
            }
            if(Character.isDigit(st.sval.charAt(0))){
                break;
            }
            switch(st.sval){
                case "table":
                    break;
                case "vdd":
                    if(st.nextToken() != StreamTokenizer.TT_WORD){
                        throw new UnsupportedOperationException("Input format error at line " + st.lineno());
                    }
                    gate.vdd = Double.parseDouble(st.sval);
                    System.out.println(gate.name + ": VDD set to " + gate.vdd);
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
                        gate.addInput(input_name);
                        System.out.println(gate.name + ": Add input " + input_name);
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
                        gate.addOutput(output_name);
                        System.out.println(gate.name + ": Add output " + output_name);
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
        if(gate.vdd == 0){
            throw new UnsupportedOperationException(gate.name + ": VDD must be set");
        }
        int table_size = (1<<gate.getInputSize());
        gate.output_table = new int[table_size];
        gate.leakage_table = new double[table_size];
        
        for(int i=0;i<table_size;i++){
            if(st.ttype != StreamTokenizer.TT_WORD){
                throw new UnsupportedOperationException("Input format error at line " + st.lineno());
            }
            String input = st.sval;
            st.nextToken();
            if(st.ttype != StreamTokenizer.TT_WORD){
                throw new UnsupportedOperationException("Input format error at line " + st.lineno());
            }
            String output = st.sval;
            st.nextToken();
            if(st.ttype != StreamTokenizer.TT_WORD){
                throw new UnsupportedOperationException("Input format error at line " + st.lineno());
            }
            double leakage = Double.parseDouble(st.sval);
            int index = Integer.parseInt(input, 2);
            int out = Integer.parseInt(output, 2);
            System.out.println(index + " " + out + " " + leakage);
            gate.output_table[index] = out;
            gate.leakage_table[index] = leakage;
            while(st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF){/*Ignore everything until EOL or EOF*/
                st.nextToken();
            }
            st.nextToken();
        }
        return gate;
    }
}
