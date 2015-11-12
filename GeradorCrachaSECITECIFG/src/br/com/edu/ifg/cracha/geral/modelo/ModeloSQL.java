package br.com.edu.ifg.cracha.geral.modelo;

import java.util.HashMap;
import java.util.Map;

public class ModeloSQL {
    //Variaveis
    //--String
    private String url = "jdbc:postgresql://localhost:5432/";
    private String username = "";
    private String password = "";
    private String sql = "";
    
    //Métodos
    //--Construtor
    public ModeloSQL(String finalURL, String username, String password) {
        url += finalURL;
        this.username = username;
        this.password = password;
    }
    
    //--Getters
    public String getURL() {return url;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getSQL() {return sql;}
    //--Setter
    public void setSQL(String sql) {this.sql = sql;}
}
