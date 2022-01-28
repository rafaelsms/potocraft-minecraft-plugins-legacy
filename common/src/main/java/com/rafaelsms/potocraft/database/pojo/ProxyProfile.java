package com.rafaelsms.potocraft.database.pojo;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.util.List;

public class ProxyProfile extends PlayerObject {

    private boolean offlineProfile = false;
    private Boolean loggedIn = null;
    private ZonedDateTime lastLoginDate;
    private InetAddress lastLoginAddress;
    private Integer pin;

    private String lastServerName;
    private ZonedDateTime firstJoinDate;
    private ZonedDateTime lastJoinDate;
    private ZonedDateTime lastQuitDate;
    private long totalPlayTime;

    private List<ReportEntryObject> reportEntries;

    public ProxyProfile() {
    }

    public boolean isOfflineProfile() {
        return offlineProfile;
    }

    public void setOfflineProfile(boolean offlineProfile) {
        this.offlineProfile = offlineProfile;
    }

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public ZonedDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(ZonedDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public InetAddress getLastLoginAddress() {
        return lastLoginAddress;
    }

    public void setLastLoginAddress(InetAddress lastLoginAddress) {
        this.lastLoginAddress = lastLoginAddress;
    }

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }

    public String getLastServerName() {
        return lastServerName;
    }

    public void setLastServerName(String lastServerName) {
        this.lastServerName = lastServerName;
    }

    public ZonedDateTime getFirstJoinDate() {
        return firstJoinDate;
    }

    public void setFirstJoinDate(ZonedDateTime firstJoinDate) {
        this.firstJoinDate = firstJoinDate;
    }

    public ZonedDateTime getLastJoinDate() {
        return lastJoinDate;
    }

    public void setLastJoinDate(ZonedDateTime lastJoinDate) {
        this.lastJoinDate = lastJoinDate;
    }

    public ZonedDateTime getLastQuitDate() {
        return lastQuitDate;
    }

    public void setLastQuitDate(ZonedDateTime lastQuitDate) {
        this.lastQuitDate = lastQuitDate;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    public List<ReportEntryObject> getReportEntries() {
        return reportEntries;
    }

    public void setReportEntries(List<ReportEntryObject> reportEntries) {
        this.reportEntries = reportEntries;
    }
}
