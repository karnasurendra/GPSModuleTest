package com.apprikart.rotationmatrixdemo.location

class LocationEngineRequest() {

    private var priority = 0
    private var interval = 0.0
    private var displacement = 0.0f

    companion object {

    }

    fun getPrority(): Int {
        return priority
    }

    fun getInterval(): Long {
        return interval.toLong()
    }

    fun getDisplacement():Float{
        return displacement
    }

}