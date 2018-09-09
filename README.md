## java-datasus-webscraper

Utilizado para extrair dados do sistema de [cadastro nacional dos estabelecimentos de saúde](http://cnes.datasus.gov.br/pages/estabelecimentos/consulta.jsp).


### Pré-requisitos

[Java](https://www.java.com/en/download/)


### Instalação

Faça o clone do repositório utilizando `git clone https://github.com/vitorvasc/java-datasus-webscraper.git`
Abra o prompt de comando e utilize o comando `java jar DataSus-1.0.jar`

### Utilização

A extração de dados será realizada especificamente para o estado de `PERNAMBUCO`no município de `OLINDA`.
Os dados extraídos ficarão salvos no arquivo `estabelecimentos.csv`.


### Observações

Dentro do código, mantive desabilitada a função `startDetailedScraping` pois devido erro de carregamento no framework utilizado pelos desenvolvedores do site, a pagina não é carregada corretamente. Observei que existe uma diferença entre abrir o site através de um navegador comum e abrir um site através do webdriver escolhido. Abrindo o site de informações detalhadas do estabelecimento atraves do webdriver, nao é possivel obter todas as informações detalhadas. Infelizmente este é um problema que nao consegui solucionar a tempo.
