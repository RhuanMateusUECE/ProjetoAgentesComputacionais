import java.util.*;

/**
 * Agente Baseado em Modelo: Mantém uma representação interna do ambiente
 * e toma decisões baseadas no conhecimento acumulado. Usa estratégias
 * inteligentes para maximizar a eficiência da limpeza.
 */
public class ModelAgent {
    private static final String[] DIRECTION_NAMES = {"up", "down", "left", "right"};
    private static final int MOVEMENT_COST = -1;
    private static final int CLEANING_REWARD = 2;
    
    // Estado básico do agente
    private final String name;
    private Position position;
    private int steps;
    private int score;
    
    // Modelo interno do ambiente
    private final Set<Position> cleanedPositions;
    private final Set<Position> exploredPositions;
    private final Set<Position> knownDirtyPositions;
    private final Map<Position, String> internalMap;
    
    // Informações do mundo
    private int worldRows = -1;
    private int worldCols = -1;
    
    // Estratégia atual
    private AgentStrategy currentStrategy;

    public enum AgentStrategy {
        TARGETING_DIRT("Indo para sujeira conhecida"),
        EXPLORING("Explorando áreas desconhecidas"),
        BACKTRACKING("Retornando por áreas limpas"),
        FINISHED("Trabalho concluído");
        
        private final String description;
        
        AgentStrategy(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }

    public ModelAgent(String name) {
        this.name = name;
        this.position = new Position(0, 0);
        this.steps = 0;
        this.score = 0;
        this.cleanedPositions = new HashSet<>();
        this.exploredPositions = new HashSet<>();
        this.knownDirtyPositions = new HashSet<>();
        this.internalMap = new HashMap<>();
        this.currentStrategy = AgentStrategy.EXPLORING;
    }

    /**
     * Atualiza o modelo interno do agente com informações do ambiente
     */
    public void updateInternalModel(String[][] world) {
        if (worldRows == -1) {
            worldRows = world.length;
            worldCols = world[0].length;
        }
        
        // Explora posições adjacentes visíveis
        Position[] adjacentPositions = position.getAdjacentPositions();
        
        for (Position pos : adjacentPositions) {
            if (pos.isValid(worldRows, worldCols)) {
                String cellContent = world[pos.getX()][pos.getY()];
                internalMap.put(pos, cellContent);
                
                // Detecta sujeira
                if ("[D]".equals(cellContent)) {
                    knownDirtyPositions.add(pos);
                }
            }
        }
        
        // Marca posição atual como explorada
        exploredPositions.add(position);
        internalMap.put(position, name);
    }

    /**
     * Escolhe a próxima ação baseada na estratégia atual
     */
    public int chooseAction() {
        // Fase 1: Ir para sujeira conhecida
        Position targetDirt = findNearestDirt();
        if (targetDirt != null) {
            currentStrategy = AgentStrategy.TARGETING_DIRT;
            int direction = findPathToTarget(targetDirt, false);
            if (direction != -1) {
                executeAction(direction);
                return direction;
            }
        }
        
        // Fase 2: Explorar áreas desconhecidas
        currentStrategy = AgentStrategy.EXPLORING;
        int explorationDirection = exploreUnknownAreas();
        if (explorationDirection != -1) {
            executeAction(explorationDirection);
            return explorationDirection;
        }
        
        // Fase 3: Backtracking por áreas limpas se necessário
        currentStrategy = AgentStrategy.BACKTRACKING;
        int backtrackDirection = findBacktrackPath();
        if (backtrackDirection != -1) {
            executeAction(backtrackDirection);
            return backtrackDirection;
        }
        
        // Fase 4: Terminou
        currentStrategy = AgentStrategy.FINISHED;
        System.out.println("🎉 " + name + " terminou! Ambiente completamente explorado.");
        this.setSteps(100);
        return -1;
    }

    private Position findNearestDirt() {
        if (knownDirtyPositions.isEmpty()) return null;
        
        return knownDirtyPositions.stream()
                .min(Comparator.comparingInt(dirt -> position.manhattanDistance(dirt)))
                .orElse(null);
    }

    private int findPathToTarget(Position target, boolean allowCleanedPositions) {
        if (target == null) return -1;
        
        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> parent = new HashMap<>();
        Set<Position> visited = new HashSet<>();
        
        queue.offer(position);
        visited.add(position);
        parent.put(position, null);
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            
            if (current.equals(target)) {
                return reconstructPath(current, parent);
            }
            
            Position[] neighbors = current.getAdjacentPositions();
            
            for (Position neighbor : neighbors) {
                if (neighbor.isValid(worldRows, worldCols) && 
                    !visited.contains(neighbor) &&
                    (allowCleanedPositions || !cleanedPositions.contains(neighbor))) {
                    
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return -1; // Caminho não encontrado
    }

    private int reconstructPath(Position target, Map<Position, Position> parent) {
        Position current = target;
        Position next = target;
        
        while (parent.get(current) != null && !parent.get(current).equals(position)) {
            next = current;
            current = parent.get(current);
        }
        
        // Determina direção do próximo movimento
        int dx = next.getX() - position.getX();
        int dy = next.getY() - position.getY();
        
        if (dx == -1 && dy == 0) return 0; // up
        if (dx == 1 && dy == 0) return 1;  // down
        if (dx == 0 && dy == -1) return 2; // left
        if (dx == 0 && dy == 1) return 3;  // right
        
        return -1;
    }

    private int exploreUnknownAreas() {
        Position[] adjacentPositions = position.getAdjacentPositions();
        
        // Prioriza áreas completamente desconhecidas
        for (int i = 0; i < adjacentPositions.length; i++) {
            Position pos = adjacentPositions[i];
            if (pos.isValid(worldRows, worldCols) && 
                !exploredPositions.contains(pos) && 
                !cleanedPositions.contains(pos)) {
                return i;
            }
        }
        
        // Se não há áreas desconhecidas, vai para áreas exploradas mas não limpas
        for (int i = 0; i < adjacentPositions.length; i++) {
            Position pos = adjacentPositions[i];
            if (pos.isValid(worldRows, worldCols) && 
                !cleanedPositions.contains(pos)) {
                return i;
            }
        }
        
        return -1;
    }

    private int findBacktrackPath() {
        // Procura por áreas não exploradas mais distantes usando backtracking
        Position nearestUnexplored = findNearestUnexploredArea();
        if (nearestUnexplored != null) {
            return findPathToTarget(nearestUnexplored, true); // Permite passar por áreas limpas
        }
        return -1;
    }

    private Position findNearestUnexploredArea() {
        Position nearest = null;
        int minDistance = Integer.MAX_VALUE;
        
        // Procura por posições válidas não exploradas
        for (int i = 0; i < worldRows; i++) {
            for (int j = 0; j < worldCols; j++) {
                Position pos = new Position(i, j);
                if (!exploredPositions.contains(pos) && !cleanedPositions.contains(pos)) {
                    int distance = position.manhattanDistance(pos);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = pos;
                    }
                }
            }
        }
        
        return nearest;
    }

    private void executeAction(int direction) {
        System.out.println(name + " escolheu ação: Move " + DIRECTION_NAMES[direction] + 
                          " (Estratégia: " + currentStrategy.getDescription() + ")");
        
        this.score += MOVEMENT_COST;
        this.steps++;
    }

    public void onCleanedDirt() {
        this.score += CLEANING_REWARD;
        cleanedPositions.add(position);
        knownDirtyPositions.remove(position);
        System.out.println("*** " + name + " ASPIROU SUJEIRA! ***");
    }

    // Getters e Setters
    public String getName() { return name; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public Set<Position> getCleanedPositions() { return cleanedPositions; }

    public void printStats() {
        System.out.println("=== " + name + " Stats ===");
        System.out.println("Posição: " + position);
        System.out.println("Passos: " + steps);
        System.out.println("Score: " + score);
        System.out.println("Estratégia: " + currentStrategy.getDescription());
        System.out.println("Sujeira conhecida: " + knownDirtyPositions.size());
        System.out.println("Áreas exploradas: " + exploredPositions.size());
        System.out.println("Áreas limpas: " + cleanedPositions.size());
        System.out.println("==========================");
    }

    public void printDetailedStatus() {
        System.out.println("\n📊 === STATUS DETALHADO DO " + name + " ===");
        System.out.println("🎯 Estratégia atual: " + currentStrategy.getDescription());
        System.out.println("📍 Posição: " + position);
        System.out.println("👣 Passos dados: " + steps);
        System.out.println("🏆 Score: " + score);
        System.out.println("🧹 Posições limpas: " + cleanedPositions.size());
        System.out.println("🗺️  Áreas exploradas: " + exploredPositions.size());
        System.out.println("💩 Sujeira conhecida: " + knownDirtyPositions.size());
        
        if (!knownDirtyPositions.isEmpty()) {
            System.out.println("📋 Sujeira pendente: " + knownDirtyPositions);
        }
        System.out.println("=======================================\n");
    }
}