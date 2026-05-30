import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class Eagle {
    static final double SIZE = 50;

    private static final double WORLD_WIDTH = 1920;
    private static final double WORLD_HEIGHT = 1080;
    private static final double CRUISE_Y = 200;
    private static final double PATROL_LEFT = 60;
    private static final double PATROL_RIGHT = 1810;
    private static final double ALERT_RADIUS = 400;
    private static final double ALERT_VERTICAL_DISTANCE = 400;
    private static final double DISENGAGE_VERTICAL_DISTANCE = 600;
    private static final double PATH_REBUILD_TIME = 0.2;
    private static final int CELL_SIZE = 50;
    private static final int GRID_COLS = 39;
    private static final int GRID_ROWS = 22;
    private static final int[] ANIMATION_SEQUENCE = {0, 1, 2, 3, 4, 4, 3, 2, 1, 0};

    enum EagleState {
        CRUISING,
        CHASING,
        RETURNING
    }

    double x;
    double y = CRUISE_Y;
    double width = SIZE;
    double height = SIZE;

    private final Image[] frames;
    private EagleState state = EagleState.CRUISING;
    private int direction = 1;
    private int animationIndex = 0;
    private double animationTimer = 0;
    private double pathTimer = 0;
    private boolean facingRight = true;
    private boolean warnRequested = false;
    private player target;
    private ArrayList<GridPoint> path = new ArrayList<>();
    private int pathIndex = 0;

    Eagle(double x, Image[] frames) {
        this.x = x;
        this.frames = frames;
    }

    void update(double dt, player[] players, level currentLevel) {
        updateAnimation(dt);

        if (state == EagleState.CRUISING) {
            updateCruise(dt, players);
        } else if (state == EagleState.CHASING) {
            updateChase(dt, players, currentLevel);
        } else {
            updateReturn(dt, players, currentLevel);
        }

        handlePlayerCollision(players);
    }

    Image getCurrentImage() {
        if (frames == null || frames.length == 0) {
            return null;
        }

        int frameIndex = ANIMATION_SEQUENCE[animationIndex];
        if (frameIndex >= frames.length) {
            frameIndex = frames.length - 1;
        }

        return frames[frameIndex];
    }

    boolean isFacingRight() {
        return facingRight;
    }

    boolean consumeWarnRequest() {
        if (!warnRequested) {
            return false;
        }

        warnRequested = false;
        return true;
    }

    boolean isHitBy(PlayerKnife knife) {
        return knife != null && CollisionManager.obbCollision(
                knife.x, knife.y, knife.width, knife.height, knife.getDirectionAngleDegrees(),
                x, y, width, height, 0
        );
    }

    private void updateAnimation(double dt) {
        animationTimer += dt;
        double frameTime = 2.0 / ANIMATION_SEQUENCE.length;

        while (animationTimer >= frameTime) {
            animationTimer -= frameTime;
            animationIndex = (animationIndex + 1) % ANIMATION_SEQUENCE.length;
        }
    }

    private void updateCruise(double dt, player[] players) {
        y = CRUISE_Y;
        double speed = cruiseSpeed(players);
        x += direction * speed * dt;
        facingRight = direction > 0;

        if (x <= PATROL_LEFT) {
            x = PATROL_LEFT;
            direction = 1;
        } else if (x + width >= PATROL_RIGHT) {
            x = PATROL_RIGHT - width;
            direction = -1;
        }

        player nearest = nearestActivePlayer(players);
        if (nearest != null && isNearCruiseLevel(nearest) && distanceTo(nearest) < ALERT_RADIUS) {
            enterChase(nearest);
        }
    }

    private void updateChase(double dt, player[] players, level currentLevel) {
        player nearest = nearestActivePlayer(players);

        if (nearest == null || isFarFromCruiseLevel(nearest)) {
            enterReturn();
            return;
        }

        target = nearest;
        pathTimer -= dt;

        if (pathTimer <= 0) {
            rebuildPath(currentLevel, target);
            pathTimer = PATH_REBUILD_TIME;
        }

        followPath(dt, target.speed * 1.5);
    }

    private void updateReturn(double dt, player[] players, level currentLevel) {
        double speed = cruiseSpeed(players);
        pathTimer -= dt;

        if (pathTimer <= 0) {
            rebuildReturnPath(currentLevel);
            pathTimer = PATH_REBUILD_TIME;
        }

        if (pathIndex < path.size()) {
            followPath(dt, speed);
        } else {
            moveToward(x, CRUISE_Y, speed, dt);
        }

        if (isAtCruiseLevel()) {
            finishReturn();
        }
    }

    private void enterChase(player newTarget) {
        state = EagleState.CHASING;
        target = newTarget;
        pathTimer = 0;
        warnRequested = true;
        path.clear();
        pathIndex = 0;
    }

    private void enterReturn() {
        state = EagleState.RETURNING;
        target = null;
        pathTimer = 0;
        path.clear();
        pathIndex = 0;
    }

    private void finishReturn() {
        state = EagleState.CRUISING;
        target = null;
        path.clear();
        pathIndex = 0;
        y = CRUISE_Y;
        direction = facingRight ? 1 : -1;
    }

    private double cruiseSpeed(player[] players) {
        double totalSpeed = 0;
        int activePlayers = 0;

        for (player p : players) {
            if (isActivePlayer(p)) {
                totalSpeed += p.speed;
                activePlayers++;
            }
        }

        if (activePlayers == 0) {
            return player.DEFAULT_SPEED;
        }

        return totalSpeed / activePlayers;
    }

    private player nearestActivePlayer(player[] players) {
        player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (player p : players) {
            if (!isActivePlayer(p)) {
                continue;
            }

            double distance = distanceTo(p);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }

    private boolean isActivePlayer(player p) {
        return p != null && !p.dead && !p.reachedGate && !p.trappedInFakeGate;
    }

    private boolean isNearCruiseLevel(player p) {
        return Math.abs(p.y - CRUISE_Y) < ALERT_VERTICAL_DISTANCE;
    }

    private boolean isFarFromCruiseLevel(player p) {
        return Math.abs(p.y - CRUISE_Y) > DISENGAGE_VERTICAL_DISTANCE;
    }

    private boolean isAtCruiseLevel() {
        return Math.abs(y - CRUISE_Y) <= 4;
    }

    private double distanceTo(player p) {
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double playerCenterX = p.x + p.width / 2.0;
        double playerCenterY = p.y + p.height / 2.0;
        double dx = playerCenterX - centerX;
        double dy = playerCenterY - centerY;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private void rebuildPath(level currentLevel, player targetPlayer) {
        if (targetPlayer == null) {
            return;
        }

        rebuildPathToPoint(
                currentLevel,
                targetPlayer.x + targetPlayer.width / 2.0,
                targetPlayer.y + targetPlayer.height / 2.0
        );
    }

    private void rebuildReturnPath(level currentLevel) {
        rebuildPathToPoint(currentLevel, x + width / 2.0, CRUISE_Y + height / 2.0);
    }

    private void rebuildPathToPoint(level currentLevel, double targetCenterX, double targetCenterY) {
        path.clear();
        pathIndex = 0;

        if (currentLevel == null) {
            return;
        }

        boolean[][] blocked = buildBlockedGrid(currentLevel);
        GridPoint start = cellFor(x + width / 2.0, y + height / 2.0);
        GridPoint goal = cellFor(targetCenterX, targetCenterY);
        blocked[start.row][start.col] = false;
        blocked[goal.row][goal.col] = false;

        ArrayList<GridPoint> foundPath = findPath(start, goal, blocked);
        if (foundPath != null && foundPath.size() > 1) {
            path = foundPath;
            pathIndex = 1;
        }
    }

    private boolean[][] buildBlockedGrid(level currentLevel) {
        boolean[][] blocked = new boolean[GRID_ROWS][GRID_COLS];

        for (Platform platform : currentLevel.getPlatforms()) {
            if (!platform.isSolid()) {
                continue;
            }

            for (Rectangle2D.Double bounds : platform.getCollisionBounds()) {
                markBlocked(blocked, bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        return blocked;
    }

    private void markBlocked(boolean[][] blocked, double x, double y, double width, double height) {
        int minCol = clamp((int)Math.floor(x / CELL_SIZE), 0, GRID_COLS - 1);
        int maxCol = clamp((int)Math.floor((x + width - 1) / CELL_SIZE), 0, GRID_COLS - 1);
        int minRow = clamp((int)Math.floor(y / CELL_SIZE), 0, GRID_ROWS - 1);
        int maxRow = clamp((int)Math.floor((y + height - 1) / CELL_SIZE), 0, GRID_ROWS - 1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                blocked[row][col] = true;
            }
        }
    }

    private ArrayList<GridPoint> findPath(GridPoint start, GridPoint goal, boolean[][] blocked) {
        Node[][] nodes = new Node[GRID_ROWS][GRID_COLS];
        PriorityQueue<Node> open = new PriorityQueue<>();

        Node startNode = node(nodes, start.col, start.row);
        startNode.g = 0;
        startNode.f = heuristic(start, goal);
        open.add(startNode);

        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.closed) {
                continue;
            }

            current.closed = true;

            if (current.col == goal.col && current.row == goal.row) {
                return buildPath(current);
            }

            for (int i = 0; i < dc.length; i++) {
                int nextCol = current.col + dc[i];
                int nextRow = current.row + dr[i];

                if (!isWalkable(nextCol, nextRow, blocked)) {
                    continue;
                }

                if (dc[i] != 0 && dr[i] != 0 &&
                        (!isWalkable(current.col + dc[i], current.row, blocked) ||
                                !isWalkable(current.col, current.row + dr[i], blocked))) {
                    continue;
                }

                Node next = node(nodes, nextCol, nextRow);
                double moveCost = (dc[i] == 0 || dr[i] == 0) ? 1.0 : 1.4142;
                double candidateG = current.g + moveCost;

                if (candidateG >= next.g) {
                    continue;
                }

                next.parent = current;
                next.g = candidateG;
                next.f = candidateG + heuristic(new GridPoint(nextCol, nextRow), goal);
                open.add(next);
            }
        }

        return null;
    }

    private boolean isWalkable(int col, int row, boolean[][] blocked) {
        return col >= 0 && col < GRID_COLS && row >= 0 && row < GRID_ROWS && !blocked[row][col];
    }

    private Node node(Node[][] nodes, int col, int row) {
        if (nodes[row][col] == null) {
            nodes[row][col] = new Node(col, row);
        }

        return nodes[row][col];
    }

    private double heuristic(GridPoint a, GridPoint b) {
        double dx = a.col - b.col;
        double dy = a.row - b.row;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private ArrayList<GridPoint> buildPath(Node endNode) {
        ArrayList<GridPoint> reversed = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            reversed.add(new GridPoint(current.col, current.row));
            current = current.parent;
        }

        Collections.reverse(reversed);
        return reversed;
    }

    private GridPoint cellFor(double centerX, double centerY) {
        int col = clamp((int)Math.floor(centerX / CELL_SIZE), 0, GRID_COLS - 1);
        int row = clamp((int)Math.floor(centerY / CELL_SIZE), 0, GRID_ROWS - 1);
        return new GridPoint(col, row);
    }

    private void followPath(double dt, double speed) {
        double targetX;
        double targetY;

        if (pathIndex < path.size()) {
            GridPoint waypoint = path.get(pathIndex);
            targetX = waypoint.col * CELL_SIZE + CELL_SIZE / 2.0 - width / 2.0;
            targetY = waypoint.row * CELL_SIZE + CELL_SIZE / 2.0 - height / 2.0;

            if (distanceToPoint(targetX, targetY) < 8) {
                pathIndex++;
            }
        }

        if (pathIndex < path.size()) {
            GridPoint waypoint = path.get(pathIndex);
            targetX = waypoint.col * CELL_SIZE + CELL_SIZE / 2.0 - width / 2.0;
            targetY = waypoint.row * CELL_SIZE + CELL_SIZE / 2.0 - height / 2.0;
        } else if (target != null) {
            targetX = target.x;
            targetY = target.y;
        } else {
            return;
        }

        moveToward(targetX, targetY, speed, dt);
    }

    private double distanceToPoint(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void moveToward(double targetX, double targetY, double speed, double dt) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.001) {
            return;
        }

        double step = Math.min(distance, speed * dt);
        double moveX = dx / distance * step;
        double moveY = dy / distance * step;
        x = clampDouble(x + moveX, 0, WORLD_WIDTH - width);
        y = clampDouble(y + moveY, 0, WORLD_HEIGHT - height);

        if (Math.abs(moveX) > 0.001) {
            facingRight = moveX > 0;
        }
    }

    private void handlePlayerCollision(player[] players) {
        for (player p : players) {
            if (!isActivePlayer(p)) {
                continue;
            }

            if (CollisionManager.rectCollision(x, y, width, height, p.x, p.y, p.width, p.height)) {
                p.die();
            }
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class GridPoint {
        int col;
        int row;

        GridPoint(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    private static class Node implements Comparable<Node> {
        int col;
        int row;
        double g = Double.MAX_VALUE;
        double f = Double.MAX_VALUE;
        boolean closed = false;
        Node parent;

        Node(int col, int row) {
            this.col = col;
            this.row = row;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(f, other.f);
        }
    }
}
