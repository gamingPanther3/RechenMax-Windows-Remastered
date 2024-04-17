/*
 * Copyright (c) 2024 by Max Lemberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.swing.*;
import java.awt.*;

/**
 * Defines a user interface for a calculator application.
 * <p>
 * This class provides methods to create buttons with custom actions and styles for the calculator UI.
 * It also contains default font sizes and button dimensions.
 */
public class RechenMaxUI {
    protected static float                      LARGE_BUTTON_FONT;
    protected static float                      SMALL_BUTTON_FONT;
    protected static Dimension                  LARGE_BUTTON;
    protected static Dimension                  SMALL_BUTTON;
    protected static float                      INCREASE_FONT_ON_HOVER_BY;
    static {
        RechenMaxUI.LARGE_BUTTON_FONT           = 19.0f;
        RechenMaxUI.SMALL_BUTTON_FONT           = 13.0f;
        RechenMaxUI.INCREASE_FONT_ON_HOVER_BY   = 7.0f;
        RechenMaxUI.LARGE_BUTTON                = new Dimension(50, 50);
        RechenMaxUI.SMALL_BUTTON                = new Dimension(30, 15);
    }

    // Label for displaying calculation inputs.
    public static JLabel calculateLabel;
    // Label for displaying calculation results.
    public static JLabel resultLabel;
    // Indicates whether to remove the previous value on input.
    public static boolean removeValue;
    // Holds the last entered number.
    public static String lastNumber;
    // Holds the last performed operation.
    public static String lastOperation;

    /**
     * Entry point of the application.
     * <p>
     * Creates and displays the calculator UI.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        final JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setSize(new Dimension(335, 510));
        jFrame.setMinimumSize(new Dimension(335, 510));
        jFrame.setTitle("RechenMax");
        jFrame.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> jFrame.setVisible(true));
    }

    /**
     * Private constructor to prevent instantiation.
     * <p>
     * Initializes the UI components and sets default values.
     */
    private RechenMaxUI() {
        removeValue = false;
        lastNumber = "";
        lastOperation = "";
        this.initialize();
    }

    /**
     * Initializes the UI components.
     * <p>
     * Creates and configures the labels for calculation inputs and results.
     */
    private void initialize() {
        calculateLabel = new JLabel("");
        resultLabel = new JLabel("0");
        resultLabel.setFont(new Font("Serif", Font.PLAIN, 45));
        calculateLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        calculateLabel.setForeground(new Color(90, 90, 90));
    }

    protected JComponent createPanel1() {
        final JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, -5, 5));
        jPanel.add(RechenMaxUI.calculateLabel, "East");
        return jPanel;
    }

    protected JComponent createPanel2() {
        final JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
        jPanel.add(RechenMaxUI.resultLabel, "East");
        return jPanel;
    }
}