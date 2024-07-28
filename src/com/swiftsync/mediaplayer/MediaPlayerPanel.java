package com.swiftsync.mediaplayer;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.ImageProducer;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * MediaPlayerComponent is a JPanel that encapsulates an embedded VLC media player using VLCJ.
 * It provides basic media controls such as play/pause and seek.
 */
public class MediaPlayerPanel extends JPanel {

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JButton playPauseButton;
    private final JButton openFileLocationButton;
    private final JSlider positionSlider;
    private final JSlider volumeSlider;
    private final JLabel timeLabel;
    private boolean isPlaying;
    private String currentMediaPath;

    public static final Color DARK_MODE = new Color(45,45,45);
    public static final Color LIGHT_MODE = new Color(245, 245, 245);

    /**
     * Constructs a MediaPlayerComponent.
     */
    public MediaPlayerPanel() {
        setLayout(new BorderLayout());

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(DARK_MODE);

        try {
            playPauseButton = createIconButton("mediaplayer-playicon.png", "Play");
            openFileLocationButton = createIconButton("mediaplayer-foldericon.png", "Open File Location");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        positionSlider = createSlider(400);
        positionSlider.setMaximum(100); // Set the slider maximum to 100 for percentage calculation
        positionSlider.setValue(0);

        volumeSlider = createSlider(65);
        volumeSlider.setMaximum(100);
        volumeSlider.setValue(50);

        timeLabel = new JLabel("00:00:00");
        timeLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(45, 45, 45));
        buttonPanel.add(playPauseButton);
        buttonPanel.add(openFileLocationButton);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new FlowLayout());
        sliderPanel.setBackground(new Color(45, 45, 45));
        sliderPanel.add(timeLabel);
        sliderPanel.add(positionSlider);
        sliderPanel.add(volumeSlider);

        controlsPanel.add(buttonPanel);
        controlsPanel.add(sliderPanel);

        add(controlsPanel, BorderLayout.SOUTH);

        registerListeners();
    }

    /**
     * Creates a JButton with an icon and tooltip.
     *
     * @param iconFileName the name of the icon file.
     * @param tooltip the tooltip text for the button.
     * @return the created JButton.
     */
    private JButton createIconButton(String iconFileName, String tooltip) throws IOException {
        JButton button;
        InputStream iconStream = getClass().getResourceAsStream("/" + iconFileName);
        if (iconStream == null) {
            System.out.println("Couldn't find icon: " + iconFileName);
            button = new JButton(tooltip){
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(50, 30); // Adjust size as needed
                }
            };
        } else {
            Image iconImage = ImageIO.read(iconStream);
            Image scaledIconImage = iconImage.getScaledInstance(-1, 30, Image.SCALE_SMOOTH);
            Image scaledIconImage2x = iconImage.getScaledInstance(-1, 60, Image.SCALE_SMOOTH);
            MultiResolutionImage multiResImage = new BaseMultiResolutionImage(scaledIconImage, scaledIconImage2x);
            ImageIcon icon = new ImageIcon(multiResImage.getResolutionVariant(30, 30));
            button = new JButton(icon) {
                @Override
                protected void paintComponent(Graphics g) {
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setContentAreaFilled(false);
                    setBorderPainted(false);
                    super.paintComponent(g);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(icon.getIconWidth(), icon.getIconHeight());
                }
            };
        }
        button.setOpaque(false);
        button.setBackground(new Color(0,0,0,0));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setToolTipText(tooltip);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    /**
     * Creates a JSlider with custom settings.
     *
     * @return the created JSlider.
     */
    private JSlider createSlider(int prefWidth) {
        JSlider slider = new JSlider();
        slider.setBackground(new Color(45, 45, 45));
        slider.setForeground(Color.WHITE);
        slider.setPreferredSize(new Dimension(prefWidth, 30));
        return slider;
    }

    /**
     * Registers action listeners for the media control buttons and sliders.
     */
    private void registerListeners() {
        playPauseButton.addActionListener(e -> {
            try {
                togglePlayPause();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        openFileLocationButton.addActionListener(e -> openFileLocation());

        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                SwingUtilities.invokeLater(() -> updateUI(newTime));
            }

            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, uk.co.caprica.vlcj.media.MediaRef media) {
                SwingUtilities.invokeLater(() -> positionSlider.setValue(0)); // Reset slider to 0
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    mediaPlayerComponent.mediaPlayer().controls().pause();
                    InputStream playIconStream = getClass().getResourceAsStream("/mediaplayer-playicon.png");
                    Image playIconImage = null;
                    try {
                        playIconImage = ImageIO.read(playIconStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Image scaledPlayIconImage = playIconImage.getScaledInstance(-1, 30, Image.SCALE_SMOOTH);
                    Image scaledPlayIconImage2x = playIconImage.getScaledInstance(-1, 60, Image.SCALE_SMOOTH);
                    MultiResolutionImage multiResPlayImage = new BaseMultiResolutionImage(scaledPlayIconImage, scaledPlayIconImage2x);
                    playPauseButton.setIcon(new ImageIcon(multiResPlayImage.getResolutionVariant(30, 30)));
                    playPauseButton.setToolTipText("Restart");
                    isPlaying = false;
                });
            }
        });

        positionSlider.addChangeListener(e -> {
            if(positionSlider.getValueIsAdjusting()) {
                float positionValue = positionSlider.getValue() / 100.0f;
                mediaPlayerComponent.mediaPlayer().controls().setPosition(positionValue);
            }
        });

        volumeSlider.addChangeListener(e -> {
            if (!volumeSlider.getValueIsAdjusting()) {
                int volumeValue = volumeSlider.getValue();
                mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeValue);
            }
        });
    }

    /**
     * Toggles between play and pause states.
     */
    private void togglePlayPause() throws IOException {
        if (isPlaying) {
            mediaPlayerComponent.mediaPlayer().controls().pause();
            InputStream playIconStream = getClass().getResourceAsStream("/mediaplayer-playicon.png");
            Image playIconImage = ImageIO.read(playIconStream);
            Image scaledPlayIconImage = playIconImage.getScaledInstance(-1, 30, Image.SCALE_SMOOTH);
            Image scaledPlayIconImage2x = playIconImage.getScaledInstance(-1, 60, Image.SCALE_SMOOTH);
            MultiResolutionImage multiResPlayImage = new BaseMultiResolutionImage(scaledPlayIconImage, scaledPlayIconImage2x);
            playPauseButton.setIcon(new ImageIcon(multiResPlayImage.getResolutionVariant(30, 30)));
            playPauseButton.setToolTipText("Play");
        } else {
            mediaPlayerComponent.mediaPlayer().controls().play();
            InputStream pauseIconStream = getClass().getResourceAsStream("/mediaplayer-pauseicon.png");
            Image pauseIconImage = ImageIO.read(pauseIconStream);
            Image scaledPauseIconImage = pauseIconImage.getScaledInstance(-1, 30, Image.SCALE_SMOOTH);
            Image scaledPauseIconImage2x = pauseIconImage.getScaledInstance(-1, 60, Image.SCALE_SMOOTH);
            MultiResolutionImage multiResPauseImage = new BaseMultiResolutionImage(scaledPauseIconImage, scaledPauseIconImage2x);
            playPauseButton.setIcon(new ImageIcon(multiResPauseImage.getResolutionVariant(30, 30)));
            playPauseButton.setToolTipText("Pause");
        }
        isPlaying = !isPlaying;
    }

    /**
     * Updates the UI components based on the media player's current state.
     *
     * @param newTime the current playback time in milliseconds.
     */
    private void updateUI(long newTime) {
        timeLabel.setText(formatTime(newTime));
        long totalLength = mediaPlayerComponent.mediaPlayer().media().info().duration();
        if (totalLength > 0) {
            float positionValue = (newTime * 100.0f) / totalLength;
            positionSlider.setValue(Math.round(positionValue)); // Avoid interpolation issues
        }
    }

    /**
     * Formats the time from milliseconds to a "HH:mm:ss" string.
     *
     * @param milliseconds the time in milliseconds.
     * @return the formatted time string.
     */
    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Starts playing the specified media file.
     *
     * @param mediaPath the path to the media file.
     */
    public void playMedia(String mediaPath) {
        this.currentMediaPath = mediaPath;
        mediaPlayerComponent.mediaPlayer().media().startPaused(mediaPath);
    }

    /**
     * Opens the file location of the current media.
     */
    private void openFileLocation() {
        if (currentMediaPath != null) {
            File file = new File(currentMediaPath);
            if (file.exists()) {
                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Releases the media player component and associated resources.
     */
    public void release() {
        mediaPlayerComponent.release();
    }

    /**
     * Sets the maximum size for the MediaPlayerComponent.
     *
     * @param width  the maximum width.
     * @param height the maximum height.
     */
    public void setMaximumSize(int width, int height) {
        setMaximumSize(new Dimension(width, height));
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }



        JFrame frame = new JFrame("SwiftSync Media Player");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        try {
            InputStream iconStream = MediaPlayerPanel.class.getResourceAsStream("/mediaplayer-playicon.png");
            Image iconImage = ImageIO.read(iconStream);
            frame.setIconImage(iconImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaPlayerPanel mediaPlayerComponent = new MediaPlayerPanel();
        frame.add(mediaPlayerComponent);

        frame.setVisible(true);

        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        // Show the file chooser dialog and get the user's response
        int response = fileChooser.showOpenDialog(frame);

        // If the user selected a file, play it
        if (response == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            mediaPlayerComponent.playMedia(selectedFile.getAbsolutePath());
        }
    }
}
