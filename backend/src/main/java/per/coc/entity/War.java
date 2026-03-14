package per.coc.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "wars")
public class War {

    @Id
    @Column(length = 50)
    private String warTag;

    private Date startTime;
    private Date endTime;
    private int totalAttacks;
    private double averageDestruction;
    private int totalStars;
    private int clanLevel;

    // Constructors
    public War() {
    }

    public War(String warTag, Date startTime, Date endTime, int totalAttacks, double averageDestruction, int totalStars, int clanLevel) {
        this.warTag = warTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAttacks = totalAttacks;
        this.averageDestruction = averageDestruction;
        this.totalStars = totalStars;
        this.clanLevel = clanLevel;
    }

    // Getters and setters
    public String getWarTag() {
        return warTag;
    }

    public void setWarTag(String warTag) {
        this.warTag = warTag;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getTotalAttacks() {
        return totalAttacks;
    }

    public void setTotalAttacks(int totalAttacks) {
        this.totalAttacks = totalAttacks;
    }

    public double getAverageDestruction() {
        return averageDestruction;
    }

    public void setAverageDestruction(double averageDestruction) {
        this.averageDestruction = averageDestruction;
    }

    public int getTotalStars() {
        return totalStars;
    }

    public void setTotalStars(int totalStars) {
        this.totalStars = totalStars;
    }

    public int getClanLevel() {
        return clanLevel;
    }

    public void setClanLevel(int clanLevel) {
        this.clanLevel = clanLevel;
    }
}

