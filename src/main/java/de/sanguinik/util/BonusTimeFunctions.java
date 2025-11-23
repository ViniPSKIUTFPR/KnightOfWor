package de.sanguinik.util;

public class BonusTimeFunctions {
    
    private String getTimeCategoryDescription(long timeInSeconds) {
		if (timeInSeconds <= 10) {
			return "(LEGENDARIO!)";
		} else if (timeInSeconds <= 15) {
			return "(PERFEITO!)";
		} else if (timeInSeconds <= 25) {
			return "(EXCELENTE!)";
		} else if (timeInSeconds <= 35) {
			return "(MUITO BOM!)";
		} else if (timeInSeconds <= 45) {
			return "(BOM!)";
		} else if (timeInSeconds <= 60) {
			return "(RAZOAVEL)";
		} else {
			return "(SEM BONUS)";
		}
	}

    public String formatTimeBonus(int bonus, long timeInSeconds) {
		if (bonus > 0) {
			String timeCategory = getTimeCategoryDescription(timeInSeconds);
			return "Bonus de Tempo: +" + bonus + " pontos! " + timeCategory;
		}
		return "Sem bonus de tempo (mais de 1 minuto)";
	}
}
