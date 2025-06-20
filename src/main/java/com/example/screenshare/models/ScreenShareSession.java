package com.example.screenshare.models;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScreenShareSession {

    private final UUID suspectId;
    private final UUID staffId;
    private final String suspectName;
    private final String staffName;
    private final Location originalSuspectLocation;
    private final Location originalStaffLocation;
    private final LocalDateTime startTime;

    public ScreenShareSession(Player suspect, Player staff, Location originalSuspectLocation, Location originalStaffLocation) {
        this.suspectId = suspect.getUniqueId();
        this.staffId = staff.getUniqueId();
        this.suspectName = suspect.getName();
        this.staffName = staff.getName();
        this.originalSuspectLocation = originalSuspectLocation;
        this.originalStaffLocation = originalStaffLocation;
        this.startTime = LocalDateTime.now();
    }

    public UUID getSuspectId() {
        return suspectId;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public String getSuspectName() {
        return suspectName;
    }

    public String getStaffName() {
        return staffName;
    }

    public Location getOriginalSuspectLocation() {
        return originalSuspectLocation;
    }

    public Location getOriginalStaffLocation() {
        return originalStaffLocation;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return String.format("ScreenShareSession{suspect='%s', staff='%s', startTime=%s}",
                suspectName, staffName, startTime);
    }
}