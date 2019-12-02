package com.apprikart.rotationmatrixdemo.filters

// This is Primary Constructor in Kotlin
class GPSAccKalmanFilter(
    useGpsSpeed: Boolean,
    x: Double,
    y: Double,
    xVel: Double,
    yVel: Double,
    accDev: Double,
    posDev: Double,
    timeStampMs: Double,
    velFactor: Double,
    posVector: Double
) {


    private var mTimeStampMsPredict: Double = 0.0
    private var mTimeStampMsUpdate: Double = 0.0
    private var mUseGpsSpeed = false
    private var kalmanFilter: KalmanFilter
    private var accSigma: Double = 0.0
    private var predictCount = 0
    private var mVelFactor: Double = 0.0
    private var mPosFactor: Double = 0.0


    // If we want to initialize any code on object creation code will sit here
    init {
        val mesDim = if (useGpsSpeed) 4 else 2
        mUseGpsSpeed = useGpsSpeed

        kalmanFilter = KalmanFilter(4, mesDim, 2)
        mTimeStampMsPredict = timeStampMs.also { mTimeStampMsUpdate = it }
        accSigma = accDev
        predictCount = 0
        kalmanFilter.xkK?.setData(x, y, xVel, yVel)

        kalmanFilter.h?.setIdentityDiag() //state has 4d and measurement has 4d too. so here is identity
        kalmanFilter.pkK?.setIdentity()
        kalmanFilter.pkK?.scale(posDev)
        mVelFactor = velFactor
        mPosFactor = posVector


    }

    fun predict(timeNowMs: Double, xAcc: Double, yAcc: Double) {

        val dtPredict = (timeNowMs - mTimeStampMsPredict) / 1000.0
        val dtUpdate = (timeNowMs - mTimeStampMsUpdate) / 1000.0
        reBuildF(dtPredict)
        rebuildB(dtPredict)
        reBuildU(xAcc, yAcc)

        ++predictCount
        rebuildQ(dtUpdate, accSigma)

        mTimeStampMsPredict = timeNowMs
        kalmanFilter.predict()
        Matrix.matrixCopy(kalmanFilter.xkKm1,kalmanFilter.xkK)

    }


    private fun rebuildQ(
        dtUpdate: Double,
        accDev: Double
    ) { //        now we use predictCount. but maybe there is way to use dtUpdate.
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
        kalmanFilter.q?.setData(*q)
    }

    private fun reBuildU(xAcc: Double, yAcc: Double) {
        kalmanFilter.uk?.setData(xAcc, yAcc)
    }

    private fun rebuildB(dtPredict: Double) {
        val dt2 = 0.5 * dtPredict * dtPredict
        val b = doubleArrayOf(
            dt2, 0.0,
            0.0, dt2,
            dtPredict, 0.0,
            0.0, dtPredict
        )
        kalmanFilter.b.setData(*b)
    }

    private fun reBuildF(dtPredict: Double) {
        val f = doubleArrayOf(
            1.0, 0.0, dtPredict, 0.0,
            0.0, 1.0, 0.0, dtPredict,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0
        )
        kalmanFilter.f.setData(*f)
    }

}