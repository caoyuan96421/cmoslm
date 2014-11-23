/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;

/**
 *
 * @author caoyuan9642
 */
public class Device {
    
    public Gate gate;
    
    public Device(Gate gate, Node ... nodes){
        this.gate = gate;
        for (Node n : nodes){
            n.addDevice(this);
        }
    }
    
    public String toString(){
        return "";
    }
}
