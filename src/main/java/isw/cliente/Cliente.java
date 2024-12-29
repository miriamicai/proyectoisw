package isw.cliente;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import isw.configuration.PropertiesISW;
import isw.domain.Customer;
import isw.message.Message;

public class Cliente {
    private String host;
    private int port;
    public ArrayList<Customer> seguidores;
    public ArrayList<Customer> seguidos;

    public Cliente(String host, int port) { //constructor de Cliente: caracterísiticas petición host y puerto
        this.host = host;
        this.port = port;
    }

    public Cliente() { //constructor vacío de Cliente: host y puerto sacado de PropertiesISW.java
        this.host = PropertiesISW.getInstance().getProperty("host");
        this.port = Integer.parseInt(PropertiesISW.getInstance().getProperty("port"));
        if (this.host == null || this.port == 0) {
            throw new IllegalArgumentException("Host o puerto no válidos.");
        }
    }

    public HashMap<String, Object> sentMessage(String Context, HashMap<String, Object> session) {
        //función que devuelve un HashMap<String, Object> que recibe un String y un HashMap


        //Configure connections -> ya hecho en el constructor vacío
        //String host = PropertiesISW.getInstance().getProperty("host");
        //int port = Integer.parseInt(PropertiesISW.getInstance().getProperty("port"));

        System.out.println("Host: " + host + " port" + port);
        //Create a cliente class                             -> por qué se necesita una clase Cliente??
        //Client cliente=new Client(host, port);

        //HashMap<String,Object> session=new HashMap<String, Object>();
        //session.put("/getCustomer",""); //clase CustomerControler -> se saca de la base de datos

        Message mensajeEnvio = new Message();
        Message mensajeVuelta = new Message();
        mensajeEnvio.setContext(Context);///getCustomer"
        mensajeEnvio.setSession(session);
        this.sent(mensajeEnvio, mensajeVuelta);

        //CÓDIGO DE PRUEBA
        if (mensajeVuelta.getContext() != null) {
            System.out.println("Contexto: " + mensajeVuelta.getContext() + mensajeVuelta.getSession());
            //processServerResponse(mensajeVuelta);
        } else {
            System.out.println("Error en la respuesta del servidor");
        }


        switch (mensajeVuelta.getContext()) { //Devolver los Customers dependiendo del mensaje que devuelva el servidor (mensajeVuelta)
            case "/getCustomersResponse": //CustomerS (varios)
                ArrayList<Customer> customerList = (ArrayList<Customer>) (mensajeVuelta.getSession().get("Customer"));
                for (Customer customer : customerList) { //se recorre la tabla de clientes y los muestra por pantalla
                    System.out.println("He leído el id: " + customer.getId() + " con nombre: " + customer.getPassword());
                }
                break;

            case "/getCustomerResponse": //1 Customer solo
                session = mensajeVuelta.getSession();
                Customer customer = (Customer) (session.get("Customer"));
                if (customer != null) {
                    System.out.println("He leído el id: " + customer.getId() + " con nombre: " + customer.getPassword());
                } else {
                    System.out.println("No se ha recuperado nada de la base de datos");
                }
                break;

            case "/addUserResponse":
                String message = (String) mensajeVuelta.getSession().get("message");
                if (message != null) {
                    System.out.println("Server response: " + message);
                } else if (mensajeVuelta.getSession().containsKey("error")) {
                    System.out.println("Error: " + mensajeVuelta.getSession().get("error"));
                } else {
                    System.out.println("Unexpected response from server for /addUserResponse");
                }
                break;

            case "/loginResponse":
                String mensaje = (String) mensajeVuelta.getSession().get("message");
                if (mensaje != null) {
                    System.out.println("Server response: " + mensaje);
                } else if (mensajeVuelta.getSession().containsKey("error")) {
                    System.out.println("Error: " + mensajeVuelta.getSession().get("error"));
                } else {
                    System.out.println("Unexpected response from server for /addUserResponse");
                }
                break;

            case "/connectUserResponse":
                String messg = (String) mensajeVuelta.getSession().get("message");
                if (messg != null) {
                    System.out.println("Server response: " + messg);
                } else if (mensajeVuelta.getSession().containsKey("error")) {
                    System.out.println("Error: " + mensajeVuelta.getSession().get("error"));
                } else {
                    System.out.println("Unexpected response from server for /addUserResponse");
                }
                break;

            case "/getSeguidoresResponse": // Seguidores
                seguidores = (ArrayList<Customer>) mensajeVuelta.getSession().get("Seguidores");
                if (seguidores != null && !seguidores.isEmpty()) {
                    System.out.println("Lista de seguidores:");
                    for (Customer seguidor : seguidores) {
                        System.out.println("Id: " + seguidor.getId() + ", Nombre: " + seguidor.getNombreUsuario());
                    }
                } else {
                    System.out.println("No se encontraron seguidores.");
                }
                break;

            case "/getSeguidosResponse": // Seguidos
                seguidos = (ArrayList<Customer>) mensajeVuelta.getSession().get("Seguidos");
                if (seguidos != null && !seguidos.isEmpty()) {
                    System.out.println("Lista de seguidos:");
                    for (Customer seguido : seguidos) {
                        System.out.println("Id: " + seguido.getId() + ", Nombre: " + seguido.getNombreUsuario());
                    }
                } else {
                    System.out.println("No se encontraron personas seguidas.");
                }
                break;

            default:

                System.out.println("\nError a la vuelta");
                break;

        }
        //System.out.println("3.- En Main.- El valor devuelto es: "+((String)mensajeVuelta.getSession().get("Nombre")));
        return session;
    }

    public ArrayList<Customer> getSeguidoresList(){
        return seguidores;
    }


    public Message sent(Message messageOut, Message messageIn) {
        try {

            System.out.println("Connecting to host " + host + " on port " + port + ".");

            //Socket echoSocket = null; //A socket is an endpoint for communication between two machines.
            //OutputStream out = null;
            //InputStream in = null;

            try {
                Socket echoSocket = new Socket(host, port); //se crea la conexión a Internet entre el cliente y el servidor
                InputStream in = echoSocket.getInputStream(); //flujo de entrada desde el socket (leer datos del servidor)
                OutputStream out = echoSocket.getOutputStream(); //flujo de salida del socket (enviar datos del servidor)
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
                ObjectInputStream objectInputStream = new ObjectInputStream(in);
                //Create the object to send
                objectOutputStream.writeObject(messageOut);
                objectOutputStream.flush(); // Asegúrate de vaciar el flujo de salida

                // create a DataInputStream so we can read data from it.
            
                Message response = (Message) objectInputStream.readObject();
                messageIn.setContext(response.getContext());
                messageIn.setSession(response.getSession());
		        /*System.out.println("\n1.- El valor devuelto es: "+messageIn.getContext());
		        String cadena=(String) messageIn.getSession().get("Nombre");
		        System.out.println("\n2.- La cadena devuelta es: "+cadena);*/

                objectOutputStream.close();
                objectInputStream.close();
                echoSocket.close();

                return messageIn;

            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server");
                System.exit(1);
            }

            /** Closing all the resources */
            //out.close();
            //in.close();
            //echoSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void establishConnection(int followerId, int followingId){
        Message messageOut = new Message();
        messageOut.setContext("/connectUser");

        HashMap<String, Object> session = new HashMap<>();
        session.put("followerId", followerId);
        session.put("followingId", followingId);
        messageOut.setSession(session);
        sent(messageOut, new Message());

        System.out.println("Connection established from Cliente establishConnection() method.");
    }

    public void getFollowers(int id){
        Message messageOut = new Message();
        messageOut.setContext("/getSeguidores");
        HashMap<String, Object> session = new HashMap<>();
        session.put("id_logged", id);
        messageOut.setSession(session);
        sent(messageOut, new Message());
        System.out.println("Followers list retrieved from Cliente method.");
    }

    public void registerUser(String username, String name, String email, String password) {
        //Cliente cliente = new Cliente();

        Message messageOut = new Message();
        messageOut.setContext("/addUser");

        HashMap<String, Object> session = new HashMap<>();
        session.put("usuario", username);
        session.put("nombre", name);
        session.put("email", email);
        session.put("contraseña", password);
        messageOut.setSession(session);

        //sent(messageOut, new Message());
        Message response = new Message();
        response = sent(messageOut, response);

        System.out.println("Response from server en registerUser: " + response.getContext());

        if (response != null && response.getContext().equals("/addUserSuccess")) {
            System.out.println("User added to database from Cliente registerUser() method.");
        } else {
            System.out.println("Failed to add user to database from Cliente registerUser() method.");
        }

        //System.out.println("User added to database from Cliente registerUser() method.");
    }



    public boolean login(HashMap<String, Object> session) {
        // Enviar el mensaje al servidor
        HashMap<String, Object> respuesta = this.sentMessage("/login", session);

        // Procesar la respuesta

        //PRUEBA
        System.out.println("Recorriendo el HashMap (clave, valor):");
        for (Map.Entry<String, Object> entry : respuesta.entrySet()) {
            System.out.println("Clave: " + entry.getKey() + ", Valor: " + entry.getValue());
        }

        if (respuesta != null && respuesta.containsKey("id_logged")) {
            int idLogged = (int) respuesta.get("id_logged");

            if (idLogged!=0) {
                //session.put("id_logged", idLogged);
                System.out.println("Inicio de sesión exitoso. ID de usuario: " + idLogged);
                return true;
            } else {
                System.out.println("Error en el inicio de sesión.");
            }
        } else {
            System.out.println("Respuesta inesperada del servidor.");
        }
        return false;
    }





    public Customer getCustomer(int id) {
        Message messageOut = new Message();
        messageOut.setContext("/getCustomer");

        //se envía el id del cliente en el HashMap
        HashMap<String, Object> session = new HashMap<>();
        session.put("id", id);
        messageOut.setSession(session);

        Message messageIn = new Message();
        sent(messageOut, messageIn); //se envía el mensaje al servidor

        //procesar la respuesta del SocketServer
        Customer customer = null;
        if ("/getCustomerResponse".equals(messageIn.getContext())) {
            customer = (Customer) messageIn.getSession().get("Customer");
            if (customer != null) {
                System.out.println("Cliente recuperado: " + customer.getId() + ", " + customer.getNombreUsuario());
            } else {
                System.out.println("Cliente no encontrado.");
            }
        } else {
            System.out.println("Respuesta inesperada del servidor.");
        }

        return customer;
    }


    /*public static void main(String[] args) {
        Cliente c = new Cliente();
        c.establishConnection(25, 2);
    }*/
}