package per.coc.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import per.coc.model.Status;

@Entity
@Table(name = "players")
public class Player {

    @Id
    private String tag;

    private String name;

    private int townHallLevel;

    private Date playerSince;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private Status currentStatus;

    public Player() {
    }

    public Player(String tag, String name, int townHallLevel, Date playerSince, Status currentStatus) {
        this.tag = tag;
        this.name = name;
        this.townHallLevel = townHallLevel;
        this.playerSince = playerSince;
        this.currentStatus = currentStatus;
    }

    // Getters and setters

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTownHallLevel() {
        return townHallLevel;
    }

    public void setTownHallLevel(int townHallLevel) {
        this.townHallLevel = townHallLevel;
    }

    public Date getPlayerSince() {
        return playerSince;
    }

    public void setPlayerSince(Date playerSince) {
        this.playerSince = playerSince;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

}
