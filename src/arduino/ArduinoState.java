/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

/**
 *
 * @author Alvaro
 */
public abstract class ArduinoState {
    
    
    
    /**
     * manda un mensaje 
     * @param msg mensaje a enviar
     * @param connection conexion a través de la que se envia
     * @param group grupo al que se envia
     */
    protected void sendMSGtoSpread(String msg, SpreadConnection connection, SpreadGroup group){
        try{
            SpreadMessage ms = new SpreadMessage();
            ms.setData(msg.getBytes());
            ms.addGroup(group);
            connection.multicast(ms);
        }catch(SpreadException ex){
            System.err.println("imposible enviar mensaje al grupo");
        }
        
    }
    
    /**
     * envia un mensaje
     * @param user usuario que lo manda
     * @param msg mensaje a enviar
     */
    protected abstract void sendMSG(ArduinoUser user, String msg);
    
    /**
     * trata los mensajes que llegan
     * @param user usuario al que le llega el mensaje
     * @param sm mensaje recibido
     */
    protected abstract void reciveMSG(ArduinoUser user, SpreadMessage sm);
    
    /**
     * ejecuta las intrucciones a realizar por el usuario dado
     * @param user usuario
     */
    protected abstract void work(ArduinoUser user);
    
    /**
     * envia la informacion al servidor o al lider dependiendo del
     * estado en el que se encuentre el usuario
     * @param user usuario que envia los datos
     * @param grupo grupo de practicas
     * @param media valor de la media
     */
    protected abstract void sendData(ArduinoUser user, String grupo, Double media);
    
    /**
     * 
     * @return true si es lider
     */
    protected abstract boolean isLeader();

    
}
