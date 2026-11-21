package com.my.vortex

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupStageTest {
    @Test fun accessComesFirst() {
        assertEquals(StartupStage.PERMISSIONS, startupStage(false, false, false))
        assertEquals(StartupStage.PERMISSIONS, startupStage(false, true, true))
        assertEquals(StartupStage.PERMISSIONS, startupStage(true, false, true))
    }

    @Test fun accountFollowsAccess() {
        assertEquals(StartupStage.SIGN_IN, startupStage(true, true, false))
        assertEquals(StartupStage.COMPLETE, startupStage(true, true, true))
    }
}
