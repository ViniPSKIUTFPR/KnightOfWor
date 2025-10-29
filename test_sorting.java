import java.util.List;
import java.util.stream.Collectors;

public class test_sorting {
    public static void main(String[] args) {
        // Simula a ordenação que está implementada no HighscoreImpl
        List<Integer> scores = List.of(3300, 900, 900, 900, 100, 0, 2900, 900, 900, 900, 2850, 2375);
        
        System.out.println("Pontuações originais:");
        scores.forEach(System.out::println);
        
        System.out.println("\nPontuações ordenadas (maior para menor):");
        List<Integer> sortedScores = scores.stream()
            .sorted((a, b) -> Integer.compare(b, a))  // Mesma lógica do HighscoreImpl
            .collect(Collectors.toList());
            
        sortedScores.forEach(System.out::println);
    }
}
