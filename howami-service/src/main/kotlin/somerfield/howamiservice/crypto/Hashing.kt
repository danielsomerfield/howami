package somerfield.howamiservice.crypto

import com.lambdaworks.crypto.SCryptUtil

object Hashing {

    private val N = 16_384
    private val r = 8
    private val p = 1

    fun buildHash(string: String): String {
        return SCryptUtil.scrypt(string, N, r, p)
    }

    fun validate(string: String, hash: String): Boolean {
        return SCryptUtil.check(string, hash)
    }

}