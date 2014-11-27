/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author caoyuan9642
 * @param <T>
 */
public class Node <T> {
    public List<T> devices;
    public int id;
    public String alias = "";
    
    public Logic logic = Logic.UNKNOWN;
    int retries = 0;
    
    public static final int GROUND = 0;
    public static final int VDD = 1;
    
    public Node(int id){
        devices = new ArrayList<>();
        this.id = id;
    }
    
    public Node(int id, String alias){
        this(id);
        this.alias = alias;
    }
    
    public void addDevice(T d){
        devices.add(d);
    }
    
    @Override
    public String toString(){
        return "node " + alias + " (" + id + ")";
    }
    
    public void printDevices(){
        System.out.println("Node " + alias + " (" + id + ")");
        devices.stream().forEachOrdered((device) -> {
            System.out.println(" -" + device.toString());
        });
    }
}
