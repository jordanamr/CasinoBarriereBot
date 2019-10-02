package net.aquazus.casinobarrierebot.base;

import lombok.Getter;

import java.util.Objects;

/**
 * Class representing a playing card from a standard 52-card deck.
 */
public class Card {
    /**
     * Enum representing playing card suits.
     */
    public enum Suit {
        SPADES, HEARTS, DIAMONDS, CLUBS
    }

    // The min and max valid card ranks
    public static final int MIN_RANK = 1;
    public static final int MAX_RANK = 13;

    // This instance's rank and suit
    @Getter
    private int rank;
    @Getter
    private Suit suit;

    /**
     * Construct a Card with a given rank and suit.
     */
    public Card(int rank, Suit suit) {
        setRank(rank);
        setSuit(suit);
    }

    /**
     * Set the card's rank, with input validation.
     */
    public void setRank(int rank) {
        if (rank < MIN_RANK || rank > MAX_RANK)
            throw new RuntimeException(
                    String.format("Invalid rank: %d (must be between %d and %d inclusive)",
                            rank, MIN_RANK, MAX_RANK));
        this.rank = rank;
    }

    /**
     * Set the card's suit, with input validation.
     */
    public void setSuit(Suit suit) {
        if (suit == null)
            throw new RuntimeException("Suit must be non-null");
        this.suit = suit;
    }

    @Override
    public String toString() {
        return String.format("%s[rank=%d, suit=%s]",
                getClass().getSimpleName(),
                getRank(),
                getSuit().name());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card))
            return false;
        if (obj == this)
            return true;

        Card that = (Card)obj;
        return that.getRank() == getRank() && that.getSuit() == getSuit();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRank(), getSuit());
    }

    /**
     * Return an array of the available suits.
     */
    public static Suit[] getSuits() {
        return Suit.values();
    }

    public String getEmoji() {
        switch (suit) {
            case SPADES:
                switch (rank) {
                    case 1:
                        return "A";
                    case 2:
                        return "2";
                    case 3:
                        return "3";
                    case 4:
                        return "4";
                    case 5:
                        return "5";
                    case 6:
                        return "6";
                    case 7:
                        return "7";
                    case 8:
                        return "8";
                    case 9:
                        return "9";
                    case 10:
                        return "10";
                    case 11:
                        return "J";
                    case 12:
                        return "Q";
                    case 13:
                        return "K";
                }
                break;
            case HEARTS:
                switch (rank) {
                    case 1:
                        return "A";
                    case 2:
                        return "2";
                    case 3:
                        return "3";
                    case 4:
                        return "4";
                    case 5:
                        return "5";
                    case 6:
                        return "6";
                    case 7:
                        return "7";
                    case 8:
                        return "8";
                    case 9:
                        return "9";
                    case 10:
                        return "10";
                    case 11:
                        return "J";
                    case 12:
                        return "Q";
                    case 13:
                        return "K";
                }
                break;
            case DIAMONDS:
                switch (rank) {
                    case 1:
                        return "A";
                    case 2:
                        return "2";
                    case 3:
                        return "3";
                    case 4:
                        return "4";
                    case 5:
                        return "5";
                    case 6:
                        return "6";
                    case 7:
                        return "7";
                    case 8:
                        return "8";
                    case 9:
                        return "9";
                    case 10:
                        return "10";
                    case 11:
                        return "J";
                    case 12:
                        return "Q";
                    case 13:
                        return "K";
                }
                break;
            case CLUBS:
                switch (rank) {
                    case 1:
                        return "A";
                    case 2:
                        return "2";
                    case 3:
                        return "3";
                    case 4:
                        return "4";
                    case 5:
                        return "5";
                    case 6:
                        return "6";
                    case 7:
                        return "7";
                    case 8:
                        return "8";
                    case 9:
                        return "9";
                    case 10:
                        return "10";
                    case 11:
                        return "J";
                    case 12:
                        return "Q";
                    case 13:
                        return "K";
                }
                break;
        }
        return "\u274C";
    }
}