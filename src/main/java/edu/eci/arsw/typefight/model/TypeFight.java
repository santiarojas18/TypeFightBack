package edu.eci.arsw.typefight.model;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@RedisHash("TypeFight")
public class TypeFight implements Serializable{
    @Id
    @JsonProperty("id")
    private final Integer id = 1;

    @JsonProperty("winner")
    private Player winner;

    @JsonProperty("players")
    private HashMap<String, Player> players = new HashMap<>();

    @JsonProperty("words")
    private ArrayList<String> words;

    @JsonProperty("colors")
    private String[] colors;

    @JsonProperty("currentWords")
    private ArrayList<String> currentWords = new ArrayList<>();

    @JsonProperty("playerNames")
    private ArrayList<String> playersNames = new ArrayList<>();

    @JsonProperty("MAX_CURRENT_WORDS")
    public static final int MAX_CURRENT_WORDS = 5; // Máximo de palabras actuales

    @JsonProperty("goToPlay")
    private int goToPlay;

    @JsonProperty("gameReset")
    private boolean gameReset  = false;


    @JsonIgnore
    private final SecureRandom random;

    public TypeFight(){
        words = new ArrayList<>(Arrays.asList("Abrir", "Búsqueda", "Cautivar", "Difuso", "Esencia", "Fabuloso", "Galaxia", "Habilidad", "Inquietud", "Júbilo",
                "Kilómetro", "Luminoso", "Mariposa", "Navegante", "Ocasión", "Palabra", "Química", "Razonar", "Silencio", "Tardanza",
                "Unicornio", "Vehículo", "Wéstern", "Xenofobia", "Yacimiento", "Zigzag", "Amarillo", "Búho", "Cuaderno", "Dama",
                "Elegancia", "Fuego", "Gorrión", "Hombre", "Isla", "Jardín", "Kiosco", "Lámpara", "Mañana", "Nube",
                "Otoño", "Pájaro", "Querido", "Río", "Sol", "Tren", "Uva", "Viento", "Xilófono", "Yate",
                "Zanahoria", "Aluminio", "Barco", "Cielo", "Dientes", "Escritura", "Fábrica", "Galleta", "Hielo", "Invierno",
                "Jirafa", "Koala", "Lápiz", "Manzana", "Noche", "Orquídea", "Pintura", "Queso", "Ratón", "Silla",
                "Té", "Uña", "Vaso", "Whisky", "Xilografía", "Yoyo", "Zoológico", "Alabanza", "Beso", "Caramelo",
                "Dibujo", "Estrella", "Flauta", "Guitarra", "Hada", "Iglesia", "Juguete", "Kilogramo", "Lobo", "Mar",
                "Nido", "Océano", "Pantalón", "Quirófano", "Reloj", "Sapo", "Trenza", "Unicornio", "Vela", "Zapato"));
        //players = new HashMap<>();
        colors = new String[] {"Rojo", "Amarillo", "Azul", "Verde", "Naranja"};
        this.random = new SecureRandom();

        //Mock
        //Player juan = new Player("Juan", "azul");
        //juan.decreaseHealth(12);
        //players.put("azul",juan);
        //Player santiago = new Player("santiago", "verde");
        //santiago.decreaseHealth(10);
        //players.put("verde",santiago);
        //Player daniel = new Player("daniel", "rojo");
        //daniel.decreaseHealth(14);
        //players.put("rojo",daniel);
    }

    public String getRandomWord(){
        int randomIndex = random.nextInt(words.size());
        return words.get(randomIndex);
    }

    public void deleteWord(String word){
        words.remove(word);
    }

    public void addPlayer(Player player){
        players.putIfAbsent(player.getColor(), player);
        playersNames.add(player.getName());
    }

    public void addPointToPlayer(String color, String word) {
        players.get(color).addPoints(word.length());
    }

    public String getColorByPlayers () {
        return colors[players.keySet().size()];
    }

    public void doDamage(String color, String word) {
        players.get(color).decreaseHealth(word.length());
    }


    public Player isThereAWinner() {
        int alive = 0;
        for (Player player : players.values()){
            if (player.isAlive()){
                alive++;
                winner = player;
            }
        }
        if (alive != 1){
            winner = null;
        }
        return winner;
    }

    public int getAmountOfPlayers () {
        return players.size();
    }

    public List<Player> getSortedPlayers() {
        List<Player> playerList = new ArrayList<>(players.values());
        playerList.sort(Comparator.comparing(Player::getPoints, (points1, points2) -> points2 - points1)
                .thenComparing(Player::getHealth, (life1, life2) -> life2 - life1)
        );
        return playerList;
    }

    @Override
    public String toString() {
        return "TypeFight{" +
                "winner=" + winner +
                ", players=" + players +
                '}';
    }

    public void addRandomWord(String word) {
        if(currentWords.size() < MAX_CURRENT_WORDS && !currentWords.contains(word)){
            currentWords.add(word);
        }
    }

    public ArrayList<String> getPlayersNames() {
        return playersNames;
    }

    public List<String> getCurrentWords() {
        return currentWords;
    }

    public Collection<Player> getPlayers () {
        return players.values();
    }

    public void removeCurrentWord(String word){
        currentWords.remove(word);
    }

    public void setPlayers(HashMap<String, Player> players) {
        this.players = players;
    }

    public int getGoToPlay(){
        return goToPlay;
    }

    public void setGoToPlay(int newGoToPlay){
        this.goToPlay=  newGoToPlay;
    }

    public void addGoToPlay(){
        this.goToPlay++;
    }

    public boolean getGameReset(){
        return gameReset;
    }

    public void setGameReset(boolean gameReset){
        this.gameReset = gameReset;
    }

    public void  setCurrentWords(ArrayList<String> currentWords){
        this.currentWords = currentWords;
    }

    public void setPlayersNames(ArrayList<String> playerNames){
        this.playersNames = playerNames;
    }

}
