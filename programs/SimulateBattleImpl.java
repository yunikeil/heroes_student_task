package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulateBattleImpl implements SimulateBattle {

    private final PrintBattleLog printBattleLog;

    public SimulateBattleImpl() {
        this.printBattleLog = (attacker, target) -> {};
    }

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        if (playerArmy == null || computerArmy == null) {
            return;
        }

        while (hasAlive(playerArmy) && hasAlive(computerArmy)) {

            List<Unit> turnOrder = buildTurnOrder(playerArmy, computerArmy);
            if (turnOrder.isEmpty()) {
                break;
            }

            for (Unit attacker : turnOrder) {
                if (!attacker.isAlive()) {
                    continue;
                }

                if (!hasAlive(playerArmy) || !hasAlive(computerArmy)) {
                    return;
                }

                Unit target = attacker.getProgram().attack();
                if (target != null) {
                    printBattleLog.printBattleLog(attacker, target);
                }

            }
        }
    }

    private boolean hasAlive(Army army) {
        List<Unit> units = army.getUnits();
        if (units == null || units.isEmpty()) {
            return false;
        }
        for (Unit u : units) {
            if (u != null && u.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private List<Unit> buildTurnOrder(Army playerArmy, Army computerArmy) {
        List<Unit> order = new ArrayList<>();

        addAlive(order, playerArmy);
        addAlive(order, computerArmy);

        order.sort(
                Comparator.<Unit>comparingInt(Unit::getBaseAttack).reversed()
                        .thenComparingInt(Unit::getHealth).reversed()
                        .thenComparing(Unit::getName, Comparator.nullsLast(String::compareTo))
        );

        return order;
    }

    private void addAlive(List<Unit> out, Army army) {
        List<Unit> units = army.getUnits();
        if (units == null) {
            return;
        }
        for (Unit u : units) {
            if (u != null && u.isAlive()) {
                out.add(u);
            }
        }
    }
}
