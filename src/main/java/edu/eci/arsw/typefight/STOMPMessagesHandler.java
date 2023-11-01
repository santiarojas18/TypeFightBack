package edu.eci.arsw.typefight;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.model.TypeFight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Controller
public class STOMPMessagesHandler {

    @Autowired
    SimpMessagingTemplate msgt;
    TypeFight typeFight, tempTypeFight;
    int goToPlay;
    boolean gameReset;

    public STOMPMessagesHandler() {
        typeFight = new TypeFight();
        goToPlay = 0;
        gameReset = false;
    }

    @Scheduled(fixedRate = 1000)
    public void getInitialWord() {
        if(typeFight.getCurrentWords().size() < typeFight.MAX_CURRENT_WORDS){
            String currentWord = typeFight.getRandomWord(); // Obtén la palabra actual desde tu modelo TypeFight
            typeFight.addRandomWord(currentWord);
        }
        msgt.convertAndSend("/topic/showCurrentWord", typeFight.getCurrentWords()); // Envía la palabra actual a todos los jugadores.
    }

    @MessageMapping("catchword")
    public void handleWordEvent(String message) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> messageMap = objectMapper.readValue(message, new TypeReference<Map<String,String>>() {});

        String username = messageMap.get("username");
        String word = messageMap.get("writtenWord");
        synchronized (typeFight){
            List<String> currentWords = typeFight.getCurrentWords();
            if (currentWords.contains(word)) {
                for (Player player : typeFight.getPlayers()) {
                    String playerName = player.getName();
                    if (!playerName.equals(username)) {
                        player.decreaseHealth(word.length());
                        msgt.convertAndSend("/topic/updateHealth." + playerName, player.getHealth());
                    } else {
                        player.addPoints(word.length());
                    }
                }
                typeFight.removeCurrentWord(word);
                msgt.convertAndSend("/topic/catchword", word);
            }
        }

        if(typeFight.isThereAWinner() != null){
            msgt.convertAndSend("/topic/thereIsAWinner", typeFight.getSortedPlayers().get(0));
        }       
    }

    @MessageMapping("newplayer")
    public void handleNewPlayerEvent(String name) {
        System.out.println("Jugador añadido:" + name);
        if (gameReset) {
            Player player = new Player(name, tempTypeFight.getColorByPlayers());
            tempTypeFight.addPlayer(player);
        } else {
            Player player = new Player(name, typeFight.getColorByPlayers());
            typeFight.addPlayer(player);
        }
        msgt.convertAndSend("/topic/newplayer", name);
    }

    @MessageMapping("newentry")
    public void handleNewEntry () {
        System.out.println("Entrada registrada");
        if (gameReset){
            msgt.convertAndSend("/topic/newentry", tempTypeFight.getPlayers());
            if (tempTypeFight.getAmountOfPlayers() >= 2) {
                msgt.convertAndSend("/topic/readytoplay", true);
            }
        } else if (!gameReset) {
            msgt.convertAndSend("/topic/newentry", typeFight.getPlayers());
            if (typeFight.getAmountOfPlayers() >= 2){
                msgt.convertAndSend("/topic/readytoplay", true);
            }
        }
    }

    @MessageMapping("gotoplay")
    public void handleGoToPlay () {
        System.out.println("Jugador quiere jugar!!");
        goToPlay++;
        System.out.println(goToPlay);
        System.out.println(gameReset);
        if (gameReset && goToPlay == tempTypeFight.getPlayers().size()) {
            typeFight = tempTypeFight;
            gameReset = false;
            System.out.println("Ir a jugar!!");
            msgt.convertAndSend("/topic/gotoplay", true);
            goToPlay = 0;

        } else if (!gameReset && goToPlay == typeFight.getPlayers().size()) {
            System.out.println("Ir a jugar!!");
            msgt.convertAndSend("/topic/gotoplay", true);
            goToPlay = 0;
        }
    }

    @MessageMapping("showWinner")
    public void handleShowWinner () {
        System.out.println("Ganador: " +  typeFight.getSortedPlayers().get(0));
        msgt.convertAndSend("/topic/showWinner", typeFight.getSortedPlayers());
    }

    @MessageMapping("playAgain")
    public void handlePlayAgain (String name) {
        System.out.println("Jugador: " + name + " quiere jugar de nuevo!");
        if (!gameReset) {
            gameReset = true;
            tempTypeFight = new TypeFight();
            System.out.println("Reiniciado");
        }
        Player player = new Player(name, tempTypeFight.getColorByPlayers());
        tempTypeFight.addPlayer(player);
        msgt.convertAndSend("/topic/playAgain", name);
    }

    @MessageMapping("newentrygame")
    public void handleNewEntryGame () {
        System.out.println("Entrada registrada");
        msgt.convertAndSend("/topic/newentrygame", typeFight.getPlayers());
    }
}
