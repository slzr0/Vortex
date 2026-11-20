/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * NotificationListener.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_rpc_base.services

import android.service.notification.NotificationListenerService

open class NotificationListener: NotificationListenerService()
/** Stable system component identity used by Android's notification access grant. */
fun notificationListenerComponent(context: android.content.Context): android.content.ComponentName =
    android.content.ComponentName(context, com.my.kizzy.feature_rpc_base.services.NotificationListener::class.java)
