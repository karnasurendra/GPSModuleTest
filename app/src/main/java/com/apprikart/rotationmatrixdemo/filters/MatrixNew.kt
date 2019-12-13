package com.apprikart.rotationmatrixdemo.filters

import com.elvishew.xlog.XLog

class MatrixNew(private var rows: Int, private var cols: Int) {

    var data: Array<DoubleArray> = Array(rows) { DoubleArray(cols) }

    // vararg will accept n number of elements as input
    fun setData(vararg args: Double) {
        if (args.size != rows * cols) {
            XLog.i("Checking for ZK issue in Matrix rows : $rows, Cols : $cols and Args Size ${args.size}")
        }
        assert(args.size == rows * cols)
         for (r in 0 until rows) {
            for (c in 0 until cols) {
                data[r][c] = args[r * cols + c]
            }
        }
    }

    // Setting the Identity Matrix
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

    private fun swapRows(r1: Int, r2: Int) {
        assert(r1 != r2)
        val tmp = data[r1]
        data[r1] = data[r2]
        data[r2] = tmp
    }

    private fun scaleRow(r: Int, scalar: Double) {
        assert(r < rows)
        var c = 0
        while (c < cols) {
            data[r][c] *= scalar
            ++c
        }
    }

    private fun shearRow(r1: Int, r2: Int, scalar: Double) {
        assert(r1 != r2)
        assert(r1 < rows && r2 < rows)
        var c = 0
        while (c < cols) {
            data[r1][c] += data[r2][c] * scalar
            ++c
        }
    }

    companion object {

        fun matrixAdd(ma: MatrixNew, mb: MatrixNew, mc: MatrixNew) {
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

        fun matrixMultiply(ma: MatrixNew, mb: MatrixNew, mc: MatrixNew) {
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
            ma: MatrixNew,
            mb: MatrixNew,
            mc: MatrixNew
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
            mSrc: MatrixNew,
            mDst: MatrixNew
        ) {
            assert(mSrc.rows == mDst.rows && mSrc.cols == mDst.cols)
            for (r in 0 until mSrc.rows) {
                for (c in 0 until mSrc.cols) {
                    mDst.data[r][c] = mSrc.data[r][c]
                }
            }
        }

        fun matrixSubtract(
            ma: MatrixNew,
            mb: MatrixNew,
            mc: MatrixNew
        ) {
            assert(ma.cols == mb.cols && mb.cols == mc.cols)
            assert(ma.rows == mb.rows && mb.rows == mc.rows)
            for (r in 0 until ma.rows) {
                for (c in 0 until ma.cols) {
                    mc.data[r][c] = ma.data[r][c] - mb.data[r][c]
                }
            }
        }

        fun matrixDestructiveInvert(
            mtxIn: MatrixNew,
            mtxOut: MatrixNew
        ): Boolean {
            assert(mtxIn.cols == mtxIn.rows)
            assert(mtxOut.cols == mtxIn.cols)
            assert(mtxOut.rows == mtxIn.rows)
            var ri: Int
            var scalar: Double
            mtxOut.setIdentity()
            var r = 0
            while (r < mtxIn.rows) {
                if (mtxIn.data[r][r] == 0.0) { //we have to swap rows here to make nonzero diagonal
                    ri = r
                    while (ri < mtxIn.rows) {
                        if (mtxIn.data[ri][ri] != 0.0) break
                        ++ri
                    }
                    if (ri == mtxIn.rows) return false //can't get inverse matrix
                    mtxIn.swapRows(r, ri)
                    mtxOut.swapRows(r, ri)
                } //if mtxin.data[r][r] == 0.0
                scalar = 1.0 / mtxIn.data.get(r).get(r)
                mtxIn.scaleRow(r, scalar)
                mtxOut.scaleRow(r, scalar)
                ri = 0
                while (ri < r) {
                    scalar = -mtxIn.data.get(ri).get(r)
                    mtxIn.shearRow(ri, r, scalar)
                    mtxOut.shearRow(ri, r, scalar)
                    ++ri
                }
                ri = r + 1
                while (ri < mtxIn.rows) {
                    scalar = -mtxIn.data.get(ri).get(r)
                    mtxIn.shearRow(ri, r, scalar)
                    mtxOut.shearRow(ri, r, scalar)
                    ++ri
                }
                ++r
            }
            return true
        }

        fun matrixSubtractFromIdentity(m: MatrixNew) {
            var c: Int
            var r = 0
            while (r < m.rows) {
                c = 0
                while (c < r) {
                    m.data[r][c] = -m.data[r][c]
                    ++c
                }
                m.data[r][r] = 1.0 - m.data[r][r]
                c = r + 1
                while (c < m.cols) {
                    m.data[r][c] = -m.data[r][c]
                    ++c
                }
                ++r
            }
        }

    }

}