public class CollisionManager {

    public static boolean rectCollision(
            double x1, double y1, double w1, double h1,
            double x2, double y2, double w2, double h2) {

        return x1 < x2 + w2 &&
                x1 + w1 > x2 &&
                y1 < y2 + h2 &&
                y1 + h1 > y2;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}