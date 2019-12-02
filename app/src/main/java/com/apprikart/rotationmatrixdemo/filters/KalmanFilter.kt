package com.apprikart.rotationmatrixdemo.filters

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
    private var r: Matrix =
        Matrix(measureDimension, measureDimension) //observation noise covariance

    /*these matrices will be updated by user*/
    var uk: Matrix = Matrix(controlDimension, 1)//control vector
    private var zk: Matrix = Matrix(measureDimension, 1)//actual values (measured)
    var xkKm1: Matrix = Matrix(stateDimension, 1)//predicted state estimate
    private var pkKm1: Matrix =
        Matrix(stateDimension, stateDimension)//predicted estimate covariance
    private var yK: Matrix = Matrix(measureDimension, 1)//measurement innovation

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
    }

}