package per.coc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import per.coc.entity.Attack;
import per.coc.entity.Player;
import per.coc.entity.War;
import per.coc.model.Clan;
import per.coc.model.Status;

@Service
public class ScheduleWarDataSaver {

    private static final Logger LOGGER = Logger.getLogger(ScheduleWarDataSaver.class.getName());
    
    private static final String CLAN_TAG = "#2R08P0L9";
    private static final String CLAN_TAG_ENCODED = "%232R08P0L9";
    
    // Track war end time to detect changes
    private Instant lastKnownWarEndTime = null;
    
    @Value("${coc.api.token}")
    private String token;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private WarService warService;
    
    @Autowired
    private AttackService attackService;

    private String fetchDataFromClashAPI(String endpoint) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                LOGGER.info("Error: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostConstruct
    public void onStartup() {
        LOGGER.info("Service started. Will check war status every 10 minutes.");
        checkAndFetchWarData();
    }

    /**
     * Checks war status every 10 minutes.
     * Only fetches data if:
     * - Currently in war (state = "inWar")
     * - Within last 6 hours of war OR within 5 minutes of war end
     */
    @Scheduled(fixedRate = 600000) // Check every 10 minutes
    public void scheduledWarCheck() {
        try {
            checkAndFetchWarData();
        } catch (Exception e) {
            LOGGER.warning("Scheduled war check failed: " + e.getMessage());
        }
    }

    private void checkAndFetchWarData() {
        LOGGER.info("Checking war status...");
        
        // First try CWL
        String clanWarLeagueDetails = fetchDataFromClashAPI(
                "https://api.clashofclans.com/v1/clans/" + CLAN_TAG_ENCODED + "/currentwar/leaguegroup");
        
        if (clanWarLeagueDetails != null) {
            // CWL is active - process it
            LOGGER.info("CWL detected, processing...");
            processCWL(clanWarLeagueDetails);
            return;
        }
        
        // Try normal war
        String normalWarDetails = fetchDataFromClashAPI(
                "https://api.clashofclans.com/v1/clans/" + CLAN_TAG_ENCODED + "/currentwar");
        
        if (normalWarDetails == null) {
            LOGGER.info("Could not fetch war data.");
            return;
        }
        
        JSONObject warJson = new JSONObject(normalWarDetails);
        String state = warJson.optString("state", "");
        
        if (state.equals("notInWar")) {
            LOGGER.info("Clan is not currently in a war.");
            resetWarTracking();
            return;
        }
        
        if (state.equals("preparation")) {
            LOGGER.info("War is in preparation phase. Waiting for battle day.");
            return;
        }
        
        if (state.equals("warEnded")) {
            LOGGER.info("War has ended.");
            resetWarTracking();
            return;
        }
        
        if (!state.equals("inWar")) {
            LOGGER.info("Unknown war state: " + state);
            return;
        }
        
        // We're in war - check timing
        String endTimeStr = warJson.optString("endTime", "");
        if (endTimeStr.isEmpty()) {
            LOGGER.warning("War end time not available.");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX");
        Instant warEndTime = OffsetDateTime.parse(endTimeStr, formatter).toInstant();
        Instant now = Instant.now();
        long timeUntilEnd = warEndTime.toEpochMilli() - now.toEpochMilli();
        
        // Check if war end time changed
        if (lastKnownWarEndTime != null && !lastKnownWarEndTime.equals(warEndTime)) {
            LOGGER.info("War end time changed from " + lastKnownWarEndTime + " to " + warEndTime);
        }
        lastKnownWarEndTime = warEndTime;
        
        long hoursRemaining = timeUntilEnd / (1000 * 60 * 60);
        long minutesRemaining = (timeUntilEnd / (1000 * 60)) % 60;
        LOGGER.info("War in progress. Time remaining: " + hoursRemaining + "h " + minutesRemaining + "m");
        
        // Fetch data every 10 minutes during war
        LOGGER.info("Fetching war data...");
        processNormalWar(warJson);
    }
    
    private void resetWarTracking() {
        lastKnownWarEndTime = null;
    }
    
    private void processCWL(String clanWarLeagueDetails) {
        JSONObject allJsonObject = new JSONObject(clanWarLeagueDetails);
        JSONArray clansJson = allJsonObject.getJSONArray("clans");
        Clan clan = null;

        for (int i = 0; i < clansJson.length(); i++) {
            JSONObject clanJson = clansJson.getJSONObject(i);
            if (!clanJson.getString("tag").equals(CLAN_TAG)) {
                continue;
            }
            clan = new Clan(clanJson.getString("tag"), clanJson.getString("name"), clanJson.getInt("clanLevel"));
            storePlayers(clan, clanJson);
        }

        JSONArray rounds = allJsonObject.getJSONArray("rounds");
        for (int i = 0; i < rounds.length(); i++) {
            JSONObject round = rounds.getJSONObject(i);
            JSONArray warTags = round.getJSONArray("warTags");
            for (int j = 0; j < warTags.length(); j++) {
                processCWLWar(i, warTags, j);
            }
        }
        
        LOGGER.info("CWL data fetch completed!");
    }
    
    private void processCWLWar(int roundIndex, JSONArray warTags, int warIndex) {
        String warTag = warTags.getString(warIndex);
        if (warTag.equals("#0")) return;
        
        String warDetailEndpoint = "https://api.clashofclans.com/v1/clanwarleagues/wars/"
                + warTag.replace("#", "%23");
        String warDetailResponse = fetchDataFromClashAPI(warDetailEndpoint);
        
        if (warDetailResponse == null) {
            LOGGER.warning("Failed to fetch CWL war details for tag: " + warTag);
            return;
        }
        
        JSONObject warDetail = new JSONObject(warDetailResponse);
        String state = warDetail.optString("state", "");
        
        // Only process if in war or war ended (to capture final attacks)
        if (!state.equals("inWar") && !state.equals("warEnded")) {
            return;
        }
        
        // For active wars, check if within last 6 hours
        if (state.equals("inWar")) {
            String endTimeStr = warDetail.optString("endTime", "");
            if (!endTimeStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX");
                Instant warEndTime = OffsetDateTime.parse(endTimeStr, formatter).toInstant();
                long timeUntilEnd = warEndTime.toEpochMilli() - Instant.now().toEpochMilli();
                
                if (timeUntilEnd > SIX_HOURS_MS) {
                    LOGGER.info("CWL Round " + (roundIndex + 1) + " has more than 6 hours. Skipping.");
                    return;
                }
            }
        }
        
        storeWarData(roundIndex, warTag, warDetail);
    }

    private void processNormalWar(JSONObject warJson) {
        String state = warJson.optString("state", "");
        if (state.equals("notInWar")) {
            LOGGER.info("Clan is not currently in a war.");
            return;
        }
        
        String startTimeStr = warJson.optString("preparationStartTime", "");
        String warTag = "NW-" + startTimeStr;
        
        storeWarData(0, warTag, warJson);
    }

    private void storePlayers(Clan clan, JSONObject clanJson) {
        JSONArray membersJson = clanJson.getJSONArray("members");
        List<Player> players = new ArrayList<>();
        int newPlayersCount = 0;
        
        for (int j = 0; j < membersJson.length(); j++) {
            JSONObject memberJson = membersJson.getJSONObject(j);
            Player player = new Player(memberJson.getString("tag"), memberJson.getString("name"),
                    memberJson.getInt("townHallLevel"), new Date(), Status.ACTIVE);
            players.add(player);
        }
        clan.setPlayers(players);

        if (clan != null && clan.getPlayers() != null) {
            for (Player player : players) {
                if (!playerService.playerExists(player.getTag())) {
                    playerService.savePlayer(player);
                    newPlayersCount++;
                }
            }
        }

        if (newPlayersCount > 0) {
            LOGGER.info("Saved " + newPlayersCount + " new players to database.");
        } else {
            LOGGER.info("No new players to save.");
        }
    }

    private void storeWarData(int i, String warTag, JSONObject warDetail) {
        String clanTag1 = warDetail.getJSONObject("clan").getString("tag");
        String clanTag2 = warDetail.getJSONObject("opponent").getString("tag");
        boolean isOurClanFirst = clanTag1.equals(CLAN_TAG);
        if (isOurClanFirst || clanTag2.equals(CLAN_TAG)) {
            JSONObject ourClan = isOurClanFirst ? warDetail.getJSONObject("clan")
                    : warDetail.getJSONObject("opponent");
            int stars = ourClan.optInt("stars", -1);
            int destruction = ourClan.optInt("destructionPercentage", -1);

            if (!warService.warExists(warTag)) {
                String startTimeStr = warDetail.optString("startTime", "");
                String endTimeStr = warDetail.optString("endTime", "");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX");
                Date startTime = startTimeStr.isEmpty() ? null
                        : Date.from(OffsetDateTime.parse(startTimeStr, formatter).toInstant());
                Date endTime = endTimeStr.isEmpty() ? null
                        : Date.from(OffsetDateTime.parse(endTimeStr, formatter).toInstant());
                int totalAttacks = ourClan.optInt("attacks", 0);
                double averageDestruction = ourClan.optDouble("destructionPercentage", 0.0);
                int totalStars = ourClan.optInt("stars", 0);
                int clanLevel = ourClan.optInt("clanLevel", 0);

                War war = new War(warTag, startTime, endTime, totalAttacks, averageDestruction, totalStars, clanLevel);
                warService.saveWar(war);
                LOGGER.info("Saved new war: " + warTag);
            }

            int newAttacksCount = 0;
            if (ourClan.has("members")) {
                JSONArray members = ourClan.getJSONArray("members");
                for (int k = 0; k < members.length(); k++) {
                    JSONObject member = members.getJSONObject(k);
                    String attackerTag = member.getString("tag");
                    if (member.has("attacks")) {
                        JSONArray memberAttacks = member.getJSONArray("attacks");
                        for (int l = 0; l < memberAttacks.length(); l++) {
                            JSONObject attack = memberAttacks.getJSONObject(l);
                            if (storeAttackData(warTag, attackerTag, attack)) {
                                newAttacksCount++;
                            }
                        }
                    }
                }
            }

            if (newAttacksCount > 0) {
                LOGGER.info("Round " + (i + 1) + " warTag: " + warTag + " - Saved " + newAttacksCount + " new attacks. Stars: " + stars + ", Destruction: " + destruction);
            } else {
                LOGGER.info("Round " + (i + 1) + " warTag: " + warTag + " - No new attacks. Stars: " + stars + ", Destruction: " + destruction);
            }
        }
    }

    private boolean storeAttackData(String warTag, String attackerTag, JSONObject attack) {
        if (!attackService.attackExists(attackerTag, warTag)) {
            Attack atk = new Attack(attackerTag, attack.optInt("destructionPercentage", 0), attack.optInt("stars", 0),
                    attack.optInt("mapPosition"), attack.optInt("townhallLevel"), warTag);
            attackService.saveAttack(atk);
            return true;
        }
        return false;
    }

}
