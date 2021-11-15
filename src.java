static final long serialVersionUID = 8962277232270632278L;
public Solist() { super(); }
public Solist(Solist solist) { super(solist); }

class HamsterImpl {}

class RotationTracker {
    private int rotation = 1;

    public void rotate () {
        if (rotation > 0) {
            rotation--;
        } else {
            rotation = 3;
        }
    }

    public int getRotation () {
        return rotation;
    }
}

class Vector2d {
    int x;
    int y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d(Vector2d z) {
        this.x = z.x;
        this.y = z.y;
    }

    public Vector2d offset (int xd, int yd) {
        return new Vector2d(xd + x, yd + y);
    }

    public Vector2d diff (Vector2d other) {
        return new Vector2d(x - other.x, y - other.y);
    }
}

class HamsterTracker {
    private final Vector2d loc = new Vector2d(1, 1);
    private final RotationTracker rotation = new RotationTracker();

    public void processMovement () {
        switch (rotation.getRotation()) {
            case 0:
                loc.y--;
                break;
            case 1:
                loc.x++;
                break;
            case 2:
                loc.y++;
                break;
            case 3:
                loc.x--;
                break;
        }
    }

    public Vector2d getLoc() {
        return loc;
    }

    public RotationTracker getRotation() {
        return rotation;
    }
}

class Field {
    boolean wall = false;
    boolean traversed = false;
    boolean checked = false;
}

class PathTracker {
    private final java.util.ArrayList<java.util.ArrayList<Field>> array = new java.util.ArrayList<>();

    private void exist (Vector2d loc) {
        while (loc.x > array.size() - 1) {
            array.add(new java.util.ArrayList<>());
        }

        if (array.get(loc.x) == null) {
            array.set(loc.x, new java.util.ArrayList<>());
        }

        while (loc.y > array.get(loc.x).size() - 1) {
            array.get(loc.x).add(new Field());
        }

        if (array.get(loc.x).get(loc.y) == null) {
            array.get(loc.x).set(loc.y, new Field());
        }
    }

    public boolean shouldCheck (Vector2d loc) {
        exist(loc);
        return !array.get(loc.x).get(loc.y).checked;
    }

    public void check (Vector2d loc, boolean isWall) {
        exist(loc);
        array.get(loc.x).get(loc.y).checked = true;
        array.get(loc.x).get(loc.y).wall = isWall;
    }

    public void traverse (Vector2d loc) {
        exist(loc);
        array.get(loc.x).get(loc.y).traversed = true;
    }

    public boolean shouldTraverse (Vector2d loc) {
        exist(loc);
        return !array.get(loc.x).get(loc.y).wall && !array.get(loc.x).get(loc.y).traversed;
    }
}

public class Bot extends HamsterImpl {
    private final HamsterTracker tracker = new HamsterTracker();
    private final RotationTracker rotation = tracker.getRotation();
    private final PathTracker path = new PathTracker();
    private final java.util.Stack<Vector2d> stack = new java.util.Stack<>();
    private int cornTracker = 0;

    private void setRotation (int to) {
        while (rotation.getRotation() != to) {
            turnLeft();
            rotation.rotate();
        }
    }

    private void moveWrapper (boolean push) {
        path.traverse(tracker.getLoc());
        if (push) stack.push(new Vector2d(tracker.getLoc()));

        move();
        tracker.processMovement();
    }

    private boolean checkFieldFree (Vector2d loc, int rotation) {
        if (path.shouldCheck(loc)) {
            setRotation(rotation);
            path.check(loc, !frontIsClear());
        }

        return path.shouldTraverse(loc);
    }

    private int findIdeal() {
        boolean right = false;
        boolean up = false;
        boolean left = false;
        boolean down = false;
        int currentRotation = rotation.getRotation();

        if (currentRotation == 1) {
            right = checkFieldFree(tracker.getLoc().offset(1, 0), 1);
            up = checkFieldFree(tracker.getLoc().offset(0, -1), 0);
            left = checkFieldFree(tracker.getLoc().offset(-1, 0), 3);
            down = checkFieldFree(tracker.getLoc().offset(0, 1), 2);
        }
        if (currentRotation == 2) {
            down = checkFieldFree(tracker.getLoc().offset(0, 1), 2);
            right = checkFieldFree(tracker.getLoc().offset(1, 0), 1);
            up = checkFieldFree(tracker.getLoc().offset(0, -1), 0);
            left = checkFieldFree(tracker.getLoc().offset(-1, 0), 3);
        }
        if (currentRotation == 3) {
            left = checkFieldFree(tracker.getLoc().offset(-1, 0), 3);
            down = checkFieldFree(tracker.getLoc().offset(0, 1), 2);
            right = checkFieldFree(tracker.getLoc().offset(1, 0), 1);
            up = checkFieldFree(tracker.getLoc().offset(0, -1), 0);
        }
        if (currentRotation == 0) {
            up = checkFieldFree(tracker.getLoc().offset(0, -1), 0);
            left = checkFieldFree(tracker.getLoc().offset(-1, 0), 3);
            down = checkFieldFree(tracker.getLoc().offset(0, 1), 2);
            right = checkFieldFree(tracker.getLoc().offset(1, 0), 1);
        }

        if (up) return 0;
        if (right) return 1;
        if (down) return 2;
        if (left) return 3;

        return -1;
    }

    private void goBackOne () {
        Vector2d movementVector = stack.pop().diff(tracker.getLoc());

        if (movementVector.y == -1) setRotation(0);
        if (movementVector.x == 1) setRotation(1);
        if (movementVector.y == 1) setRotation(2);
        if (movementVector.x == -1) setRotation(3);

        moveWrapper(false);
    }

    void run () {
        while (true) {
            while (grainAvailable()) {
                pickGrain();
                cornTracker++;
            }

            int ideal = findIdeal();

            if (ideal == -1 && tracker.getLoc().y == 1 && tracker.getLoc().x == 1) break;
            if (ideal == -1) {
                goBackOne();
                continue;
            }

            setRotation(ideal);
            moveWrapper(true);
        }
    }
}

public void main () {
    Bot b = new Bot();
    b.run();
}
