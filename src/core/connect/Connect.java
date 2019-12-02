package core.connect;

import core.commands.Commands;
import core.topic.Topic;
import org.apache.commons.cli.CommandLine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connect extends Thread{

    Socket cliente1 = null;
    DataInputStream buffEntrada;
    DataOutputStream buffSalida;
    DataInputStream teclado;
    public static Vector<Connect> clientesConectados = new Vector();
    public static List<Topic> topics = new ArrayList<Topic>();
    Commands comandos = new Commands();
    public String username;

    public Connect(Socket cliente, DataInputStream buffEntrada, DataOutputStream buffSalida, String username) {
        cliente1 = cliente;
        this.buffEntrada = buffEntrada;
        this.buffSalida = buffSalida;
        this.username = username;
        clientesConectados.add(this);
        Topic topic = topics.stream().
                filter(current -> "Broadcast".equals(current.getTopicTitle()))
                .findAny()
                .orElse(null);
        topic.getUserList().add(this);
        Topic som = new Topic();
        som.setTopicTitle(username);
        som.setAdministrador(username);
        som.getUserList().add(this);
        topics.add(som);
    }

    public void run() {
        try {
            Boolean done = true;
            System.out.println("Num: " + clientesConectados.size());
            while (done) {
                String mensaje = buffEntrada.readUTF();
                System.out.println("Mensaje de "+username+": "+mensaje);
                Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
                Matcher regexMatcher = regex.matcher(mensaje);
                ArrayList<String> args = new ArrayList<String>();
                while (regexMatcher.find()) {
                    if (regexMatcher.group(1) != null) {
                        args.add(regexMatcher.group(1));
                    } else if (regexMatcher.group(2) != null) {
                        args.add(regexMatcher.group(2));
                    } else {
                        args.add(regexMatcher.group());
                    }
                }
                if (args.get(0).equals("enviar")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("m")) {
                            String messageBody = commandLine.getOptionValue("m").trim();
                            if (commandLine.hasOption("t")) {
                                String topicName = commandLine.getOptionValue("t").trim();
                                Topic temp = null;
                                for (Topic t : topics) {
                                    if (topicName.equals(t.getTopicTitle())) {
                                        temp = t;
                                    }
                                }
                                if (temp == null) {
                                    buffSalida.writeUTF("No se encontró el topic");
                                } else {
                                    temp.Publish(messageBody, this.username, this);
                                }
                            } else {
                                for (int i = 0; i < clientesConectados.size(); i++) {
                                    if (i != clientesConectados.indexOf(this)) {
                                        clientesConectados.get(i).EnviarMensaje("[Broadcast]<"+this.username+"> " + messageBody);
                                    }
                                }
                            }
                        }
                    }
                }
                //CREATE TOPIC
                if (args.get(0).equals("crear")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine.hasOption("t")) {
                        String topicName = commandLine.getOptionValue("t").trim();
                        Topic topic = topics.stream().
                                filter(current -> topicName.equals(current.getTopicTitle()))
                                .findAny()
                                .orElse(null);
                        if (topic == null) {
                            topic = new Topic();
                            topic.setTopicTitle(topicName);
                            topic.setAdministrador(this.username);
                            topic.getUserList().add(this);
                            topics.add(topic);
                            EnviarMensaje("Se creó el topic");
                        } else {
                            EnviarMensaje("El topic ya existe");
                        }
                    }
                }
                if (args.get(0).equals("subscribe")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("t")) {
                            String topicName = commandLine.getOptionValue("t").trim();
                            Topic temp = null;
                            for (Topic t : topics) {
                                if (topicName.equals(t.getTopicTitle())) {
                                    temp = t;
                                }
                            }
                            if (temp == null) {
                                EnviarMensaje("No existe el topic");

                            }else{
                                if(temp.getUserList().contains(this)){
                                    EnviarMensaje("Ya estabas suscrito");
                                }else{
                                    temp.getUserList().add(this);
                                    EnviarMensaje("Suscrito a " + topicName);
                                }
                            }
                        }
                    }
                    else{
                        EnviarMensaje("Uso: subscribe –t '<topic name>'");
                    }
                }
                if (args.get(0).equals("remove")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("t")) {
                            String topicName = commandLine.getOptionValue("t").trim();
                            Topic temp = null;
                            for (Topic t : topics) {
                                if (topicName.equals(t.getTopicTitle())) {
                                    temp = t;
                                }
                            }
                            if (temp == null) {
                                EnviarMensaje("No existe el topic");
                            }else{
                                if(temp.getAdministrador().equals(this.username)){
                                    topics.remove(temp);
                                    EnviarMensaje("Topic removido");
                                }else{
                                    EnviarMensaje("No eres el administrador del topic, imposible remover");
                                }

                            }
                        }
                    }
                    else{
                        EnviarMensaje("Uso: remove –t '<topic name>'");
                    }
                }
                if (args.get(0).equals("unsubscribe")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("t")) {
                            String topicName = commandLine.getOptionValue("t").trim();
                            Topic temp = null;
                            for (Topic t : topics) {
                                if (topicName.equals(t.getTopicTitle())) {
                                    temp = t;
                                }
                            }
                            if (temp == null) {
                                EnviarMensaje("No existe el topic");
                            }else{
                                temp.getUserList().remove(this);
                                EnviarMensaje("Removido del topic");
                            }
                        }else{
                            EnviarMensaje("Uso: unsubscribe –t '<topic name>'");
                        }
                    }
                }
                if (args.get(0).equals("topic")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("l")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Topics:>");
                            for(int i = 0; i < topics.size(); i++){
                                boolean t = true;
                                for(int j = 0; j < clientesConectados.size(); j++){
                                    if(topics.get(i).getTopicTitle().equals(clientesConectados.get(j).username)){
                                        t = false;
                                        break;
                                    }
                                }
                                if(t){
                                    if(i == topics.size()-1){
                                        sb.append(topics.get(i).getTopicTitle());
                                    }else{
                                        sb.append(topics.get(i).getTopicTitle());
                                        sb.append(",");
                                    }
                                }
                            }
                            EnviarMensaje(sb.toString());
                        }
                    }
                }
                if (args.get(0).equals("user")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("l")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Users:>");
                            for(int i = 0; i < clientesConectados.size(); i++){
                                if(i == clientesConectados.size()-1){
                                    sb.append(clientesConectados.get(i).username);
                                }else{
                                    sb.append(clientesConectados.get(i).username);
                                    sb.append(",");
                                }
                            }
                            EnviarMensaje(sb.toString());
                        }
                    }
                }
                if (args.get(0).equals("mio")) {
                    CommandLine commandLine = comandos.parse(args.toArray(new String[args.size()]));
                    if (commandLine != null) {
                        if (commandLine.hasOption("l")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Mios:>");
                            for(int i = 0; i < topics.size(); i++){
                                if(topics.get(i).getTopicTitle().equals("Broadcast")){
                                    continue;
                                }
                                Vector<Connect> u = topics.get(i).getUserList();
                                for(int j = 0; j < u.size(); j++){
                                    if(username.equals(u.get(j).username)){
                                        if(i == topics.size()-1){
                                            sb.append(topics.get(i).getTopicTitle());
                                        }else{
                                            sb.append(topics.get(i).getTopicTitle());
                                            sb.append(",");
                                        }
                                    }
                                }
                            }
                            System.out.println(sb.toString());
                            EnviarMensaje(sb.toString());
                        }
                    }
                }
                if(mensaje.equals("exit")){
                    for(int i = 0; i <= clientesConectados.size(); i++){
                        if(username.equals(clientesConectados.get(i).username)){
                            clientesConectados.remove(i);
                        }
                    }
                    for(int i = 0; i < topics.size(); i++){
                        if(username.equals(topics.get(i).getTopicTitle())){
                            topics.remove(i);
                        }
                    }
                }
                done = !mensaje.equals("exit");
            }
        } catch (Exception e) {
        }
    }
    public void EnviarMensaje(String mensaje) {
        try {
            buffSalida.writeUTF(mensaje);
            buffSalida.flush();
        } catch (Exception e) {
        }
    }
}
