package com.ninezero.sns

import com.ninezero.data.UserDataStore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ExampleHiltTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userDataStore: UserDataStore

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun injection_test() {
        Assert.assertNotNull(userDataStore)
    }
}