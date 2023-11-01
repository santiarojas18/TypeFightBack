package edu.eci.arsw.typefight.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TypeFight {
    private Player winner;
    private HashMap<String, Player> players;
    private ArrayList<String> words;
    private String[] colors;
    private ArrayList<String> currentWords = new ArrayList<>();
    public static final int MAX_CURRENT_WORDS = 5; // Máximo de palabras actuales

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
        players = new HashMap<>();
        colors = new String[] {"Rojo", "Amarillo", "Azul", "Verde", "Naranja"};

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
        Random random = new Random();
        int randomIndex = random.nextInt(words.size());
        return words.get(randomIndex);
    }

    public void deleteWord(String word){
        words.remove(word);
    }

    public void addPlayer(Player player){
        players.putIfAbsent(player.getColor(), player);
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
        playerList.sort(Comparator.comparing(Player::getPoints, (points1, points2) -> points2.get() - points1.get())
                .thenComparing(Player::getHealth, (life1, life2) -> life2.get() - life1.get())
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

    public List<String> getCurrentWords() {
        return currentWords;
    }

    public Collection<Player> getPlayers () {
        return players.values();
    }

    public void removeCurrentWord(String word){
        currentWords.remove(word);
    }
}
