package ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

//panel to create test dataPages
public class DataPanel extends JPanel implements ActionListener {

	private JLabel textAreaLabel;
	private JTextField textArea;
	private JButton createBtn;
	private JProgressBar progressBar;
	private static boolean testDataCreatedOnce;

	public DataPanel(JTabbedPane tabPane) throws Exception {

		tabPane.addTab("Data", this);
		initComponents();
	}

	private void initComponents() throws Exception {

		this.setBorder(BorderFactory.createMatteBorder(22, 22, 22, 22, Color.darkGray));
		this.setBackground(new Color(2,178,144));

		textAreaLabel = new JLabel();
		textAreaLabel.setFont(new Font("Monospaced", 0, 51));
		textAreaLabel.setForeground(new Color(255, 255, 255));
		textAreaLabel.setText("Number of rows to create:");

		textArea = new JTextField("");
		textArea.setColumns(10);
		textArea.setFont(new Font("Monospaced", 0, 51));

		createBtn = new JButton();
		createBtn.setFont(new Font("Monospaced", 0, 51));
		createBtn.setText("Create Test Data");
		createBtn.addActionListener(this);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setFont(new Font("Monospaced", 0, 51));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(textAreaLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(textArea, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(createBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(
				layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(textArea, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(textAreaLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
				.addGap(50, 50, 50)
				.addComponent(createBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(50, 50, 50)
				.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(textArea.getText().length() == 0) {
	
			JOptionPane.showMessageDialog(this, "Please enter total number of rows to be created");
			return;
		}

		DataPanel obj = this;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					if(!testDataCreatedOnce) {

						testDataCreatedOnce = true;
						String num = textArea.getText();
						int numOfRows = Integer.parseInt(num);
						long start = System.currentTimeMillis();
						DataManager.CreateTestData(numOfRows, obj);
						long timeTaken = (System.currentTimeMillis() - start)/1000;
						JOptionPane.showMessageDialog(obj, "Data created successfully in "+timeTaken+" s");
						progressBar.setValue(0); // progressBar set to 0 once OK is clicked on the pop up(JOptionPane)
					}
					else
					    JOptionPane.showMessageDialog(obj, "For new test data, delete old test data and rerun application");

				}
				catch (Exception ex) {

					JOptionPane.showMessageDialog(obj, ex);
				}
			}
		});
		t.start();
	}

	public void updateStatus(int percent) {

		progressBar.setValue(percent);
	}
}