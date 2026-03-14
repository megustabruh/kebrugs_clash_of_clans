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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import per.coc.PlayerAppApplication;
import per.coc.entity.Attack;
import per.coc.entity.Player;
import per.coc.entity.War;
import per.coc.model.Clan;
import per.coc.model.Status;

public class ScheduleWarDataSaver {

    // #2R08P0L9
    public static Logger LOGGER = Logger.getLogger(ScheduleWarDataSaver.class.getName());
    public static String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjBkYzE0Nzc0LWRlNjQtNDk5Yy1iNzA2LTdhNzg2MmI4ZGZjZSIsImlhdCI6MTc3MzQ3MDU3MCwic3ViIjoiZGV2ZWxvcGVyLzkzM2UwN2IwLWMzYmItNWY2Zi1iYjRiLWZmZjYzMzI4NzZkNyIsInNjb3BlcyI6WyJjbGFzaCJdLCJsaW1pdHMiOlt7InRpZXIiOiJkZXZlbG9wZXIvc2lsdmVyIiwidHlwZSI6InRocm90dGxpbmcifSx7ImNpZHJzIjpbIjQ5LjM3LjE4MC4xNzEiXSwidHlwZSI6ImNsaWVudCJ9XX0.NqPkZk8kde_KuqzRxEiYcUika9H9Zkte_Gnb-rKlAxaaz1bE0nXiebzIqMUdRjXy-6zl_3jLd9FF6d33zbrLag";

    // public static String fetchClanWarLeagueInfo() {
    // try {
    // String endpoint =
    // "https://api.clashofclans.com/v1/clans/%232R08P0L9/currentwar/leaguegroup";
    // URL url = new URL(endpoint);
    // HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    // connection.setRequestMethod("GET");
    // connection.setRequestProperty("Authorization", "Bearer " + token);
    // connection.setRequestProperty("Content-Type", "application/json");
    // if (connection.getResponseCode() == 200) {
    // BufferedReader reader = new BufferedReader(new
    // InputStreamReader(connection.getInputStream()));
    // StringBuilder response = new StringBuilder();
    // String line;
    // while ((line = reader.readLine()) != null) {
    // response.append(line);
    // }
    // reader.close();
    // return response.toString();
    // } else {
    // LOGGER.info("Error: " + connection.getResponseCode() + " - " +
    // connection.getResponseMessage());
    // return null;
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }

    public static String fetchDataFromClashAPI(String endpoint) {
        try {
            // String endpoint =
            // "https://api.clashofclans.com/v1/clans/%232R08P0L9/currentwar/leaguegroup";
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

    public static void main(String[] args) {
        run(args);
    }

    public static void run(String[] args) {
        ApplicationContext context = SpringApplication.run(PlayerAppApplication.class, args);

        LOGGER.setLevel(Level.INFO);
        LOGGER.info("Starting clan data fetch...");

        String warType = "CWL";
        String clanWarLeagueDetails = fetchDataFromClashAPI(
                "https://api.clashofclans.com/v1/clans/%232R08P0L9/currentwar/leaguegroup");
        if (clanWarLeagueDetails == null) {
            warType = "normal";
            clanWarLeagueDetails = fetchDataFromClashAPI(
                    "https://api.clashofclans.com/v1/clans/%232R08P0L9/currentwar");
            JSONObject allJsonObject = new JSONObject(clanWarLeagueDetails);
            // storePlayers(clan, clanJson, context);
            // getWarDetailResponse(i, warTags, j, context);
        } else {
            JSONObject allJsonObject = new JSONObject(clanWarLeagueDetails);
            JSONArray clansJson = allJsonObject.getJSONArray("clans");
            Clan clan = null;

            for (int i = 0; i < clansJson.length(); i++) {
                JSONObject clanJson = clansJson.getJSONObject(i);
                if (!clanJson.getString("tag").equals("#2R08P0L9")) {
                    continue;
                }
                clan = new Clan(clanJson.getString("tag"), clanJson.getString("name"), clanJson.getInt("clanLevel"));
                storePlayers(clan, clanJson, context);
            }

            JSONArray rounds = allJsonObject.getJSONArray("rounds");
            for (int i = 0; i < rounds.length(); i++) {
                JSONObject round = rounds.getJSONObject(i);
                JSONArray warTags = round.getJSONArray("warTags");
                for (int j = 0; j < warTags.length(); j++) {
                    getWarDetailResponse(i, warTags, j, context);
                }
            }
        }

        LOGGER.info("War data saved to database!");
    }

    private static void storePlayers(Clan clan, JSONObject clanJson, ApplicationContext context) {
        JSONArray membersJson = clanJson.getJSONArray("members");
        List<Player> players = new ArrayList<>();
        for (int j = 0; j < membersJson.length(); j++) {
            JSONObject memberJson = membersJson.getJSONObject(j);
            Player player = new Player(memberJson.getString("tag"), memberJson.getString("name"),
                    memberJson.getInt("townHallLevel"), new Date(), Status.ACTIVE);
            players.add(player);
        }
        clan.setPlayers(players);

        if (clan != null && clan.getPlayers() != null) {
            storePlayerData(clan.getPlayers(), context);
        }

        LOGGER.info("Player data saved to database!");
    }

    private static void storePlayerData(List<Player> players, ApplicationContext context) {
        PlayerService playerService = context.getBean(PlayerService.class);
        for (Player player : players) {
            boolean exists = playerService.playerExists(player.getTag());
            if (!exists) {
                // System.out.println("Saving player: " + player.getName());
                playerService.savePlayer(player);
            } else {
                // System.out.println("Player already exists: " + player.getName());
            }
        }
    }

    private static void getWarDetailResponse(int i, JSONArray warTags, int j, ApplicationContext context) {
        String warTag = warTags.getString(j);
        if (warTag.equals("#0"))
            return;
        String warDetailEndpoint = "https://api.clashofclans.com/v1/clanwarleagues/wars/"
                + warTag.replace("#", "%23");
        String warDetailResponse = fetchDataFromClashAPI(warDetailEndpoint);
        if (warDetailResponse != null) {
            JSONObject warDetail = new JSONObject(warDetailResponse);
            storeWarData(i, context, warTag, warDetail);
        } else {
            LOGGER.warning("Failed to fetch war details for tag: " + warTag);
        }
    }

    private static void storeWarData(int i, ApplicationContext context, String warTag, JSONObject warDetail) {
        // Check if our clan is in this war
        String clanTag1 = warDetail.getJSONObject("clan").getString("tag");
        String clanTag2 = warDetail.getJSONObject("opponent").getString("tag");
        boolean isOurClanFirst = clanTag1.equals("#2R08P0L9");
        if (isOurClanFirst || clanTag2.equals("#2R08P0L9")) {
            JSONObject ourClan = isOurClanFirst ? warDetail.getJSONObject("clan")
                    : warDetail.getJSONObject("opponent");
            JSONObject enemyClan = isOurClanFirst ? warDetail.getJSONObject("opponent")
                    : warDetail.getJSONObject("clan");
            int stars = ourClan.optInt("stars", -1);
            int destruction = ourClan.optInt("destructionPercentage", -1);
            int attacks = ourClan.optInt("attacks", 0);
            String state = warDetail.optString("state", "");
            Date now = new Date();

            // Save war details
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
            war.setWarTag(warTag);
            WarService warService = context.getBean(WarService.class);
            if (!warService.warExists(warTag)) {
                warService.saveWar(war);
            }

            // Save attack details
            if (ourClan.has("members")) {
                JSONArray members = ourClan.getJSONArray("members");
                for (int k = 0; k < members.length(); k++) {
                    JSONObject member = members.getJSONObject(k);
                    String attackerTag = member.getString("tag");
                    if (member.has("attacks")) {
                        JSONArray memberAttacks = member.getJSONArray("attacks");
                        for (int l = 0; l < memberAttacks.length(); l++) {
                            JSONObject attack = memberAttacks.getJSONObject(l);
                            storeAttackData(context, warTag, attackerTag, attack);
                        }
                    }
                }
            }

            LOGGER.info("Round " + (i + 1) + " warTag: " + warTag + " - Stars: " + stars + ", Destruction: "
                    + destruction);
        }
    }

    private static void storeAttackData(ApplicationContext context, String warTag, String attackerTag,
            JSONObject attack) {
        Attack atk = new Attack(attackerTag, attack.optInt("destructionPercentage", 0), attack.optInt("stars", 0),
                attack.optInt("mapPosition"), attack.optInt("townhallLevel"), warTag);
        AttackService attackService = context.getBean(AttackService.class);
        if (!attackService.attackExists(attackerTag, warTag)) {
            attackService.saveAttack(atk);
        }
    }

}
