package com.webscraper.datasus;


public class Estabelecimento {
    
    private String UF;
    private String municipio;
    private String bairro;
    private String CEP;
    private String logradouro;
    private int numero;
    private String complemento;
    private String telefone;
    private long CNES;
    private long CNPJ;
    private String nome;
    private String nomeFantasia;
    
    public String getUF() {
        return UF;
    }

    public void setUF(String UF) {
        this.UF = UF;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCEP() {
        return CEP;
    }

    public void setCEP(String CEP) {
        this.CEP = CEP;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public long getCNES() {
        return CNES;
    }

    public void setCNES(long CNES) {
        this.CNES = CNES;
    }

    public long getCNPJ() {
        return CNPJ;
    }

    public void setCNPJ(long CNPJ) {
        this.CNPJ = CNPJ;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    @Override
    public String toString() {
        return nome + ","
                + nomeFantasia + ","
                + CNES + ","
                + CNPJ + ","
                + logradouro + ","
                + numero + ","
                + complemento + ","
                + bairro + ","
                + municipio + ","
                + UF + ","
                + CEP + ","
                + telefone;
    }
}
