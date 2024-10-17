/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.mycompany.geradordedietas;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Classe principal
public class GeradorDeDietas {

    private static int TAMANHO_POPULACAO ;
    private static int NUMERO_DE_GERACOES;
    private static int ELITISMO = 10; // Número de melhores indivíduos que são passados diretamente para a próxima geração
    private static double TAXA_MUTACAO;
    private static final double PESO_DIFERENCIAL = 0.7;

    //Variaveis
    private static List<Refeicao> refeicoes; // Refeições
    private static double objetivoCalorico; //Objetico Calorico
    private static List<IndividuoGenetico> todasDietas = new ArrayList<>();

    //Componentes para interface gráfica
    private static JProgressBar progressBar;
    private static JTextField pesoField, alturaField, idadeField;
    private static JTextField taxaMutacaoField, tamanhoPopulacaoField, numeroGeracoesField;
    private static JComboBox<String> sexoComboBox, objetivoComboBox, atividadeFisicaComboBox;
    private static JTextArea resultTextArea;

    //Classe Alimento
    public static class Alimento {

        String nome;
        double quantidade;
        double caloriasPor100g;

        //Construtor
        public Alimento(String nome, double quantidade, double caloriasPor100g) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.caloriasPor100g = caloriasPor100g;
        }
    }

    //Classe Refeição
    public static class Refeicao {

        String tipo;
        List<Alimento> alimentos;

        //Construtor
        public Refeicao(String tipo, List<Alimento> alimentos) {
            this.tipo = tipo;
            this.alimentos = alimentos;
        }
    }

    //Classe para representar um individuo genetico
    public static class IndividuoGenetico {

        List<Double> genes;
        double fitness;

        public IndividuoGenetico(List<Double> genes) {
            this.genes = genes;
            this.fitness = calcularFitness(genes);
        }
    }

    //Metodo com toda interface
    private static void gerarInterface() {
        JFrame frame = new JFrame("Algoritmo Genético - Otimização de Dieta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.lightGray);

        //Campos e caixas seletoras
        JPanel inputPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        adicionarCampo(inputPanel, "Peso (kg)", pesoField = new JTextField());
        adicionarCampo(inputPanel, "Altura (cm):", alturaField = new JTextField());
        adicionarCampo(inputPanel, "Idade:", idadeField = new JTextField());
        adicionarCampo(inputPanel, "Sexo:", sexoComboBox = new JComboBox<>(new String[]{"Masculino", "Feminino"}));
        adicionarCampo(inputPanel, "Objetivo:", objetivoComboBox = new JComboBox<>(new String[]{"Manter Peso", "Emagrecer", "Engordar"}));
        adicionarCampo(inputPanel, "Atividade Física:", atividadeFisicaComboBox = new JComboBox<>(new String[]{"Sedentária", "Levemente Ativa", "Moderadamente Ativa", "Muito Ativa", "Extremamente Ativa"}));
        adicionarCampo(inputPanel, "Taxa de Mutação: (MAX 1) (Valor sugerido) ->", taxaMutacaoField = new JTextField());
        adicionarCampo(inputPanel, "Tamanho da População: (Valor sugerido) ->", tamanhoPopulacaoField = new JTextField());
        adicionarCampo(inputPanel, "Número de Gerações: (Valor sugerido) ->", numeroGeracoesField = new JTextField());

        //valores pre configurados na interface, para a condiguração do algoritimo
        taxaMutacaoField.setText("0.55");
        tamanhoPopulacaoField.setText("100");
        numeroGeracoesField.setText("50");

        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Botão
        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Gerar Dieta");
        progressBar = new JProgressBar(0, 100);
        buttonPanel.add(startButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Painel de resultado
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultTextArea = new JTextArea(20, 30);
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(resultPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> executarAlgoritmoGenetico());

        // Definindo cores de fundo e texto para aprimorar a aparência
        inputPanel.setBackground(Color.lightGray);
        buttonPanel.setBackground(Color.lightGray);
        resultPanel.setBackground(Color.lightGray);
        resultTextArea.setBackground(Color.white);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void adicionarCampo(JPanel panel, String label, JComponent component) {
        panel.add(new JLabel(label));
        panel.add(component);
    }

    // Método para calcular o fitness do indivíduo
    private static double calcularFitness(List<Double> genes) {
        double totalCalorias = genes.stream()
                .mapToDouble(gene -> {
                    int index = genes.indexOf(gene);
                    int refeicaoIndex = index / refeicoes.get(0).alimentos.size();
                    Alimento alimento = refeicoes.get(refeicaoIndex).alimentos.get(index % refeicoes.get(0).alimentos.size());
                    return calcularCalorias(alimento, gene);
                })
                .sum();

        double desvioCalorico = Math.abs(totalCalorias - objetivoCalorico);
        double penalizacao = desvioCalorico > objetivoCalorico ? desvioCalorico / objetivoCalorico : 0;



        return desvioCalorico + 0.5 * penalizacao;
    }

    //Metodo para gerar os genes
    private static List<Double> gerarGenes() {
        List<Double> genes = new ArrayList<>();
        double soma = 0.0;

        for (Refeicao refeicao : refeicoes) {
            for (Alimento alimento : refeicao.alimentos) {
                double quantidade = Math.random() * 200;
                genes.add(quantidade);
                soma += quantidade;
            }
        }

        double fatorNormalizacao = objetivoCalorico / soma;

        for (int i = 0; i < genes.size(); i++) {
            genes.set(i, genes.get(i) * fatorNormalizacao);
        }

        return genes;
    }

    //Método para gerar a dieta, com os alimentos pré-definidos
    private static void gerarDietaEExibirResultado() {
        try {
            double peso = Double.parseDouble(pesoField.getText());
            double altura = Double.parseDouble(alturaField.getText());
            int idade = Integer.parseInt(idadeField.getText());

            String sexo = (String) sexoComboBox.getSelectedItem();
            String objetivo = (String) objetivoComboBox.getSelectedItem();
            String nivelAtividade = (String) atividadeFisicaComboBox.getSelectedItem();

            double alturaMetros = altura / 100;
            double imc = peso / (alturaMetros * alturaMetros);

            objetivoCalorico = calcularCaloriasTotais(peso, alturaMetros, idade, objetivo, sexo, nivelAtividade);

            refeicoes = List.of(
                    new Refeicao("Café da Manhã", List.of(
                            new Alimento("Ovo Mexido", 100.0, 150),
                            new Alimento("Aveia", 40.0, 120),
                            new Alimento("Frutas Frescas", 150.0, 70),
                            new Alimento("Chá Verde", 250.0, 0) // Sem calorias
                    )),
                    new Refeicao("Almoço", List.of(
                            new Alimento("Peito de Frango Grelhado", 150.0, 200),
                            new Alimento("Arroz Integral", 100.0, 120),
                            new Alimento("Brócolis Cozidos", 150.0, 50),
                            new Alimento("Salada Verde", 200.0, 30)
                    )),
                    new Refeicao("Jantar", List.of(
                            new Alimento("Salmão Assado", 150.0, 250),
                            new Alimento("Quinoa", 100.0, 120),
                            new Alimento("Espinafre Refogado", 150.0, 40),
                            new Alimento("Abacate", 100.0, 160)
                    ))
            );

            StringBuilder infoStringBuilder = new StringBuilder();
            
            infoStringBuilder.append(String.format("APÓS APERTAR O BOTÃO PARA GERAR DIETA, APERTE NOVAMENTE, PARA E VERIFIQUE O CONSOLE.\n"));
            
            infoStringBuilder.append(String.format("Seu IMC é %.2f.\n", imc));

            String mensagemObjetivo;
            if (imc > 25 && objetivo.equals("Engordar")) {
                mensagemObjetivo = "Você está acima do peso. Recomendamos que você faça uma dieta para perder peso.\n";
            } else {
                mensagemObjetivo = String.format("Para seu objetivo de %s, você precisa consumir %.2f calorias.\n\n", objetivo, objetivoCalorico);
            }
            infoStringBuilder.append(mensagemObjetivo);

            List<List<String>> dietaRecomendada = gerarDietaPorRefeicao(refeicoes, objetivo);

            exibirResultado(new IndividuoGenetico(gerarGenes()), dietaRecomendada, infoStringBuilder.toString());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos com valores numéricos válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Metodo para calcular quantas calorias o usuario tem que consumir para chegar ao seu objetivo, seja ele emagrecer, engordar ou manter peso
    private static double calcularCaloriasTotais(double peso, double altura, int idade, String objetivo, String sexo, String nivelAtividade) {
        double tmb;

        if (sexo.equals("Masculino")) {
            tmb = (0.063 * peso + 2.896) * 239;
        } else {
            tmb = (0.062 * peso + 2.036) * 239;
        }

        double fatorAtividade = 1.0;

        switch (nivelAtividade) {
            case "Sedentária":
                fatorAtividade = 1.2;
                break;
            case "Levemente Ativa":
                fatorAtividade = 1.375;
                break;
            case "Moderadamente Ativa":
                fatorAtividade = 1.55;
                break;
            case "Muito Ativa":
                fatorAtividade = 1.725;
                break;
            case "Extremamente Ativa":
                fatorAtividade = 1.9;
                break;
        }
        double fatorPeso = 1.0;

        switch (objetivo) {
            case "Manter Peso":
                break;
            case "Emagrecer":
                tmb *= 0.8;
                fatorPeso = 0.9; // Por exemplo, reduzir calorias para perda de peso
                break;
            case "Engordar":
                tmb *= 1.2;
                fatorPeso = 1.1; // Por exemplo, aumentar calorias para ganho de peso
                break;
        }

        return tmb * fatorAtividade * fatorPeso;
    }

    private static double calcularQuantidade(Alimento alimento, String objetivo) {
        if (objetivo.equals("Manter Peso")) {
            return alimento.quantidade;
        } else if (objetivo.equals("Emagrecer")) {
            return alimento.quantidade * 0.8;
        } else if (objetivo.equals("Engordar")) {
            return alimento.quantidade * 1.2;
        }
        return 0;
    }

    //Método para calcular q quantidade de caloria cada alimento ter
    private static double calcularCalorias(Alimento alimento, double quantidade) {
        return (quantidade / 100.0) * alimento.caloriasPor100g;
    }

    //Metodo para separar as dietas por refeição (cafe, almoço e ceia) sem esse metodo,
    // iria ser gerado uma "dieta" toda misturada, com feijao e frango no cafe da manhã, iogurte no almoço
    private static List<List<String>> gerarDietaPorRefeicao(List<Refeicao> refeicoes, String objetivo) {
        List<Double> genes = gerarGenes();

        List<List<String>> dietaPorRefeicao = new ArrayList<>();

        int geneIndex = 0;
        for (Refeicao refeicao : refeicoes) {
            List<String> alimentosRefeicao = new ArrayList<>();
            double somaQuantidades = 0.0;

            for (Alimento alimento : refeicao.alimentos) {
                double quantidade = genes.get(geneIndex);
                somaQuantidades += quantidade;

                // Limite a quantidade para evitar valores excessivos
                quantidade = Math.min(200.0, quantidade);

                alimentosRefeicao.add(alimento.nome + ": " + String.format("%.2f", quantidade) + "g = " + String.format("%.2f", calcularCalorias(alimento, quantidade)) + " calorias");
                geneIndex++;
            }

            // Normaliza as quantidades proporcionalmente
            double fatorNormalizacao = somaQuantidades > 0 ? 200.0 / somaQuantidades : 1.0;
            for (int i = geneIndex - refeicao.alimentos.size(); i < geneIndex; i++) {
                genes.set(i, genes.get(i) * fatorNormalizacao);
            }

            dietaPorRefeicao.add(alimentosRefeicao);
        }

        return dietaPorRefeicao;
    }

    // Função para selecionar um pai da população com base na roleta
    private static IndividuoGenetico selecionarPai(List<IndividuoGenetico> populacao) {
        double totalFitness = populacao.stream().mapToDouble(individuo -> 1 / (1 + individuo.fitness)).sum();
        double valorAleatorio = Math.random() * totalFitness;

        double acumulado = 0;
        for (IndividuoGenetico individuo : populacao) {
            acumulado += 1 / (1 + individuo.fitness);
            if (acumulado >= valorAleatorio) {
                return individuo;
            }
        }
        return populacao.get(populacao.size() - 1);
    }

    // Função para realizar o crossover (combinação) de dois conjuntos de genes
    private static List<Double> crossover(List<Double> genesPai1, List<Double> genesPai2) {
        List<Double> genesFilho = new ArrayList<>();

        for (int i = 0; i < genesPai1.size(); i++) {
            double gene = genesPai1.get(i) + PESO_DIFERENCIAL * (genesPai2.get(i) - genesPai1.get(i));
            if (Math.random() < TAXA_MUTACAO) {
                gene = Math.random() * 200;
            }
            genesFilho.add(gene);
        }

        return genesFilho;
    }

    // Função para realizar a mutação de um conjunto de genes
    private static void mutacao(List<Double> genes) {
        for (int i = 0; i < genes.size(); i++) {
            if (Math.random() < TAXA_MUTACAO) {
                genes.set(i, Math.random() * 200);
            }
        }
    }

    // Método para validar as entradas do usuário, garantindo que estejam dentro de faixas aceitáveis
    private static void validarEntradas() {
        try {
            double peso = Double.parseDouble(pesoField.getText());
            double altura = Double.parseDouble(alturaField.getText());
            int idade = Integer.parseInt(idadeField.getText());
            double taxaMutacao = Double.parseDouble(taxaMutacaoField.getText());

            // Verifica se a taxa de mutação está no intervalo [0, 1]
            if (taxaMutacao < 0 || taxaMutacao > 1) {
                throw new NumberFormatException("A taxa de mutação deve estar entre 0 e 1.");
            }

            // Verifica se os valores de peso, altura e idade estão dentro de faixas aceitáveis
            if (peso <= 0 || peso > 350 || altura <= 0 || altura > 300 || idade <= 0 || idade > 105) {
                throw new NumberFormatException("Preencha os campos com valores válidos. Peso, altura e idade devem estar dentro das faixas aceitáveis.");
            }

            // Configuração dos parâmetros globais com base nas entradas do usuário
            TAXA_MUTACAO = taxaMutacao;
            TAMANHO_POPULACAO = Integer.parseInt(tamanhoPopulacaoField.getText());
            NUMERO_DE_GERACOES = Integer.parseInt(numeroGeracoesField.getText());

        } catch (NumberFormatException e) {
            // Captura exceção caso as entradas não sejam válidas
            JOptionPane.showMessageDialog(null, "Por favor, preencha os campos com valores numéricos válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JFrame exibirBarraProgresso() {
        JFrame progressFrame = new JFrame("Aguarde");
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        progressFrame.setLayout(new BorderLayout());

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

        progressPanel.add(new JLabel("Gerando dieta otimizada, aguarde..."));
        progressBar = new JProgressBar(0, 100);
        progressPanel.add(progressBar);

        progressFrame.add(progressPanel, BorderLayout.CENTER);

        progressFrame.setSize(300, 100);
        progressFrame.setLocationRelativeTo(null);
        progressFrame.setVisible(true);

        return progressFrame;
    }

    /*Inicializa uma população de indivíduos genéticos, criando instâncias de IndividuoGenetico
    com genes gerados aleatoriamente*/
    private static List<IndividuoGenetico> inicializarPopulacao() {
        List<IndividuoGenetico> populacao = new ArrayList<>();
        for (int i = 0; i < TAMANHO_POPULACAO; i++) {
            populacao.add(new IndividuoGenetico(gerarGenes()));
        }
        return populacao;
    }

    //Método para avaliar o fitnees de cada individuo
    private static void avaliarFitness(List<IndividuoGenetico> populacao) {
        for (IndividuoGenetico individuo : populacao) {
            individuo.fitness = calcularFitness(individuo.genes);
        }
    }

    //Metodo para selecionar os melhores individuos da população
    private static List<IndividuoGenetico> selecionarMelhores(List<IndividuoGenetico> populacao) {
        List<IndividuoGenetico> novaPopulacao = new ArrayList<>();
        for (int i = 0; i < ELITISMO; i++) {
            novaPopulacao.add(new IndividuoGenetico(populacao.get(i).genes));
        }
        return novaPopulacao;
    }

    /* Metodo para realizar o crossover e mutação para criar novos indivíduos e completar a população até atingir
    o tamanho desejado (TAMANHO_POPULACAO). */
    private static void realizarCrossoverEMutacao(List<IndividuoGenetico> populacao, List<IndividuoGenetico> novaPopulacao) {
        while (novaPopulacao.size() < TAMANHO_POPULACAO) {
            IndividuoGenetico pai1 = selecionarPai(populacao);
            IndividuoGenetico pai2 = selecionarPai(populacao);

            List<Double> genesFilho = crossover(pai1.genes, pai2.genes);
            mutacao(genesFilho);

            novaPopulacao.add(new IndividuoGenetico(genesFilho));
        }
    }

    // Método para realizar o algoritmo genético completo, incluindo exibição de resultados no console
    private static void executarAlgoritmoCompleto() {
        try {
            // Inicializa uma população de indivíduos genéticos
            List<IndividuoGenetico> populacao = inicializarPopulacao();

            // Realiza as iterações do algoritmo genético
            for (int geracao = 0; geracao < NUMERO_DE_GERACOES; geracao++) {
                avaliarFitness(populacao);
                populacao.sort(Comparator.comparingDouble(o -> o.fitness));

                // Exibe as informações da melhor dieta no console
                exibirResultado(populacao.get(0), gerarDietaPorRefeicao(refeicoes, "Manter Peso"), "");

                // Adiciona todas as dietas geradas no console
                System.out.println("\nDieta da Geração " + (geracao + 1) + ":\n");
                for (IndividuoGenetico individuo : populacao) {
                    List<List<String>> dieta = gerarDietaPorRefeicao(refeicoes, "Manter Peso");
                    exibirResultado(individuo, dieta, "");
                    System.out.println("Fitness: " + individuo.fitness);
                    System.out.println("----------------------------");
                }

                List<IndividuoGenetico> novaPopulacao = selecionarMelhores(populacao);

                realizarCrossoverEMutacao(populacao, novaPopulacao);

                populacao = new ArrayList<>(novaPopulacao);
            }
        } catch (NumberFormatException e) {
            // Captura exceção caso as entradas não sejam válidas
            JOptionPane.showMessageDialog(null, "Por favor, preencha os campos com valores numéricos válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para executar o algoritmo genético, incluindo a exibição de resultados na interface gráfica
    private static void executarAlgoritmoGenetico() {
        try {
            validarEntradas();

            // Exibe uma barra de progresso simulando a dieta sendo gerada
            JFrame progressFrame = exibirBarraProgresso();

            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    executarAlgoritmoCompleto(); // Chama o novo método diretamente
                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    int progressValue = chunks.get(chunks.size() - 1);
                    progressBar.setValue(progressValue);
                }

                @Override
                protected void done() {
                    // Código após as iterações do algoritmo genético
                    progressFrame.dispose();
                    // A linha abaixo mantém a exibição da melhor dieta na interface gráfica
                    gerarDietaEExibirResultado();
                }
            };

            worker.execute();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Por favor, preencha os campos com valores numéricos válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Metodo para exibir o resultado final da dieta
    private static void exibirResultado(IndividuoGenetico individuo, List<List<String>> dieta, String infoAdicional) {
       
        
        
        resultTextArea.setText("");

        resultTextArea.append(infoAdicional);

        resultTextArea.append("Indivíduo:\n");
        resultTextArea.append("Fitness: " + individuo.fitness + "\n\n");

        resultTextArea.append("Dieta sugerida:\n\n");

        int refeicaoIndex = 0;
        for (List<String> alimentosRefeicao : dieta) {
            String nomeRefeicao = refeicoes.get(refeicaoIndex).tipo;
            resultTextArea.append(nomeRefeicao + ":\n");

            for (String alimento : alimentosRefeicao) {
                resultTextArea.append(alimento + "\n");
            }

            resultTextArea.append("\n");
            refeicaoIndex++;
        }

        resultTextArea.setCaretPosition(resultTextArea.getDocument().getLength());
        
        resultTextArea.append("APÓS APERTAR O BOTÃO PARA GERAR DIETA, APERTE NOVAMENTE, PARA E VERIFIQUE O CONSOLE.\n\n");
    }

    // Método Main que inicia a interface gráfica e faz tudo acontecer
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> gerarInterface());
    }

}

