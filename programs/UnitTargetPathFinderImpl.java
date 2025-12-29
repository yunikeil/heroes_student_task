package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;

    private static final int[] DX = {-1, 1, 0, 0, -1, -1, 1, 1};
    private static final int[] DY = {0, 0, -1, 1, -1, 1, -1, 1};

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        if (attackUnit == null || targetUnit == null || existingUnitList == null) {
            return Collections.emptyList();
        }
        if (!targetUnit.isAlive() || !attackUnit.isAlive()) {
            return Collections.emptyList();
        }

        int sx = attackUnit.getxCoordinate();
        int sy = attackUnit.getyCoordinate();
        int tx = targetUnit.getxCoordinate();
        int ty = targetUnit.getyCoordinate();

        if (!inBounds(sx, sy) || !inBounds(tx, ty)) {
            return Collections.emptyList();
        }

        if (sx == tx && sy == ty) {
            List<Edge> single = new ArrayList<>(1);
            single.add(new Edge(sx, sy));
            return single;
        }

        boolean[][] blocked = new boolean[WIDTH][HEIGHT];
        for (Unit u : existingUnitList) {
            if (u == null || !u.isAlive()) {
                continue;
            }
            if (u == attackUnit || u == targetUnit) {
                continue;
            }
            int x = u.getxCoordinate();
            int y = u.getyCoordinate();
            if (inBounds(x, y)) {
                blocked[x][y] = true;
            }
        }

        if (blocked[sx][sy] || blocked[tx][ty]) {
            return Collections.emptyList();
        }

        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        int[][] parentX = new int[WIDTH][HEIGHT];
        int[][] parentY = new int[WIDTH][HEIGHT];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                parentX[x][y] = -1;
                parentY[x][y] = -1;
            }
        }

        Deque<int[]> q = new ArrayDeque<>();
        q.addLast(new int[]{sx, sy});
        visited[sx][sy] = true;

        boolean found = false;

        while (!q.isEmpty()) {
            int[] cur = q.removeFirst();
            int cx = cur[0];
            int cy = cur[1];

            if (cx == tx && cy == ty) {
                found = true;
                break;
            }

            for (int i = 0; i < 8; i++) {
                int nx = cx + DX[i];
                int ny = cy + DY[i];

                if (!inBounds(nx, ny) || visited[nx][ny] || blocked[nx][ny]) {
                    continue;
                }

                if (isDiagonalMove(cx, cy, nx, ny)) {
                    int ox1x = cx;
                    int ox1y = ny;
                    int ox2x = nx;
                    int ox2y = cy;
                    if (inBounds(ox1x, ox1y) && inBounds(ox2x, ox2y)) {
                        if (blocked[ox1x][ox1y] && blocked[ox2x][ox2y]) {
                            continue;
                        }
                    }
                }

                visited[nx][ny] = true;
                parentX[nx][ny] = cx;
                parentY[nx][ny] = cy;
                q.addLast(new int[]{nx, ny});
            }
        }

        if (!found) {
            return Collections.emptyList();
        }

        return reconstructPath(parentX, parentY, sx, sy, tx, ty);
    }

    private List<Edge> reconstructPath(int[][] parentX, int[][] parentY, int sx, int sy, int tx, int ty) {
        List<Edge> path = new ArrayList<>();
        int x = tx;
        int y = ty;

        while (!(x == sx && y == sy)) {
            path.add(new Edge(x, y));
            int px = parentX[x][y];
            int py = parentY[x][y];
            if (px == -1 && py == -1) {
                return Collections.emptyList();
            }
            x = px;
            y = py;
        }
        path.add(new Edge(sx, sy));

        Collections.reverse(path);
        return path;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    private boolean isDiagonalMove(int x1, int y1, int x2, int y2) {
        return x1 != x2 && y1 != y2;
    }
}
