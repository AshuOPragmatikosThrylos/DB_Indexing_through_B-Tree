package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ui.DataManager.SearchResult;

public class QueryPanel extends JPanel implements ActionListener {

	private JLabel selectQuery;
	private JComboBox<String> columnDropDownList;
	private JLabel equalsSymbol;
	private JTextField columnValue;
	private JButton searchBtn;
	private JLabel searchStats;
	private JLabel dbRecord;

	public QueryPanel(JTabbedPane tabPane) {

		tabPane.addTab("Query", this);
		initComponents();
	}

	private void initComponents() {

		this.setBorder(BorderFactory.createMatteBorder(25, 25, 25, 25, Color.green));
		this.setBackground(new Color(244, 206, 106));

		selectQuery = new JLabel();
		selectQuery.setFont(new Font("Monospaced", 0, 35));
		selectQuery.setForeground(new Color(0, 0, 0));
		selectQuery.setText("select * from Table where ");

		String[] option = { "payer", "transactionId", "createdAt" };
		columnDropDownList = new JComboBox<>(option);
		columnDropDownList.setFont(new Font("Monospaced", 0, 21));
		columnDropDownList.setPreferredSize(new Dimension(200, 40));

		equalsSymbol = new JLabel();
		equalsSymbol.setFont(new Font("Monospaced", 0, 35));
		equalsSymbol.setForeground(new Color(0, 0, 0));
		equalsSymbol.setText("=");

		columnValue = new JTextField("");
		columnValue.setColumns(15);
		columnValue.setFont(new Font("Monospaced", 2, 35));

		searchBtn = new JButton();
		searchBtn.setFont(new Font("Monospaced", 0, 40));
		searchBtn.setText("Execute");
		searchBtn.addActionListener(this);

		searchStats = new JLabel();
		searchStats.setFont(new Font("Monospaced", 0, 26));
		searchStats.setText("Search Method: ##SM##, Time taken: ##TT##, Pages Read: ##PR##");

		dbRecord = new JLabel();
		dbRecord.setFont(new Font("Monospaced", 0, 25));
		dbRecord.setText("recordId: ##ID##, payer: ##NAME##, transactionId: ##TID##, createdAt: ##TS##");

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(selectQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(columnDropDownList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(equalsSymbol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(columnValue,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))

		.addGroup(layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(searchBtn,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))

		.addGroup(layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(searchStats,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		
		.addGroup(layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(dbRecord,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(selectQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(columnDropDownList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(equalsSymbol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(columnValue, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(50, 50, 50)
				.addComponent(searchBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(50, 50, 50)
				.addComponent(searchStats, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(50, 50, 50)
				.addComponent(dbRecord, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(columnValue.getText().length() == 0) {

			JOptionPane.showMessageDialog(this, "Please enter value of column to be searched");
			return;
		}
		DataManager.Search(columnDropDownList.getSelectedItem().toString(), columnValue.getText(), this);
	}

	public void updateResults(SearchResult result) {

		if(result.DataRow != null) {

			dbRecord.setText
			(
				"recordId: ##RID##, payer: ##NAME##, transactionId: ##TID##, createdAt: ##TS##"
				.replace("##RID##", result.DataRow.recordId + "")
				.replace("##NAME##", result.DataRow.payer + "")
				.replace("##TID##", result.DataRow.transactionId + "")
				.replace("##TS##", result.DataRow.createdAt + "")
			);
		}
		else {

			dbRecord.setText
			(
				"recordId: ##RID##, payer: ##NAME##, transactionId: ##TID##, createdAt: ##TS##"
			);

			JOptionPane.showMessageDialog(this, "Record not found!!");
		}

		searchStats.setText
		(
			"Search Method: ##SM##, Time taken: ##TT##, Pages Read: ##PR##"
			.replace("##SM##", result.IndexesUsed? "Index Seek": "Table Scan")
			.replace("##TT##", result.TimeTaken + " ms")
			.replace("##PR##", result.PagesLoaded + "")
		);
	}
}