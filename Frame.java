package com.googlecode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 9/11/13
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Frame extends JFrame {

    public Frame(){
        init();
    }

    private final void init(){
        setPreferredSize(new Dimension(610, 630));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel wrapper = new JPanel();
        wrapper.setBackground(Color.WHITE);
        getContentPane().add(wrapper);
        wrapper.setPreferredSize(new Dimension(610, 630));
        wrapper.setLayout(new FlowLayout(FlowLayout.LEADING, 14,14));

       setTitle("Settings");

        JPanel left = new JPanel();
        wrapper.add(left);
//        wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        left.setPreferredSize(new Dimension(200, 500));
        left.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        left.add(new JLine(200, new Color(73, 117, 255)));
        left.add(getLabel("Path to hz"));
        left.add(getLabel("Some text"));
        left.add(getLabel("Some text"));
        left.add(getLabel("Some text"));        left.add(getLabel("Some text"));
        left.add(getLabel("Some text"));
        left.add(getLabel("Some text"));

        JPanel right = new JPanel();
        right.setBackground(Color.WHITE);
//        left.setBackground(Color.LIGHT_GRAY);
        right.setPreferredSize(new Dimension(340, 500));
        right.setLayout(new FlowLayout(FlowLayout.RIGHT, 0,3));
        wrapper.add(right);
        right.add(new JLine(300, Color.WHITE));
        right.add(getInput());
        right.add(getInput());
        right.add(getInput());
        right.add(getInput());
        right.add(getInput());
        right.add(getInput());
        right.add(getInput());

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        buttons.setBackground(Color.WHITE);
        buttons.setPreferredSize(new Dimension(560, 60));
        wrapper.add(buttons);
        buttons.add(getButton("Save"));
        buttons.add(getButton("Cancel"));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args){
        new Frame();
    }

    private class JLine extends JPanel {
        Color color;
        public JLine(int w, Color c){
            color = c;
            setPreferredSize(new Dimension(w, 5));
        }

        @Override
        public void paint(Graphics g){
            g.setColor(color);
//            g.setColor(new Color(73, 117, 255));
            g.drawRect(0, 0, (int) getPreferredSize().getWidth(), 1);
        }
    }

    private JLabel getLabel(String v){
        JLabel l = new JLabel("       " + v);
        l.setPreferredSize(new Dimension(200, 44));
        l.setForeground(new Color(85, 85, 85));
        return l;
    }

    private JTextField getInput(){
        final JTextField f = new JTextField();
        f.setMargin(new Insets(15,15,0,0));
        f.setPreferredSize(new Dimension(300, 40));
        f.setForeground(new Color(85, 85, 85));
        f.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
               f.setBackground(new Color(253,253,253));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBackground(Color.WHITE);
            }
        });

        Border bb = BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(73, 117, 255));
        Border empty = new EmptyBorder(0, 6, 0, 0);
        Border border = new CompoundBorder(bb, empty);
        f.setBorder(border);
        return f;

    }

    private JButton getButton(String text){
        final JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(100, 50));
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(85, 85, 85));
        b.setBackground(new Color(235,235,235));
        b.setBorderPainted(false);
        b.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(new Color(73, 117, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(new Color(85, 85, 85));
            }
        });
        return b;
    }

}
