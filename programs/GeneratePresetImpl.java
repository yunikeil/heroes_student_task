package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {

    private static final int ARMY_WIDTH = 3;
    private static final int ARMY_HEIGHT = 21;
    private static final int MAX_UNITS_PER_TYPE = 11;

    private static final double ATTACK_WEIGHT = 0.7;
    private static final double HEALTH_WEIGHT = 0.3;

    private final Random random = new Random();

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        Army army = new Army();
        if (unitList == null || unitList.isEmpty() || maxPoints <= 0) {
            army.setUnits(Collections.emptyList());
            return army;
        }

        List<Unit> templates = new ArrayList<>(unitList);
        templates.sort(Comparator.comparingDouble(this::score).reversed());

        int remaining = maxPoints;

        Map<String, Integer> perTypeCount = new HashMap<>();

        boolean[][] occupied = new boolean[ARMY_WIDTH][ARMY_HEIGHT];
        int freeCells = ARMY_WIDTH * ARMY_HEIGHT;

        List<Unit> result = new ArrayList<>();

        int index = 1;
        while (remaining > 0 && freeCells > 0) {
            Unit chosen = selectTemplate(templates, remaining, perTypeCount);
            if (chosen == null) {
                break;
            }

            int[] xy = takeRandomFreeCell(occupied, freeCells);
            int x = xy[0];
            int y = xy[1];
            freeCells--;

            String unitName = buildUnitName(chosen, index++);
            Unit newUnit = cloneWithPosition(unitName, chosen, x, y);

            result.add(newUnit);
            remaining -= chosen.getCost();

            perTypeCount.merge(chosen.getUnitType(), 1, Integer::sum);
        }

        army.setUnits(result);
        return army;
    }

    private double score(Unit u) {
        int cost = Math.max(1, u.getCost());
        double attack = u.getBaseAttack() / (double) cost;
        double health = u.getHealth() / (double) cost;
        return ATTACK_WEIGHT * attack + HEALTH_WEIGHT * health;
    }

    private Unit selectTemplate(List<Unit> templates, int remainingPoints, Map<String, Integer> perTypeCount) {
        final int TOP_K = 3;

        Unit[] candidates = new Unit[TOP_K];
        int found = 0;

        for (Unit t : templates) {
            if (t.getCost() > remainingPoints) {
                continue;
            }
            int count = perTypeCount.getOrDefault(t.getUnitType(), 0);
            if (count >= MAX_UNITS_PER_TYPE) {
                continue;
            }
            candidates[found++] = t;
            if (found == TOP_K) {
                break;
            }
        }

        if (found == 0) {
            return null;
        }
        return candidates[random.nextInt(found)];
    }

    private int[] takeRandomFreeCell(boolean[][] occupied, int freeCells) {
        while (true) {
            int x = random.nextInt(ARMY_WIDTH);
            int y = random.nextInt(ARMY_HEIGHT);
            if (!occupied[x][y]) {
                occupied[x][y] = true;
                return new int[]{x, y};
            }
        }
    }

    private String buildUnitName(Unit template, int index) {
        return template.getName() + " #" + index;
    }

    private Unit cloneWithPosition(String name, Unit template, int x, int y) {
        return new Unit(
                name,
                template.getUnitType(),
                template.getHealth(),
                template.getBaseAttack(),
                template.getCost(),
                template.getAttackType(),
                template.getAttackBonuses(),
                template.getDefenceBonuses(),
                x,
                y
        );
    }
}
