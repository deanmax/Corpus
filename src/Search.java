import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.*;

public class Search {
	JTextArea textArea;
	JTextField text;
	int fileCt, rfileCt, wordAppearCt, rate = 0;
	
	//===============
	// Logic control
	//===============
	final static Path rootDir = Paths.get("./");
	
	public static void main(String[] args) {
		Search gui = new Search();
		gui.go();
	}
	
	public void go() {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Keyword:  ");
		text = new JTextField(20);
		JButton button = new JButton("Search");
		button.addActionListener(new Button_Listener());
		
		textArea = new JTextArea(50, 60);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		JScrollPane scrollPane = new JScrollPane(textArea); 
		scrollPane.setPreferredSize(new Dimension(700, 500));
		
		panel.add(label);
		panel.add(text);
		panel.add(button);
		panel.add(scrollPane);
		
		// Set frame appearance
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.setSize(800,600);
		frame.setVisible(true);
	}
	
	class Button_Listener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			textArea.setText("");
			// Walk thru mainDir directory
			try {
				Files.walkFileTree(rootDir, new MatchCont());
			} catch (IOException e) {
				e.printStackTrace();
			}
			textArea.append("---\n");
			textArea.append("Related file count: " + rfileCt + "\n");
			textArea.append("Word appearance count: " + wordAppearCt + "\n");
			textArea.append("Show up rate: " + (float)(rfileCt / fileCt * 100) + "%\n");
		}
	}
	
	public class MatchCont extends SimpleFileVisitor<Path> {
		
		String str = text.getText();
	
        // First (minor) speed up. Compile regular expression pattern only one time.
        private Pattern pattern = Pattern.compile(".*" + str + ".*");

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts)
                throws IOException {

        	fileCt++;
        	List<String> Lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
        	
        	String lastPath = "";
        	for (String line : Lines) {
        		if (pattern.matcher(line).matches()) {
        			if (lastPath != path.toString()) {
        				rfileCt++;
        				lastPath = path.toString();
        			}
        			wordAppearCt++;
        			textArea.append(path.toString() + ":\n");
        			textArea.append("\t" + line + "\n\n");
        			//System.out.println(path.toString() + ":");
        			//System.out.println("\t" + line + "\n");
        		}
        	}

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException exc)
                throws IOException {
            exc.printStackTrace();

            // If the root directory has failed it makes no sense to continue
            return path.equals(rootDir)? FileVisitResult.TERMINATE:FileVisitResult.CONTINUE;
        }
	}

}