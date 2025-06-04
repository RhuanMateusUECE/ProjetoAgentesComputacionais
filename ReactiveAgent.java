import java.util.Random;

/**
 * Agente Reativo: Toma decisões baseadas apenas no estado atual do ambiente,
 * sem memória de estados anteriores. Move-se de forma completamente aleatória.
 */
public class ReactiveAgent {
    private static final String[] DIRECTION_NAMES = {"up", "down", "left", "right"};
    private static final int MOVEMENT_COST = -1;
    private static final int CLEANING_REWARD = 2;
    
    private final String name;
    private final Random random;
    private Position position;
    private int steps;
    private int score;

    public ReactiveAgent(String name) {
        this.name = name;
        this.position = new Position(0, 0);
        this.steps = 0;
        this.score = 0;
        this.random = new Random();
    }

    public int chooseAction() {
        int action = random.nextInt(4); // 0=up, 1=down, 2=left, 3=right
        
        System.out.println(name + " escolheu ação: Move " + DIRECTION_NAMES[action] + " (Estratégia: Aleatória)");
        
        this.score += MOVEMENT_COST; // Custo do movimento
        this.steps++;
        
        return action;
    }

    public void onCleanedDirt() {
        this.score += CLEANING_REWARD;
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

    public void printStats() {
        System.out.println("=== " + name + " Stats ===");
        System.out.println("Posição: " + position);
        System.out.println("Passos: " + steps);
        System.out.println("Score: " + score);
        System.out.println("========================");
    }
}