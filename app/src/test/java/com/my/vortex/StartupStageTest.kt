package com.my.vortex

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupStageTest {
    @Test fun permissionsAlwaysComeFirst() {
        assertEquals(StartupStage.PERMISSIONS, startupStage(false, false))
        assertEquals(StartupStage.PERMISSIONS, startupStage(false, true))
    }

    @Test fun signInIsRequiredAfterPermissions() {
        assertEquals(StartupStage.SIGN_IN, startupStage(true, false))
        assertEquals(StartupStage.MAIN, startupStage(true, true))
    }
}
