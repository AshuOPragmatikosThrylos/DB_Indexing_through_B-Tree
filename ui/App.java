package ui;

import java.awt.*;
import javax.swing.*;

public class App extends JFrame {

	JTabbedPane tabPane;
	HomePanel homePanel;
	DataPanel dataPanel;
	IndexPanel indexPanel;
	QueryPanel queryPanel;

	public App() throws Exception {

		tabPane = new JTabbedPane();
		tabPane.setFont(new Font("", 2, 32));
		tabPane.setBackground(Color.CYAN);
		tabPane.setForeground(new Color(106,27,77));

		homePanel = new HomePanel(tabPane);
		dataPanel = new DataPanel(tabPane);
		indexPanel = new IndexPanel(tabPane);
		queryPanel = new QueryPanel(tabPane);

		super.add(tabPane);

		pack();

		super.setTitle("DB Indexing through B+ Tree");
		super.setExtendedState(MAXIMIZED_BOTH);
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setVisible(true);
	}
}