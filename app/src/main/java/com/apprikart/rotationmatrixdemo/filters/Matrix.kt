package com.apprikart.rotationmatrixdemo.filters

class Matrix(private var rows: Int, private var cols: Int) {

    private var data: Array<DoubleArray> = Array(rows) { DoubleArray(cols) }

    // vararg will accept n number of elements as input
    fun setData(vararg args: Double) {
        assert(args.size == rows * cols)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                data[r][c] = args[r * cols + c]
            }
        }
    }

    fun setIdentityDiag() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                data[r][c] = 0.0
            }
            data[r][r] = 1.0
        }
    }

    fun setIdentity() {
        assert(rows == cols)
        setIdentityDiag()
    }

    fun scale(scalar: Double) {
        var c: Int
        var r = 0
        while (r < rows) {
            c = 0
            while (c < cols) {
                data[r][c] *= scalar
                ++c
            }
            ++r
        }
    }

    companion object {

        fun matrixAdd(ma: Matrix, mb: Matrix, mc: Matrix) {
            assert(ma.cols == mb.cols && mb.cols == mc.cols)
            assert(ma.rows == mb.rows && mb.rows == mc.rows)

            var r = 0
            var c: Int

            while (r < ma.rows) {
                c = 0
                while (c < ma.cols) {
                    mc.data[r][c] = ma.data[r][c] + mb.data[r][c]
                    ++c
                }
                ++r
            }
        }

        fun matrixMultiply(ma: Matrix, mb: Matrix, mc: Matrix) {
            assert(ma.cols == mb.rows)
            assert(ma.rows == mc.rows)
            assert(mb.cols == mc.cols)

            var c: Int
            var rc: Int

            val mcRows = mc.rows
            val mcCols = mc.cols
            val maCols = ma.cols

            var r = 0
            while (r < mcRows) {
                c = 0
                while (c < mcCols) {
                    mc.data[r][c] = 0.0
                    rc = 0
                    while (rc < maCols) {
                        mc.data[r][c] += ma.data[r][rc] * mb.data[rc][c]
                        ++rc
                    }
                    ++c
                }
                ++r
            }
        }

        fun matrixMultiplyByTranspose(
            ma: Matrix,
            mb: Matrix,
            mc: Matrix
        ) {
            assert(ma.cols == mb.cols)
            assert(ma.rows == mc.rows)
            assert(mb.rows == mc.cols)
            var c: Int
            var rc: Int
            var r = 0
            while (r < mc.rows) {
                c = 0
                while (c < mc.cols) {
                    mc.data[r][c] = 0.0
                    rc = 0
                    while (rc < ma.cols) {
                        mc.data[r][c] += ma.data[r][rc] * mb.data[c][rc]
                        ++rc
                    }
                    ++c
                }
                ++r
            }
        }

        fun matrixCopy(
            mSrc: Matrix,
            mDst: Matrix
        ) {
            assert(mSrc.rows == mDst.rows && mSrc.cols == mDst.cols)
            for (r in 0 until mSrc.rows) {
                for (c in 0 until mSrc.cols) {
                    mDst.data[r][c] = mSrc.data[r][c]
                }
            }
        }

    }

}