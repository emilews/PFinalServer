package core.topic;

import core.connect.Connect;

import java.util.Vector;

public class Topic {

    public Vector<Connect> usuarios = new Vector();
    public String topicTitle;
    public String admin;


    public Vector<Connect> getUserList() {
        return usuarios;
    }

    public void setUserList(Vector<Connect> conexion) {
        this.usuarios = usuarios;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public void setAdministrador(String admin) {
        this.admin = admin;
    }

    public String getAdministrador() {
        return this.admin;
    }

    public void Publish(String msg, String user, Connect con){
        for (int i = 0; i < usuarios.size(); i++) {
            if(con != usuarios.get(i)){
                usuarios.get(i).EnviarMensaje("["+ getTopicTitle() +"]<" + user + "> " + msg);
            }
        }
    }
}
