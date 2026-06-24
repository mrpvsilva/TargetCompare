# TargetCompare

Aplicação desktop em Java para análise de microRNAs (miRNAs) em bioinformática. O sistema identifica **genes-alvo em comum** entre múltiplos miRNAs consultando bancos de dados de predição de alvos e, em seguida, utiliza uma **Rede Neural SOM (Self-Organizing Map / Mapa de Kohonen)** para classificar e visualizar padrões de co-regulação gênica.

---

## Sumário

- [Como funciona](#como-funciona)
- [Requisitos](#requisitos)
- [Configuração do banco de dados](#configuração-do-banco-de-dados)
- [Onde obter os dados e como carregar](#onde-obter-os-dados-e-como-carregar)
- [Executando o projeto](#executando-o-projeto)
- [Usando a aplicação](#usando-a-aplicação)
- [Parâmetros do SOM](#parâmetros-do-som)
- [Avaliando os resultados](#avaliando-os-resultados)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Dependências](#dependências)

---

## Como funciona

O fluxo de análise é dividido em duas etapas:

### Etapa 1 — Comparação de miRNAs

O usuário informa uma lista de miRNAs e o sistema consulta o banco de dados selecionado, recuperando todos os genes-alvo preditos para cada miRNA. Em seguida, compara os conjuntos par a par e identifica os genes que aparecem como alvo de **dois ou mais** miRNAs simultaneamente (genes compartilhados).

O resultado é uma tabela com:
- Nome do gene
- Quais miRNAs o visam (true/false por coluna)
- Quantidade total de miRNAs que o visam

### Etapa 2 — Rede Neural SOM

Os genes compartilhados encontrados na Etapa 1 são convertidos em uma matriz binária (gene × miRNA) e usados como entrada para treinar uma rede SOM. O algoritmo agrupa os genes de acordo com o padrão de quais miRNAs os regulam, permitindo identificar clusters de co-regulação.

Após o treinamento, cada gene é atribuído ao neurônio mais próximo (BMU — *Best Matching Unit*), e o resultado é exibido em uma tabela de classificação. Um scatter plot visual também é gerado para inspecionar a distribuição dos dados versus os neurônios treinados.

---

## Requisitos

| Ferramenta | Versão mínima |
|-----------|--------------|
| Java JDK | 8 (1.8) |
| Docker + Docker Compose | qualquer versão recente |
| Eclipse IDE | 2020+ (ou qualquer IDE com suporte a projeto Java clássico) |
| MySQL Client | opcional, para importar dados manualmente |

---

## Configuração do banco de dados

O banco de dados MySQL é inicializado via Docker Compose. O script `sql/01-init.sql` cria automaticamente os bancos e tabelas na primeira vez que o container sobe — não é necessário nenhum passo manual de criação de schema.

### 1. Subir o MySQL com Docker

```bash
docker-compose up -d
```

Isso sobe um container MySQL 8.0 (`targetcompare-mysql`) na porta `3306` com:

| Parâmetro | Valor |
|----------|-------|
| Usuário | `root` |
| Senha | `123456` |
| Banco microrna.org | `targets` |
| Banco TargetScan | `targetscan` |

Os dados ficam persistidos no volume Docker `mysql_data`. O script SQL só é executado automaticamente na **primeira inicialização** (quando o volume está vazio). Para forçar uma reinicialização limpa:

```bash
docker-compose down -v   # remove o volume
docker-compose up -d     # sobe novamente e recria o schema
```

### 2. Estrutura dos bancos

#### Banco `targets` (microrna.org)

```sql
CREATE TABLE micrornaorg (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    mirna VARCHAR(100) NOT NULL,
    gene  VARCHAR(100) NOT NULL
);
```

Cada linha mapeia diretamente um miRNA a um gene-alvo predito.

#### Banco `targetscan` (TargetScan)

```sql
CREATE TABLE mirna (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    mirna     VARCHAR(100) NOT NULL,
    miRFamily VARCHAR(255) NOT NULL
);

CREATE TABLE targets (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    miRFamily VARCHAR(255) NOT NULL,
    Gene      VARCHAR(100) NOT NULL
);
```

O relacionamento é feito via família de miRNA (`miRFamily`). A consulta une as duas tabelas para retornar os genes preditos para cada miRNA individualmente.

---

## Onde obter os dados e como carregar

O Docker Compose monta a pasta `carga/` do projeto em `/tmp/carga/` dentro do container, permitindo importar arquivos via `LOAD DATA INFILE` sem precisar copiá-los para dentro do container.

### Carga inicial com os dados de teste

O arquivo `carga.zip` (~375 MB compactado, ~458 MB descompactado) já contém os dados prontos para uso. Descompacte-o na raiz do projeto:

**Linux/macOS:**
```bash
unzip carga.zip
```

**Windows (PowerShell):**
```powershell
Expand-Archive -Path carga.zip -DestinationPath .
```

Isso vai popular a pasta `carga/` com a seguinte estrutura:

```
carga/
├── micrornaorg/
│   └── miRTarBase_MTI.csv                          # fonte: miRTarBase
└── Targetscan/
    ├── miR_Family_Info.txt                         # famílias de miRNAs
    └── Predicted_Targets_Info.default_predictions.txt  # genes preditos
```

### Carregando os dados no banco

Suba o container antes de continuar:

```bash
docker-compose up -d
```

Conecte ao MySQL do container:

```bash
docker exec -it targetcompare-mysql mysql -uroot -p123456
```

#### Banco `targets` — miRTarBase (usado pela opção microrna.org)

O arquivo `miRTarBase_MTI.csv` tem 9 colunas separadas por vírgula. As colunas relevantes são `miRNA` (col 2) e `Target Gene` (col 4); as demais são descartadas com variáveis `@`:

```sql
USE targets;

LOAD DATA INFILE '/tmp/carga/micrornaorg/miRTarBase_MTI.csv'
INTO TABLE micrornaorg
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(@col1, mirna, @col3, gene, @col5, @col6, @col7, @col8, @col9);
```

#### Banco `targetscan` — TargetScan

São dois arquivos separados por tabulação.

**Tabela `mirna`** — a partir de `miR_Family_Info.txt` (7 colunas):

```sql
USE targetscan;

LOAD DATA INFILE '/tmp/carga/Targetscan/miR_Family_Info.txt'
INTO TABLE mirna
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(miRFamily, @col2, @col3, mirna, @col5, @col6, @col7);
```

**Tabela `targets`** — a partir de `Predicted_Targets_Info.default_predictions.txt` (11 colunas):

```sql
LOAD DATA INFILE '/tmp/carga/Targetscan/Predicted_Targets_Info.default_predictions.txt'
INTO TABLE targets
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(miRFamily, @col2, Gene, @col4, @col5, @col6, @col7, @col8, @col9, @col10, @col11);
```

### Onde obter versões atualizadas dos arquivos

| Fonte | URL de download | Arquivo |
|-------|----------------|---------|
| **miRTarBase** | [mirtarbase.cuhk.edu.cn/~miRTarBase/miRTarBase_2025](https://mirtarbase.cuhk.edu.cn/~miRTarBase/miRTarBase_2025) | `miRTarBase_MTI.xlsx` → salvar como CSV (separado por vírgula) |
| **TargetScan** | [targetscan.org/cgi-bin/targetscan/data_download.cgi](https://www.targetscan.org/cgi-bin/targetscan/data_download.cgi) | `miR_Family_Info.txt` e `Predicted_Targets_Info.default_predictions.txt` |

> **Nota:** o antigo domínio `microrna.org` foi descontinuado. O miRTarBase é atualmente mantido pela CUHK (Chinese University of Hong Kong).

### Dados de exemplo incluídos

O sistema já traz conjuntos de miRNAs de exemplo com resultados confirmados para validar o funcionamento:

**microrna.org — 6 miRNAs / ~674 genes em comum:**
```
hsa-miR-3689a-3p, hsa-miR-3689c, hsa-miR-4728-5p,
hsa-miR-1827, hsa-miR-940, hsa-miR-485-5p
```

**TargetScan — 5 miRNAs / ~108 genes em comum:**
```
hsa-miR-124-3p.1, hsa-miR-16-5p, hsa-miR-27a-3p,
hsa-miR-30a-5p, hsa-miR-15a-5p
```

Ao selecionar um banco de dados na interface, esses exemplos são carregados automaticamente no campo de entrada para facilitar o teste.

---

## Executando o projeto

### No Eclipse

1. Importe o projeto: `File → Import → Existing Projects into Workspace`
2. Selecione a pasta raiz do projeto
3. Todas as dependências já estão em `lib/` e configuradas no `.classpath`
4. Execute a classe `targetcompare.Compare` como aplicação Java

### Via linha de comando (após compilar)

```bash
java -cp "bin;lib/*" targetcompare.Compare
```

No Linux/macOS substitua `;` por `:`:

```bash
java -cp "bin:lib/*" targetcompare.Compare
```

---

## Usando a aplicação

### Janela principal — Compare

![Fluxo principal](docs/fluxo.png)

1. **Selecione o banco de dados** no combo superior:
   - `microrna.org` — banco baseado em predições do microrna.org
   - `targetScan` — banco baseado em predições do TargetScan

2. **Insira os miRNAs** na caixa de texto, separados por vírgula:
   ```
   hsa-miR-16-5p, hsa-miR-15a-5p, hsa-miR-27a-3p
   ```

3. **Defina o mínimo de matches** (combo "Mínimo"): número mínimo de miRNAs que devem visar um gene para ele aparecer no resultado. Valores de 2 a 15.

4. Clique em **"Iniciar Análise"**. O processamento ocorre em segundo plano; aguarde a saída ser exibida.

5. O resultado aparece na caixa de texto inferior no formato:

   ```
   nome          hsa-miR-16-5p   hsa-miR-15a-5p   hsa-miR-27a-3p
   CCND1         true            true             false          (Qtdade: 2)
   BCL2          true            true             true           (Qtdade: 3)
   ...

   Total de genes encontrados: 45
   Genes com 3 ou mais matches: 12
   ```

6. Após a análise, clique em **"Abrir SOM"** para treinar a rede neural sobre os resultados.

### Janela SOM — Rede Neural

1. Os **parâmetros são pré-configurados** conforme o banco selecionado (veja a seção abaixo). Você pode ajustá-los manualmente.

2. Clique em **"Iniciar Rede"** para criar a rede com os parâmetros definidos.

3. Clique em **"Treinar Rede"** para executar o algoritmo de Kohonen. Ao final, o resultado da classificação é automaticamente anexado à saída da janela Compare.

4. Clique em **"Plotar Gráfico"** para abrir o scatter plot com:
   - **Pontos azuis:** padrões de entrada (genes)
   - **Pontos vermelhos:** neurônios treinados (centróides dos clusters)

5. Use o menu **Arquivo** para salvar ou carregar uma rede treinada (arquivos `.ob`).

---

## Parâmetros do SOM

O algoritmo de Kohonen possui quatro parâmetros principais. Os valores abaixo são os pré-configurados e validados:

| Parâmetro | microrna.org | TargetScan | Descrição |
|----------|-------------|-----------|----------|
| **Épocas (T)** | 8000 | 5000 | Número de iterações de treinamento |
| **Neurônios** | 20 | 12 | Número de neurônios (clusters) na rede |
| **Taxa de aprendizado (α)** | 0.7 | 0.7 | Velocidade de atualização dos pesos no início |
| **Raio gaussiano (σ)** | 0.30 | 0.25 | Raio inicial de vizinhança entre neurônios |

### Como o treinamento funciona

A cada época:

1. Um padrão de entrada é selecionado aleatoriamente
2. O neurônio mais próximo é encontrado por distância Euclidiana (BMU)
3. Os pesos do BMU e dos seus vizinhos são atualizados, atraindo-os em direção ao padrão
4. O raio de vizinhança e a taxa de aprendizado decaem exponencialmente:

```
σ(t) = σ(0) × e^(−t/T)
α(t) = α(0) × e^(−1/T)
```

O decaimento para quando σ < 0.01 ou α < 0.01.

### Quando ajustar os parâmetros

| Situação | Ajuste sugerido |
|---------|----------------|
| Resultado muito fragmentado (muitos clusters) | Reduza o número de neurônios |
| Genes muito agrupados (poucos clusters) | Aumente o número de neurônios |
| Rede não converge (scatter plot desorganizado) | Aumente épocas ou reduza α inicial |
| Treinamento muito lento | Reduza épocas |

---

## Avaliando os resultados

### Resultado da comparação (Etapa 1)

- **Qtdade** indica quantos miRNAs visam aquele gene. Quanto maior, mais relevante é o gene como regulado conjuntamente.
- Genes com **Qtdade = max** (igual ao número de miRNAs inseridos) são visados por **todos** os miRNAs da análise — candidatos mais fortes para regulação compartilhada.
- O filtro "Mínimo" permite focar apenas nos genes mais relevantes. Comece com o mínimo 2 para ver o panorama completo e aumente gradualmente.

### Resultado do SOM (Etapa 2)

O resultado da classificação é uma tabela onde cada linha corresponde a um gene e a coluna indica o neurônio (cluster) ao qual foi atribuído:

```
         N1    N2    N3    ...
GENE_A    X
GENE_B          X
GENE_C    X
GENE_D                X
```

- Genes atribuídos ao **mesmo neurônio** possuem padrões similares de regulação (são visados pelos mesmos miRNAs).
- No **scatter plot**, neurônios muito próximos entre si indicam que os clusters são similares; neurônios distantes indicam grupos bem distintos.
- Neurônios sem nenhum gene atribuído indicam que o número de neurônios pode estar superestimado — tente reduzir.

### Boas práticas de análise

1. Execute a análise primeiro com o preset de exemplo para validar que os dados estão carregados corretamente.
2. Use o banco do **microrna.org** para conjuntos maiores de genes; use o **TargetScan** para conjuntos menores com famílias de miRNAs.
3. Salve a rede treinada (`Arquivo → Salvar`) para poder recarregar e comparar diferentes configurações.
4. Varie o número de neurônios e observe como os clusters se reorganizam no scatter plot.

---

## Estrutura do projeto

```
TargetCompare/
├── src/
│   ├── targetcompare/
│   │   ├── Compare.java        # Janela principal: busca e comparação de miRNAs
│   │   ├── Gene.java           # Modelo de dados: gene com mapeamento de miRNAs
│   │   └── Mirna.java          # Modelo de dados: miRNA com flag de alvo
│   └── com/som/
│       ├── core/
│       │   ├── Rede.java            # Implementação do algoritmo SOM (Kohonen)
│       │   ├── Neuronio.java        # Nó da rede neural
│       │   ├── Parametros.java      # Parâmetros serializáveis do SOM
│       │   ├── Entrada.java         # Modelo de entrada (legado)
│       │   └── MetodosAcessorios.java  # Utilitários: plot, CSV, serialização
│       ├── grafico/
│       │   └── GeradorGrafico.java  # Stub para geração de gráficos
│       └── view/
│           ├── view.java            # Janela de treinamento SOM
│           └── ScatterAdd.java      # Scatter plot (JFreeChart)
├── lib/                    # JARs de dependências
├── sql/
│   └── 01-init.sql         # Script de criação das tabelas
├── bin/                    # Saída de compilação (gerado pelo Eclipse)
├── docker-compose.yml      # MySQL 8.0 via Docker
└── .classpath              # Configuração de classpath do Eclipse
```

---

## Dependências

Todas as bibliotecas estão incluídas na pasta `lib/` e não requerem instalação adicional.

| Biblioteca | Versão | Uso |
|-----------|--------|-----|
| mysql-connector-j | 9.0.0 | Conexão JDBC com MySQL |
| JFreeChart | 1.0.17 | Scatter plot de visualização |
| jcommon | 1.0.21 | Suporte ao JFreeChart |
| JAMA | 1.0.2 | Transposição de matrizes |
| FlatLaf | 3.5.4 | Visual moderno (Light/Dark) |
| JUnit | 4.11 | Framework de testes |
| jfreesvg | 1.4 | Exportação de gráficos em SVG |
