package com.my.kizzy.feature_rpc_base.services

// Component names are persisted by Android for notification access, pinned shortcuts and tiles.
// Keep these thin adapters so upgrades retain those grants and existing user shortcuts.
class NotificationListener : com.my.vortex.feature_rpc_base.services.NotificationListener()
class KizzyTileService : com.my.vortex.feature_rpc_base.services.VortexTileService()
class ShortcutsActivity : com.my.vortex.feature_rpc_base.services.ShortcutsActivity()
