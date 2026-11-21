package com.my.vortex.domain.model.rpc

fun RpcConfig.forConsoleRestart(startTimeMillis: Long): RpcConfig = copy(
    timestampsStart = if (timestampsStart.isNotBlank()) startTimeMillis.toString() else ""
)
