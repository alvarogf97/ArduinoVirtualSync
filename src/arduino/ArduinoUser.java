/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import jssc.SerialPort;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import virtualsync.VirtualSync;

/**
 *
 * @author Alvaro
 */
public class ArduinoUser{
    
    private String INFO;
    private String user;
    private String address;
    private int port;
    private ArduinoState estado;
    private SpreadConnection connection;
    private SpreadGroup group;
    private List<SpreadMessage> mensajes;
    private List<Double> medidas;
    private List<Double> medidasRecibidas;
    private SerialPort sp;
    private SerialPortReader reader;
    private MessageReceiver reciver;
    private volatile boolean EXIT = false;
    
    //para manejar los accesos concurrentes de las hebras
    private Semaphore readerSemaphore;
    private Semaphore reciverSemaphore;
    private Semaphore reciverDataSemaphore;
    
    
    public void start(){
        while(!EXIT){
            this.work();
        }
        
        try{
            System.out.println(INFO + "Matando reader y reciver...");
            reader.stopExecution();
            reader.join(); System.out.println("     " + INFO + "Reader -> STOPPED");
            reciver.stopExecution();
            reciver.join(); System.out.println("     " + INFO + "Reciver -> STOPPED");
            Thread.sleep(1000);
            System.out.println(INFO + "\033[35mMUERTO -> ¯\\_(ツ)_/¯");
        }catch(InterruptedException ex){
            System.err.println("interrupted Thread");
        }
    }
    
    /**
     * stop the current Thread waiting for reciver and listener
     */
    protected void stopExcecution(){
        EXIT = true;
    }
    
    /**
     * constructor
     * @param user nombre del usuario
     * @param COM puerto serie para leer datos
     * @param lider indica si comienza como lider o no
     */
    public ArduinoUser(String user, String COM, boolean lider){
        this.INFO = ("\033[31m"+ user + "_INFO:\033[30m ");
        this.user = user;
        this.address = null;
        this.port = 0;
        mensajes = new ArrayList<>();
        medidas = new ArrayList<>();
        medidasRecibidas = new ArrayList<>();
        readerSemaphore = new Semaphore(1,true);
        reciverSemaphore = new Semaphore(1,true);
        reciverDataSemaphore = new Semaphore(1,true);
        initState(lider);
        initConnection();
        initSerialPort(COM);
        initReciver();
    }
    
    /**
     * constructor
     * @param user nombre del usuario
     * @param address direccion del proceso demonio
     * @param port puerto del proceso demonio
     * @param COM puerto serie para leer datos
     * @param lider indica si comienza como lider o no
     */
    public ArduinoUser(String user, String address, int port, String COM, boolean lider){
        this.INFO = ("\033[31m"+ user + "_INFO:\033[30m ");
        this.user = user;
        this.address = address;
        this.port = port;
        mensajes = new ArrayList<>();
        medidas = new ArrayList<>();
        medidasRecibidas = new ArrayList<>();
        readerSemaphore = new Semaphore(1,true);
        reciverSemaphore = new Semaphore(1,true);
        reciverDataSemaphore = new Semaphore(1,true);
        initState(lider);
        initConnection();
        initSerialPort(COM);
        initReciver();
    }
    
    /**
     * ejecuta las intrucciones que deber realizar el usuario
     * en funcion del estado en el que se encuentre
     */
    private void work(){
        estado.work(this);
    }
    
    /**
     * añade un mensaje a la lista de mensajes pendientes por leer
     * @param sm mensaje a añadir
     */
    protected void addMSGReceived(SpreadMessage sm) {
        try{
            reciverSemaphore.acquire();
            mensajes.add(sm);
        }catch (InterruptedException ex){
            System.err.println(INFO + "interrupted Thread");
        }finally{
            reciverSemaphore.release();
        }
        
    }
    
    /**
     * añade los datos recibidos de los demas usuarios
     * @param d valor recibido
     */
    protected void addDataReceived(double d){
        try{
            reciverDataSemaphore.acquire();
            System.out.println(INFO + "recibido: " + d);
            medidasRecibidas.add(d);
        }catch (InterruptedException ex){
            System.err.println(INFO + "interrupted Thread");
        }finally{
            reciverDataSemaphore.release();
        }
    }
    
    /**
     * 
     * @return info del usuario
     */
    protected String getINFO(){
        return INFO;
    }
    
    /**
     * mira si hay mensajes nuevos y actualiza el estado del usuario
     * en funcion de los mensajes encontrados
     * @throws SpreadException
     * @throws InterruptedException 
     */
    protected void checkMSG() throws SpreadException, InterruptedException{
        Date inicio = new Date();
        boolean TIME_OUT = true;
        
        try{
            while (new Date().getTime()-inicio.getTime()<1000){
                reciverSemaphore.acquire();
                while(mensajes.size()>=1){
                    TIME_OUT = false;
                    reciveMSG(mensajes.get(0)); //trato ese mensaje
                    mensajes.remove(0); //borro el que he tratado
                    reciverSemaphore.release();
                    Thread.sleep(100);
                    reciverSemaphore.acquire();
                }
                reciverSemaphore.release();
            }
            mensajes.clear(); //limpio pues he leido todos los mensajes;
            if(TIME_OUT){
                SpreadMessage sm = new SpreadMessage();
                sm.setData((user+"@TIME_OUT").getBytes());
                reciveMSG(sm);
            }
        }catch(InterruptedException ex){
            System.out.println(INFO + "interrupted Thread");
        }finally{
            reciverSemaphore.release();
        }
     
    }
    
    /**
     * @return la media de las medidas realizadas del puerto serie
     */
    protected double getMediaPuertoSerie(){
        double media = Double.NaN;
        try{
            readerSemaphore.acquire();
            media = media();
            reciverDataSemaphore.acquire();
            if(medidasRecibidas.size()>0){
                for(Double d : medidasRecibidas){
                    media += d;
                }
                media = media/(medidasRecibidas.size()+1);
                medidasRecibidas.clear();
            }
        } catch (InterruptedException ex) {
            System.err.println(INFO + "interrupted thread");
        }finally{
            readerSemaphore.release();
            reciverDataSemaphore.release();
        }
        return media;
    }
    
    /**
     * 
     * @return true el usuario es lider
     */
    protected boolean isLeader(){
        return estado.isLeader();
    }
    
    /**
     * 
     * @return  media medidas
     */
    private double media(){
        double res = 0;
        
        for(Double d : medidas){
            res += d;
        }
        
        if(!medidas.isEmpty()){
            res = res/medidas.size();
            medidas.clear();
        }
            
        return res;
    }
    
    /**
     * 
     * @return nombre del usuario
     */
    protected String getNombre(){
        return this.user;
    }
    
    /**
     * cambia el estado del usuario a otro dado
     * @param state nuevo estado
     */
    protected void setEstado(ArduinoState state){
        this.estado = state;
        //deleteMSG();
    }
    
    /**
     * 
     * @return la conexion con el proceso demonio
     */
    protected SpreadConnection getConnection(){
        return connection;
    }
    
    /**
     * 
     * @return el grupo actual en el que se comunican
     */
    protected SpreadGroup getConnectionGroup(){
        return group;
    }
   
    /**
     * añade la medida a la lista de medidas
     * @param d medida del puerto serie
     */
    protected void addMedida(double d){
        try{
            readerSemaphore.acquire();
            medidas.add(d);
        }catch(InterruptedException ex){
            System.out.println(INFO + "interrupted Thread");
        }finally{
            readerSemaphore.release();
        }
    }
    
    /**
     * elimina los mensajes de la lista de mensajes
     */
    private void deleteMSG(){
        try{
            reciverSemaphore.acquire();
            mensajes.clear();
        }catch(InterruptedException ex){
            System.err.println("interrupted Thread");
        }finally{
            reciverSemaphore.release();
        }
    }
    
    /**
     * envia ELIGIENDO al grupo
     * @throws InterruptedException 
     */
    protected void sendELIGIENDO() throws InterruptedException{
        estado.sendMSG(this, "ELIGIENDO");
    }
    
    /**
     * envia VIVO al grupo
     * @throws InterruptedException 
     */
    protected void sendVIVO() throws InterruptedException{
        estado.sendMSG(this, "VIVO");
    }
    
    /**
     * indica al estado que ha recibido un mensaje nuevo
     * @param sm mensaje recibido
     */
    private void reciveMSG(SpreadMessage sm){
        estado.reciveMSG(this, sm);
    }
    
    /**
     * inicializa el puerto serie
     * @param COM puerto serie a inicializar
     */
    private void initSerialPort(String COM){
        System.out.println(INFO + "\033[34mestabilizando conexion con el puerto serie " + COM);
        sp = new SerialPort(COM);
        reader = new SerialPortReader(sp,this);
        reader.start();
    }
    
    /**
     * inicializa la hebra que recolecta los mensajes
     * que llegan del grupo y la ejecuta
     */
    private void initReciver(){
        System.out.println(INFO + "\033[34mestabilizando listener de mensajes");
        reciver = new MessageReceiver(connection, this);
        reciver.start();
    }
   
    /**
     * inicializa el estado
     * @param lider si comienza como lider o no
     */
    private void initState(boolean lider){
        if(lider){
            System.out.println(INFO + "\033[34mestado inicial-> Lider");
            estado = new Lider();
        }else{
            System.out.println(INFO + "\033[34mestado inicial-> Participante");
            estado = new Participante();
        }
    }
    
    /**
     * inicializa la conexion con el proceso demonio
     */
    private void initConnection(){
        try{
            System.out.println(INFO + "\033[34mestabilizando conexion con Spread ");
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(address), port, user, false, true);
            group = new SpreadGroup();
            group.join(connection, VirtualSync.GROUP_NAME);
	}catch(SpreadException e){
            System.err.println(INFO + "Se ha encontrado un problema al conectar con el demonio");
            System.exit(1);
	}catch(UnknownHostException e){
            System.err.println(INFO + "no se puede encontrar el demonio " + address);
            System.exit(1);
	}
    }
    
}
