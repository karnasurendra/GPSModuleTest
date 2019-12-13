package com.apprikart.rotationmatrixdemo.filters

import com.apprikart.rotationmatrixdemo.Utils
import com.elvishew.xlog.XLog

class KalmanFilterNew(
    stateDimension: Int,
    measureDimension: Int,
    controlDimension: Int
) {
    /*these matrices should be provided by user*/
    var f: MatrixNew = MatrixNew(stateDimension, stateDimension)//state transition model
    var h: MatrixNew = MatrixNew(measureDimension, stateDimension) //observation model
    var b: MatrixNew = MatrixNew(stateDimension, controlDimension) //control matrix
    var q: MatrixNew = MatrixNew(stateDimension, stateDimension)//process noise covariance
    var r: MatrixNew =
        MatrixNew(measureDimension, measureDimension) //observation noise covariance

    /*these matrices will be updated by user*/
    var uk: MatrixNew = MatrixNew(controlDimension, 1)//control vector
    var zk: MatrixNew = MatrixNew(measureDimension, 1)//actual values (measured)
    var xkKm1: MatrixNew = MatrixNew(stateDimension, 1)//predicted state estimate
    private var pkKm1: MatrixNew =
        MatrixNew(stateDimension, stateDimension)//predicted estimate covariance
    private var yK: MatrixNew = MatrixNew(measureDimension, 1)//measurement innovation

    private var sk: MatrixNew = MatrixNew(measureDimension, measureDimension)//innovation covariance
    private var skInv: MatrixNew =
        MatrixNew(measureDimension, measureDimension)//innovation covariance inverse

    private var k: MatrixNew = MatrixNew(stateDimension, measureDimension)// Kalman gain (optimal)
    var xkK: MatrixNew = MatrixNew(stateDimension, 1) //updated (current) state
    var pkK: MatrixNew = MatrixNew(stateDimension, stateDimension)//updated estimate covariance
    private var ykK: MatrixNew = MatrixNew(measureDimension, 1) //post fit residual

    /*auxiliary matrices*/
    private var auxBxU: MatrixNew = MatrixNew(stateDimension, 1)
    private var auxSDxSD: MatrixNew = MatrixNew(stateDimension, stateDimension)
    private var auxSDxMD: MatrixNew = MatrixNew(stateDimension, measureDimension)

    fun predict() {
        //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
        MatrixNew.matrixMultiply(f, xkK, xkKm1)
        MatrixNew.matrixMultiply(b, uk, auxBxU)
        MatrixNew.matrixAdd(xkKm1, auxBxU, xkKm1)

        //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
        MatrixNew.matrixMultiply(f, pkK, auxSDxSD)
        MatrixNew.matrixMultiplyByTranspose(auxSDxSD, f, pkKm1)
        MatrixNew.matrixAdd(pkKm1, q, pkKm1)

        XLog.i(" ${Utils.KALMAN_FILTER_PREDICTED_STATE} \n [ ${xkKm1.data[0][0]} \n ${xkKm1.data[1][0]} \n ${xkKm1.data[2][0]} \n ${xkKm1.data[3][0]} ]")

        XLog.i(" ${Utils.KALMAN_FILTER_PREDICTED_ESTIMATE_COVARIANCE} \n [ " +
                "${pkKm1.data[0][0]} ${pkKm1.data[0][1]} ${pkKm1.data[0][2]} ${pkKm1.data[0][3]} \n" +
                "${pkKm1.data[1][0]} ${pkKm1.data[1][1]} ${pkKm1.data[1][2]} ${pkKm1.data[1][3]} \n" +
                "${pkKm1.data[2][0]} ${pkKm1.data[2][1]} ${pkKm1.data[2][2]} ${pkKm1.data[2][3]} \n" +
                "${pkKm1.data[3][0]} ${pkKm1.data[3][1]} ${pkKm1.data[3][2]} ${pkKm1.data[3][3]} ]")

    }

    fun update() {
        //Yk = Zk - Hk*Xk|k-1
        MatrixNew.matrixMultiply(h, xkKm1, yK)
        MatrixNew.matrixSubtract(zk, yK, yK)

        //Sk = Rk + Hk*Pk|k-1*Hk(t)
        MatrixNew.matrixMultiplyByTranspose(pkKm1, h, auxSDxMD)
        MatrixNew.matrixMultiply(h, auxSDxMD, sk)
        MatrixNew.matrixAdd(r, sk, sk)

        //Kk = Pk|k-1*Hk(t)*Sk(inv)
        if (!(MatrixNew.matrixDestructiveInvert(sk, skInv)))
            return//matrix hasn't inversion
        MatrixNew.matrixMultiply(auxSDxMD, skInv, k)

        //xk|k = xk|k-1 + Kk*Yk
        MatrixNew.matrixMultiply(k, yK, xkK)
        MatrixNew.matrixAdd(xkKm1, xkK, xkK)

        //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
        MatrixNew.matrixMultiply(k, h, auxSDxSD)
        MatrixNew.matrixSubtractFromIdentity(auxSDxSD)
        MatrixNew.matrixMultiply(auxSDxSD, pkKm1, pkK)

        XLog.i(" ${Utils.KALMAN_FILTER_UPDATED_STATE} \n [ ${xkK.data[0][0]} \n ${xkK.data[1][0]} \n ${xkK.data[2][0]} \n ${xkK.data[3][0]} ]")

        XLog.i(" ${Utils.KALMAN_FILTER_UPDATED_ESTIMATE_COVARIANCE} \n [ " +
                "${pkK.data[0][0]} ${pkK.data[0][1]} ${pkK.data[0][2]} ${pkK.data[0][3]} \n" +
                "${pkK.data[1][0]} ${pkK.data[1][1]} ${pkK.data[1][2]} ${pkK.data[1][3]} \n" +
                "${pkK.data[2][0]} ${pkK.data[2][1]} ${pkK.data[2][2]} ${pkK.data[2][3]} \n" +
                "${pkK.data[3][0]} ${pkK.data[3][1]} ${pkK.data[3][2]} ${pkK.data[3][3]} ]")

        //we don't use this :
        //Yk|k = Zk - Hk*Xk|k
//        Matrix.matrixMultiply(H, Xk_k, Yk_k);
//        Matrix.matrixSubtract(Zk, Yk_k, Yk_k);
    }


}