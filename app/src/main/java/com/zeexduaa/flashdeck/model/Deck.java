package com.zeexduaa.flashdeck.model;

import java.util.Objects;

/** A named group of cards, such as "Spanish verbs" or "Biology ch. 3". */
public final class Deck {

    public static final long NEW_ID = 0;

    private final long id;
    private final String name;
    private final long createdAt;

    public Deck(long id, String name, long createdAt) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A deck needs a name");
        }
        this.id = id;
        this.name = name.trim();
        this.createdAt = createdAt;
    }

    public static Deck create(String name, long now) {
        return new Deck(NEW_ID, name, now);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Deck withId(long newId) {
        return new Deck(newId, name, createdAt);
    }

    public Deck withName(String newName) {
        return new Deck(id, newName, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deck)) return false;
        Deck other = (Deck) o;
        return id == other.id && createdAt == other.createdAt && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdAt);
    }

    @Override
    public String toString() {
        return "Deck{" + id + ", " + name + "}";
    }
}
