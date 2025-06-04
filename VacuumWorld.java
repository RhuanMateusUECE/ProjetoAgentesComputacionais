import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Mundo do Aspirador: Representa o ambiente onde os agentes operam.
 * Gerencia o estado do mundo, movimento dos agentes e detecção de limpeza.
 */
public class VacuumWorld {
    private static final String DIRTY_CELL = "[D]";
    private static final String CLEAN_CELL = "[ ]";
    private static final String CLEANED_CELL = "[X]";
    
    private int rows, cols;
    private String[][] world;
    private final String[][] originalWorld; // Backup do estado inicial

    public VacuumWorld(String pathFile) {
        initializeWorld(pathFile);
        // Cria backup do mundo original
        this.originalWorld = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            this.originalWorld[i] = Arrays.copyOf(world[i], cols);
        }
    }

    /**
     * Move o agente reativo para uma nova posição
     */
    public boolean moveAgent(ReactiveAgent agent, Position newPosition) {
        boolean wasDirty = isDirty(newPosition);
        
        // Limpa posição anterior
        world[agent.getPosition().getX()][agent.getPosition().getY()] = CLEAN_CELL;
        
        // Move agente
        world[newPosition.getX()][newPosition.getY()] = agent.getName();
        agent.setPosition(newPosition);
        
        // Se havia sujeira, notifica o agente
        if (wasDirty) {
            agent.onCleanedDirt();
        }
        
        return true;
    }

    /**
     * Move o agente baseado em modelo para uma nova posição
     */
    public boolean moveAgent(ModelAgent agent, Position newPosition) {
        boolean wasDirty = isDirty(newPosition);
        
        // Atualiza modelo interno do agente
        agent.updateInternalModel(world);
        
        // Marca posição anterior adequadamente
        Position currentPos = agent.getPosition();
        if (agent.getCleanedPositions().contains(currentPos)) {
            world[currentPos.getX()][currentPos.getY()] = CLEANED_CELL;
        } else {
            world[currentPos.getX()][currentPos.getY()] = CLEAN_CELL;
        }
        
        // Move agente
        world[newPosition.getX()][newPosition.getY()] = agent.getName();
        agent.setPosition(newPosition);
        
        // Se havia sujeira, notifica o agente
        if (wasDirty) {
            agent.onCleanedDirt();
        }
        
        return true;
    }

    private boolean isDirty(Position position) {
        return DIRTY_CELL.equals(world[position.getX()][position.getY()]);
    }

    /**
     * Adiciona agente reativo ao mundo
     */
    public void addAgent(ReactiveAgent agent) {
        world[0][0] = agent.getName();
        agent.setPosition(new Position(0, 0));
    }

    /**
     * Adiciona agente baseado em modelo ao mundo
     */
    public void addAgent(ModelAgent agent) {
        world[0][0] = agent.getName();
        agent.setPosition(new Position(0, 0));
        agent.updateInternalModel(world);
    }

    /**
     * Conta quantas células sujas restam
     */
    public int getDirtyCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (DIRTY_CELL.equals(world[i][j])) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Exibe o estado atual do mundo
     */
    public void printWorld() {
        System.out.println("🗺️  ================= MUNDO =================");
        for (int i = 0; i < rows; i++) {
            System.out.print("   ");
            for (int j = 0; j < cols; j++) {
                System.out.print(world[i][j]);
            }
            System.out.println();
        }
        System.out.println("   Sujeira restante: " + getDirtyCount() + " célula(s)");
        System.out.println("   ==========================================");
    }

    /**
     * Exibe comparação entre estado original e atual
     */
    public void printComparison() {
        System.out.println("📊 ========== COMPARAÇÃO DO MUNDO ==========");
        System.out.println("   ORIGINAL                    ATUAL");
        
        for (int i = 0; i < rows; i++) {
            System.out.print("   ");
            for (int j = 0; j < cols; j++) {
                System.out.print(originalWorld[i][j]);
            }
            System.out.print("     ->     ");
            for (int j = 0; j < cols; j++) {
                System.out.print(world[i][j]);
            }
            System.out.println();
        }
        System.out.println("   ==========================================");
    }

    /**
     * Reinicia o mundo para o estado original
     */
    public void reset() {
        for (int i = 0; i < rows; i++) {
            this.world[i] = Arrays.copyOf(originalWorld[i], cols);
        }
    }

    /**
     * Retorna uma cópia do mundo atual
     */
    public String[][] getWorldCopy() {
        String[][] copy = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            copy[i] = Arrays.copyOf(world[i], cols);
        }
        return copy;
    }

    // Getters
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    private void initializeWorld(String pathFile) {
        try {
            String input = readEnvironmentFile(pathFile);
            String[][] matrix = parseEnvironmentMatrix(input);
            
            this.rows = matrix.length;
            this.cols = matrix[0].length;
            this.world = new String[rows][cols];
            
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    world[i][j] = matrix[i][j];
                }
            }
            
            System.out.println("✅ Mundo carregado: " + rows + "x" + cols + " com " + getDirtyCount() + " células sujas");
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar mundo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String readEnvironmentFile(String pathFile) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(pathFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String[][] parseEnvironmentMatrix(String input) {
        String[] lines = input.strip().split("\n");
        int rows = lines.length;
        int cols = lines[0].split("\\[").length - 1;

        String[][] matrix = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            String line = lines[i].trim();
            String[] cells = line.split("\\]");

            for (int j = 0; j < cols; j++) {
                String content = cells[j].replace("[", "").trim();
                matrix[i][j] = "D".equals(content) ? DIRTY_CELL : CLEAN_CELL;
            }
        }

        return matrix;
    }
}