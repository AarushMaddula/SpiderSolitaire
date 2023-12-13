package com.solitaire.solitaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class SpiderSolitaire {

    private int turn;
    private int globalId = 0;
    private ArrayList<List<Card>> piles;
    private ArrayList<Card> foundation;
    private List<List<Card>> stock;
    private ArrayList<String> moves;

    public SpiderSolitaire(String boardString) {
        moves = new ArrayList<>();

        setBoardState(boardString);
    }

    public SpiderSolitaire() {
        //init cards

        List<Card> cards = new ArrayList<>();

        char[] suits = {'h', 's'};

        for (char suit: suits) {
            for (int x = 0; x < 4; x++) {
                for (int rank = 1; rank < 14; rank++) {
                    Card card = new Card(suit, rank);
                    cards.add(card);
                }
            }
        }

        //suffles cards
        Collections.shuffle(cards);

        //makes piles

        piles = new ArrayList<>();

        for (int x = 0; x < 10; x++) {
            List<Card> stack = new ArrayList<>();
            piles.add(stack);
        }

        for (int num = 0; num < 54; num++) {
            int stackIndex = num % 10;
            List<Card> stack = piles.get(stackIndex);

            Card card = cards.get(0);

            if (num > 43) card.setVisible(true);

            card.setPosition(stackIndex, num / 10);
            stack.add(card);
            cards.remove(0);
        }

        //makes stock
        stock = new ArrayList<>();

        for (int s = 0; s < 5; s++) {
            List<Card> set = new ArrayList<>();
            for (int c = 0; c < 10; c++) {
                Card card = cards.get(0);
                set.add(card);
                cards.remove(0);
            }
            stock.add(set);
        }

        turn = 0;

        moves = new ArrayList<>();

        System.out.println(getBoardState());
    }

    public class Card {
        private char suit;
        private int rank;
        private int id;
        private boolean isVisible;
        private int pile;
        private int order;

        Card(char suit, int rank) {
            this.suit = suit;
            this.rank = rank;
            this.id = globalId;
            this.isVisible = false;
            globalId++;
        }

        public void setPosition(int pile, int order) {
            this.pile = pile;
            this.order = order;
        }

        public int getPile() {
            return pile;
        }

        public int getOrder() {
            return order;
        }

        public int getId() {
            return id;
        }

        public String getState() {
            String rankString = rank < 10 ? "0" + String.valueOf(rank) : String.valueOf(rank);
            char suitChar = isVisible ? Character.toUpperCase(suit) : suit;

            return suitChar + String.valueOf(rankString);
        }

        public int getRank() {
            return rank;
        }

        public char getSuit() {
            return suit;
        }

        public boolean isVisible() {
            return isVisible;
        }

        public void setVisible(boolean bool) {
            isVisible = bool;
        }

    }

    public void setBoardState(String boardState) {

        String[] boardStateSplit = boardState.split("\\.");
        turn = Integer.valueOf(boardStateSplit[0]);
        String[] state = boardStateSplit[1].split("\\|");

        piles = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String stackString = state[i];
            List<Card> stack = new ArrayList<>();

            for (int j = 0; j < stackString.length(); j = j + 3) {
                char suit = stackString.charAt(j);
                String rankString = stackString.charAt(j + 1) + Character.toString(stackString.charAt(j + 2));
                int rank = Integer.valueOf(rankString);

                Card card = new Card(Character.toLowerCase(suit), rank);
                if (Character.isUpperCase(suit)) {
                    card.setVisible(true);
                }

                card.setPosition(i, j / 3);

                stack.add(card);
            }
            piles.add(stack);
        }

        stock = new ArrayList<>();

        for (int i = 0; i < state.length - 11; i++) {
            String stockPile = state[i + 10];
            List<Card> set = new ArrayList<>();

            for (int j = 0; j < stockPile.length(); j = j + 3) {
                char suit = stockPile.charAt(j);
                String rankString = stockPile.charAt(j + 1) + Character.toString(stockPile.charAt(j + 2));
                int rank = Integer.parseInt(rankString);

                Card card = new Card(suit, rank);
                set.add(card);
            }

            stock.add(set);
        }

        foundation = new ArrayList<>();
        String foundationCards = state[state.length - 1];

        for (int i = 0; i < foundationCards.length(); i++) {
            char foundationCard = foundationCards.charAt(i);

            if (Character.isDigit(foundationCard)) continue;
            Card card = new Card(foundationCard, 13);

            foundation.add(card);
        }
    }

    public String getBoardState() {
        StringBuilder boardState = new StringBuilder();

        boardState.append(turn).append(".");

        for (List<Card> pile: piles) {
            for (Card card: pile) {
                boardState.append(card.getState());
            }
            boardState.append("|");
        }

        for (List<Card> stockPile: stock) {
            for (Card card: stockPile) {
                boardState.append(card.getState());
            }
            boardState.append("|");
        }

        foundation = new ArrayList<>();

        int num = 8;

        for (Card card: foundation) {
            boardState.append(card.getState().charAt(0));
            num--;
        }

        boardState.append(num);

        return boardState.toString();
    }

    public ArrayList<List<Card>> getPiles() {
        return piles;
    }

    public List<List<Card>> getStock() {
        return stock;
    }

    public ArrayList<Card> getFoundation() {
        return foundation;
    }

    public Boolean isValidCard(Card card) {
        if (!card.isVisible()) return false;
        int id = card.getId();

        boolean encountered = false;
        Card previousCard = null;

        for (List<SpiderSolitaire.Card> p: piles) {
            for (int j = 0; j < p.size(); j++) {
                Card cardC = p.get(j);

                if (cardC.getId() == id) {
                    encountered = true;
                    previousCard = card;

                } else if (encountered) {
                    char suitP = previousCard.getSuit();
                    int rankP = previousCard.getRank();

                    char suitC = cardC.getSuit();
                    int rankC = cardC.getRank();

                    if (!cardC.isVisible()) return false;
                    if (suitP != suitC) return false;
                    if (rankP - 1 != rankC) return false;

                    previousCard = cardC;
                }
            }
            if (encountered) return true;
        }
        return false;
    }

    public void makeMove(Card card, int pileIndex) {
        List<Card> toPile = piles.get(pileIndex);

        if (!piles.get(pileIndex).isEmpty()) {
            Card topCard = toPile.get(toPile.size() - 1);
            int tcRank = topCard.getRank();
            int cRank = card.getRank();

            if (tcRank - 1 != cRank) return;
        }

        List<Card> cardPile = piles.get(card.getPile());

        int index = card.getOrder();

        while (cardPile.size() > index) {
            Card chosenCard = cardPile.get(index);
            toPile.add(chosenCard);

            int p = piles.indexOf(toPile);
            int o = toPile.size() - 1;
            chosenCard.setPosition(p, o);

            cardPile.remove(chosenCard);
        }

        if (!cardPile.isEmpty()) {
            Card newTopCard = cardPile.get(cardPile.size() - 1);
            newTopCard.setVisible(true);
        }
        turn++;

        String move = getBoardState();
        System.out.println(move);
        moves.add(move);

        findFullStack();

    }

    public void findFullStack(){

        for (List<Card> pile: piles) {
            if (pile.size() < 12) continue;

            boolean startedCount = false;
            int prevRank = -1;
            char suit = '0';
            int startOrder = 0;

            for (Card card: pile) {
                if (card.getRank() == 13) {
                    suit = card.getSuit();
                    startOrder = card.getOrder();
                    startedCount = true;
                    prevRank = 13;
                } else if (startedCount) {
                    if (prevRank - 1 != card.getRank() || suit != card.getSuit()) {
                        startedCount = false;
                        suit = '0';
                        continue;
                    }

                    prevRank = card.getRank();

                    if (card.getRank() == 1) {
                        startedCount = false;

                        Card kCard = pile.get(startOrder);
                        foundation.add(kCard);

                        for (int i = 0; i < 13; i++) {
                            pile.remove(startOrder);
                        }

                        if (startOrder > 0) pile.get(startOrder - 1).setVisible(true);
                        return;

                    }
                }
            }
        }


    }
}
