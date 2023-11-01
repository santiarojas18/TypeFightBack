package edu.eci.arsw.typefight.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Player {
    private String name;
    private String color;
    private AtomicInteger health;
    private AtomicInteger points;
    private boolean alive;

    public Player(String name, String color){
        this.name = name;
        this.color = color;
        health = new AtomicInteger(100);
        points = new AtomicInteger(0);
        alive = true;

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

    public AtomicInteger getHealth() {
        return health;
    }

    public void setHealth(AtomicInteger health) {
        this.health = health;
    }

    public AtomicInteger getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points.set(points);
    }

    public void addPoints(int points) {
        this.points.addAndGet(points);
    }

    public void decreaseHealth(int damage){
        if (health.get() - damage <= 0) {
            setAlive(false);
            health.set(0);
        } else {
            health.addAndGet(-damage);
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
