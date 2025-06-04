import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Gerador de ambientes de teste para avaliar diferentes cenários
 * de performance dos agentes aspirador.
 */
public class TestEnvironments {
    
    /**
     * Gera um ambiente pequeno para testes rápidos
     */
    public static void generateSmallTest() {
        String content = "[ ][D][ ]\n[D][ ][D]\n[ ][D][ ]";
        saveEnvironment("enviroments/test_small.txt", content);
        System.out.println("✅ Ambiente pequeno criado: test_small.txt");
    }
    
    /**
     * Gera um ambiente em corredor (teste de eficiência)
     */
    public static void generateCorridorTest() {
        String content = "[D][D][D][D][D][D][D][D][D][D]";
        saveEnvironment("enviroments/test_corridor.txt", content);
        System.out.println("✅ Ambiente corredor criado: test_corridor.txt");
    }
    
    /**
     * Gera um ambiente labirinto simples
     */
    public static void generateMazeTest() {
        String content = 
            "[D][ ][D][ ][D]\n" +
            "[ ][ ][ ][ ][ ]\n" +
            "[D][ ][D][ ][D]\n" +
            "[ ][ ][ ][ ][ ]\n" +
            "[D][ ][D][ ][D]";
        saveEnvironment("enviroments/test_maze.txt", content);
        System.out.println("✅ Ambiente labirinto criado: test_maze.txt");
    }
    
    /**
     * Gera um ambiente com sujeira concentrada em um canto
     */
    public static void generateCornerTest() {
        String content = 
            "[D][D][D][ ][ ]\n" +
            "[D][D][D][ ][ ]\n" +
            "[D][D][D][ ][ ]\n" +
            "[ ][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][ ]";
        saveEnvironment("enviroments/test_corner.txt", content);
        System.out.println("✅ Ambiente canto criado: test_corner.txt");
    }
    
    /**
     * Gera um ambiente esparso (poucas sujeiras espalhadas)
     */
    public static void generateSparseTest() {
        String content = 
            "[ ][ ][ ][ ][ ][ ][D]\n" +
            "[ ][ ][ ][ ][ ][ ][ ]\n" +
            "[ ][ ][D][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][D][ ][ ]\n" +
            "[ ][ ][ ][ ][ ][ ][ ]\n" +
            "[D][ ][ ][ ][ ][ ][ ]";
        saveEnvironment("enviroments/test_sparse.txt", content);
        System.out.println("✅ Ambiente esparso criado: test_sparse.txt");
    }
    
    /**
     * Gera um ambiente grande e complexo
     */
    public static void generateLargeTest() {
        StringBuilder content = new StringBuilder();
        Random random = new Random(42); // Seed fixo para reprodutibilidade
        
        int rows = 10;
        int cols = 10;
        double dirtProbability = 0.3;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (random.nextDouble() < dirtProbability) {
                    content.append("[D]");
                } else {
                    content.append("[ ]");
                }
            }
            if (i < rows - 1) {
                content.append("\n");
            }
        }
        
        saveEnvironment("enviroments/test_large.txt", content.toString());
        System.out.println("✅ Ambiente grande criado: test_large.txt");
    }
    
    /**
     * Gera um ambiente vazio (caso extremo)
     */
    public static void generateEmptyTest() {
        String content = 
            "[ ][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][ ]\n" +
            "[ ][ ][ ][ ][ ]";
        saveEnvironment("enviroments/test_empty.txt", content);
        System.out.println("✅ Ambiente vazio criado: test_empty.txt");
    }
    
    /**
     * Gera um ambiente completamente sujo
     */
    public static void generateFullDirtyTest() {
        String content = 
            "[D][D][D][D][D]\n" +
            "[D][D][D][D][D]\n" +
            "[D][D][D][D][D]\n" +
            "[D][D][D][D][D]\n" +
            "[D][D][D][D][D]";
        saveEnvironment("enviroments/test_full_dirty.txt", content);
        System.out.println("✅ Ambiente completamente sujo criado: test_full_dirty.txt");
    }
    
    /**
     * Gera ambiente em formato de cruz
     */
    public static void generateCrossTest() {
        String content = 
            "[ ][ ][D][ ][ ]\n" +
            "[ ][ ][D][ ][ ]\n" +
            "[D][D][D][D][D]\n" +
            "[ ][ ][D][ ][ ]\n" +
            "[ ][ ][D][ ][ ]";
        saveEnvironment("enviroments/test_cross.txt", content);
        System.out.println("✅ Ambiente cruz criado: test_cross.txt");
    }
    
    private static void saveEnvironment(String filename, String content) {
        try {
            // Cria diretório se não existir
            java.io.File file = new java.io.File(filename);
            java.io.File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("📁 Criando diretório: " + parentDir.getPath());
            }
            
            // Salva o arquivo
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(content);
            }
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar " + filename + ": " + e.getMessage());
        }
    }
    
    /**
     * Executa testes automatizados em todos os ambientes
     */
    public static void runAllTests() {
        System.out.println("🧪 === EXECUTANDO TESTES AUTOMATIZADOS ===\n");
        
        String[] environments = {
            "environments/test_small.txt",
            "environments/test_corridor.txt", 
            "environments/test_maze.txt",
            "environments/test_corner.txt",
            "environments/test_sparse.txt",
            "environments/test_cross.txt"
        };
        
        Environment env = new Environment();
        
        for (String envPath : environments) {
            System.out.println("🗺️  Testando: " + envPath);
            try {
                env.compareAgents(envPath);
                System.out.println("\n" + "=".repeat(60) + "\n");
            } catch (Exception e) {
                System.out.println("❌ Erro ao testar " + envPath + ": " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🏭 === GERADOR DE AMBIENTES DE TESTE ===");
        
        if (args.length > 0) {
            switch (args[0]) {
                case "generate":
                    System.out.println("Gerando todos os ambientes de teste...");
                    generateSmallTest();
                    generateCorridorTest();
                    generateMazeTest();
                    generateCornerTest();
                    generateSparseTest();
                    generateLargeTest();
                    generateEmptyTest();
                    generateFullDirtyTest();
                    generateCrossTest();
                    System.out.println("\n🎉 Todos os ambientes foram gerados!");
                    break;
                    
                case "test":
                    System.out.println("Executando testes automatizados...");
                    runAllTests();
                    break;
                    
                default:
                    System.out.println("Uso: java TestEnvironments [generate|test]");
            }
        } else {
            // Gera apenas alguns ambientes básicos
            generateSmallTest();
            generateCorridorTest();
            generateMazeTest();
            generateCornerTest();
            
            System.out.println("\n💡 Use 'java TestEnvironments generate' para gerar todos os ambientes");
            System.out.println("💡 Use 'java TestEnvironments test' para executar testes automatizados");
        }
    }
}