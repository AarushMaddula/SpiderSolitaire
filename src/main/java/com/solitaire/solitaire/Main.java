package com.solitaire.solitaire;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import com.solitaire.solitaire.SpiderSolitaire.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {

    private ImageView[] imageViews = new ImageView[108];

    private Stage gameStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));

        gameStage = stage;

        SpiderSolitaire game = new SpiderSolitaire("28.h10s01h05h11H11S11S10S09H06|h03h13h08h09h02H07H06H05H04S13|s12h08s11h02h12H02H01H06|s07s05s05H11H10S09S05S04S03S02S01|h02S11S05S04S03S02S01|h13h13S02H01|h01s08s07H13H12H11H10H09H01|h05s07h03h06H07S06H05S13S12S11S10S09||h03s03S13H04|s12h10s02s08s06h03s06h12h09s04|h09h07s08h04s12s03s10s08h07s04|h08s07s10h08s13h12h04s01s06s09|8");

        Group root = displayCards(game, stage);
        Scene scene = new Scene(root, 600, 800);

        scene.setFill(Color.DARKGREEN);

        stage.setTitle("Spider Solitaire");
        stage.setWidth(940);
        stage.setHeight(600);
        stage.setResizable(true);

        Image icon = new Image("/unnamed.png");
        stage.getIcons().add(icon);

        stage.setScene(scene);
        stage.show();

    }

    private double startX, startY;

    public Group displayCards(SpiderSolitaire game, Stage stage) {
        //display piles
        ArrayList<List<Card>> piles = game.getPiles();
        Group group = new Group();

        int x = 50;
        int y = 200;

        for (List<Card> pile: piles) {
            for (int i = 0; i < pile.size(); i++) {
                Card card = pile.get(i);

                Image image;

                if (card.isVisible()) {
                    image = getCardImage(card);
                } else {
                    image = new Image(getClass().getResourceAsStream("/cards/back.png"));
                }

                ImageView imageView = new ImageView(image);

                int ivPosX = x;
                int ivPosY = y;

                imageView.setTranslateX(x);
                imageView.setTranslateY(y);

                imageView.setPreserveRatio(true);
                imageView.setFitWidth(75);

                imageView.setSmooth(true);
                group.getChildren().add(imageView);
                imageViews[card.getId()] = imageView;

                imageView.setOnMousePressed(e-> {
                    startX = e.getSceneX() - imageView.getTranslateX();
                    startY = e.getSceneY() - imageView.getTranslateY();

                });

                imageView.setOnMouseDragged(e -> {

                    if (!game.isValidCard(card)) return;

                    imageView.setTranslateX(e.getSceneX() - startX);
                    imageView.setTranslateY(e.getSceneY() - startY);

                    imageView.toFront();

                    boolean encounter = false;
                    int offsetY = 30;

                    List<Card> p = piles.get(card.getPile());

                    for (int j = 0; j < p.size(); j++) {
                        Card cardChosen = p.get(j);

                        if (cardChosen.getId() == card.getId()) {
                            encounter = true;
                        } else if (encounter) {
                            ImageView iv = imageViews[cardChosen.getId()];

                            iv.toFront();
                            iv.setTranslateX(e.getSceneX() - startX);
                            iv.setTranslateY(e.getSceneY() - startY + offsetY);
                            offsetY += 30;
                        }
                    }

                });

                imageView.setOnMouseReleased(e -> {
                    if (!game.isValidCard(card)) return;

                    boolean valid = true;

                    double xPos = e.getSceneX();
                    double yPos = e.getSceneY();

                    int pileStack = (int) Math.floor((xPos + 35) / 85);
                    double pileRelative = (xPos + 35) % 85;

                    if (pileStack > 10 || pileStack <= 0) valid = false;
                    if (pileRelative > 75) valid = false;

                    if (pileStack <= 10 && pileStack > 0) {

                        int numCards = piles.get(pileStack - 1).size();
                        int upperBound = 200 + (30 * (numCards - 1));
                        int lowerBound = upperBound + 115;

                        if (yPos < upperBound || yPos > lowerBound) valid = false;

                    }

                    if (valid) {
                        game.makeMove(card, pileStack - 1);

                        Group root = displayCards(game, stage);
                        Scene scene = new Scene(root, 600, 800);
                        scene.setFill(Color.DARKGREEN);

                        gameStage.setScene(scene);

                        return;
                    }

                    imageView.setTranslateX(ivPosX);
                    imageView.setTranslateY(ivPosY);

                    Boolean encounter = false;
                    int offsetY = 30;

                    List<Card> p = piles.get(card.getPile());

                    for (Card card1 : p) {
                        if (card1.getId() == card.getId()) {
                            encounter = true;
                        } else if (encounter) {
                            ImageView iv = imageViews[card1.getId()];
                            iv.setTranslateX(ivPosX);
                            iv.setTranslateY(ivPosY + offsetY);
                            offsetY += 30;
                        }
                    }
                });

                y += 30;
            }
            y = 200;
            x += 85;
        }

        //display stacks

        List<List<Card>> stock = game.getStock();

        int x2 = 50;

        for (List<Card> stockPile: stock) {
            Image image = new Image(getClass().getResourceAsStream("/cards/back.png"));

            ImageView imageView = new ImageView(image);
            imageView.setX(x2);
            imageView.setY(50);

            imageView.setPreserveRatio(true);
            imageView.setFitWidth(75);

            imageView.setOnMousePressed(e -> {
                List<Card> stockDeck = game.getStock().get(0);
                int np = 0;
                for (List<Card> pile: piles) {
                    Card c = stockDeck.get(0);
                    c.setPosition(np, pile.size());
                    c.setVisible(true);
                    pile.add(c);
                    stockDeck.remove(0);
                    np++;
                }

                game.getStock().remove(0);

                Group root = displayCards(game, stage);
                Scene scene = new Scene(root, 600, 800);
                scene.setFill(Color.DARKGREEN);

                gameStage.setScene(scene);
            });

            imageView.setSmooth(true);
            group.getChildren().add(imageView);

            x2 += 20;
        }

        int xP = 220;

        for (Card card : game.getFoundation()) {
            Image image = getCardImage(card);

            ImageView imageView = new ImageView(image);

            imageView.setX(xP);
            imageView.setY(50);

            imageView.setPreserveRatio(true);
            imageView.setFitWidth(75);

            imageView.setSmooth(true);
            group.getChildren().add(imageView);
            imageViews[card.getId()] = imageView;

            xP += 85;
        }

        return group;
    }

    public static void main(String[] args) {
        launch();
    }

    public Image getCardImage(Card card) {
        char suit = card.getState().charAt(0);
        int rank = Integer.parseInt(card.getState().substring(1, 3));

        String rankString;

        switch (rank) {
            case 1:
                rankString = "A";
                break;
            case 11:
                rankString = "J";
                break;
            case 12:
                rankString = "Q";
                break;
            case 13:
                rankString = "K";
                break;
            default:
                rankString = String.valueOf(rank);
                break;
        }

        String suitUpper = Character.toString(Character.toUpperCase(suit));

        String name = rankString + suitUpper;
        String path = "/cards/" + name + ".png";

        return new Image(getClass().getResourceAsStream(path));
    }

}
