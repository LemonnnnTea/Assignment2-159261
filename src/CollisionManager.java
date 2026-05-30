public class CollisionManager {

    public static boolean rectCollision(
            double x1, double y1, double w1, double h1,
            double x2, double y2, double w2, double h2) {

        return obbCollision(x1, y1, w1, h1, 0, x2, y2, w2, h2, 0);
    }

    public static boolean obbCollision(
            double x1, double y1, double w1, double h1, double angle1Degrees,
            double x2, double y2, double w2, double h2, double angle2Degrees) {

        if (w1 <= 0 || h1 <= 0 || w2 <= 0 || h2 <= 0) {
            return false;
        }

        OrientedBox box1 = new OrientedBox(x1, y1, w1, h1, angle1Degrees);
        OrientedBox box2 = new OrientedBox(x2, y2, w2, h2, angle2Degrees);

        return overlapsOnAxis(box1, box2, box1.axisXx, box1.axisXy) &&
                overlapsOnAxis(box1, box2, box1.axisYx, box1.axisYy) &&
                overlapsOnAxis(box1, box2, box2.axisXx, box2.axisXy) &&
                overlapsOnAxis(box1, box2, box2.axisYx, box2.axisYy);
    }

    public static double angleFromVelocity(double velocityX, double velocityY) {
        if (Math.abs(velocityX) < 0.001 && Math.abs(velocityY) < 0.001) {
            return 0;
        }

        return Math.toDegrees(Math.atan2(velocityY, velocityX));
    }

    private static boolean overlapsOnAxis(OrientedBox box1, OrientedBox box2, double axisX, double axisY) {
        double centerDx = box2.centerX - box1.centerX;
        double centerDy = box2.centerY - box1.centerY;
        double centerDistance = Math.abs(centerDx * axisX + centerDy * axisY);
        double radius1 = box1.projectedRadius(axisX, axisY);
        double radius2 = box2.projectedRadius(axisX, axisY);

        return centerDistance <= radius1 + radius2;
    }

    private static class OrientedBox {
        double centerX;
        double centerY;
        double halfWidth;
        double halfHeight;
        double axisXx;
        double axisXy;
        double axisYx;
        double axisYy;

        OrientedBox(double x, double y, double width, double height, double angleDegrees) {
            double radians = Math.toRadians(angleDegrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);

            centerX = x + width / 2.0;
            centerY = y + height / 2.0;
            halfWidth = width / 2.0;
            halfHeight = height / 2.0;
            axisXx = cos;
            axisXy = sin;
            axisYx = -sin;
            axisYy = cos;
        }

        double projectedRadius(double axisX, double axisY) {
            double xProjection = Math.abs(axisXx * axisX + axisXy * axisY) * halfWidth;
            double yProjection = Math.abs(axisYx * axisX + axisYy * axisY) * halfHeight;

            return xProjection + yProjection;
        }
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
