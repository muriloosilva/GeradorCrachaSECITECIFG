package br.com.edu.ifg.cracha.geral.modelo;

public class ModeloCracha {
	//Variaveis
    //--String

    private String tipo = "";
    private String nome = "";
    private String nome_simplif = "";
    private String cpf = "";
    private String url = "";

	//Métodos
    //--Construtor
    public ModeloCracha(String nome, String cpf, String tipo, String URLImagem, String nomeSimplif) {
        this.nome = nome;
        this.cpf = cpf;
        this.tipo = tipo;
        this.url = URLImagem;
        this.nome_simplif = nomeSimplif;
    }

    //--Getters
    public String getNome() {
        return nome;
    }

    public String getCPF() {
        return cpf;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public String getURL() {
        return url;
    }
    
    public String getNomeSimplif() {
        return nome_simplif;
    }
}
