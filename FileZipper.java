import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class FileZipper extends JFrame {

    public FileZipper() {
        setTitle("File Zipper - Huffman Encoding");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new HuffmanPanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileZipper::new);
    }
}

class HuffmanNode {
    char ch;
    int freq;
    HuffmanNode left, right;

    public HuffmanNode(char ch, int freq) {
        this.ch = ch;
        this.freq = freq;
    }

    public HuffmanNode(int freq, HuffmanNode left, HuffmanNode right) {
        this.ch = '\0';
        this.freq = freq;
        this.left = left;
        this.right = right;
    }
}

class HuffmanPanel extends JPanel {
    private final int SCREEN_WIDTH = 750;
    private final int SCREEN_HEIGHT = 550;

    private JTextArea inputArea;
    private JTextArea encodedArea;
    private Map<Character, String> huffmanCodes = new HashMap<>();
    private String originalText = "";
    private String encodedText = "";
    private int originalBits = 0;
    private int compressedBits = 0;

    public HuffmanPanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(28, 30, 34));
        setLayout(null);

        JLabel titleLabel = new JLabel("File Zipper (Huffman Encoding & Compression)");
        titleLabel.setBounds(145, 15, 500, 30);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel);

        JLabel inputLabel = new JLabel("Enter Text / File Content:");
        inputLabel.setBounds(40, 55, 250, 25);
        inputLabel.setForeground(Color.lightGray);
        inputLabel.setFont(new Font("Arial", Font.BOLD, 13));
        add(inputLabel);

        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBounds(40, 80, 670, 85);
        add(inputScroll);

        JButton compressButton = new JButton("Compress (Huffman Encode)");
        compressButton.setBounds(250, 178, 250, 35);
        compressButton.setBackground(new Color(46, 139, 87));
        compressButton.setForeground(Color.white);
        compressButton.setFont(new Font("Arial", Font.BOLD, 14));
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compressData();
            }
        });
        add(compressButton);

        JLabel encodedLabel = new JLabel("Encoded Binary Stream:");
        encodedLabel.setBounds(40, 225, 250, 25);
        encodedLabel.setForeground(Color.cyan);
        encodedLabel.setFont(new Font("Arial", Font.BOLD, 13));
        add(encodedLabel);

        encodedArea = new JTextArea();
        encodedArea.setEditable(false);
        encodedArea.setLineWrap(true);
        encodedArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        encodedArea.setBackground(new Color(40, 44, 52));
        encodedArea.setForeground(new Color(152, 195, 121));
        JScrollPane encodedScroll = new JScrollPane(encodedArea);
        encodedScroll.setBounds(40, 250, 320, 260);
        add(encodedScroll);
    }

    private void compressData() {
        originalText = inputArea.getText();
        if (originalText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some text to compress.");
            return;
        }

        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : originalText.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<HuffmanNode> minHeap = new PriorityQueue<>(Comparator.comparingInt(node -> node.freq));
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            minHeap.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        if (minHeap.size() == 1) {
            HuffmanNode single = minHeap.poll();
            huffmanCodes.clear();
            huffmanCodes.put(single.ch, "0");
        } else {
            while (minHeap.size() > 1) {
                HuffmanNode left = minHeap.poll();
                HuffmanNode right = minHeap.poll();
                HuffmanNode merged = new HuffmanNode(left.freq + right.freq, left, right);
                minHeap.add(merged);
            }
            huffmanCodes.clear();
            buildCodes(minHeap.peek(), "");
        }

        StringBuilder sb = new StringBuilder();
        for (char c : originalText.toCharArray()) {
            sb.append(huffmanCodes.get(c));
        }
        encodedText = sb.toString();
        encodedArea.setText(encodedText);

        originalBits = originalText.length() * 8;
        compressedBits = encodedText.length();
        repaint();
    }

    private void buildCodes(HuffmanNode node, String code) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.ch, code);
            return;
        }
        buildCodes(node.left, code + "0");
        buildCodes(node.right, code + "1");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.orange);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Optimal Huffman Codes:", 390, 243);

        int yOffset = 268;
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.setColor(Color.white);
        int count = 0;
        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            if (count >= 8) {
                g.drawString("...", 390, yOffset);
                break;
            }
            String displayChar = entry.getKey() == ' ' ? "'SPACE'" : (entry.getKey() == '\n' ? "'NEWLINE'" : "'" + entry.getKey() + "'");
            g.drawString(displayChar + " -> " + entry.getValue(), 390, yOffset);
            yOffset += 20;
            count++;
        }

        g.setColor(new Color(97, 175, 239));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Compression Efficiency:", 390, 455);

        g.setColor(Color.lightGray);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        if (originalBits > 0) {
            double ratio = (double) originalBits / compressedBits;
            double savings = (1.0 - ((double) compressedBits / originalBits)) * 100;
            g.drawString("Original Size   : " + originalBits + " bits", 390, 478);
            g.drawString("Compressed Size : " + compressedBits + " bits", 390, 498);
            g.drawString(String.format("Compression Ratio: %.2fx (%.1f%% saved)", ratio, savings), 390, 518);
        } else {
            g.drawString("Waiting for text input...", 390, 478);
        }
    }
}
