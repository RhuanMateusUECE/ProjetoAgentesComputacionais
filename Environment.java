import java.util.Scanner;

/**
 * Ambiente de Simulação: Controla a execução e interação entre agentes e mundo.
 * Suporta simulações manuais, automáticas e comparativas entre diferentes agentes.
 */
public class Environment {
    private static final int MAX_STEPS = 50; // Limite de 50 passos
    private static final String[] DIRECTION_NAMES = {"up", "down", "left", "right"};
    
    private final Scanner scanner;
    private boolean autoMode = false;
    private int delayMs = 500; // Delay para modo automático

    public Environment() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Simula um agente reativo
     */
    public SimulationResult simulate(VacuumWorld world, ReactiveAgent agent) {
        return simulate(world, agent, false);
    }

    public SimulationResult simulate(VacuumWorld world, ReactiveAgent agent, boolean autoMode) {
        this.autoMode = autoMode;
        
        System.out.println("\n🤖 === INICIANDO SIMULAÇÃO: " + agent.getName() + " (REATIVO) ===");
        world.printWorld();
        world.addAgent(agent);
        world.printWorld();

        int initialDirtCount = world.getDirtyCount();
        long startTime = System.currentTimeMillis();

        while (agent.getSteps() < MAX_STEPS) { // Remove condição de sujeira - sempre 50 passos
            if (!autoMode) {
                System.out.println("\nPressione Enter para continuar... (Passo " + (agent.getSteps() + 1) + "/" + MAX_STEPS + ")");
                scanner.nextLine();
            } else {
                sleep(delayMs);
            }

            int action = agent.chooseAction();
            boolean moveExecuted = executeMove(world, agent, action);

            if (!moveExecuted) {
                agent.setSteps(agent.getSteps() - 1); // Desfaz incremento se movimento falhou
                agent.setScore(agent.getScore() + 1);  // Remove penalidade
            }

            System.out.println("Score: " + agent.getScore() + " | Passos: " + agent.getSteps() + "/" + MAX_STEPS + " | Sujeira restante: " + world.getDirtyCount());
            world.printWorld();

            if (agent.getSteps() % 10 == 0) {
                agent.printStats();
            }
        }

        // Sempre completa 50 passos
        System.out.println("✅ SIMULAÇÃO CONCLUÍDA! Agente completou " + MAX_STEPS + " passos.");
        if (world.getDirtyCount() == 0) {
            System.out.println("🎉 BÔNUS: Todo o ambiente foi limpo durante os " + MAX_STEPS + " passos!");
        } else {
            System.out.println("🔄 Restam " + world.getDirtyCount() + " célula(s) sujas.");
        }

        long endTime = System.currentTimeMillis();
        return new SimulationResult(agent.getName(), agent.getSteps(), agent.getScore(), 
                                  initialDirtCount - world.getDirtyCount(), endTime - startTime);
    }

    /**
     * Simula um agente baseado em modelo
     */
    public SimulationResult simulate(VacuumWorld world, ModelAgent agent) {
        return simulate(world, agent, false);
    }

    public SimulationResult simulate(VacuumWorld world, ModelAgent agent, boolean autoMode) {
        this.autoMode = autoMode;
        
        System.out.println("\n🧠 === INICIANDO SIMULAÇÃO: " + agent.getName() + " (MODELO) ===");
        world.printWorld();
        world.addAgent(agent);
        world.printWorld();

        int initialDirtCount = world.getDirtyCount();
        long startTime = System.currentTimeMillis();

        while (agent.getSteps() < MAX_STEPS) { // Remove condição de sujeira - sempre 50 passos
            if (!autoMode) {
                System.out.println("\nPressione Enter para continuar... (Passo " + (agent.getSteps() + 1) + "/" + MAX_STEPS + ")");
                scanner.nextLine();
            } else {
                sleep(delayMs);
            }

            int action = agent.chooseAction();

            // Agente modelo pode decidir "não fazer nada" se quiser, mas ainda conta como passo
            if (action == -1) {
                System.out.println(agent.getName() + " escolheu não se mover neste turno.");
                agent.setSteps(agent.getSteps() + 1); // Conta como passo mesmo sem movimento
                continue;
            }

            boolean moveExecuted = executeMove(world, agent, action);

            if (!moveExecuted) {
                agent.setSteps(agent.getSteps() - 1);
                agent.setScore(agent.getScore() + 1);
            }

            System.out.println("Score: " + agent.getScore() + " | Passos: " + agent.getSteps() + "/" + MAX_STEPS + " | Sujeira restante: " + world.getDirtyCount());
            world.printWorld();

            if (agent.getSteps() % 5 == 0) {
                agent.printDetailedStatus();
            }
        }

        // Sempre completa 50 passos
        System.out.println("✅ SIMULAÇÃO CONCLUÍDA! Agente completou " + MAX_STEPS + " passos.");
        if (world.getDirtyCount() == 0) {
            System.out.println("🎉 BÔNUS: Todo o ambiente foi limpo durante os " + MAX_STEPS + " passos!");
        } else {
            System.out.println("🔄 Restam " + world.getDirtyCount() + " célula(s) sujas.");
        }

        long endTime = System.currentTimeMillis();
        return new SimulationResult(agent.getName(), agent.getSteps(), agent.getScore(), 
                                  initialDirtCount - world.getDirtyCount(), endTime - startTime);
    }

    private boolean executeMove(VacuumWorld world, ReactiveAgent agent, int direction) {
        Position newPosition = calculateNewPosition(agent.getPosition(), direction);
        
        if (!newPosition.isValid(world.getRows(), world.getCols())) {
            System.out.println("💥 " + agent.getName() + " bateu na parede!");
            return false;
        }

        world.moveAgent(agent, newPosition);
        return true;
    }

    private boolean executeMove(VacuumWorld world, ModelAgent agent, int direction) {
        Position newPosition = calculateNewPosition(agent.getPosition(), direction);
        
        if (!newPosition.isValid(world.getRows(), world.getCols())) {
            System.out.println("💥 " + agent.getName() + " bateu na parede!");
            return false;
        }

        world.moveAgent(agent, newPosition);
        return true;
    }

    private Position calculateNewPosition(Position current, int direction) {
        switch (direction) {
            case 0: return current.moveUp();
            case 1: return current.moveDown();
            case 2: return current.moveLeft();
            case 3: return current.moveRight();
            default: return current;
        }
    }

    /**
     * Executa comparação entre dois agentes no mesmo ambiente
     */
    public void compareAgents(String worldPath) {
        System.out.println("\n🏆 === COMPARAÇÃO DE AGENTES ===");
        
        // Teste com Agente Reativo
        VacuumWorld world1 = new VacuumWorld(worldPath);
        ReactiveAgent reactiveAgent = new ReactiveAgent("[R]");
        SimulationResult reactiveResult = simulate(world1, reactiveAgent, true);
        
        System.out.println("\n" + "=".repeat(50));
        
        // Teste com Agente Baseado em Modelo
        VacuumWorld world2 = new VacuumWorld(worldPath);
        ModelAgent modelAgent = new ModelAgent("[M]");
        SimulationResult modelResult = simulate(world2, modelAgent, true);
        
        // Exibe comparação final
        printComparison(reactiveResult, modelResult);
    }

    private void printComparison(SimulationResult reactive, SimulationResult model) {
        System.out.println("\n📊 === RESULTADO DA COMPARAÇÃO (50 passos obrigatórios) ===");
        System.out.println("┌─────────────────────┬─────────────┬─────────────┐");
        System.out.println("│ Métrica             │ Reativo     │ Modelo      │");
        System.out.println("├─────────────────────┼─────────────┼─────────────┤");
        System.out.printf("│ Passos              │ %-11d │ %-11d │%n", reactive.steps, model.steps);
        System.out.printf("│ Score Final         │ %-11d │ %-11d │%n", reactive.score, model.score);
        System.out.printf("│ Sujeira Limpa       │ %-11d │ %-11d │%n", reactive.dirtCleaned, model.dirtCleaned);
        System.out.printf("│ Tempo (ms)          │ %-11d │ %-11d │%n", reactive.timeMs, model.timeMs);
        System.out.printf("│ Eficiência*         │ %-11.2f │ %-11.2f │%n", reactive.getEfficiency(), model.getEfficiency());
        System.out.println("└─────────────────────┴─────────────┴─────────────┘");
        System.out.println("* Eficiência = Sujeira Limpa / Passos");
        
        // Lógica para determinar vencedor baseada em performance
        String winner;
        if (reactive.dirtCleaned != model.dirtCleaned) {
            // Quem limpou mais sujeira vence
            winner = model.dirtCleaned > reactive.dirtCleaned ? "🧠 Agente Modelo" : "🤖 Agente Reativo";
        } else if (reactive.dirtCleaned == 0 && model.dirtCleaned == 0) {
            // Se ninguém limpou nada, empate
            winner = "🤝 Empate (ninguém limpou)";
        } else {
            // Se limparam a mesma quantidade, vence quem teve melhor score
            winner = model.score > reactive.score ? "🧠 Agente Modelo" : 
                    reactive.score > model.score ? "🤖 Agente Reativo" : "🤝 Empate";
        }
        
        System.out.println("\n🏆 Vencedor: " + winner);
        
        // Análise de eficiência
        if (model.getEfficiency() > reactive.getEfficiency()) {
            System.out.println("💡 Agente Modelo foi mais eficiente (limpou mais por passo)!");
        } else if (reactive.getEfficiency() > model.getEfficiency()) {
            System.out.println("💡 Agente Reativo foi mais eficiente (surpreendente!)");
        }
    }

    /**
     * Menu interativo para escolher modo de simulação
     */
    public void runInteractiveMenu() {
        while (true) {
            System.out.println("\n🎮 === SIMULADOR DE ASPIRADOR ===");
            System.out.println("1. Simular Agente Reativo");
            System.out.println("2. Simular Agente Baseado em Modelo");
            System.out.println("3. Comparar Agentes");
            System.out.println("4. Configurações");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opção: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    runSingleAgentSimulation("reactive");
                    break;
                case "2":
                    runSingleAgentSimulation("model");
                    break;
                case "3":
                    runComparisonSimulation();
                    break;
                case "4":
                    configureSettings();
                    break;
                case "5":
                    System.out.println("👋 Até logo!");
                    return;
                default:
                    System.out.println("❌ Opção inválida!");
            }
        }
    }

    private void runSingleAgentSimulation(String agentType) {
        System.out.print("Digite o caminho do ambiente (ex: environments/environment1.txt): ");
        String worldPath = scanner.nextLine().trim();
        
        System.out.print("Modo automático? (s/n): ");
        boolean auto = scanner.nextLine().trim().toLowerCase().startsWith("s");
        
        try {
            VacuumWorld world = new VacuumWorld(worldPath);
            
            if ("reactive".equals(agentType)) {
                ReactiveAgent agent = new ReactiveAgent("[R]");
                SimulationResult reactiveResult = simulate(world, agent, auto);
                reactiveResult.printDetailedReport();
            } else {
                ModelAgent agent = new ModelAgent("[M]");
                SimulationResult modelResult = simulate(world, agent, auto);
                modelResult.printDetailedReport();
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void runComparisonSimulation() {
        System.out.print("Digite o caminho do ambiente: ");
        String worldPath = scanner.nextLine().trim();
        
        try {
            compareAgents(worldPath);
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void configureSettings() {
        System.out.println("\n⚙️  === CONFIGURAÇÕES ===");
        System.out.println("Delay atual (modo automático): " + delayMs + "ms");
        System.out.print("Novo delay em ms (0-2000): ");
        
        try {
            int newDelay = Integer.parseInt(scanner.nextLine().trim());
            if (newDelay >= 0 && newDelay <= 2000) {
                this.delayMs = newDelay;
                System.out.println("✅ Delay atualizado para " + delayMs + "ms");
            } else {
                System.out.println("❌ Valor inválido!");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido!");
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Classe para armazenar resultados de simulação
    public static class SimulationResult {
        public final String agentName;
        public final int steps;
        public final int score;
        public final int dirtCleaned;
        public final long timeMs;

        public SimulationResult(String agentName, int steps, int score, int dirtCleaned, long timeMs) {
            this.agentName = agentName;
            this.steps = steps;
            this.score = score;
            this.dirtCleaned = dirtCleaned;
            this.timeMs = timeMs;
        }

        public double getEfficiency() {
            return steps > 0 ? (double) dirtCleaned / steps : 0.0;
        }

        public void printDetailedReport() {
            System.out.println("\n📋 === RELATÓRIO FINAL ===");
            System.out.println("🤖 Agente: " + agentName);
            System.out.println("👣 Total de passos: " + steps + "/50 (COMPLETO)");
            System.out.println("🏆 Score final: " + score);
            System.out.println("🧹 Sujeira limpa: " + dirtCleaned);
            System.out.println("⏱️  Tempo total: " + timeMs + "ms");
            System.out.println("📈 Eficiência: " + String.format("%.3f", getEfficiency()));
            
            if (dirtCleaned > 0) {
                System.out.println("✅ Status: SUJEIRA LIMPA DURANTE OS 50 PASSOS");
            } else {
                System.out.println("❌ Status: NENHUMA SUJEIRA LIMPA");
            }
            System.out.println("========================");
        }
    }

    /**
     * Método principal com exemplos de uso
     */
    public static void main(String[] args) {
        Environment env = new Environment();
        
        if (args.length > 0 && "menu".equals(args[0])) {
            // Modo menu interativo
            env.runInteractiveMenu();
        } else {
            // Modo demonstração rápida
            System.out.println("🚀 Modo demonstração - use 'java Environment menu' para modo interativo");
            
            try {
                // Exemplo: Comparação rápida
                env.compareAgents("environments/environment1.txt");
                
                System.out.println("\n" + "=".repeat(60));
                
                // Exemplo: Simulação individual
                VacuumWorld world = new VacuumWorld("environments/environment2.txt");
                ModelAgent agent = new ModelAgent("[DEMO]");
                SimulationResult demoResult = env.simulate(world, agent, true);
                demoResult.printDetailedReport();
                
            } catch (Exception e) {
                System.out.println("❌ Erro durante demonstração: " + e.getMessage());
                System.out.println("💡 Certifique-se de que os arquivos environment1.txt e environment2.txt existem na pasta environments/");
                System.out.println("💡 Use 'java TestEnvironments generate' para criar ambientes de teste");
            }
        }
    }
}