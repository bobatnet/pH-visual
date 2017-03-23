package com.csir.csio.instapH;

import android.graphics.Color;
import android.util.Log;

/**
 * Created by bob on 22/12/15.
 */

public class MatrixMath {
    static public double[][] Multiply(double[][] a, double[][] b){
        double[][] c;

        if (a[0].length != b.length) {
            Log.d("MatrixMath","Matrix dimensions are improper !");
            return new double[0][0];
        }

        c = new double[a.length][b[0].length];

        for (int i = 0; i < a.length; i++ ){
            for (int j = 0; j < b[0].length; j++){
                double s = 0;
                for (int k = 0; k < a[0].length; k++) s += a[i][k] * b[k][j];
                c[i][j] = s;
            }
        }
        return c;
    }

    static public double[][] LinearPerceptron(double[][] a, double[][] weights) {
        return Multiply(a, weights);
    }

    static public double[][] TanhPerceptron(double[][] a, double[][] weights){
        double[][] c = Multiply(a,weights);

        for (int i = 0; i < c.length; i++)
            for (int j = 0; j < c[i].length; j++) c[i][j] = Math.tanh(c[i][j]);

        return c;
    }

    static public double[][] addBias(double[][] in){
        double[][] out = new double[1][in[0].length + 1];

        System.arraycopy(out[0],0,in[0],0,in[0].length);
        out[0][in[0].length] = 1.0;
        return out;
    }

    static double predict(int a){
        double[][] invect = {{Color.red(a)/236.0, (Color.green(a) - 57.0)/121.0, Color.blue(a)/176.0, 1.0} }; //1 x 4

        double[][] weights1 =  {{-8.588197 ,-45.141997 ,181.588069 ,93.872106 ,-1.208899},
                                {7.179990 ,54.250813 ,-25.241350 ,-76.750799 ,-0.470486 },
                                {-4.964430 ,-31.228256 ,-62.605892 ,56.779155 ,1.691076 },
                                {6.57052815e+00,   1.33971029e+01,  -1.35644269e+02, -3.99249381e+01,  -6.79540366e-02}}; // 4 x 5

        double[][] outvect1 = MatrixMath.TanhPerceptron(invect,weights1); // 1 x 5

        double[][] invect2 = addBias(outvect1); // 1 x 6

        double[][] weights2 = {{179.550921 ,-0.189907},{-56.390912 ,-1.344259}, {58.937352 ,228.728561},{-7.382017 ,19.132025},{1.242348 ,0.354161 }, {-71.53383143,  207.23495576}}; // 6 x 2

        double[][] outvect2 = MatrixMath.TanhPerceptron(invect2,weights2); // 1 x 2

        double[][] invect3 = addBias(outvect2); // 1 x 3

        double[][] weights3 = {{4.497745},{-11.789449},{-2.28645186}}; // 3 x 1

        return MatrixMath.LinearPerceptron(invect3,weights3)[0][0]; // 1 x 1
    }
}