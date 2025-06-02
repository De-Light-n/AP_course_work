// DerivativeManager.java
package proj.Service;

import proj.Models.Derivative;
import proj.Models.insurance.InsuranceObligation;
import proj.Models.Risk;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервіс для управління бізнес-логікою фільтрації та сортування деривативів.
 */
public class DerivativeService {
    private static final DerivativeService INSTANCE = new DerivativeService();

    private DerivativeService() {
    }

    /**
     * Повертає єдиний екземпляр сервісу.
     *
     * @return екземпляр DerivativeService
     */
    public static DerivativeService getInstance() {
        return INSTANCE;
    }

    /**
     * Сортує зобов'язання деривативу за рівнем ризику (від більшого до меншого).
     *
     * @param derivative дериватив для сортування зобов'язань
     */
    public void sortByRiskLevel(Derivative derivative) {
        List<InsuranceObligation> sorted = new ArrayList<>(derivative.getObligations());
        sorted.sort((o1, o2) -> Double.compare(o2.getRiskLevel(), o1.getRiskLevel()));
        derivative.setObligations(sorted);
    }

    /**
     * Повертає список зобов'язань, що відповідають заданим діапазонам ризику та
     * суми.
     *
     * @param derivative дериватив
     * @param minRisk    мінімальний рівень ризику
     * @param maxRisk    максимальний рівень ризику
     * @param minAmount  мінімальна сума
     * @param maxAmount  максимальна сума
     * @return список зобов'язань у заданих межах
     */
    public List<InsuranceObligation> findObligationsInRange(Derivative derivative, double minRisk, double maxRisk,
            double minAmount, double maxAmount) {
        return derivative.getObligations().stream()
                .filter(o -> o.getRiskLevel() >= minRisk && o.getRiskLevel() <= maxRisk)
                .filter(o -> o.getAmount() >= minAmount && o.getAmount() <= maxAmount)
                .collect(Collectors.toList());
    }

    /**
     * Повертає кількість ризиків за категоріями серед усіх зобов'язань деривативу.
     *
     * @param derivative дериватив
     * @return мапа категорія ризику → кількість
     */
    public Map<Risk.RiskCategory, Long> countRisksByCategory(Derivative derivative) {
        return derivative.getObligations().stream()
                .flatMap(o -> o.getCoveredRisks().stream())
                .collect(Collectors.groupingBy(
                        Risk::getCategory,
                        Collectors.counting()));
    }

    /**
     * Обчислює середній рівень ризику серед усіх зобов'язань деривативу.
     *
     * @param derivative дериватив
     * @return середній рівень ризику або 0.0, якщо зобов'язань немає
     */
    public double calculateAverageRisk(Derivative derivative) {
        return derivative.getObligations().stream()
                .mapToDouble(InsuranceObligation::getRiskLevel)
                .average()
                .orElse(0.0);
    }

    /**
     * Повертає кількість зобов'язань за типом.
     *
     * @param derivative дериватив
     * @return мапа тип зобов'язання → кількість
     */
    public Map<String, Long> countObligationsByType(Derivative derivative) {
        return derivative.getObligations().stream()
                .collect(Collectors.groupingBy(
                        o -> o.getClass().getSimpleName(),
                        Collectors.counting()));
    }

    /**
     * Повертає список активних зобов'язань деривативу.
     *
     * @param derivative дериватив
     * @return список активних зобов'язань
     */
    public List<InsuranceObligation> getActiveObligations(Derivative derivative) {
        return derivative.getObligations().stream()
                .filter(InsuranceObligation::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Фільтрує список деривативів за назвою та діапазоном вартості.
     *
     * @param derivatives список деривативів для фільтрації
     * @param nameSearch  текст для пошуку в назві
     * @param minValueStr мінімальна вартість (рядок)
     * @param maxValueStr максимальна вартість (рядок)
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
     * Сортує список деривативів згідно з вибраним критерієм.
     *
     * @param derivatives  список деривативів для сортування
     * @param selectedSort критерій сортування
     */
    public void sortDerivatives(List<Derivative> derivatives, String selectedSort) {
        if (selectedSort == null)
            return;

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