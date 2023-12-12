package edu.eci.arsw.typefight;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.model.TypeFight;


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


    private final SimpMessagingTemplate msgt;


    private final RedissonClient redissonClient;

    private final CacheService cacheService;
    
    TypeFight typeFight;
    TypeFight tempTypeFight;
    int goToPlay;
    boolean gameReset;

    boolean inGame;

    private static final String NEW_ENTRY = "/topic/newentry";
    private static final String READY_TO_PLAY = "/topic/readytoplay";
    private static final String GO_TO_PLAY = "/topic/gotoplay";

    @Autowired
    public STOMPMessagesHandler(SimpMessagingTemplate msgt,
                                RedissonClient redissonClient,
                                @Lazy CacheService cacheService) {

        this.msgt = msgt;
        this.redissonClient = redissonClient;
        this.cacheService = cacheService;

        goToPlay = 0;
        gameReset = false;
        inGame = false;
    }

    @Scheduled(fixedRate = 1000)
    public void getInitialWord() {
        typeFight = cacheService.loadOrCreateTypeFight();
        if(typeFight.getCurrentWords().size() < TypeFight.MAX_CURRENT_WORDS){
            String currentWord = typeFight.getRandomWord(); // Obtén la palabra actual desde tu modelo TypeFight
            typeFight.addRandomWord(currentWord);
        }
        cacheService.saveSharedTypeFight(typeFight);
        msgt.convertAndSend("/topic/showCurrentWord", typeFight.getCurrentWords()); // Envía la palabra actual a todos los jugadores.
        msgt.convertAndSend("/topic/updateHealth", typeFight.getPlayers());
        if (typeFight.getGameReset()){
                tempTypeFight = cacheService.loadOrCreateTempTypeFight();
                msgt.convertAndSend(NEW_ENTRY, tempTypeFight.getPlayers());
                if (tempTypeFight.getAmountOfPlayers() >= 2) {
                    msgt.convertAndSend(READY_TO_PLAY, true);
                }
        } else if (!typeFight.getGameReset()) {
                typeFight = cacheService.loadOrCreateTypeFight();
                msgt.convertAndSend(NEW_ENTRY, typeFight.getPlayers());
                if (typeFight.getAmountOfPlayers() >= 2){
                    msgt.convertAndSend(READY_TO_PLAY, true);
                }
        }
        for (Player player : typeFight.getPlayers()) {
            msgt.convertAndSend("/topic/gameOver." + player.getName(), player.getHealth());
        }
        if(typeFight.isThereAWinner() != null){
            msgt.convertAndSend("/topic/thereIsAWinner", typeFight.getSortedPlayers().get(0));
        }       
        
        tempTypeFight = cacheService.loadOrCreateTempTypeFight();
        typeFight = cacheService.loadOrCreateTypeFight();
        if (typeFight.getGameReset() && tempTypeFight.getGoToPlay() == tempTypeFight.getPlayers().size()) {
            typeFight = tempTypeFight;
            cacheService.saveSharedTypeFight(typeFight);
            msgt.convertAndSend(GO_TO_PLAY, true);
        } else if (!typeFight.getGameReset() && typeFight.getGoToPlay() == typeFight.getPlayers().size()) {
            msgt.convertAndSend(GO_TO_PLAY, true);
        }
    }

    @MessageMapping("catchword")
    public void handleWordEvent(String message) throws Exception {
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
                    typeFight.addPlayer(player);
                    cacheService.saveSharedTypeFight(typeFight);
                }   
            }

        }
        msgt.convertAndSend("/topic/newplayer." + uniqueId, isUsed);

    }

    @MessageMapping("newentry")
    public void handleNewEntry () {
        if (typeFight.getGameReset()){
            tempTypeFight = cacheService.loadOrCreateTempTypeFight();
            msgt.convertAndSend(NEW_ENTRY, tempTypeFight.getPlayers());
            if (tempTypeFight.getAmountOfPlayers() >= 2) {
                msgt.convertAndSend(READY_TO_PLAY, true);
            }
        } else if (!typeFight.getGameReset()) {
            typeFight = cacheService.loadOrCreateTypeFight();
            msgt.convertAndSend(NEW_ENTRY, typeFight.getPlayers());
            if (typeFight.getAmountOfPlayers() >= 2){
                msgt.convertAndSend(READY_TO_PLAY, true);
            }
        }
    }

    @MessageMapping("gotoplay")
    public void handleGoToPlay () {
        tempTypeFight = cacheService.loadOrCreateTempTypeFight();
        typeFight = cacheService.loadOrCreateTypeFight();

        tempTypeFight.addGoToPlay();
        typeFight.addGoToPlay();

        cacheService.saveSharedTypeFight(typeFight);
        cacheService.saveSharedTempTypeFight(tempTypeFight);

        if (typeFight.getGameReset() && tempTypeFight.getGoToPlay() == tempTypeFight.getPlayers().size()) {
            typeFight = tempTypeFight;
            cacheService.saveSharedTypeFight(typeFight);
            msgt.convertAndSend(GO_TO_PLAY, true);
        } else if (!typeFight.getGameReset() && typeFight.getGoToPlay() == typeFight.getPlayers().size()) {
            msgt.convertAndSend(GO_TO_PLAY, true);
        }
    }


    @MessageMapping("showWinner")
    public void handleShowWinner () {
        typeFight = cacheService.loadOrCreateTypeFight();
        msgt.convertAndSend("/topic/showWinner", typeFight.getSortedPlayers());
    }

    @MessageMapping("playAgain")
    public void handlePlayAgain (String name) {
        if (!typeFight.getGameReset()) {
            typeFight.setGameReset(true);
            cacheService.saveSharedTypeFight(typeFight);
            tempTypeFight = new TypeFight();
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
        typeFight = cacheService.loadOrCreateTypeFight();
        msgt.convertAndSend("/topic/newentrygame", typeFight.getPlayers());
    }
}
