/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import spread.SpreadMessage;

/**
 *
 * @author Alvaro
 */
public class Lider extends ArduinoState{

    @Override
    public void sendMSG(ArduinoUser user, String msg) {
        super.sendMSGtoSpread(user.getNombre()+"@"+msg, user.getConnection(), user.getConnectionGroup());
    }

    @Override
    public void reciveMSG(ArduinoUser user, SpreadMessage sm) {
        String [] tokens = (new String(sm.getData())).split("@");
        System.out.println("Not lider send MSG \n \033[31m" + user.getNombre()+ "_INFO:\033[30m received from: " + tokens[0] + " content: " + tokens[1]);     
    }

    @Override
    public void work(ArduinoUser user) {
        try{
           user.sendData("7", user.getMediaPuertoSerie());
           user.sendVIVO(); 
        } catch (InterruptedException ex) {
            System.out.println("interruped Thread");
        }
        
    }

    
}
