package com.apprikart.rotationmatrixdemo.filters

import com.apprikart.rotationmatrixdemo.Utils
import com.elvishew.xlog.XLog

class KalmanFilter(
    stateDimension: Int,
    measureDimension: Int,
    controlDimension: Int
) {
    /*these matrices should be provided by user*/
    var f: Matrix = Matrix(stateDimension, stateDimension)//state transition model
    var h: Matrix = Matrix(measureDimension, stateDimension) //observation model
    var b: Matrix = Matrix(stateDimension, controlDimension) //control matrix
    var q: Matrix = Matrix(stateDimension, stateDimension)//process noise covariance
    var r: Matrix =
        Matrix(measureDimension, measureDimension) //observation noise covariance

    /*these matrices will be updated by user*/
    var uk: Matrix = Matrix(controlDimension, 1)//control vector
    var zk: Matrix = Matrix(measureDimension, 1)//actual values (measured)
//    var zk: MatrixNew = MatrixNew(stateDimension, 1)//actual values (measured)
    var xkKm1: Matrix = Matrix(stateDimension, 1)//predicted state estimate
    private var pkKm1: Matrix =
        Matrix(stateDimension, stateDimension)//predicted estimate covariance
    private var yK: Matrix = Matrix(measureDimension, 1)//measurement innovation
//    private var yK: MatrixNew = MatrixNew(stateDimension, 1)//measurement innovation

    private var sk: Matrix = Matrix(measureDimension, measureDimension)//innovation covariance
    private var skInv: Matrix =
        Matrix(measureDimension, measureDimension)//innovation covariance inverse

    private var k: Matrix = Matrix(stateDimension, measureDimension)// Kalman gain (optimal)
    var xkK: Matrix = Matrix(stateDimension, 1) //updated (current) state
    var pkK: Matrix = Matrix(stateDimension, stateDimension)//updated estimate covariance
    private var ykK: Matrix = Matrix(measureDimension, 1) //post fit residual

    /*auxiliary matrices*/
    private var auxBxU: Matrix = Matrix(stateDimension, 1)
    private var auxSDxSD: Matrix = Matrix(stateDimension, stateDimension)
    private var auxSDxMD: Matrix = Matrix(stateDimension, measureDimension)

    fun predict() {
        //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
        Matrix.matrixMultiply(f, xkK, xkKm1)
        Matrix.matrixMultiply(b, uk, auxBxU)
        Matrix.matrixAdd(xkKm1, auxBxU, xkKm1)

        //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
        Matrix.matrixMultiply(f, pkK, auxSDxSD)
        Matrix.matrixMultiplyByTranspose(auxSDxSD, f, pkKm1)
        Matrix.matrixAdd(pkKm1, q, pkKm1)

//        XLog.i(" ${Utils.KALMAN_FILTER_PREDICTED_STATE} \n [ ${xkKm1.data[0][0]} \n ${xkKm1.data[1][0]} \n ${xkKm1.data[2][0]} \n ${xkKm1.data[3][0]} ]")

        val geoPoint = Coordinates.metersToGeoPoint(
            xkKm1.data[0][0],
            xkKm1.data[1][0]
        )

        XLog.i(" ${Utils.KALMAN_FILTER_PREDICTED_STATE_LOCATION} \n Latitude : ${geoPoint.latitude}, Longitude : ${geoPoint.longitude}")

        /*XLog.i(
            " ${Utils.KALMAN_FILTER_PREDICTED_ESTIMATE_COVARIANCE} \n [ " +
                    "${pkKm1.data[0][0]} ${pkKm1.data[0][1]} ${pkKm1.data[0][2]} ${pkKm1.data[0][3]} \n" +
                    "${pkKm1.data[1][0]} ${pkKm1.data[1][1]} ${pkKm1.data[1][2]} ${pkKm1.data[1][3]} \n" +
                    "${pkKm1.data[2][0]} ${pkKm1.data[2][1]} ${pkKm1.data[2][2]} ${pkKm1.data[2][3]} \n" +
                    "${pkKm1.data[3][0]} ${pkKm1.data[3][1]} ${pkKm1.data[3][2]} ${pkKm1.data[3][3]} ]"
        )*/

    }

    fun update() {
        //Yk = Zk - Hk*Xk|k-1
        Matrix.matrixMultiply(h, xkKm1, yK)
        Matrix.matrixSubtract(zk, yK, yK)

        //Sk = Rk + Hk*Pk|k-1*Hk(t)
        Matrix.matrixMultiplyByTranspose(pkKm1, h, auxSDxMD)
        Matrix.matrixMultiply(h, auxSDxMD, sk)
        Matrix.matrixAdd(r, sk, sk)

        //Kk = Pk|k-1*Hk(t)*Sk(inv)
        if (!(Matrix.matrixDestructiveInvert(sk, skInv)))
            return//matrix hasn't inversion
        Matrix.matrixMultiply(auxSDxMD, skInv, k)

        //xk|k = xk|k-1 + Kk*Yk
        Matrix.matrixMultiply(k, yK, xkK)
        Matrix.matrixAdd(xkKm1, xkK, xkK)

        //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
        Matrix.matrixMultiply(k, h, auxSDxSD)
        Matrix.matrixSubtractFromIdentity(auxSDxSD)
        Matrix.matrixMultiply(auxSDxSD, pkKm1, pkK)

//        XLog.i(" ${Utils.KALMAN_FILTER_UPDATED_STATE} \n [ ${xkK.data[0][0]} \n ${xkK.data[1][0]} \n ${xkK.data[2][0]} \n ${xkK.data[3][0]} ]")

        val geoPoint = Coordinates.metersToGeoPoint(
            xkK.data[0][0],
            xkK.data[1][0]
        )

//        XLog.i(" ${Utils.KALMAN_FILTER_UPDATED_STATE_LOCATION} \n Lat : ${geoPoint.Latitude} Long : ${geoPoint.Longitude}")

      /*  XLog.i(
            " ${Utils.KALMAN_FILTER_UPDATED_ESTIMATE_COVARIANCE} \n [ " +
                    "${pkK.data[0][0]} ${pkK.data[0][1]} ${pkK.data[0][2]} ${pkK.data[0][3]} \n" +
                    "${pkK.data[1][0]} ${pkK.data[1][1]} ${pkK.data[1][2]} ${pkK.data[1][3]} \n" +
                    "${pkK.data[2][0]} ${pkK.data[2][1]} ${pkK.data[2][2]} ${pkK.data[2][3]} \n" +
                    "${pkK.data[3][0]} ${pkK.data[3][1]} ${pkK.data[3][2]} ${pkK.data[3][3]} ]"
        )*/

        //we don't use this :
        //Yk|k = Zk - Hk*Xk|k
//        Matrix.matrixMultiply(H, Xk_k, Yk_k);
//        Matrix.matrixSubtract(Zk, Yk_k, Yk_k);
    }


}