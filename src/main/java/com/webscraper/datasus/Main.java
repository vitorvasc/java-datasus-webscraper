package com.webscraper.datasus;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    //Define a URL da página que o programa irá acessar
    private static final String PAGE = "http://cnes.datasus.gov.br/pages/estabelecimentos/consulta.jsp";
    private static final String PAGE_DETAILED = "http://cnes.datasus.gov.br/pages/estabelecimentos/ficha/index.jsp?coUnidade=260960";
    
    //Intervalos do JavaScript
    private static final long DEFAULT_JS_TIMEOUT = 2000;
    private static final long REDUCED_JS_TIMEOUT = 500;
    
    //Lista de estabelecimentos contidos na página
    private static List<Estabelecimento> estabelecimentos = new ArrayList<>();
    
    //Instância da página atual
    private static HtmlPage page;
    
    //Definições do arquivo CSV
    private static final String FILE_NAME = "estabelecimentos.csv";
        private static final String FILE_HEADER = "Nome,NomeEmpresarial,Cnes,Cnpj,Logradouro,Numero,Complemento,Bairro,Municipio,UF,CEP,Telefone";
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
       
    public static void main(String[] args) {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
        startScraping();
    }
    
    private static void startScraping() {
        try(final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            setWebClientConfigurations(webClient);
            
            //Abre a página através do webClient
            page = webClient.getPage(PAGE);
            
            //Procura pelos campos de estado e município dentro da página
            HtmlSelect state = getSelectByNgModel("Estado");
            HtmlSelect city = getSelectByNgModel("Municipio");
            
            //Seleciona especificamente o estado de PERNAMBUCO para realizar a pesquisa
            selectOption(state, "PERNAMBUCO");
            webClient.waitForBackgroundJavaScript(DEFAULT_JS_TIMEOUT);
            
            //Seleciona especificiamente o município de OLINDA para realizar a pesquisa
            selectOption(city, "OLINDA");
            webClient.waitForBackgroundJavaScript(DEFAULT_JS_TIMEOUT);
            
            //Finaliza a pesquisa após definição dos filtros acima
            HtmlButton search = getButtonByClass("btn btn-primary");
            search.click();
            
            HtmlSelect pageSize = getSelectByNgModel("registrosPorPagina");
            selectOption(pageSize, "30");
            
            for(int i = 0; i < 9; i++) {
                HtmlTable resultsTable = getUpdatedResultTable();
                HtmlAnchor nextPageLink = getNextPageButton();

                resultsTable.getRows().forEach(row -> {                 
                    Estabelecimento estabelecimento = new Estabelecimento();
                    
                    estabelecimento.setUF(String.valueOf(row.getCell(0).asText()));
                    estabelecimento.setMunicipio(String.valueOf(row.getCell(1).asText()));
                    estabelecimento.setCNES(Long.valueOf(row.getCell(2).asText()));
                    estabelecimento.setNomeFantasia(String.valueOf(row.getCell(3).asText()));
                    
                    estabelecimentos.add(estabelecimento);
                });

                page = nextPageLink.click();
                webClient.waitForBackgroundJavaScript(DEFAULT_JS_TIMEOUT);
            }
            
            /*
            Mantive a função abaixo desabilitada pois devido erro de carregamento no framework utilizado
            pelos desenvolvedores do site, a página não é carregada corretamente. 
            Existe uma diferença entre abrir o site através de um navegador comum e abrir um site através
            deste webdriver. Abrindo o site com informações detalhadas através do webdriver, não é possível
            obter todas as informações detalhadas. Infelizmente este é um problema que não consegui solucionar
            a tempo.
            */
//            estabelecimentos.forEach(e -> {
//                startDetailedScraping(e);
//            });

            estabelecimentos.forEach(System.out::println);
            
            //Escreve todos os resultados obtidos em um arquivo CSV
            FileWriter writer = new FileWriter(FILE_NAME);
                
            writer.append(FILE_HEADER);
            writer.append(NEW_LINE_SEPARATOR);
            for(Estabelecimento estabelecimento : estabelecimentos) {
                writer.append(estabelecimento.toString());
                writer.append(NEW_LINE_SEPARATOR);
            }
            writer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void startDetailedScraping(Estabelecimento e) {        
        try(final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            setWebClientConfigurations(webClient);
            
            //Abre a página com informações detalhadas através do webClient
            page = webClient.getPage(PAGE_DETAILED+e.getCNES());
            webClient.waitForBackgroundJavaScript(REDUCED_JS_TIMEOUT);
     
            //Percorre a página e busca a informação contendo nome empresarial do estabelecimento
            HtmlInput inputNome = getInputByNgValue("estabelecimento.noEmpresarial");
            e.setNome(inputNome.getValueAttribute());
            
            //Percorre a página e busca a informação contendo CNPJ do estabelecimento
            HtmlInput inputCnpj = getInputByMask("99.999.999/9999-99");
            e.setCNPJ(Long.parseLong(inputCnpj.getValueAttribute()));
            
            //Percorre a página e busca a informação contendo logradouro do estabelecimento
            HtmlInput inputLogradouro = getInputByNgValue("estabelecimento.noLogradouro");
            e.setLogradouro(inputLogradouro.getValueAttribute());
            
            //Percorre a página e busca a informação contendo número do estabelecimento
            HtmlInput inputNumero = getInputByNgValue("estabelecimento.nuEndereco");
            e.setNumero(Integer.parseInt(inputNumero.getValueAttribute()));
            
            //Percorre a página e busca a informação contendo complemento do estabelecimento
            HtmlInput inputComplemento = getInputByNgValue("estabelecimento.noComplemento");
            e.setComplemento(inputComplemento.getValueAttribute());
            
            //Percorre a página e busca a informação contendo bairro do estabelecimento
            HtmlInput inputBairro = getInputByNgValue("estabelecimento.bairro");
            e.setBairro(inputBairro.getValueAttribute());
            
            //Percorre a página e busca a informação contendo CNPJ do estabelecimento
            HtmlInput inputCEP = getInputByMask("99999-999");
            e.setCEP(inputCEP.getValueAttribute());
            
            //Percorre a página e busca a informação contendo município do estabelecimento
            HtmlInput inputTelefone = getInputByNgValue("estabelecimento.nuTelefone");
            e.setTelefone(inputTelefone.getValueAttribute());            
                        
            estabelecimentos.forEach(System.out::println);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
   
    private static HtmlButton getButtonByClass(String css) {
        return page.getFormByName("formPesquisa").getFirstByXPath("//*[@class=\""+ css +"\"]");
    }
    
    private static HtmlTable getTableByClass(String css) {
        return page.getFirstByXPath("//*[@class=\""+ css +"\"]");
    }
    
    private static void selectOption(HtmlSelect select, String option) {
        HtmlOption opt = select.getOptionByText(option);
        select.setSelectedAttribute(opt, true);
    }
    
    private static HtmlInput getInputByMask(String mask) {
        return page.getFirstByXPath("//*[@ui-mask='"+ mask +"']");
    }
    
    private static HtmlInput getInputByNgValue(String ngValue) {
        return page.getFirstByXPath("//*[@ng-value='"+ ngValue + "']");
    }
    
    private static HtmlSelect getSelectByNgModel(String ngModel) {
        return page.getFirstByXPath("//*[@ng-model='" + ngModel + "']");
    }
        
    private static HtmlTable getUpdatedResultTable() {
        return getTableByClass("table table-condensed table-bordered table-striped " +
                "ng-scope ng-table");
    }
    
    private static HtmlAnchor getNextPageButton() {
        return page.getFirstByXPath("//ul/li/a[@ng-switch-when=\"next\"]");
    }
    
    private static void setWebClientConfigurations(WebClient webClient) {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setUseInsecureSSL(true);
    }    
}
