package me.trajkot.aih.config;

import me.trajkot.aih.AIH;

public class AIHConfig {

    private boolean cancelClick;

    private String prefix;
    private String alertMessage;
    private String devSymbol;

    private boolean updateMessage;

    private int maxViolations;
    private int violationsResetInterval;
    private String punishCommand;
    private String broadcastMessage;

    public AIHConfig() {
        reloadConfig();
    }

    public void reloadConfig() {
        cancelClick = AIH.INSTANCE.getConfig().getBoolean("cancel-click");

        prefix = AIH.INSTANCE.getConfig().getString("prefix");
        alertMessage = AIH.INSTANCE.getConfig().getString("alerts.alert-message");
        devSymbol = AIH.INSTANCE.getConfig().getString("alerts.dev-symbol");

        updateMessage = AIH.INSTANCE.getConfig().getBoolean("alerts.update-message");

        punishCommand = AIH.INSTANCE.getConfig().getString("punishments.punish-command");
        broadcastMessage = AIH.INSTANCE.getConfig().getString("punishments.broadcast-message");
        maxViolations = AIH.INSTANCE.getConfig().getInt("violations.max-violations");
        violationsResetInterval = AIH.INSTANCE.getConfig().getInt("violations.violations-reset-interval");
    }

    public boolean isCancelClick() {
        return cancelClick;
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

    public boolean isUpdateMessage() {
        return updateMessage;
    }
}