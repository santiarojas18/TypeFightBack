package edu.eci.arsw.typefight;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.model.TypeFight;


import edu.eci.arsw.typefight.service.PlayerService;
import edu.eci.arsw.typefight.service.CacheService;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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

    @Autowired
    PlayerService playerService;

    @Autowired
    private RedissonClient redissonClient;

    @Lazy
    @Autowired
    CacheService cacheService;
    
    TypeFight typeFight, tempTypeFight;
    int goToPlay;
    boolean gameReset;

    boolean inGame;

    public STOMPMessagesHandler() {
        goToPlay = 0;
        gameReset = false;
        inGame = false;
    }

    @Scheduled(fixedRate = 1000)
    public void getInitialWord() {
        //System.out.println("-----Antes de agregar palabras-----: " + typeFightService.getTypeFightById(1).getCurrentWords());
        typeFight = cacheService.loadOrCreateTypeFight();
        if(typeFight.getCurrentWords().size() < typeFight.MAX_CURRENT_WORDS){
            String currentWord = typeFight.getRandomWord(); // Obtén la palabra actual desde tu modelo TypeFight
            typeFight.addRandomWord(currentWord);
        }
        cacheService.saveSharedTypeFight(typeFight);
        //System.out.println("-------Después de agregar palabras-------:" + typeFightService.getTypeFightById(1).getCurrentWords());
        msgt.convertAndSend("/topic/showCurrentWord", typeFight.getCurrentWords()); // Envía la palabra actual a todos los jugadores.
        msgt.convertAndSend("/topic/updateHealth", typeFight.getPlayers());
        if (typeFight.getGameReset()){
                tempTypeFight = cacheService.loadOrCreateTempTypeFight();
                msgt.convertAndSend("/topic/newentry", tempTypeFight.getPlayers());
                if (tempTypeFight.getAmountOfPlayers() >= 2) {
                    msgt.convertAndSend("/topic/readytoplay", true);
                }
        } else if (!typeFight.getGameReset()) {
                typeFight = cacheService.loadOrCreateTypeFight();
                msgt.convertAndSend("/topic/newentry", typeFight.getPlayers());
                if (typeFight.getAmountOfPlayers() >= 2){
                    msgt.convertAndSend("/topic/readytoplay", true);
                }
        }
        if(!inGame){
            tempTypeFight = cacheService.loadOrCreateTempTypeFight();
            typeFight = cacheService.loadOrCreateTypeFight();
            if (typeFight.getGameReset() && tempTypeFight.getGoToPlay() == tempTypeFight.getPlayers().size()) {
                typeFight = tempTypeFight;
                cacheService.saveSharedTypeFight(typeFight);
                System.out.println("Ir a jugar!!");
                msgt.convertAndSend("/topic/gotoplay", true);
            } else if (!typeFight.getGameReset() && typeFight.getGoToPlay() == typeFight.getPlayers().size()) {
                System.out.println("Ir a jugar!!");
                msgt.convertAndSend("/topic/gotoplay", true);
            }
        }


    }

    @MessageMapping("catchword")
    public void handleWordEvent(String message) throws Exception {
        if(!inGame){
            inGame = true;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> messageMap = objectMapper.readValue(message, new TypeReference<Map<String,String>>() {});

        String username = messageMap.get("username");
        String word = messageMap.get("writtenWord");
        RLock lock = redissonClient.getLock("wordLock");
        try {
            lock.lock(); 
            synchronized (typeFight) {
                typeFight = cacheService.loadOrCreateTypeFight();
                List<String> currentWords = typeFight.getCurrentWords();
                if (currentWords.contains(word)) {
                    for (Player player : typeFight.getPlayers()) {
                        String playerName = player.getName();
                        if (!playerName.equals(username)) {
                            player.decreaseHealth(word.length());
                            msgt.convertAndSend("/topic/gameOver." + player.getName(), player.getHealth());
                        } else {
                            player.addPoints(word.length());
                        }
                    }
                    typeFight.removeCurrentWord(word);
                    cacheService.saveSharedTypeFight(typeFight);
                    msgt.convertAndSend("/topic/catchword", word);
                }
            }
        } finally {
            lock.unlock(); 
        }

        msgt.convertAndSend("/topic/updateHealth", typeFight.getPlayers());

        if(typeFight.isThereAWinner() != null){
            msgt.convertAndSend("/topic/thereIsAWinner", typeFight.getSortedPlayers().get(0));
        }       
    }

    @MessageMapping("newplayer.{uniqueId}")
    public void handleNewPlayerEvent(String name, @DestinationVariable String uniqueId) {
        System.out.println("Jugador añadido:" + name);
        boolean isUsed;
        if (typeFight.getGameReset()) {
            synchronized (typeFight) {
                tempTypeFight = cacheService.loadOrCreateTempTypeFight();
                if (tempTypeFight.getPlayersNames().contains(name)) {
                    isUsed = true;
                } else {
                    isUsed = false;
                    Player player = new Player(name, tempTypeFight.getColorByPlayers());
                    tempTypeFight.addPlayer(player);
                    playerService.addPlayer(player);
                    cacheService.saveSharedTempTypeFight(tempTypeFight);
                }
            }
        } else {
            synchronized (typeFight) {
                typeFight = cacheService.loadOrCreateTypeFight();
                if (typeFight.getPlayersNames().contains(name)) {
                    isUsed = true;
                } else {
                    isUsed = false;
                    Player player = new Player(name, typeFight.getColorByPlayers());
                    playerService.addPlayer(player);
                    typeFight.addPlayer(player);
                    cacheService.saveSharedTypeFight(typeFight);
                }   
            }

        }
        System.out.println("Jugadore agregados después de volver a meter al redis: " + typeFight.getPlayers());
        System.out.println("type:" + typeFight);
        msgt.convertAndSend("/topic/newplayer." + uniqueId, isUsed);

    }

    @MessageMapping("newentry")
    public void handleNewEntry () {
        System.out.println("Entrada registrada");
        System.out.println("Jugadores del type: " + typeFight.getPlayers());
        if (typeFight.getGameReset()){
            tempTypeFight = cacheService.loadOrCreateTempTypeFight();
            msgt.convertAndSend("/topic/newentry", tempTypeFight.getPlayers());
            if (tempTypeFight.getAmountOfPlayers() >= 2) {
                msgt.convertAndSend("/topic/readytoplay", true);
            }
        } else if (!typeFight.getGameReset()) {
            typeFight = cacheService.loadOrCreateTypeFight();
            msgt.convertAndSend("/topic/newentry", typeFight.getPlayers());
            if (typeFight.getAmountOfPlayers() >= 2){
                msgt.convertAndSend("/topic/readytoplay", true);
            }
        }
    }

    @MessageMapping("gotoplay")
    public void handleGoToPlay () {
        System.out.println("Jugador quiere jugar!!");
        tempTypeFight = cacheService.loadOrCreateTempTypeFight();
        typeFight = cacheService.loadOrCreateTypeFight();

        tempTypeFight.addGoToPlay();
        typeFight.addGoToPlay();

        cacheService.saveSharedTypeFight(typeFight);
        cacheService.saveSharedTempTypeFight(tempTypeFight);
        
        System.out.println(goToPlay);
        System.out.println(gameReset);
        if (typeFight.getGameReset() && tempTypeFight.getGoToPlay() == tempTypeFight.getPlayers().size()) {
            inGame = true;
            typeFight = tempTypeFight;
            cacheService.saveSharedTypeFight(typeFight);
            System.out.println("Ir a jugar!!");
            msgt.convertAndSend("/topic/gotoplay", true);
        } else if (!typeFight.getGameReset() && typeFight.getGoToPlay() == typeFight.getPlayers().size()) {
            System.out.println("Ir a jugar!!");
            inGame = true;
            msgt.convertAndSend("/topic/gotoplay", true);
        }
    }


    @MessageMapping("showWinner")
    public void handleShowWinner () {
        typeFight = cacheService.loadOrCreateTypeFight();
        System.out.println("Ganador: " +  typeFight.getSortedPlayers().get(0));
        msgt.convertAndSend("/topic/showWinner", typeFight.getSortedPlayers());
    }

    @MessageMapping("playAgain")
    public void handlePlayAgain (String name) {
        System.out.println("Jugador: " + name + " quiere jugar de nuevo!");
        if (!typeFight.getGameReset()) {
            typeFight.setGameReset(true);
            cacheService.saveSharedTypeFight(typeFight);
            tempTypeFight = new TypeFight();
            System.out.println("Reiniciado");
        }
        Player player = new Player(name, tempTypeFight.getColorByPlayers());
        inGame = false;
        tempTypeFight.setGoToPlay(0);
        tempTypeFight.addPlayer(player);
        cacheService.saveSharedTempTypeFight(tempTypeFight);
        msgt.convertAndSend("/topic/playAgain", name);
    }

    @MessageMapping("newentrygame")
    public void handleNewEntryGame () {
        System.out.println("Entrada registrada");
        typeFight = cacheService.loadOrCreateTypeFight();
        msgt.convertAndSend("/topic/newentrygame", typeFight.getPlayers());
    }
}
