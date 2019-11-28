package core.mediator;

import core.connect.Connect;
import core.connect.database.DatabaseCommands;
import core.topic.Topic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static core.connect.Connect.topics;

public class Mediator {
    public ServerSocket server;
    public int puerto = 9000;
    public List<Connect> conexiones = new ArrayList<Connect>();
    private DatabaseCommands db;
    public void init() {
        db = new DatabaseCommands();
        Socket socket;
        try {
            Topic broadcast = new Topic();
            broadcast.setTopicTitle("Broadcast");
            topics.add(broadcast);

            server = new ServerSocket(puerto);
            System.out.println("Esperando peticiones por el puerto " + puerto);
            while (true) {
                socket = server.accept();
                DataInputStream buffEntrada = new DataInputStream(socket.getInputStream());
                DataOutputStream buffSalida = new DataOutputStream(socket.getOutputStream());
                String userdata = buffEntrada.readUTF();
                System.out.println(userdata);
                int result = -50;
                if(userdata.contains("NewUser:>")){
                    String[] s1 = userdata.split(":>");
                    String[] s = s1[1].split(",");
                    result = db.signUp(s[0],s[1]);
                }
                if (userdata.contains("LogIn:>")){
                    String[] s1 = userdata.split(":>");
                    String[] s = s1[1].split(",");
                    result = db.logInIfUserExists(s[0],s[1]);
                }
                switch(result){
                    case 0:
                        System.out.println("not found");
                        buffSalida.writeUTF("Usuario no encontrado.");
                        buffSalida.flush();
                        break;
                    case 1:
                        Connect conexion = new Connect(socket, buffEntrada, buffSalida,
                                userdata.split(":>")[1].split(",")[0]);
                        conexion.start();
                        conexiones.add(conexion);
                        buffSalida.writeUTF("Aceptado");
                        buffSalida.flush();
                        break;
                    case 2:
                        buffSalida.writeUTF("Contraseña incorrecta.");
                        buffSalida.flush();
                        break;
                    case 3:
                        buffSalida.writeUTF("Ya existe un usuario con ese nombre.");
                        buffSalida.flush();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
