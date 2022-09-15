package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class IndexPanel extends JPanel implements ActionListener {

	private JLabel columnDropDownListLabel;
	private JComboBox<String> columnDropDownList;
	private JButton createBtn;
	private JProgressBar progressBar;
	private static boolean[] indexAlreadyCreated = new boolean[4];

	public IndexPanel(JTabbedPane tabPane) {

		tabPane.addTab("Indices", this);
		initComponents();
	}

	private void initComponents() {

		this.setBorder(BorderFactory.createMatteBorder(22, 22, 22, 22, Color.PINK));
		this.setBackground(new Color(202, 213, 226));

		columnDropDownListLabel = new JLabel();
		columnDropDownListLabel.setFont(new Font("Monospaced", 0, 45));
		columnDropDownListLabel.setForeground(new Color(0, 0, 0));
		columnDropDownListLabel.setText("Column on which index has to be created:");

		columnDropDownList = new JComboBox<String>(new String[] {"--Select One--", "payer", "transactionId", "createdAt"});
		columnDropDownList.setFont(new Font("Monospaced", 0, 35));

		createBtn = new JButton();
		createBtn.setFont(new Font("Monospaced", 0, 50));
		createBtn.setText("Create Index");
		createBtn.addActionListener(this);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setFont(new Font("Monospaced", 0, 50));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(
					layout.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(columnDropDownListLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(columnDropDownList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			.addGroup(
					layout.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(createBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			.addGroup(layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					  .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					  .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(
			layout.createSequentialGroup()
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(columnDropDownList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(columnDropDownListLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			.addGap(50, 50, 50)
			.addComponent(createBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(50, 50, 50)
			.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		IndexPanel obj = this;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					int selectedColumnIndex = columnDropDownList.getSelectedIndex();
					if(selectedColumnIndex == 0){

						JOptionPane.showMessageDialog(obj, "Please select a column");
						return;
					}
					String columnName = columnDropDownList.getSelectedItem().toString();
					if(!indexAlreadyCreated[selectedColumnIndex]) {

						indexAlreadyCreated[selectedColumnIndex] = true;
						long start = System.currentTimeMillis();
						DataManager.CreateIndex(columnName, obj);
						long now = (System.currentTimeMillis() - start)/1000;
						long timeTakenInSec = now % 60;
						long timeTakenInMin = now / 60;
						JOptionPane.showMessageDialog(obj, "Index created successfully in "+timeTakenInMin+" min and "+timeTakenInSec+" sec");
						progressBar.setValue(0);
						Thread.currentThread().interrupt();
					}
					else {

					    JOptionPane.showMessageDialog(obj, "Index already created for column: " + columnName);
						JOptionPane.showMessageDialog(obj, "For new index on same column, delete old index and rerun application");
					}
				} 
				catch (Exception ex) {

					JOptionPane.showMessageDialog(obj, ex);
				}
			}
		});
		t.start();
	}

	public void updateStatus(int percent){

		progressBar.setValue(percent);
	}
}