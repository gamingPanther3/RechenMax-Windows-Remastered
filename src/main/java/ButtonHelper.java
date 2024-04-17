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

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serial;

public class ButtonHelper {
    /**
     * Abstract class defining button actions.
     * <p>
     * This class extends AbstractAction and provides a base for creating custom button actions.
     */
    protected abstract static class ButtonAction extends AbstractAction {
        // Serial version UID for serialization.
        @Serial
        private static final long serialVersionUID = 1L;
        // Array of keystrokes associated with the action.
        private final KeyStroke[] keyStrokes;

        /**
         * Constructs a ButtonAction with the specified name and key.
         *
         * @param name The name of the action.
         * @param key  The key code associated with the action.
         */
        public ButtonAction(final RechenMaxUI this$0, final String name, final int key) {
            this(name, new KeyStroke[] { KeyStroke.getKeyStroke((char) key) });
        }

        /**
         * Constructs a ButtonAction with the specified name and keystrokes.
         *
         * @param name       The name of the action.
         * @param keyStrokes The array of keystrokes associated with the action.
         */
        public ButtonAction(final String name, final KeyStroke... keyStrokes) {
            super(name);
            this.keyStrokes = keyStrokes;
            if (keyStrokes != null && keyStrokes.length > 0) {
                this.putValue("MnemonicKey", (int) keyStrokes[0].getKeyChar());
            }
        }

        /**
         * Gets the array of keystrokes associated with the action.
         *
         * @return The array of keystrokes.
         */
        public KeyStroke[] getKeyStrokes() {
            return this.keyStrokes;
        }
    }

    protected JComponent createButton(final ButtonAction action) {
        return this.createButton(action, RechenMaxUI.LARGE_BUTTON_FONT, null, true);
    }

    protected JComponent createButton(final ButtonAction action, final boolean large) {
        return this.createButton(action, null, null, large);
    }

    protected JComponent createButton(final ButtonAction action, final float size) {
        return this.createButton(action, size, null, null);
    }

    protected JComponent createButton(final ButtonAction action, final float size, final boolean large) {
        return this.createButton(action, size, null, large);
    }

    protected JComponent createButton(final ButtonAction action, final int style) {
        return this.createButton(action, null, style, null);
    }

    /**
     * Creates action button with the specified parameters.
     * <p>
     * This method creates an action JButton with the provided action and styling parameters,
     * and encapsulates it within an action JPanel for layout purposes.
     * It also adds mouse hover effects and keyboard shortcuts to the button.
     *
     * @param action The action associated with the button.
     * @param size   The font size of the button text. Can be {@code null}.
     * @param style  The font style of the button text. Can be {@code null}.
     * @param large  Specifies whether the button is large or small. Can be {@code null}.
     * @return The JPanel containing the button.
     */
    protected JComponent createButton(final ButtonAction action, @Nullable final Float size, @Nullable final Integer style, @Nullable final Boolean large) {
        final JButton jButton = new JButton(action);
        final JPanel jPanel = new JPanel(new BorderLayout());

        jPanel.add(jButton, BorderLayout.CENTER);

        if (size != null)
            jButton.setFont(jButton.getFont().deriveFont(size));
        if (style != null)
            jButton.setFont(jButton.getFont().deriveFont(style));

        jButton.setPreferredSize((large != null && large) ? RechenMaxUI.LARGE_BUTTON : RechenMaxUI.SMALL_BUTTON);

        jButton.setBorderPainted(false);
        jButton.setFocusPainted(false);
        jButton.setContentAreaFilled(false);
        jButton.setFocusable(false);
        jButton.setOpaque(false);

        final float s = jButton.getFont().getSize2D();
        final Border raisedBevelBorder = BorderFactory.createMatteBorder(0, 1, 0, 1, Color.GRAY);
        final EmptyBorder emptyBorder = new EmptyBorder(1, 1, 1, 1);
        jPanel.setBorder(emptyBorder);

        jButton.getModel().addChangeListener(e -> {
            final ButtonModel model = (ButtonModel) e.getSource();
            if (model.isRollover()) {
                for (int x = 0; x != 10; ++x) {
                    jButton.setFont(jButton.getFont().deriveFont(s + RechenMaxUI.INCREASE_FONT_ON_HOVER_BY));
                    jPanel.setBorder(raisedBevelBorder);
                }
            } else {
                jButton.setFont(jButton.getFont().deriveFont(s));
                jPanel.setBorder(emptyBorder);
            }
        });

        final KeyStroke[] keys = action.getKeyStrokes();
        if (keys != null && keys.length > 0) {
            final String name = action.getValue("Name").toString();
            final InputMap im = jButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            KeyStroke[] array;
            for (int length = (array = keys).length, i = 0; i < length; ++i) {
                final KeyStroke ks = array[i];
                im.put(ks, name);
            }
            jButton.getActionMap().put(name, action);
        }
        return jPanel;
    }
}
