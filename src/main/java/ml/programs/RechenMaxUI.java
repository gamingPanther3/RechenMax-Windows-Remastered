package ml.programs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


public class RechenMaxUI extends JComponent {

    private static final long                   serialVersionUID = 3375903438785223753L;
    public static float                         INCREASE_FONT_ON_HOVER_BY;
    protected static float                      LARGE_BUTTON_FONT;
    protected static float                      SMALL_BUTTON_FONT;
    protected static Dimension                  LARGE_BUTTON;
    protected static Dimension                  SMALL_BUTTON;
    private static CalculatorEngine             calculatorEngine;
    private boolean                             removevalue;
    private JLabel                              calculatelabel;
    private JLabel                              resultlabel;
    private String                              last_number;
    private String                              last_op;
    static {
        RechenMaxUI.INCREASE_FONT_ON_HOVER_BY   = 7.0f;
        RechenMaxUI.LARGE_BUTTON_FONT           = 19.0f;
        RechenMaxUI.SMALL_BUTTON_FONT           = 13.0f;
        RechenMaxUI.LARGE_BUTTON                = new Dimension(50, 50);
        RechenMaxUI.SMALL_BUTTON                = new Dimension(30, 15);
        RechenMaxUI.calculatorEngine            = new CalculatorEngine();
    }

    public static void main(final String[] args) {
        final JFrame jFrame = new JFrame();
        jFrame.setTitle("RechenMax");

        jFrame.getContentPane().add(new RechenMaxUI());
        jFrame.setDefaultCloseOperation(2);
        jFrame.setLocationRelativeTo(null);

        jFrame.setSize(new Dimension(335, 510));
        jFrame.setMinimumSize(new Dimension(335, 510));

        SwingUtilities.invokeLater(() -> jFrame.setVisible(true));
    }

    public RechenMaxUI() {
        this.removevalue    = false;
        this.last_number    = "";
        this.last_op        = "";
        this.initialize();
    }

    protected void calculate() {
        if (this.getCalculateText().contains("=")) {
            final String result = this.getResultText() + " " + this.getLastOp() + " " + this.getLastNumber();
            this.setCalculateText(result + " =");
            this.setResultText(calculatorEngine.calculate(result));
        } else {
            final String calc = this.getCalculateText() + this.getResultText();
            this.setCalculateText(this.getCalculateText() + this.getResultText() + " =");
            this.setResultText(calculatorEngine.calculate(calc));
        }

        final int len = this.getResultText().length();
        if (len > 17) {
            this.resultlabel.setFont(new Font("Serif", 0, 29));
        }
    }

    protected JComponent createButton(final MyOperationAction a, final Float size, final Integer style, final Boolean large) {
        final JButton jButton               = new JButton(a);
        final JPanel jPanel                  = new JPanel(new BorderLayout());

        final Border raisedBevelBorder      = BorderFactory.createMatteBorder(0, 1, 0, 1, Color.GRAY);
        final EmptyBorder emptyBorder       = new EmptyBorder(1, 1, 1, 1);
        final float size2D                  = jButton.getFont().getSize2D();

        jPanel.add(jButton, "Center");
        if (size != null) {
            jButton.setFont(jButton.getFont().deriveFont(size));
        }
        if (style != null) {
            jButton.setFont(jButton.getFont().deriveFont(style));
        }
        if (large != null) {
            jButton.setPreferredSize((large) ? RechenMaxUI.LARGE_BUTTON : RechenMaxUI.SMALL_BUTTON);
        }

        jButton.setBorderPainted(false);
        jButton.setFocusPainted(false);
        jButton.setContentAreaFilled(false);
        jButton.setFocusable(false);
        jButton.setOpaque(false);

        jPanel.setBorder(emptyBorder);
        jButton.getModel().addChangeListener(e -> {
            final ButtonModel model = (ButtonModel) e.getSource();
            if (model.isRollover()) {
                for (int x = 0; x != 10; ++x) {
                    jButton.setFont(jButton.getFont().deriveFont(size2D + 7.0f));
                    jPanel.setBorder(raisedBevelBorder);
                }
            } else {
                jButton.setFont(jButton.getFont().deriveFont(size2D));
                jPanel.setBorder(emptyBorder);
            }
        });
        final KeyStroke[] keys = a.getKeyStrokes();
        if (keys != null && keys.length > 0) {
            final String name = a.getValue("Name").toString();
            final InputMap im = jButton.getInputMap(2);
            KeyStroke[] array;
            for (int length = (array = keys).length, i = 0; i < length; ++i) {
                final KeyStroke ks = array[i];
                im.put(ks, name);
            }
            jButton.getActionMap().put(name, a);
        }
        return jPanel;
    }

    protected void initialize() {
        final InputMap inputMap = this.getInputMap(2);
        final ActionMap actionMap = this.getActionMap();
        String name;

        this.calculatelabel = new JLabel("");
        this.calculatelabel.setFont(new Font("Serif", 0, 20));
        this.calculatelabel.setForeground(new Color(90, 90, 90));

        this.resultlabel = new JLabel("0");
        this.resultlabel.setFont(new Font("Serif", 0, 45));

        this.setLayout(new BoxLayout(this, 1));
        this.add(this.createPanel1());
        this.add(this.createPanel2());
        this.add(this.createPanel3());
        this.add(this.createPanel4());

        name = "Copy";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(67, 128);
        
        inputMap.put(keyStroke, name);
        actionMap.put(name, new CopyToClipboardAction(name, keyStroke));

        name = "Paste";
        keyStroke = KeyStroke.getKeyStroke(86, 128);
        
        inputMap.put(keyStroke, name);
        actionMap.put(name, new PasteFromClipboardAction(name, keyStroke));
    }

    protected class BackspaceAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public BackspaceAction() {
            super("\u232b", KeyStroke.getKeyStroke(8, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final String resultText = getResultText();
            if (getResultText() != "Ungültige Eingabe") {
                if (!"0".equals(resultText) && !resultText.isEmpty()) {
                    setResultText(resultText.substring(0, resultText.length() - 1));
                    if ("-".equals(getResultText())) {
                        setResultText(getResultText() + "0");
                    }
                } else {
                    setResultText('0');
                }
                if (getResultText() == "") {
                    setResultText('0');
                }
            }
            final int length = getResultText().length();
            if (length <= 13) {
                resultlabel.setFont(new Font("Serif", 0, 45));
            } else if (length <= 15) {
                resultlabel.setFont(new Font("Serif", 0, 40));
            } else if (length == 16) {
                resultlabel.setFont(new Font("Serif", 0, 35));
            }
        }
    }

    protected class NumberAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;
        private final String      num;

        public NumberAction(final String op, final int key) {
            this(op, KeyStroke.getKeyStroke(key, 0));
        }

        public NumberAction(final String num, final KeyStroke... keyStrokes) {
            super(num, keyStrokes);
            this.num = num;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final int length = getResultText().length();

            if (Objects.equals(getResultText(), "Ungültige Eingabe")) {
                setRemoveValue(true);
            }
            
            if (getRemoveValue()) {
                setResultText("0");
            }
            setRemoveValue(false);

            if (getResultText().length() < 17) {
                if (getResultText().length() <= 13) {
                    resultlabel.setFont(new Font("Serif", 0, 45));
                }

                if ("0".equals(getResultText())) {
                    setResultText(this.num);
                } else if (getResultText().startsWith("-0") && !getResultText().contains(",")) {
                    setResultText("-" + getResultText().substring(1, 1) + this.num);
                } else {
                    setResultText(getResultText() + this.num);
                }
                setLastNumber(getResultText());
            }

            if (length > 17) {
                resultlabel.setFont(new Font("Serif", 0, 25));
            } else if (length >= 16) {
                resultlabel.setFont(new Font("Serif", 0, 35));
            } else if (length >= 14) {
                resultlabel.setFont(new Font("Serif", 0, 40));
            }
        }
    }

    protected class OperationAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;
        private final String      op;

        public OperationAction(final String op, final int key) {
            this(op, KeyStroke.getKeyStroke(key, 0));
        }

        public OperationAction(final String op, final KeyStroke... keyStrokes) {
            super(op, keyStrokes);
            this.op = op;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            setLastOp(this.op);

            if (getCalculateText().contains("=")) {
                setCalculateText(getResultText() + " " + this.op + " ");
            } else {
                addCalculateText(getResultText() + " " + this.op + " ");
            }

            setRemoveValue(true);
        }
    }

    protected class PasteFromClipboardAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public PasteFromClipboardAction(final String op, final int key) {
            this(op, KeyStroke.getKeyStroke(key, 0));
        }

        public PasteFromClipboardAction(final String op, final KeyStroke... keyStrokes) {
            super(op, keyStrokes);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            String data;
            try {
                data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                try {
                    if (data != "" && data != " ") {
                        if (data.length() < 18) {
                            calculatorEngine.calculate("1+" + data);
                            if (data.contains("+") || data.contains("-") || data.contains("*") || data.contains("/")) {
                                if (data.startsWith("-") && !data.contains("+") && !data.contains("*") && !data.contains("/")) {
                                    setResultText(data);
                                } else {
                                    addCalculateText(data);
                                }
                            } else if (getResultText() == "0") {
                                setResultText(data);
                            } else if (getRemoveValue()) {
                                setResultText(data);
                            } else if (getResultText().length() < 17) {
                                addResultText(data);
                            }
                        } else {
                            resultlabel.setFont(new Font("Serif", 0, 41));
                            setResultText("Ungültige Eingabe");
                        }
                    }
                } catch (Exception ex) {
                    resultlabel.setFont(new Font("Serif", 0, 41));
                    setResultText("Ungültige Eingabe");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            final int len = getResultText().length();
            if (len >= 14 && getResultText() != "Ungültige Eingabe") {
                resultlabel.setFont(new Font("Serif", 0, 40));
                if (len >= 16 && getResultText() != "Ungültige Eingabe") {
                    resultlabel.setFont(new Font("Serif", 0, 35));
                }
            }
        }
    }

    
    
    
    protected class CalculateAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public CalculateAction() {
            super("=", KeyStroke.getKeyStroke(10, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            calculate();
        }
    }

    protected class CommaAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public CommaAction() {
            super(",", KeyStroke.getKeyStroke(44, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (!getResultText().contains(",")) {
                setResultText(getResultText() + ",");
            }
        }
    }

    protected class CopyToClipboardAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public CopyToClipboardAction(final String op, final int key) {
            this(op, KeyStroke.getKeyStroke(key, 0));
        }

        public CopyToClipboardAction(final String op, final KeyStroke... keyStrokes) {
            super(op, keyStrokes);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getResultText()), null);
        }
    }

    protected class EmptyAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public EmptyAction() {
            super("CE", KeyStroke.getKeyStroke(127, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            setResultText("0");
        }
    }

    protected class EmptyAllAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public EmptyAllAction() {
            super("C", KeyStroke.getKeyStroke(27, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            setResultText("0");
            setCalculateText("");
        }
    }

    protected class EmptyClipboard extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public EmptyClipboard(final String op, final int key) {
            this(op, KeyStroke.getKeyStroke(key, 0));
        }

        public EmptyClipboard(final String op, final KeyStroke... keyStrokes) {
            super(op, keyStrokes);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
        }
    }

    protected abstract class MyOperationAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private final KeyStroke[] keyStrokes;

        public MyOperationAction(final String name, final int key) {
            this(name, KeyStroke.getKeyStroke((char) key));
        }

        public MyOperationAction(final String name, final KeyStroke... keyStrokes) {
            super(name);
            this.keyStrokes = keyStrokes;
            if (keyStrokes != null && keyStrokes.length > 0) {
                this.putValue("MnemonicKey", (int) keyStrokes[0].getKeyChar());
            }
        }

        public KeyStroke[] getKeyStrokes() {
            return this.keyStrokes;
        }
    }

    protected class NegativeAction extends MyOperationAction {

        private static final long serialVersionUID = 1L;

        public NegativeAction() {
            super("±", KeyStroke.getKeyStroke(120, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (getResultText().indexOf("-") == -1) {
                setResultText("-" + getResultText());
            } else {
                setResultText(getResultText().substring(1));
            }
        }
    }
    
    protected JComponent createPanel1() {
        final JPanel jPanel = new JPanel(new BorderLayout());

        jPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, -5, 5));
        jPanel.add(this.calculatelabel, "East");

        return jPanel;
    }

    protected JComponent createPanel2() {
        final JPanel jPanel = new JPanel(new BorderLayout());

        jPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
        jPanel.add(this.resultlabel, "East");

        return jPanel;
    }

    protected JComponent createPanel3() {
        final JPanel jPanel = new JPanel(new GridLayout(1, 3));

        jPanel.add(this.createButton(new EmptyClipboard(
                "MC", KeyStroke.getKeyStroke(76, 128)),
                RechenMaxUI.SMALL_BUTTON_FONT, false)
        );

        jPanel.add(this.createButton(new PasteFromClipboardAction(
                "MR", KeyStroke.getKeyStroke(82, 128)),
                RechenMaxUI.SMALL_BUTTON_FONT, false)
        );

        jPanel.add(this.createButton(new CopyToClipboardAction(
                "MS", KeyStroke.getKeyStroke(77, 128)),
                RechenMaxUI.SMALL_BUTTON_FONT, false)
        );

        return jPanel;
    }

    protected JComponent createPanel4() {
        final JPanel jPanel = new JPanel(new GridLayout(5, 4, 0, 0));

        jPanel.add(this.createButton(new EmptyAction()));
        jPanel.add(this.createButton(new EmptyAllAction()));
        jPanel.add(this.createButton(new BackspaceAction()));

        jPanel.add(this.createButton(new OperationAction("\u00f7", KeyStroke.getKeyStroke(111, 0), KeyStroke.getKeyStroke(55, 64))));
        jPanel.add(this.createButton(new NumberAction("7", KeyStroke.getKeyStroke(55, 0), KeyStroke.getKeyStroke(103, 0))));
        jPanel.add(this.createButton(new NumberAction("8", KeyStroke.getKeyStroke(56, 0), KeyStroke.getKeyStroke(104, 0))));
        jPanel.add(this.createButton(new NumberAction("9", KeyStroke.getKeyStroke(57, 0), KeyStroke.getKeyStroke(105, 0))));
        jPanel.add(this.createButton(new OperationAction("\u00d7", KeyStroke.getKeyStroke(106, 0), KeyStroke.getKeyStroke(521, 64))));

        jPanel.add(this.createButton(new NumberAction("4", KeyStroke.getKeyStroke(52, 0), KeyStroke.getKeyStroke(100, 0))));
        jPanel.add(this.createButton(new NumberAction("5", KeyStroke.getKeyStroke(53, 0), KeyStroke.getKeyStroke(101, 0))));
        jPanel.add(this.createButton(new NumberAction("6", KeyStroke.getKeyStroke(54, 0), KeyStroke.getKeyStroke(102, 0))));
        jPanel.add(this.createButton(new OperationAction("-", KeyStroke.getKeyStroke(109, 0), KeyStroke.getKeyStroke(45, 0))));

        jPanel.add(this.createButton(new NumberAction("1", KeyStroke.getKeyStroke(49, 0), KeyStroke.getKeyStroke(97, 0))));
        jPanel.add(this.createButton(new NumberAction("2", KeyStroke.getKeyStroke(50, 0), KeyStroke.getKeyStroke(98, 0))));
        jPanel.add(this.createButton(new NumberAction("3", KeyStroke.getKeyStroke(51, 0), KeyStroke.getKeyStroke(99, 0))));
        jPanel.add(this.createButton(new OperationAction("+", KeyStroke.getKeyStroke(107, 0), KeyStroke.getKeyStroke(521, 0))));

        jPanel.add(this.createButton(new NegativeAction()));

        jPanel.add(this.createButton(new NumberAction("0", KeyStroke.getKeyStroke(48, 0), KeyStroke.getKeyStroke(96, 0))));

        jPanel.add(this.createButton(new CommaAction()));
        jPanel.add(this.createButton(new CalculateAction()));

        return jPanel;
    }

    protected JComponent createButton(final MyOperationAction a) {
        return this.createButton(a, RechenMaxUI.LARGE_BUTTON_FONT, null, true);
    }

    protected JComponent createButton(final MyOperationAction a, final int style) {
        return this.createButton(a, null, style, null);
    }

    protected JComponent createButton(final MyOperationAction a, final boolean large) {
        return this.createButton(a, null, null, large);
    }

    protected JComponent createButton(final MyOperationAction a, final float size) {
        return this.createButton(a, size, null, null);
    }

    protected JComponent createButton(final MyOperationAction a, final float size, final boolean large) {
        return this.createButton(a, size, null, large);
    }

    public void addCalculateText(final char c) {
        this.setCalculateText(String.valueOf(this.calculatelabel.getText()) + c);
    }

    public void addCalculateText(final String s) {
        this.setCalculateText(String.valueOf(this.calculatelabel.getText()) + s);
    }

    public void addResultText(final char c) {
        this.setResultText(String.valueOf(this.getResultText()) + c);
    }

    public void addResultText(final String s) {
        this.setResultText(String.valueOf(this.getResultText()) + s);
    }

    public void setCalculateText(final char c) {
        this.calculatelabel.setText(new StringBuilder().append(c).toString());
    }

    public void setCalculateText(final String s) {
        this.calculatelabel.setText(new StringBuilder().append(s).toString());
    }

    public void setLastNumber(final String s) {
        this.last_number = s;
    }

    public void setLastOp(final String s) {
        this.last_op = s;
    }

    public void setRemoveValue(final boolean b) {
        this.removevalue = b;
    }

    public void setResultText(final char c) {
        this.setResultText(new StringBuilder().append(c).toString());
    }

    public void setResultText(final String s) {
        this.resultlabel.setText(s);
    }

    public String getCalculateText() {
        return this.calculatelabel.getText();
    }

    public String getLastNumber() {
        return this.last_number;
    }

    public String getLastOp() {
        return this.last_op;
    }

    public boolean getRemoveValue() {
        return this.removevalue;
    }

    public String getResultText() {
        return this.resultlabel.getText();
    }
}