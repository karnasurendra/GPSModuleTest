package com.apprikart.rotationmatrixdemo.filters

import android.util.Log

// This is Primary Constructor in Kotlin
class GPSAccKalmanFilterNew(private var isFromDependency: Boolean) {

    private var mTimeStampMsPredict: Double = 0.0
    private var mTimeStampMsUpdate: Double = 0.0
    private var mUseGpsSpeed = false
    private lateinit var kalmanFilterNew: KalmanFilterNew
    private var accSigma: Double = 0.0
    private var predictCount = 0
    private var mVelFactor: Double = 0.0
    private var mPosFactor: Double = 0.0


    // As this class will get initialized from DI, So use this method as initialization part by calling it when onLocation updated get triggered
    fun manualInit(
        useGpsSpeed: Boolean,
        x: Double, // Long to Meters using Coordinates class
        y: Double, // Lat to Meters using Coordinates class
        xVel: Double, // Velocity using speed and cos of Course ( Bearing value from GPS )
        yVel: Double, // Velocity using speed and sin of Course ( Bearing value from GPS )
        accDev: Double, // Accelerometer default deviation always 0.1
        posDev: Double, // Accuracy from GPS
        timeStampMs: Double, // Current timestamp
        velFactor: Double, // Default velocity factor 1.0
        posVector: Double, // Default position factor 1.0
        isFromDependency: Boolean
    ) {

        this.isFromDependency = isFromDependency

        val mesDim = if (useGpsSpeed) 4 else 2
        mUseGpsSpeed = useGpsSpeed

        kalmanFilterNew = KalmanFilterNew(4, mesDim, 2)
        mTimeStampMsPredict = timeStampMs.also { mTimeStampMsUpdate = it }
        accSigma = accDev
        predictCount = 0
        // Logs
        // System State vector constructing here
        kalmanFilterNew.xkK.setData(x, y, xVel, yVel)

        // Log for all below lines
        kalmanFilterNew.h.setIdentityDiag()
        kalmanFilterNew.pkK.setIdentity()
        kalmanFilterNew.pkK.scale(posDev) // posDev is accuracy from GPS
        mVelFactor = velFactor
        mPosFactor = posVector

    }

    fun isInitializedFromDI(): Boolean {
        return isFromDependency
    }

    // xAcc - absEastAcc which is calculated in SensorGpsDataItem
    // yAcc - absNorthAcc which is calculate in SensorGpsDataItem
    fun predict(timeNowMs: Double, xAcc: Double, yAcc: Double) {

        // Logs
        val dtPredict = (timeNowMs - mTimeStampMsPredict) / 1000.0
        val dtUpdate = (timeNowMs - mTimeStampMsUpdate) / 1000.0
        reBuildF(dtPredict)
        rebuildB(dtPredict)
        reBuildU(xAcc, yAcc)

        ++predictCount
        // accSigma here is accDev which is acceleration default deviation 0.1
        rebuildQ(dtUpdate, accSigma)

        mTimeStampMsPredict = timeNowMs
        kalmanFilterNew.predict()
        MatrixNew.matrixCopy(kalmanFilterNew.xkKm1, kalmanFilterNew.xkK)

    }

    fun update(
        timeStamp: Double,
        x: Double, // longitude in Meters from Coordinate
        y: Double, // Latitude in Meters from Coordinates
        xVel: Double, // velocity from Speed and cos of Course
        yVel: Double, // Velocity from Speed and sin of Course
        posDev: Double, // Accuracy from Location
        velErr: Double // Accuracy from Location with 0.1
    ) {

        predictCount = 0
        mTimeStampMsUpdate = timeStamp
        rebuildR(posDev, velErr)
        kalmanFilterNew.zk.setData(x, y, xVel, yVel)
        Log.d("Matric::", "Checking for Assertion before Update")
        kalmanFilterNew.update()

    }

    private fun rebuildR(posSigma: Double, velSigma: Double) {
        val mPosSigma = posSigma * mPosFactor
        val mVelSigma = velSigma * mVelFactor
        if (mUseGpsSpeed) {
            val r = doubleArrayOf(
                mPosSigma, 0.0, 0.0, 0.0,
                0.0, mPosSigma, 0.0, 0.0,
                0.0, 0.0, mVelSigma, 0.0,
                0.0, 0.0, 0.0, mVelSigma
            )
            kalmanFilterNew.r.setData(*r)
        } else {
            kalmanFilterNew.r.setIdentity()
            kalmanFilterNew.r.scale(mPosSigma)
        }
    }


    private fun rebuildQ(
        dtUpdate: Double,
        accDev: Double
    ) {
        //        now we use predictCount. but maybe there is way to use dtUpdate.
        //        m_kf.Q.setIdentity();
        //        m_kf.Q.scale(accSigma * dtUpdate);
        val velDev: Double = accDev * predictCount
        val posDev: Double = velDev * predictCount / 2
        val covDev = velDev * posDev

        val posSig = posDev * posDev
        val velSig = velDev * velDev

        val q = doubleArrayOf(
            posSig, 0.0, covDev, 0.0,
            0.0, posSig, 0.0, covDev,
            covDev, 0.0, velSig, 0.0,
            0.0, covDev, 0.0, velSig
        )
        kalmanFilterNew.q.setData(*q)
    }

    // Control Vector constructing here
    private fun reBuildU(xAcc: Double, yAcc: Double) {
        kalmanFilterNew.uk.setData(xAcc, yAcc)
    }

    /*Control - Input Model Constructing here*/
    private fun rebuildB(dtPredict: Double) {
        val dt2 = 0.5 * dtPredict * dtPredict
        val b = doubleArrayOf(
            dt2, 0.0,
            0.0, dt2,
            dtPredict, 0.0,
            0.0, dtPredict
        )
        kalmanFilterNew.b.setData(*b)
    }

    /* State Transition model constructing here*/
    private fun reBuildF(dtPredict: Double) {
        val f = doubleArrayOf(
            1.0, 0.0, dtPredict, 0.0,
            0.0, 1.0, 0.0, dtPredict,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0
        )
        kalmanFilterNew.f.setData(*f)
    }

    fun getCurrentX(): Double {
        return kalmanFilterNew.xkK.data[0][0]
    }

    fun getCurrentY(): Double {
        return kalmanFilterNew.xkK.data[1][0]
    }

    fun getCurrentXVel(): Double {
        return kalmanFilterNew.xkK.data[2][0]
    }

    fun getCurrentYVel(): Double {
        return kalmanFilterNew.xkK.data[3][0]
    }

}