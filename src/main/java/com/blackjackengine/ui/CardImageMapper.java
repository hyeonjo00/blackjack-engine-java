package com.blackjackengine.ui;

import com.blackjackengine.Card;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class CardImageMapper {
    private static final Logger LOGGER = Logger.getLogger(CardImageMapper.class.getName());
    private static final String CARD_RESOURCE_ROOT = "/cards/";
    private static final String BACK_FILE_NAME = "back.png";
    private static final String BACK_FILE_NAME_UPPER = "BACK.png";
    private static final int SOURCE_IMAGE_WIDTH = 192;
    private static final int SOURCE_IMAGE_HEIGHT = 272;
    private static final double CARD_WIDTH = 96;
    private static final double CARD_HEIGHT = 136;

    private static Image fallbackImage;

    private final Map<String, Image> imageCache = new HashMap<>();

    public String toFileName(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }

        return toRankToken(card.getRank()) + "_of_" + toSuitToken(card.getSuit()) + ".png";
    }

    public ImageView createCardImageView(Card card, boolean hidden) {
        ImageView imageView = new ImageView();
        configureImageView(imageView);
        loadCardImage(card, hidden, imageView::setImage);
        return imageView;
    }

    public void loadCardImage(Card card, boolean hidden, Consumer<Image> onImageLoaded) {
        if (onImageLoaded == null) {
            throw new IllegalArgumentException("Image callback cannot be null.");
        }

        String fileName = hidden ? getBackFileName() : toFileName(card);
        onImageLoaded.accept(loadImage(fileName));
    }

    private Image loadImage(String fileName) {
        Image cachedImage = imageCache.get(fileName);
        if (cachedImage != null) {
            return cachedImage;
        }

        Image image = readImageResource(fileName);
        if (image == null && !isBackFile(fileName)) {
            LOGGER.warning("Missing card face image: " + CARD_RESOURCE_ROOT + fileName);
            image = readImageResource(getBackFileName());
        }

        if (image == null) {
            LOGGER.warning("Missing fallback card image: " + CARD_RESOURCE_ROOT + getBackFileName());
            image = getFallbackImage();
        }

        imageCache.put(fileName, image);
        return image;
    }

    private Image readImageResource(String fileName) {
        for (String resourceName : getCandidateFileNames(fileName)) {
            try (InputStream inputStream = CardImageMapper.class.getResourceAsStream(CARD_RESOURCE_ROOT + resourceName)) {
                if (inputStream == null) {
                    continue;
                }

                Image image = new Image(inputStream);
                if (!image.isError() && image.getWidth() > 0.0 && image.getHeight() > 0.0) {
                    return image;
                }

                LOGGER.warning("Unable to decode card image resource: " + CARD_RESOURCE_ROOT + resourceName);
            } catch (IOException exception) {
                LOGGER.warning(
                    "Unable to read card image resource: "
                        + CARD_RESOURCE_ROOT
                        + resourceName
                        + " ("
                        + exception.getMessage()
                        + ")"
                );
            }
        }

        return null;
    }

    private String[] getCandidateFileNames(String fileName) {
        if (isBackFile(fileName)) {
            return new String[] {BACK_FILE_NAME, BACK_FILE_NAME_UPPER};
        }

        return new String[] {fileName};
    }

    private String getBackFileName() {
        return BACK_FILE_NAME;
    }

    private boolean isBackFile(String fileName) {
        return BACK_FILE_NAME.equalsIgnoreCase(fileName);
    }

    private void configureImageView(ImageView imageView) {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setSmooth(true);
        imageView.setCache(true);
    }

    private Image getFallbackImage() {
        if (fallbackImage == null) {
            fallbackImage = buildFallbackImage();
        }

        return fallbackImage;
    }

    private Image buildFallbackImage() {
        WritableImage image = new WritableImage(SOURCE_IMAGE_WIDTH, SOURCE_IMAGE_HEIGHT);
        PixelWriter writer = image.getPixelWriter();

        fillRect(writer, 0, 0, SOURCE_IMAGE_WIDTH, SOURCE_IMAGE_HEIGHT, Color.rgb(248, 244, 233));
        fillRect(writer, 0, 0, SOURCE_IMAGE_WIDTH, 10, Color.rgb(32, 38, 42));
        fillRect(writer, 0, SOURCE_IMAGE_HEIGHT - 10, SOURCE_IMAGE_WIDTH, 10, Color.rgb(32, 38, 42));
        fillRect(writer, 0, 0, 10, SOURCE_IMAGE_HEIGHT, Color.rgb(32, 38, 42));
        fillRect(writer, SOURCE_IMAGE_WIDTH - 10, 0, 10, SOURCE_IMAGE_HEIGHT, Color.rgb(32, 38, 42));

        for (int offset = 0; offset < 8; offset++) {
            drawDiagonal(writer, 26 + offset, 26, SOURCE_IMAGE_WIDTH - 26, SOURCE_IMAGE_HEIGHT - 26, Color.rgb(183, 40, 40));
            drawDiagonal(writer, SOURCE_IMAGE_WIDTH - 26 - offset, 26, 26, SOURCE_IMAGE_HEIGHT - 26, Color.rgb(183, 40, 40));
        }

        fillRect(
            writer,
            (SOURCE_IMAGE_WIDTH / 2) - 34,
            (SOURCE_IMAGE_HEIGHT / 2) - 18,
            68,
            36,
            Color.rgb(32, 38, 42, 0.92)
        );

        return image;
    }

    private void fillRect(
        PixelWriter writer,
        int startX,
        int startY,
        int width,
        int height,
        Color color
    ) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                writer.setColor(x, y, color);
            }
        }
    }

    private void drawDiagonal(
        PixelWriter writer,
        int startX,
        int startY,
        int endX,
        int endY,
        Color color
    ) {
        int deltaX = endX - startX;
        int deltaY = endY - startY;
        int steps = Math.max(Math.abs(deltaX), Math.abs(deltaY));

        for (int step = 0; step <= steps; step++) {
            int x = startX + (deltaX * step / steps);
            int y = startY + (deltaY * step / steps);
            if (x >= 0 && x < SOURCE_IMAGE_WIDTH && y >= 0 && y < SOURCE_IMAGE_HEIGHT) {
                writer.setColor(x, y, color);
            }
        }
    }

    private String toRankToken(Card.Rank rank) {
        switch (rank) {
            case TWO:
                return "2";
            case THREE:
                return "3";
            case FOUR:
                return "4";
            case FIVE:
                return "5";
            case SIX:
                return "6";
            case SEVEN:
                return "7";
            case EIGHT:
                return "8";
            case NINE:
                return "9";
            case TEN:
                return "10";
            case JACK:
                return "jack";
            case QUEEN:
                return "queen";
            case KING:
                return "king";
            case ACE:
                return "ace";
            default:
                throw new IllegalStateException("Unsupported rank: " + rank);
        }
    }

    private String toSuitToken(Card.Suit suit) {
        switch (suit) {
            case HEARTS:
                return "hearts";
            case SPADES:
                return "spades";
            case DIAMONDS:
                return "diamonds";
            case CLUBS:
                return "clubs";
            default:
                throw new IllegalStateException("Unsupported suit: " + suit);
        }
    }
}
