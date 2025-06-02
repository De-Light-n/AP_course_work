// DerivativeManager.java
package proj.Models;

import java.util.*;

/**
 * Клас для управління бізнес-логікою фільтрації та сортування деривативів
 */
public class DerivativeManager {

    /**
     * Фільтрує список деривативів за назвою та діапазоном вартості
     * @param derivatives список деривативів для фільтрації
     * @param nameSearch текст для пошуку в назві
     * @param minValueStr мінімальна вартість (як строка)
     * @param maxValueStr максимальна вартість (як строка)
     * @return відфільтрований список деривативів
     */
    public List<Derivative> filterDerivatives(List<Derivative> derivatives, String nameSearch, 
                                           String minValueStr, String maxValueStr) {
        List<Derivative> filtered = new ArrayList<>(derivatives);

        if (!nameSearch.isEmpty()) {
            filtered.removeIf(d -> !d.getName().toLowerCase().contains(nameSearch.toLowerCase()));
        }

        try {
            double minValue = minValueStr.isEmpty() ? 0 : Double.parseDouble(minValueStr);
            double maxValue = maxValueStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxValueStr);

            filtered.removeIf(d -> d.getTotalValue() < minValue || d.getTotalValue() > maxValue);
        } catch (NumberFormatException e) {
            // Ігноруємо некоректні числа
        }

        return filtered;
    }

    /**
     * Сортує список деривативів згідно з вибраним критерієм
     * @param derivatives список деривативів для сортування
     * @param selectedSort критерій сортування
     */
    public void sortDerivatives(List<Derivative> derivatives, String selectedSort) {
        if (selectedSort == null) return;

        switch (selectedSort) {
            case "Назвою (А-Я)":
                derivatives.sort(Comparator.comparing(Derivative::getName));
                break;
            case "Назвою (Я-А)":
                derivatives.sort(Comparator.comparing(Derivative::getName).reversed());
                break;
            case "Вартістю (зростання)":
                derivatives.sort(Comparator.comparingDouble(Derivative::getTotalValue));
                break;
            case "Вартістю (спадання)":
                derivatives.sort(Comparator.comparingDouble(Derivative::getTotalValue).reversed());
                break;
            case "Датою (новіші)":
                derivatives.sort(Comparator.comparing(Derivative::getCreatedAt).reversed());
                break;
            case "Датою (старіші)":
                derivatives.sort(Comparator.comparing(Derivative::getCreatedAt));
                break;
        }
    }
}