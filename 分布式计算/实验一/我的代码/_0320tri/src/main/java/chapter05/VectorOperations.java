package chapter05;

public class VectorOperations {
    public static double dotProduct(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Vector sizes differ.");
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static double[] crossProduct(double[] a, double[] b) {
        if (a.length != 3 || b.length != 3) throw new IllegalArgumentException("Vectors must be of length 3.");
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }
}

