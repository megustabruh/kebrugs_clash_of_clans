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
import per.coc.entity.FetchLog;
import per.coc.entity.Player;
import per.coc.entity.War;
import per.coc.model.Clan;
import per.coc.model.Status;
import per.coc.repository.FetchLogRepository;

@Service
public class ScheduleWarDataSaver {

    private static final Logger LOGGER = Logger.getLogger(ScheduleWarDataSaver.class.getName());
    
    private static final String CLAN_TAG = "#2R08P0L9";
    private static final String CLAN_TAG_ENCODED = "%232R08P0L9";
    private static final long ONE_HOUR_MS = 60 * 60 * 1000;
    
    // Track last data fetch time for hourly scheduling
    private Instant lastDataFetchTime = null;
    
    @Value("${coc.api.token}")
    private String token;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private WarService warService;
    
    @Autowired
    private AttackService attackService;
    
    @Autowired
    private FetchLogRepository fetchLogRepository;
    
    // Tracking counters for logging
    private int currentRunAttacks = 0;
    private int currentRunPlayers = 0;
    private int currentRunWars = 0;
    private String currentWarType = "NONE";
    private String currentWarState = "";
    private String currentWarTag = "";

    private String fetchDataFromClashAPI(String endpoint) {
        return fetchDataFromClashAPI(endpoint, true);
    }
    
    private String fetchDataFromClashAPI(String endpoint, boolean logErrors) {
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
                if (logErrors) {
                    LOGGER.info("Error: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostConstruct
    public void onStartup() {
        LOGGER.info("Service started. Checks every 5 mins, fetches hourly (or every 5 mins in last hour of war).");
        checkAndFetchWarData(true); // Force fetch on startup
    }

    /**
     * Checks war status every 5 minutes.
     * Fetches data:
     * - Every hour during normal war
     * - Every 5 minutes in last hour of war
     */
    @Scheduled(fixedRate = 300000) // Check every 5 minutes
    public void scheduledWarCheck() {
        try {
            checkAndFetchWarData(false);
        } catch (Exception e) {
            LOGGER.warning("Scheduled war check failed: " + e.getMessage());
        }
    }

    private void checkAndFetchWarData(boolean forceFetch) {
        LOGGER.info("Checking war status...");
        
        // Reset counters for this run
        currentRunAttacks = 0;
        currentRunPlayers = 0;
        currentRunWars = 0;
        currentWarType = "NONE";
        currentWarState = "";
        currentWarTag = "";
        LocalDateTime runTime = LocalDateTime.now();
        
        // First try CWL (404 is expected if not in CWL)
        String clanWarLeagueDetails = fetchDataFromClashAPI(
                "https://api.clashofclans.com/v1/clans/" + CLAN_TAG_ENCODED + "/currentwar/leaguegroup", false);
        
        if (clanWarLeagueDetails != null) {
            // CWL is active - process it
            LOGGER.info("CWL detected, processing...");
            currentWarType = "CWL";
            processCWL(clanWarLeagueDetails);
            saveFetchLog(runTime, "CWL data fetch completed", true);
            return;
        }
        
        // Try normal war
        String normalWarDetails = fetchDataFromClashAPI(
                "https://api.clashofclans.com/v1/clans/" + CLAN_TAG_ENCODED + "/currentwar");
        
        if (normalWarDetails == null) {
            LOGGER.info("Could not fetch war data.");
            saveFetchLog(runTime, "Could not fetch war data", false);
            return;
        }
        
        JSONObject warJson = new JSONObject(normalWarDetails);
        String state = warJson.optString("state", "");
        
        if (state.equals("notInWar")) {
            LOGGER.info("Clan is not currently in a war.");
            currentWarState = "notInWar";
            resetWarTracking();
            saveFetchLog(runTime, "Clan is not in war", true);
            return;
        }
        
        if (state.equals("preparation")) {
            LOGGER.info("War is in preparation phase. Waiting for battle day.");
            currentWarState = "preparation";
            saveFetchLog(runTime, "War in preparation phase", true);
            return;
        }
        
        if (state.equals("warEnded")) {
            LOGGER.info("War has ended.");
            currentWarState = "warEnded";
            resetWarTracking();
            saveFetchLog(runTime, "War has ended", true);
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
        
        long hoursRemaining = timeUntilEnd / (1000 * 60 * 60);
        long minutesRemaining = (timeUntilEnd / (1000 * 60)) % 60;
        LOGGER.info("War in progress. Time remaining: " + hoursRemaining + "h " + minutesRemaining + "m");
        
        // Determine if we should fetch data
        boolean inLastHour = timeUntilEnd <= ONE_HOUR_MS;
        boolean hourSinceLastFetch = lastDataFetchTime == null || 
                (now.toEpochMilli() - lastDataFetchTime.toEpochMilli()) >= ONE_HOUR_MS;
        
        if (!forceFetch && !inLastHour && !hourSinceLastFetch) {
            LOGGER.info("Skipping fetch - not in last hour and less than 1 hour since last fetch.");
            return;
        }
        
        // Fetch war data
        String reason = inLastHour ? "Last hour of war" : "Hourly fetch";
        LOGGER.info(reason + " - Fetching war data...");
        currentWarType = "NORMAL";
        currentWarState = "inWar";
        processNormalWar(warJson);
        lastDataFetchTime = now;
        saveFetchLog(runTime, reason + ". Time remaining: " + hoursRemaining + "h " + minutesRemaining + "m", true);
    }
    
    private void resetWarTracking() {
        lastDataFetchTime = null;
    }
    
    private void saveFetchLog(LocalDateTime runTime, String message, boolean success) {
        try {
            FetchLog log = new FetchLog(runTime, currentWarType, currentWarState, currentWarTag,
                    currentRunAttacks, currentRunPlayers, currentRunWars, message, success);
            fetchLogRepository.save(log);
        } catch (Exception e) {
            LOGGER.warning("Failed to save fetch log: " + e.getMessage());
        }
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
            LOGGER.info("Saved " + newPlayersCount + " new players.");
            currentRunPlayers += newPlayersCount;
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
                currentRunWars++;
            }
            
            currentWarTag = warTag;

            // Save players from war members
            int newPlayersCount = 0;
            int newAttacksCount = 0;
            if (ourClan.has("members")) {
                JSONArray members = ourClan.getJSONArray("members");
                for (int k = 0; k < members.length(); k++) {
                    JSONObject member = members.getJSONObject(k);
                    String memberTag = member.getString("tag");
                    String memberName = member.optString("name", "Unknown");
                    int townHallLevel = member.optInt("townhallLevel", 0);
                    
                    // Save player if not exists
                    if (!playerService.playerExists(memberTag)) {
                        Player player = new Player(memberTag, memberName, townHallLevel, new Date(), Status.ACTIVE);
                        playerService.savePlayer(player);
                        newPlayersCount++;
                    }
                    
                    // Save attacks
                    if (member.has("attacks")) {
                        JSONArray memberAttacks = member.getJSONArray("attacks");
                        for (int l = 0; l < memberAttacks.length(); l++) {
                            JSONObject attack = memberAttacks.getJSONObject(l);
                            if (storeAttackData(warTag, memberTag, attack)) {
                                newAttacksCount++;
                            }
                        }
                    }
                }
            }
            
            if (newPlayersCount > 0) {
                LOGGER.info("Saved " + newPlayersCount + " new players.");
                currentRunPlayers += newPlayersCount;
            }

            if (newAttacksCount > 0) {
                LOGGER.info("Saved " + newAttacksCount + " new attacks. Stars: " + stars + ", Destruction: " + destruction);
                currentRunAttacks += newAttacksCount;
            }
        }
    }

    private boolean storeAttackData(String warTag, String attackerTag, JSONObject attack) {
        String defenderTag = attack.optString("defenderTag", "");
        if (!attackService.attackExists(attackerTag, warTag, defenderTag)) {
            Attack atk = new Attack(attackerTag, attack.optInt("destructionPercentage", 0), attack.optInt("stars", 0),
                    attack.optInt("mapPosition"), attack.optInt("townhallLevel"), warTag, defenderTag);
            attackService.saveAttack(atk);
            return true;
        }
        return false;
    }

}
