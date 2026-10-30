package com.segotlo.campusfindapp

import org.junit.Test

import org.junit.Assert.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {


    class AppFunctionTest {

        @Test
        fun validEmailIsAccepted() {
            val email = "player@gmail.com"

            val result = email.contains("@") &&
                    email.contains(".")

            assertTrue(result)
        }

        @Test
        fun invalidEmailIsRejected() {
            val email = "playergmail.com"

            val result = email.contains("@") &&
                    email.contains(".")

            assertFalse(result)
        }
    }
}
