/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.terracotta;

import static com.movtery.zalithlauncher.notification._NotificationIDKt.NOTIFICATION_ID_VPN_REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.notification.NotificationChannelData;

import net.burningtnt.terracotta.TerracottaAndroidAPI;

import java.io.IOException;

/**
 * [Modified from FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/5926006/FCL/src/main/java/com/tungsten/fcl/terracotta/TerracottaVPNService.java)
 */
@SuppressLint("VpnServicePolicy")
public class TerracottaVPNService extends VpnService {

    private static final String TAG                  = "TerracottaVPNService";

    private static final int VPN_NOTIFICATION_ID     = 1;

    public static final String ACTION_START          = "net.burningtnt.terracotta.action.START";
    public static final String ACTION_STOP           = "net.burningtnt.terracotta.action.STOP";
    public static final String ACTION_REPOST         = "net.burningtnt.terracotta.action.REPOST";
    public static final String ACTION_UPDATE_STATE   = "net.burningtnt.terracotta.action.UPDATE_STATE";

    private static final String EXTRA_FROM_DELETE    = "from_delete";
    public static final String EXTRA_STATE_TEXT      = "terracotta_state_text";

    private NotificationManager notificationManager;
    private int currentStateStringRes = -1;
    private volatile boolean isStopping = false;

    private ParcelFileDescriptor vpnInterface;

    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

        String action = intent != null ? intent.getAction() : null;
        Log.d(TAG, "onStartCommand, action = " + action);

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        if (ACTION_STOP.equals(action)) {
            isStopping = true;
            cleanup();
            stopForeground(true);
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        if (ACTION_UPDATE_STATE.equals(action) || ACTION_REPOST.equals(action)) {
            currentStateStringRes = getStateTextRes(intent);
        }

        if (!isStopping) {
            //经 startForegroundService 启动后必须及时进入前台，否则系统将抛出
            //ForegroundServiceDidNotStartInTimeException 导致进程崩溃
            try {
                startForeground0(buildVpnNotification());
            } catch (Exception e) {
                Log.w(TAG, "Cannot start VPN foreground service.", e);
                try {
                    TerracottaAndroidAPI.getPendingVpnServiceRequest().reject();
                } catch (IllegalStateException ignored) {
                }
                stopSelf();
                return Service.START_NOT_STICKY;
            }
        }

        if (intent == null) {
            //系统重建服务时不会重新下发联机请求，没有可建立的隧道
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        if (ACTION_UPDATE_STATE.equals(action) || ACTION_REPOST.equals(action)) {
            return Service.START_STICKY;
        }

        isStopping = false;

        Builder vpnBuilder = new Builder().setSession("Terracotta Connection");

        try {
            vpnBuilder.addDisallowedApplication(getPackageName());
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        TerracottaAndroidAPI.VpnServiceRequest request;
        try {
            request = TerracottaAndroidAPI.getPendingVpnServiceRequest();
        } catch (IllegalStateException e) {
            //原生侧已无挂起的联机请求（如等待授权超时），无需建立隧道
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        vpnInterface = request.startVpnService(vpnBuilder);

        return Service.START_STICKY;
    }

    @Override
    public void onRevoke() {
        Log.w(TAG, "onRevoke(): preempted by another VPN or revoked by user; tearing down.");
        isStopping = true;
        Terracotta.INSTANCE.setWaiting(false);
        cleanup();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy(): vpn service finished");
        isStopping = true;
        Terracotta.INSTANCE.setWaiting(false);
        cleanup();
        super.onDestroy();
    }

    private int getStateTextRes(Intent intent) {
        int res = -1;
        if (intent.hasExtra(EXTRA_STATE_TEXT)) {
            res = intent.getIntExtra(EXTRA_STATE_TEXT, -1);
        }
        return res;
    }

    private Notification buildVpnNotification() {
        String title = getString(R.string.terracotta_notification_title);

        if (currentStateStringRes == -1) {
            TerracottaState.Ready state = Terracotta.INSTANCE.getState().getValue();
            if (state != null && !(state instanceof TerracottaState.Waiting)) {
                currentStateStringRes = state.localStringRes();
            }
        }

        String contentText;
        Terracotta.Mode mode = Terracotta.INSTANCE.getMode();
        if (mode == null) {
            //联机模式尚未确定，此时通知不展示身份信息
            contentText = getString(R.string.terracotta_notification_state_preparing);
        } else {
            String modeText = mode == Terracotta.Mode.Host ? getString(R.string.terracotta_player_kind_host) : getString(R.string.terracotta_player_kind_guest);
            String stateString;
            if (currentStateStringRes == -1) {
                stateString = "Unknown";
            } else {
                stateString = getString(currentStateStringRes);
            }
            contentText = String.format(getString(R.string.terracotta_notification_desc), modeText, stateString);
        }

        Notification.Builder builder;
        builder = new Notification.Builder(this, NotificationChannelData.TERRACOTTA_VPN_CHANNEL.getChannelId());

        Intent deleteIntent = new Intent(this, TerracottaVPNService.class)
                .setAction(ACTION_REPOST)
                .putExtra(EXTRA_FROM_DELETE, true)
                .putExtra(EXTRA_STATE_TEXT, currentStateStringRes);
        PendingIntent deletePendingIntent = PendingIntent.getService(
                this,
                NOTIFICATION_ID_VPN_REQUEST_CODE,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contentText)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setDeleteIntent(deletePendingIntent);

        return builder.build();
    }

    private void startForeground0(Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(TerracottaVPNService.VPN_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(TerracottaVPNService.VPN_NOTIFICATION_ID, notification);
        }
    }

    private void cleanup() {
        Log.d(TAG, "cleanup(): close tun & cancel notification");

        if (notificationManager != null) {
            notificationManager.cancel(VPN_NOTIFICATION_ID);
        }

        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException ignored) {
            }
            vpnInterface = null;
        }

        running = false;
    }

}
