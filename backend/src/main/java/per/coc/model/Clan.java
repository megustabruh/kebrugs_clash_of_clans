package per.coc.model;

import java.util.ArrayList;
import java.util.List;

import per.coc.entity.Player;

public class Clan {

    private String tag;
    private String name;
    private int clanLevel;
    private int memberCount;
    private int requiredTrophies;
    private int warWinStreak;
    private int warWins;
    private int warLosses;
    private int warTies;
    private List<Player> players = new ArrayList<>();

    public Clan(String tag, String name, int clanLevel) {
        this.tag = tag;
        this.name = name;
        this.clanLevel = clanLevel;
    }

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

    public int getClanLevel() {
        return clanLevel;
    }

    public void setClanLevel(int clanLevel) {
        this.clanLevel = clanLevel;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getRequiredTrophies() {
        return requiredTrophies;
    }

    public void setRequiredTrophies(int requiredTrophies) {
        this.requiredTrophies = requiredTrophies;
    }

    public int getWarWinStreak() {
        return warWinStreak;
    }

    public void setWarWinStreak(int warWinStreak) {
        this.warWinStreak = warWinStreak;
    }

    public int getWarWins() {
        return warWins;
    }

    public void setWarWins(int warWins) {
        this.warWins = warWins;
    }

    public int getWarLosses() {
        return warLosses;
    }

    public void setWarLosses(int warLosses) {
        this.warLosses = warLosses;
    }

    public int getWarTies() {
        return warTies;
    }

    public void setWarTies(int warTies) {
        this.warTies = warTies;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    
}
