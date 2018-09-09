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
    //Define a URL da p�gina que o programa ir� acessar
    private static final String PAGE = "http://cnes.datasus.gov.br/pages/estabelecimentos/consulta.jsp";
    private static final String PAGE_DETAILED = "http://cnes.datasus.gov.br/pages/estabelecimentos/ficha/index.jsp?coUnidade=260960";
    
    //Intervalos do JavaScript
    private static final long DEFAULT_JS_TIMEOUT = 2000;
    private static final long REDUCED_JS_TIMEOUT = 500;
    
    //Lista de estabelecimentos contidos na p�gina
    private static List<Estabelecimento> estabelecimentos = new ArrayList<>();
    
    //Inst�ncia da p�gina atual
    private static HtmlPage page;
    
    //Defini��es do arquivo CSV
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
            
            //Abre a p�gina atrav�s do webClient
            page = webClient.getPage(PAGE);
            
            //Procura pelos campos de estado e munic�pio dentro da p�gina
            HtmlSelect state = getSelectByNgModel("Estado");
            HtmlSelect city = getSelectByNgModel("Municipio");
            
            //Seleciona especificamente o estado de PERNAMBUCO para realizar a pesquisa
            selectOption(state, "PERNAMBUCO");
            webClient.waitForBackgroundJavaScript(DEFAULT_JS_TIMEOUT);
            
            //Seleciona especificiamente o munic�pio de OLINDA para realizar a pesquisa
            selectOption(city, "OLINDA");
            webClient.waitForBackgroundJavaScript(DEFAULT_JS_TIMEOUT);
            
            //Finaliza a pesquisa ap�s defini��o dos filtros acima
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
            Mantive a fun��o abaixo desabilitada pois devido erro de carregamento no framework utilizado
            pelos desenvolvedores do site, a p�gina n�o � carregada corretamente. 
            Existe uma diferen�a entre abrir o site atrav�s de um navegador comum e abrir um site atrav�s
            deste webdriver. Abrindo o site com informa��es detalhadas atrav�s do webdriver, n�o � poss�vel
            obter todas as informa��es detalhadas. Infelizmente este � um problema que n�o consegui solucionar
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
            
            //Abre a p�gina com informa��es detalhadas atrav�s do webClient
            page = webClient.getPage(PAGE_DETAILED+e.getCNES());
            webClient.waitForBackgroundJavaScript(REDUCED_JS_TIMEOUT);
     
            //Percorre a p�gina e busca a informa��o contendo nome empresarial do estabelecimento
            HtmlInput inputNome = getInputByNgValue("estabelecimento.noEmpresarial");
            e.setNome(inputNome.getValueAttribute());
            
            //Percorre a p�gina e busca a informa��o contendo CNPJ do estabelecimento
            HtmlInput inputCnpj = getInputByMask("99.999.999/9999-99");
            e.setCNPJ(Long.parseLong(inputCnpj.getValueAttribute()));
            
            //Percorre a p�gina e busca a informa��o contendo logradouro do estabelecimento
            HtmlInput inputLogradouro = getInputByNgValue("estabelecimento.noLogradouro");
            e.setLogradouro(inputLogradouro.getValueAttribute());
            
            //Percorre a p�gina e busca a informa��o contendo n�mero do estabelecimento
            HtmlInput inputNumero = getInputByNgValue("estabelecimento.nuEndereco");
            e.setNumero(Integer.parseInt(inputNumero.getValueAttribute()));
            
            //Percorre a p�gina e busca a informa��o contendo complemento do estabelecimento
            HtmlInput inputComplemento = getInputByNgValue("estabelecimento.noComplemento");
            e.setComplemento(inputComplemento.getValueAttribute());
            
            //Percorre a p�gina e busca a informa��o contendo bairro do estabelecimento
            HtmlInput inputBairro = getInputByNgValue("estabelecimento.bairro");
            e.setBairro(inputBairro.getValueAttribute());
            
            //Percorre a p�gina e busca a informa��o contendo CNPJ do estabelecimento
            HtmlInput inputCEP = getInputByMask("99999-999");
            e.setCEP(inputCEP.getValueAttribute());
            
            //Percorre a p�gina e busca a informa��o contendo munic�pio do estabelecimento
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
