package edu.eci.arsw.typefight.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;



@RedisHash("Player")
public class Player implements Serializable {
    @Id
    @JsonProperty("name")
    private String name;

    @JsonProperty("color")
    private String color;

    @JsonProperty("health")
    private Integer health;

    @JsonProperty("points")
    private Integer points;

    @JsonProperty("alive")
    private boolean alive;

    public Player(String name, String color){
        this.name = name;
        this.color = color;
        health = 100;
        points = 0;
        alive = true;

    }

    public Player(String name, String color, Integer health, Integer points, boolean isAlive){
        this.name = name;
        this.color = color;
        this.health = health;
        this.points = points;
        this.alive = isAlive;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void decreaseHealth(int damage){
        if (health - damage <= 0) {
            setAlive(false);
            health = 0;
        } else {
            health += -damage;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", health=" + health +
                ", points=" + points +
                '}';
    }

}
