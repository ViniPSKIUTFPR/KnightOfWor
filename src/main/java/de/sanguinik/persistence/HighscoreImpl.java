package de.sanguinik.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.sanguinik.model.HighscoreModel;

public class HighscoreImpl implements Highscore {

	private static final String DATABASE = "./src/main/resources/de/sanguinik/database/highscoreDB.json";
	private static final Gson gson = new Gson();

	@Override
	public void saveHighscore(HighscoreModel score) {
		List<HighscoreModel> currentScores = loadHighscore();

		List<HighscoreDTO> scores = new ArrayList<>(); 
		if (!currentScores.isEmpty()) {
			scores = currentScores.stream()
					.map(scoreDTO -> new HighscoreDTO(scoreDTO.getName(), scoreDTO.getScore(), scoreDTO.getDate()))
					.collect(Collectors.toList());
		}
		scores.add(new HighscoreDTO(score.getName(), score.getScore(), score.getDate()));

        try (Writer writer = new FileWriter(DATABASE)) {
            gson.toJson(scores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Override
	public List<HighscoreModel> loadHighscore() {
		File file = new File(DATABASE);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<HighscoreDTO>>(){}.getType();
            List<HighscoreDTO> scores = gson.fromJson(reader, listType);
            return scores.stream()
                    .map(score -> new HighscoreModel(score.getName(), score.getScore(), score.getDate()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
	}
}
