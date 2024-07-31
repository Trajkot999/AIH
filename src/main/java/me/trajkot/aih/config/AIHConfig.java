package me.trajkot.aih.config;

import me.trajkot.aih.AIH;

public class AIHConfig {

    private boolean cancelClick;
    private boolean safeCancelClick;

    private String prefix;
    private String alertMessage;
    private String devSymbol;

    private int maxViolations;
    private int violationsResetInterval;
    private String punishCommand;
    private String broadcastMessage;

    public AIHConfig() {
        reloadConfig();
    }

    public void reloadConfig() {
        cancelClick = AIH.INSTANCE.getConfig().getBoolean("cancel-click");
        safeCancelClick = AIH.INSTANCE.getConfig().getBoolean("safe-cancel-click");

        prefix = AIH.INSTANCE.getConfig().getString("prefix");
        alertMessage = AIH.INSTANCE.getConfig().getString("alerts.alert-message");
        devSymbol = AIH.INSTANCE.getConfig().getString("alerts.dev-symbol");

        punishCommand = AIH.INSTANCE.getConfig().getString("punishments.punish-command");
        broadcastMessage = AIH.INSTANCE.getConfig().getString("punishments.broadcast-message");
        maxViolations = AIH.INSTANCE.getConfig().getInt("violations.max-violations");
        violationsResetInterval = AIH.INSTANCE.getConfig().getInt("violations.violations-reset-interval");
    }

    public boolean isCancelClick() {
        return cancelClick;
    }

    public boolean isSafeCancelClick() {
        return safeCancelClick;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public String getDevSymbol() {
        return devSymbol;
    }

    public int getMaxViolations() {
        return maxViolations;
    }

    public String getPunishCommand() {
        return punishCommand;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public int getViolationsResetInterval() {
        return violationsResetInterval;
    }
}