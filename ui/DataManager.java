// This file contains 3 main functionalities of the project along with their helper functions
// 1. CreateTestData() invoked by DataPanel
// 2. CreateIndex() invoked by IndexPanel
// 3. Search() invoked by QueryPanel

package ui;

import java.io.*;
import java.util.*;

public class DataManager {

	public static String basedir = "";

	public static class DataRow implements Serializable {

		public int recordId;
		public String payer;
		public String transactionId;
		public String createdAt;

		public String toString() {

			return this.recordId + "|" + this.payer + "|" + this.transactionId + "|" + this.createdAt;
		}
	}

	public static class SearchResult {

		public DataRow DataRow;
		public long TimeTaken;
		public int PagesLoaded;
		public boolean IndexesUsed; // false: table scan, true: index seek
	}

	public static void CreateTestData(int totalNumberOfRows, DataPanel dp) throws Exception {

		createDirectory(basedir + "\\data");

		int extentNumber = 1, pageNumber = 1;
		
		for (int i = 1; i <= totalNumberOfRows; i++) {

			String extentPath = basedir + "\\data\\extent_" + extentNumber;
			String pagePath = basedir + "\\data\\extent_" + extentNumber + "\\page_" + pageNumber;
        
			createDirectory(extentPath);
			createFile(pagePath);

			DataRow row = new DataRow();
			row.recordId = i;
			row.payer = getRandomWord();
			row.transactionId = getRandomWord();
			row.createdAt = getRandomWord();

			if (canPageAccomodateRow(pagePath, row)) {

				writeRowInFile(pagePath, row.toString());
			}
			else {

				i--;
				pageNumber++;
				if (pageNumber == 9) {

					extentNumber++;
					pageNumber = 1;
					
					dp.updateStatus((i * 100) / totalNumberOfRows);
				}
			}
		}

		dp.updateStatus(100);
		String DataPagesMetadataFilePath = basedir + "\\data\\metadata";
		createFile(DataPagesMetadataFilePath);
		String metadata = totalNumberOfRows + "";
	 	writeRowInFile(DataPagesMetadataFilePath, metadata);
	}

	//need to read all the data pages once so that future searches can be faster
	public static void CreateIndex(String columnName, IndexPanel ip) throws Exception {

		String indexDirPath = basedir + "\\indices";
		String specificIndexDirPath = indexDirPath + "\\" + columnName;

		createDirectory(indexDirPath);
		createDirectory(specificIndexDirPath);

		String dataDirPath = basedir + "\\data";
		String DataPagesMetadataFilePath = dataDirPath + "\\metadata";
		int totalNumberOfRows = 0, rowsCounter = 0, pagesCounter = 1, extentsCounter = 1;
		int columnIndex = columnName.equals("payer") ? 1 : (columnName.equals("transactionId") ? 2 : 3);

		String metadata = readRowFromFile(DataPagesMetadataFilePath, 0);
		totalNumberOfRows = Integer.parseInt(metadata);

		while (rowsCounter < totalNumberOfRows) {

			String extentPath = basedir + "\\data\\extent_" + extentsCounter;
			String pagePath = extentPath + "\\page_" + pagesCounter;
			String line = "";

			try {

				File file = new File(pagePath);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				int offset = 0;

				do {

					line = reader.readLine();

					//data page row format --> 8|tzveoqkncvq|mcwwbyhyutvimn|dkrtihedk
					if (line != null && line.trim().length() > 0) {

						String columnValue = line.split("[|]")[columnIndex];
						String actualRowAddress = extentsCounter + "|" + pagesCounter + "|" + offset;
						addRowToIndex(columnValue, actualRowAddress, specificIndexDirPath);
					}
					
					rowsCounter++;
					offset++;
				} while (line != null);

				pagesCounter++;
				if (pagesCounter == 9) {

					extentsCounter++;
					pagesCounter = 1;
					ip.updateStatus((rowsCounter * 100) / totalNumberOfRows); 
				}

				reader.close();
			}
			catch (Exception ex) {

				System.out.println(ex);
			}
		}
		ip.updateStatus(100);
	}

	public static void Search(String columnName, String columnValue, QueryPanel qp) {

		SearchResult result = new SearchResult();
		
		String indexDirPath = basedir + "\\indices";
		String specificIndexDirPath = indexDirPath + "\\" + columnName;
		long startTime = System.currentTimeMillis();
		boolean[] resultFound = new boolean[1];
		
		// search method == index seek when there exists an index on columnName else table scan
		if(new File(specificIndexDirPath).exists()) {

			// index seek
			result.IndexesUsed = true;
			result.PagesLoaded = 1; // for root
			
			String IndexPagesMetadataFilePath = specificIndexDirPath + "\\metadata";
			HashMap<String, Boolean> nodeLeafMap = getMetadataInfo(IndexPagesMetadataFilePath);
			searchValueInIndex(specificIndexDirPath, "root", nodeLeafMap, columnValue, startTime, result, qp, resultFound);
			if(!resultFound[0])
			   qp.updateResults(result); // so that updateResults can display record not found pop up
		}
		else {

			// table scan
			result.IndexesUsed = false;
			String dataDirPath = basedir + "\\data";
			String DataPagesMetadataFilePath = dataDirPath + "\\metadata";
			int totalNumberOfRows = 0, rowsCounter = 0, pagesCounter = 1, extentsCounter = 1;
			int columnIndex = columnName.equals("payer") ? 1 : (columnName.equals("transactionId") ? 2 : 3);

			String metadata = readRowFromFile(DataPagesMetadataFilePath, 0);
			totalNumberOfRows = Integer.parseInt(metadata);

            boolean recordFound = false;
			while (rowsCounter < totalNumberOfRows) {

				String extentPath = basedir + "\\data\\extent_" + extentsCounter;
				String pagePath = extentPath + "\\page_" + pagesCounter;
				String line = "";

				try {

					File file = new File(pagePath);
					BufferedReader reader = new BufferedReader(new FileReader(file));

					do {

						line = reader.readLine();

						if (line != null && line.trim().length() > 0) {

							String[] lineSplitted = line.split("[|]");
							String value = lineSplitted[columnIndex];
							if(value.equals(columnValue)) {

                                recordFound = true;

								result.PagesLoaded = (extentsCounter - 1) * 8 + pagesCounter;
								result.TimeTaken = System.currentTimeMillis() - startTime;
								result.DataRow = new DataRow();
								result.DataRow.recordId = Integer.parseInt(lineSplitted[0]);
								result.DataRow.payer = lineSplitted[1];
								result.DataRow.transactionId = lineSplitted[2];
								result.DataRow.createdAt = lineSplitted[3];
								
								qp.updateResults(result);
								break;
							}
						}

						rowsCounter++;
					} while (line != null);
                    
					
					if(recordFound)
					   break;

					reader.close();
                    
					pagesCounter++;
					if (pagesCounter == 9) {

						extentsCounter++;
						pagesCounter = 1;
					}

					result.PagesLoaded = (extentsCounter - 1) * 8 + pagesCounter;
					result.TimeTaken = System.currentTimeMillis() - startTime;

				}
				catch (Exception ex) {

					System.out.println(ex);
				}

			}
			if(!recordFound)
			   qp.updateResults(result); // so that updateResults can display record not found pop up
		}
	}

	private static String readRowFromFile(String fileName, int rowNumber) {

		String line = "";

		try {

			File file = new File(fileName);
			BufferedReader reader = new BufferedReader(new FileReader(file));

			int rowCounter = 0;
			do {

				line = reader.readLine();
				rowCounter++;
			} while (line != null && rowCounter < rowNumber);

			reader.close();
		}
		catch (Exception ex) {

			System.out.println(ex);
		}

		return line;
	}

	private static void writeRowInFile(String fileName, String row) {

		try {

			File file = new File(fileName);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			PrintWriter pwriter = new PrintWriter(writer);
			pwriter.println(row);
			pwriter.close();
		}
		catch (Exception ex) {

			System.out.println(ex);
		}
	}

	private static boolean canPageAccomodateRow(String fileName, DataRow row) {

		// we are adding rowSize to fileSize and then checking for within 8 KB
		// this ensures the necessity --> a row should never be split across 2 pages

		File file = new File(fileName);
		long byteSize = file.length() + 2 * row.toString().length();
		return byteSize < 8 * 1024; //8 KB
	}

	private static boolean doesFileExist(String fileName) {

		File file = new File(fileName);
		return file.exists();
	}

	//creates a new file if does not exist already
	private static void createFile(String fileName) throws Exception {

		if (!doesFileExist(fileName)) {

			try {

				File file = new File(fileName);
				file.createNewFile();
			}
			catch (Exception e) {

				throw new IOException(e.getMessage() + " --> " + fileName, e);
			}
		}
	}

	private static boolean doesDirectoryExist(String dirName) {

		File file = new File(dirName);
		return file.exists();
	}

	//creates a new directory if does not exist already
	private static void createDirectory(String dirName) {

		if (!doesDirectoryExist(dirName)) {

			File dir = new File(dirName);
			dir.mkdir();
		}
	}

	private static String getRandomWord() {

		//some arbitrary logic

		int length = (int) (Math.random() * 10) + 5;
		StringBuilder sb = new StringBuilder();

		// (int) (Math.random() * 26) --> never exceeds 25
		for (int i = 0; i < length; i++) {

			char ch = (char) ((int) (Math.random() * 26) + 'a');
			sb.append(ch);
		}

		return sb.toString();
	}

	// inserts actualRowAddress in one of the leaves of the B+ Tree, and changes IndexPagesMetadata file accordingly
	private static void addRowToIndex(String columnValue, String actualRowAddress, String indexDirPath) throws Exception {

		String IndexPagesMetadataFilePath = indexDirPath + "\\metadata";
		createFile(IndexPagesMetadataFilePath);

		String rootFilePath = indexDirPath + "\\root";
		createRootFile(rootFilePath, IndexPagesMetadataFilePath); // puts "root=true" in IndexPagesMetadataFilePath. So getMetadataInfo should not be called before createRootFile because having <root, true> in nodeLeafMap is important for addRowToSuitableNode

		HashMap<String, Boolean> nodeLeafMap = getMetadataInfo(IndexPagesMetadataFilePath);
		addRowToSuitableNode(indexDirPath, "root", nodeLeafMap, columnValue, actualRowAddress); // 1 B+Tree node can comprise of multiple rows(each of them identified in B+Tree by their columnValue)
		balanceNode(indexDirPath, "root", null, nodeLeafMap);	// this might create new leaves because of splitting of a B+Tree node. So addAllNewLeavesInMetadataFile() next
		addAllNewLeavesInMetadataFile(IndexPagesMetadataFilePath, nodeLeafMap);
	}

	// inserts <columnValue, actualRowAddress> in one suitable node of the subtree of B+ Tree which starts with root
	// doing so might violate the degree of that node. So balanceNode() next
	private static void addRowToSuitableNode(String indexDirPath, String root, HashMap<String, Boolean> nodeLeafMap, String columnValue, String actualRowAddress) throws Exception {

		Boolean isLeaf = nodeLeafMap.get(root);  // isLeaf = true for root when addRowToSuitableNode() is called for the first time because we put "root=true" in nodeLeafMap in createRootFile()
		//balanceNode() just after addRowToSuitableNode() ensures there will be new leaves before next addRowToSuitableNode()

		if (!isLeaf) {

			// continue search for suitable node(leaf node)
			String childNode = "";

			try {

				File file = new File(indexDirPath + "\\" + root);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String prevLine = "", line = "", lv = "";
				boolean flag = false;

				do {

					prevLine = line;
					line = reader.readLine();

					if (line != null && line.trim().length() > 0) {

						lv = line.split("=")[0];

						if (lv.compareTo(columnValue) > 0) { // columnValue < lv ==> left side of line ==> 0 index if flag == true

							flag = true;
							break;
						}
					}
				} while (line != null);

				if (flag) { // flag == true ==> line != null

					// smaller
					childNode = line.split("=")[1].split("[|]")[0];
				}
				else {

					// reached EOF ==> columnValue is the largest ==> must lie on the right side of prevLine ==> 1 index
					childNode = prevLine.split("=")[1].split("[|]")[1];
				}

				reader.close();
			}
			catch (Exception ex) {

				System.out.println(ex);
			}

			addRowToSuitableNode(indexDirPath, childNode, nodeLeafMap, columnValue, actualRowAddress); // addRowToSuitableNode in subtree whose root is childNode 
			balanceNode(indexDirPath, childNode, root, nodeLeafMap); // balance childNode whose parentNode is root cuz all descendants of childNode have already been balanced // also populates nodeLeafMap
		}
		else {

			try {

				File file = new File(indexDirPath + "\\" + root);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				ArrayList<String> lines = new ArrayList<>();
				String line = "", lv = "";
				boolean flag = false;

				// (< columnValue) then (== columnValue) then (> columnValue) 
				do {

					line = reader.readLine();

					if (line != null && line.trim().length() > 0) {

						lv = line.split("=")[0];

						if (lv.compareTo(columnValue) > 0) {

							if (!flag) {

								lines.add(columnValue + "=" + actualRowAddress); // added only once
								lines.add(line);
								flag = true;
							}
							else {

								lines.add(line); // multiple (> value) 
							}
						}
						else {

							lines.add(line);
						}
					}
				} while (line != null);

				reader.close();

				if (!flag) {

					lines.add(columnValue + "=" + actualRowAddress);
				}

				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				for (String lineContent : lines) {

					writer.write(lineContent);
					writer.write("\n");
				}
				writer.close();
			} 
			catch (Exception ex) {

				System.out.println(ex);
			}
		}
	}

	// balancing a node means --> if size of node is out of bounds then split it into two and attach the two nodes to the old parentNode through an intermediate newParentNode(might be child of old parentNode or (old parentNode + new row))
	// When a node is split and we add a row to the parent node then the degree of the parentNode might get affected. So parentNode must be balanced later after all child ==> balanceNode() must be invoked in postorder region
	private static void balanceNode(String indexDirPath, String node, String parentNode, HashMap<String, Boolean> nodeLeafMap) throws Exception {

		File file = new File(indexDirPath + "\\" + node);
		long fileSize = file.length();
		if (fileSize > 8 * 1024) {

			// split
			String siblingNode = UUID.randomUUID().toString();
			createFile(indexDirPath + "\\" + siblingNode);
			ArrayList<String> lines = new ArrayList<>();

			try {

				// reading data from node
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = "";

				do {

					line = reader.readLine();
					if (line != null && line.trim().length() > 0) {

						lines.add(line);
					}
				} while (line != null);

				reader.close();

				// half the data in original node
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				for (int i = 0; i < lines.size() / 2; i++) {

					writer.write(lines.get(i));
					writer.write("\n");
				}
				writer.close();

				if (node == "root") {

					// since node(=="root") has to be splitted and attached to a new parent node ==> new root of B+Tree has to be created
					node = UUID.randomUUID().toString();
					file.renameTo(new File(indexDirPath + "\\" + node));

					nodeLeafMap.put(node, nodeLeafMap.get("root")); // if there is only 1 node in B+ Tree, then root will be leaf else not
					nodeLeafMap.put(siblingNode, nodeLeafMap.get("root"));
					nodeLeafMap.put("root", false);
				}
				else
					nodeLeafMap.put(siblingNode, nodeLeafMap.get(node)); // if node is non leaf, siblingNode will be non leaf too

				// half the data in new siblingNode
				writer = new BufferedWriter(new FileWriter(new File(indexDirPath + "\\" + siblingNode)));
				for (int i = lines.size() / 2; i < lines.size(); i++) {

					writer.write(lines.get(i));
					writer.write("\n");
				}
				writer.close();

				// reading and preparing content from node
				String intermediateLine = lines.get(lines.size() / 2);
				lines.clear();

				if (parentNode != null) {

					boolean flag = false;
					reader = new BufferedReader(new FileReader(new File(indexDirPath + "\\" + parentNode)));
					do {

						line = reader.readLine();
						if (line != null && line.trim().length() > 0) {

							if (line.compareTo(intermediateLine) < 0) {

								lines.add(line);
							}
							else {

								if (!flag) {

									flag = true;
									lines.add(intermediateLine.split("=")[0] + "=" + node + "|" + siblingNode); //nodePreviousToMe | nodeNextToMe
									lines.add( line.split("=")[0] + "=" + siblingNode + "|" + line.split("=")[1].split("[|]")[1]);
								}
								else {

									lines.add(line);
								}
							}
						}
					} while (line != null);

					if (!flag) {

						lines.add(intermediateLine.split("=")[0] + "=" + node + "|" + siblingNode);
					}
					reader.close();

					// insert content in node
					writer = new BufferedWriter(new FileWriter(new File(indexDirPath + "\\" + parentNode)));
					for (int i = 0; i < lines.size(); i++) {

						writer.write(lines.get(i));
						writer.write("\n");
					}
					writer.close();
				}
				else {

					createFile(indexDirPath + "\\root");
					writer = new BufferedWriter(new FileWriter(new File(indexDirPath + "\\root")));
					writer.write(intermediateLine.split("=")[0] + "=" + node + "|" + siblingNode);
					writer.write("\n");
					writer.close();
				}
			}
			catch (Exception ex) {

				System.out.println(ex);
			}
		}
	}

	private static void searchValueInIndex(String indexDirPath, String node, HashMap<String, Boolean> nodeLeafMap, String columnValue, long startTime, SearchResult result, QueryPanel qp, boolean[] resultFound) {
			
			Boolean isLeaf = nodeLeafMap.get(node);

			//nodeLeafMap has leaf nodes and root

			if (!isLeaf) { // not a leaf => go from 1 index page to another

				String childNode = "";

				try {

					File file = new File(indexDirPath + "\\" + node);
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String prevLine = "", line = "", lv = "";
					boolean flag = false;

					do {

						prevLine = line;
						line = reader.readLine();

						if (line != null && line.trim().length() > 0) {

							lv = line.split("=")[0];

							if (lv.compareTo(columnValue) > 0) {

								flag = true;
								break;
							}
						}
					} while (line != null);

					// for each columnValue' we are storing address of 2 index pages -
					// 1. index page with elements with value < columnValue'
					// 2. index page with elements with value >= columnValue' ( and < next columnvalue'')
					if (flag) {

						// want smaller
						childNode = line.split("=")[1].split("[|]")[0];
					}
					else {

						// reached EOF => want larger
						childNode = prevLine.split("=")[1].split("[|]")[1];
					}

					reader.close();
				} 
				catch (Exception ex) {

					System.out.println(ex);
				}

				result.PagesLoaded++;
				result.TimeTaken = System.currentTimeMillis() - startTime;

				searchValueInIndex(indexDirPath, childNode, nodeLeafMap, columnValue, startTime, result, qp, resultFound);
			}
			else { // is a leaf => find actualRowAddress == (extent|page|rowOffset) of the data page for the columnValue

				try {

					File file = new File(indexDirPath + "\\" + node);
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = "", lv = "";

					do {

						line = reader.readLine();

						if (line != null && line.trim().length() > 0) {

							lv = line.split("=")[0];

							if (lv.compareTo(columnValue) == 0)
                                break;
						}
					} while (line != null);

					reader.close();
					
					if(line != null ) {

						// if columnValue found in index page then corresponding to it should be a (extent|page|rowOffset)
						// to direct the search to the 1 data page containing the actual record
						String[] lineSplitted = line.split("=")[1].split("[|]");
						String extentNumber = lineSplitted[0];
						String pageNumber = lineSplitted[1];
						int offsetNumber = Integer.parseInt(lineSplitted[2]);
						
						File dataFilePath = new File(basedir + "\\data\\extent_" + extentNumber + "\\page_" + pageNumber);
						reader = new BufferedReader(new FileReader(dataFilePath));
						
						for(int i = 0; i <= offsetNumber; i++) {

							line = reader.readLine();
						}
						
						result.PagesLoaded++;
						result.TimeTaken = System.currentTimeMillis() - startTime;
						result.DataRow = new DataRow();
						String[] splittedLine = line.split("[|]");
						result.DataRow.recordId = Integer.parseInt(splittedLine[0]);
						result.DataRow.payer = splittedLine[1];
						result.DataRow.transactionId = splittedLine[2];
						result.DataRow.createdAt = splittedLine[3];

						resultFound[0] = true;
						qp.updateResults(result);

						reader.close();
						return;
					}
				} 
				catch (Exception ex) {

					System.out.println(ex);
				}
			}
		}

	// reads IndexPagesMetadata file of an index on a specific column and 
	// returns a data structure(DS) with all leaf nodes and root
	// Need this DS because in a B+ Tree all actualRowAddress' are stored at the leaf nodes
	private static HashMap<String, Boolean> getMetadataInfo(String IndexPagesMetadataFilePath) {
		
		HashMap<String, Boolean> map = new HashMap<>();

		try {

			File file = new File(IndexPagesMetadataFilePath);
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line = "";
			do {

				line = reader.readLine();
				if (line != null) {

					String[] lineSplitted = line.split("=");
					map.put(lineSplitted[0], Boolean.parseBoolean(lineSplitted[1]));
				}
			} while (line != null);

			reader.close();
		} 
		catch (Exception ex) {

			ex.printStackTrace();
		}

		return map;
	}

	// update IndexPagesMetadata file with contents of nodeLeafMap
	// as nodeLeafMap changes for every record to be inserted in B+ Tree
	// but we want static information of all leaves (to be used later while querying) 
	private static void addAllNewLeavesInMetadataFile(String IndexPagesMetadataFilePath, HashMap<String, Boolean> nodeLeafMap) {

		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(IndexPagesMetadataFilePath)));
			Set<String> keys = nodeLeafMap.keySet();
			for (String key : keys) {

				writer.write(key + "=" + nodeLeafMap.get(key));
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception ex) {

			ex.printStackTrace();
		}
	}

	private static void createRootFile(String rootFilePath, String IndexPagesMetadataFilePath) {

		if (!doesFileExist(rootFilePath)) {

			try {

				File file = new File(IndexPagesMetadataFilePath);
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
				PrintWriter pwriter = new PrintWriter(writer);
				pwriter.println("root=true"); // root is leaf to begin with // so if no other nodes are added after root, then root remains leaf
				pwriter.close();
				writer.close();

				file = new File(rootFilePath);
				file.createNewFile();
			}
			catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
}