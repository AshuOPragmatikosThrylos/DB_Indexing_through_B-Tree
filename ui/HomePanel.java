package ui;

import java.awt.*;
import javax.swing.*;

public class HomePanel extends javax.swing.JPanel {

    public HomePanel(JTabbedPane tabPane) {

    	tabPane.addTab("Home", this);
        initComponents();
    }

    private JTextArea jTextArea;

    private void initComponents() {

    	this.setBorder(BorderFactory.createMatteBorder(22, 22, 22, 22, Color.LIGHT_GRAY));
    	this.setBackground(new Color(57, 68, 247));

        jTextArea = new JTextArea();
        jTextArea.setFont(new Font("Monospaced", 1, 35));
        jTextArea.setForeground(new Color(255, 255, 255));
        jTextArea.setOpaque(false);
        jTextArea.setColumns(56);
        jTextArea.setRows(4);
        jTextArea.setText("A project to demonstrate the better performance of Index Seek method over Table Scan method for searching record in a Relational Database table");
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        jTextArea.setEditable(false);

        javax.swing.GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        //add components in layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
            	layout.createSequentialGroup()
                .addContainerGap(0, Short.MAX_VALUE)
                .addComponent(jTextArea, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
            	layout.createSequentialGroup()
                .addContainerGap(0, Short.MAX_VALUE)
                .addComponent(jTextArea, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(0, Short.MAX_VALUE))
        );
    }
}