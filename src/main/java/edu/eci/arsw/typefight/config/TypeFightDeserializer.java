package edu.eci.arsw.typefight.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.model.TypeFight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TypeFightDeserializer extends StdDeserializer<TypeFight> {

    public TypeFightDeserializer() {
        this(null);
    }



    public TypeFightDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TypeFight deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        // Obtener otros atributos de TypeFight

        // Obtener el HashMap<String, Player>
        JsonNode playersNode = node.get("players");
        HashMap<String, Player> players = new HashMap<>();
        if (playersNode != null && playersNode.isArray()) {
            for (JsonNode playerNode : playersNode) {
                String playerName = playerNode.get("name").asText();
                String playerColor = playerNode.get("color").asText();
                Integer playerHealth = playerNode.get("health").asInt();
                Integer playerPoints = playerNode.get("points").asInt();
                Boolean playerIsAlive = playerNode.get("alive").asBoolean();
                // Crear un objeto Player y agregarlo al HashMap
                Player player = new Player(playerName, playerColor, playerHealth, playerPoints, playerIsAlive); // Ajusta la creación según tu clase Player
                players.put(playerColor, player);
            }
        }

        JsonNode currentWordsNode = node.get("currentWords");
        ArrayList<String> currentWords = new ArrayList<>();

        if (currentWordsNode != null && currentWordsNode.isArray()) {
            for (JsonNode currentWord : currentWordsNode) {
                currentWords.add(currentWord.asText());
            }
        }
        JsonNode playerNamesNode = node.get("playerNames");
        ArrayList<String> playerNames = new ArrayList<>();
        if (playerNamesNode != null && playerNamesNode.isArray()) {
            for (JsonNode playerName : playerNamesNode) {
                playerNames.add(playerName.asText());
            }
        }

        JsonNode nodeGoToPlay = node.get("goToPlay");
        JsonNode nodeGameReset = node.get("gameReset");


        // Crear el objeto TypeFight con los atributos deserializados
        TypeFight typeFight = new TypeFight();
        // Establecer otros atributos

        typeFight.setPlayers(players);
        typeFight.setCurrentWords(currentWords);
        typeFight.setPlayersNames(playerNames);
        typeFight.setGoToPlay(nodeGoToPlay.asInt());
        typeFight.setGameReset(nodeGameReset.asBoolean());

        return typeFight;
    }
}