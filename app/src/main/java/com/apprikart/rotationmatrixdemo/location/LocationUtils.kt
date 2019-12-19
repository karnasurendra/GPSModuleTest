package com.apprikart.rotationmatrixdemo.location

class LocationUtils {

    companion object {
        fun <T> checkNotNull(t: T?, str: String?): T {
            if (t != null) {
                return t
            }
            throw NullPointerException(str)
        }

        fun isOnClassPath(str: String): Boolean {
            Class.forName(str)
            return true
        }

    }

}