/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate;
import tech.*;
/**
 *
 * @author caoyuan9642
 */
public class MOSDevice extends Device{
    public MOSFET model;
    public Node G, D, S;

    public MOSDevice(MOSFET model, Node G, Node D, Node S) {
        super(G, D, S);
        this.model = model;
        this.G = G;
        this.D = D;
        this.S = S;
    }
    
    public String toString(){
        return String.format("0x%08x",hashCode()) + " " + model.toString() + " G: " + G.toString() + " D: " + D.toString() + " S: " + S.toString();
    }
}
