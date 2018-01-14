/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import java.util.Random;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

/**
 *
 * @author Alvaro
 */
public class SerialPortReader extends Thread{
    
    private ArduinoUser user;
    private SerialPort sp;
    private volatile boolean EXIT = false;
    private boolean MSG = false;
    
    private Random r = new Random();
    
    /**
     * Constructor
     * @param sp puerto serie del que se va a leer
     * @param user usuario que almacenara los datos
     */
    public SerialPortReader(SerialPort sp, ArduinoUser user){
        this.sp = sp;
        this.user = user;
    }
    
    /**
     * stop the curren thread waiting for finish him
     */
    public void stopExecution(){
        EXIT = true;
    }
    
    /**
     * lee del puerto serie los datos recibidos
     * y los añade a la lista de medidas del
     * usuario
     */
    private void leerPuertoSerie(){
        try{
            sp.openPort();//Open serial port
            sp.setParams(9600, 8, 1, 0);//Set params.
            boolean first = false;
            double value;
            
            while(!first){
                try{
                    value = Integer.parseInt(sp.readString(3,3000));
                    user.addMedida(value); //if list of measures are in use, thread will be blocked
                }catch(NumberFormatException ex){
                    //DO-NOTHING in order to get at least one value
                } 
            }
            sp.closePort();
        } catch (SerialPortException | SerialPortTimeoutException ex) {
            if(!MSG){
                System.out.println("\033[31m" + user.getNombre() + "_INFO: \033[30mno ha podido conectar al puerto serie " + sp.getPortName());
                MSG = true;
            }
            user.stopExcecution();
        }
        
    }
    
    
    private void TEST(){
        user.addMedida(r.nextDouble());
    }
    
    @Override
    public void run(){
        while(!EXIT){
            TEST();
        }
    }
}
