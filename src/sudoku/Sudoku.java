/***********************************************************************************************\
|* Sudoku is a game to fill in tiles of the board without repeating numbers in rows or columns *|
\***********************************************************************************************/

package Sudoku;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;

public class Sudoku extends JFrame {

    // Class wide variables
    private Tile[][] tiles;
    private Tile currentTile;
    
    // Generate a new random puzzle
    void generatePuzzle(){
        // Blank all the cells prior to loading
        for(int x=0; x<9; x++){
            for(int y=0; y<9; y++){
                tiles[x][y].clear();
                tiles[x][y].unlock();
            }
        }
        
        // Make the new puzzle
        Puzzle puzzle = new Puzzle();
        
        // Fill the tiles with the puzzle
        // Leave some of them blank
        Random r = new Random();
        for(int x=0; x<9; x++){
            for(int y=0; y<9; y++)
                if(r.nextBoolean()){
                    tiles[x][y].setValue(puzzle.getCell(x, y));
                    tiles[x][y].lock();
                }
        }
    }
    
    // Check if the puzzle has any duplicates
    boolean checkPuzzle(){
        Set<Integer> testHash = new HashSet<>();
        boolean isGood = true;
        // Check the rows
        for(int x=0; x<9; x++){
            testHash.clear();
            for(int y=0; y<9; y++){
                testHash.add(tiles[x][y].getValue());
            }
            if(testHash.size() != 9)
                isGood = false;
        }
        // Check the coloums
        for(int y=0; y<9; y++){
            testHash.clear();
            for(int x=0; x<9; x++){
                testHash.add(tiles[x][y].getValue());
            }
            if(testHash.size() != 9)
                isGood = false;
        }
        // Check the groups
        // Break the board down into 3x3 grids
        for(int i=0; i<7; i+=3){
            for(int j=0; j<7; j+=3){
                testHash.clear();
                // Now loop through the 3x3 grid
                for(int x=i; x<i+3; x++){
                    for(int y=j; y<j+3; y++){
                        testHash.add(tiles[x][y].getValue());
                    }
                }
                if(testHash.size() != 9)
                    isGood = false;
            }
        }
        return isGood;
    }
    
    // Save the puzzle to a file for later
    void save(){
        // Create a file chooser
        final JFileChooser fc = new JFileChooser();

        // Open the file and save the tiles
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            PrintWriter output = null;
            try {
                file.createNewFile();
                output = new PrintWriter(new FileWriter(file));
                // Step through the tiles and save them all
                for(int x=0; x<9; x++)
                    for(int y=0; y<9; y++)
                        output.println(tiles[x][y].toString());
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(Sudoku.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                output.close();
            }
        }
    }
    
    // Load the puzzle from a file to continue play
    void load(){
        // Create a file chooser
        final JFileChooser fc = new JFileChooser();

        // Open the file and save the tiles
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            BufferedReader input = null;
            try {
                file.createNewFile();
                input = new BufferedReader(new FileReader(file));
                // Load all the tiles from the file
                // It should have some error checking add in later
                for(int x=0; x<9; x++)
                    for(int y=0; y<9; y++){
                        String line[] = input.readLine().split(":");
                        int value = Integer.parseInt(line[0]);
                        if(value == 0)
                            tiles[x][y].clear();
                        else
                            tiles[x][y].setValue(value);
                        if(line[1] == "T")
                            tiles[x][y].lock();
                        else
                            tiles[x][y].unlock();
                    }
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(Sudoku.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(Sudoku.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    // A class to hold the puzzle
    private class Puzzle{
        // This is array that holds the actual puzzle
        private int puzzle[][] = new int[9][9];
        
        // Arrays of the available numbers for each different sections
        private ArrayList<ArrayList<Integer>> availableNumbersCol = new ArrayList<ArrayList<Integer>>();
        private ArrayList<ArrayList<Integer>> availableNumbersRow = new ArrayList<ArrayList<Integer>>();
        private ArrayList<ArrayList<Integer>> availableNumbersGrp = new ArrayList<ArrayList<Integer>>();
        
        // Initialize the puzzle with a new random unchangeable puzzle
        Puzzle(){
            // This is an arraylist so that I can remove options as they are added to the puzzle
            // Fill up the arrays with all the possible outcomes
            for(int i=0; i<9; i++){
                ArrayList<Integer> tempList = new ArrayList<>();
                // Fill the array with 1-9
                for(int j=1; j<10; j++)
                    tempList.add(j);
                // Make three new copies just to make sure nothing is linked
                availableNumbersCol.add(new ArrayList<>(tempList));
                availableNumbersRow.add(new ArrayList<>(tempList));
                availableNumbersGrp.add(new ArrayList<>(tempList));
            }
            
            //Fill the puzzle with 0's
            for(int x=0; x<9; x++)
                for(int y=0; y<9; y++)
                    puzzle[x][y] = 0;
            
            // Fill the puzzle with actual numbers
            recursiveGenerator(puzzle);
        }
        
        // Generate the puzzle recursively to allow back tracking
        boolean recursiveGenerator(int possiblePuzzle[][]){
            // Find the next empty cell
            int row = -1, col = -1, group = -1, x = 0, y = 0;
            boolean allFull = false;
            while(row < 0 && col < 0){
                if(possiblePuzzle[x][y] <= 0){
                    row = x;
                    col = y;
                    group = (row / 3) + 3 * (col / 3);
                }else{
                    x++;
                }
                if(x > 8){
                    x = 0;
                    y++;
                }
                if(y > 8){
                    allFull = true;
                    break;
                }
            }
            // All the spots have been filled
            if(allFull){
                return true;
            }
            // Choose a random number for that cell
            // Remove the number from the available lists
            // and test if that random number will work
            // If it does not work add the number back
            // and move on to the next number in the list
            // If no numbers work false out
            ArrayList<Integer> possibleNumbers = new ArrayList<>();
            possibleNumbers.addAll(availableNumbersCol.get(col));
            possibleNumbers.retainAll(availableNumbersRow.get(row));
            possibleNumbers.retainAll(availableNumbersGrp.get(group));
            if(possibleNumbers.size() <= 0)
                return false;
            Collections.shuffle(possibleNumbers);
            for(int n : possibleNumbers){
                availableNumbersCol.get(col).remove((Integer)n);
                availableNumbersRow.get(row).remove((Integer)n);
                availableNumbersGrp.get(group).remove((Integer)n);
                possiblePuzzle[row][col] = n;
                if(recursiveGenerator(possiblePuzzle)){
                    return true;
                }else{
                    availableNumbersCol.get(col).add(n);
                    availableNumbersRow.get(row).add(n);
                    availableNumbersGrp.get(group).add(n);
                    possiblePuzzle[row][col] = 0;
                }
            }
            // If all the numbers where checked and nothing worked false out
            return false;
        }
        
        // How other classes get the value in the cell
        int getCell(int row, int col){
            return puzzle[row][col];
        }
    }
    
    // Tile class for the individual puzzle elements
    private class Tile extends JPanel{
        private Sudoku parent;
        private JPanel content,keypad;
        private JLabel text;
        private int value;
        private boolean locked = false;
        
        Tile(Sudoku p){
            // Set the parent frame for functionality between tiles
            parent = p;
            
            // Create the content panel to show the curent value
            content = new JPanel();
            text = new JLabel("");
            text.setFont(text.getFont().deriveFont(20.0f));// Set the font bigger
            content.setLayout(new GridBagLayout());
            content.add(text);
            
            // Add a mouse listener to show the keypad when clicked
            content.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if(!locked){
                        // Get the Tile that was clicked
                        Tile source = (Tile)(e.getComponent().getParent());
                        // Show the keypad on this tile and hide any others
                        if(parent.currentTile != source){
                            if(parent.currentTile != null)
                                parent.currentTile.showKeypad(false);
                            parent.currentTile = source;
                            source.showKeypad(true);
                        }
                    }
                }
            });
            
            // Create the keypad panel to show the 9 possible choices
            keypad = new JPanel();
            keypad.setLayout(new GridLayout(3,3));
            for(int i=1; i<10; i++){
                JLabel digit = new JLabel(Integer.toString(i));
                // Need to set the default color to "black" for later testing
                digit.setForeground(Color.black);
                // Add a mouse listener to the digit so we know which one is clicked
                digit.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        // Get the digit that was click
                        JLabel digit = (JLabel)(e.getComponent());
                        Tile source = (Tile)digit.getParent().getParent();
                        // On left click select the number
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            // Check to see if the digit was mark as not usable (gray)
                            if((Color)(e.getComponent().getForeground()) != Color.gray){
                                source.setValue(Integer.parseInt(digit.getText()));
                                source.showKeypad(false);
                                source.parent.currentTile = null;
                            }
                        }
                        // On right click cycle the color
                        if (SwingUtilities.isRightMouseButton(e)) {
                            Color setting = (Color)(e.getComponent().getForeground());
                            if(setting == Color.black)
                                e.getComponent().setForeground(Color.red);
                            else if(setting == Color.red)
                                e.getComponent().setForeground(Color.gray);
                            else if(setting == Color.gray)
                                e.getComponent().setForeground(Color.black);
                        }
                    }
                });
                // Add the digit to the keypad Panel
                keypad.add(digit);
            }
            
            // Add a keyboard listener to the panel to allow the user to use the keyboard as well
            keypad.setFocusable(true);
            keypad.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    // Get the Tile that is active
                    Tile source = (Tile)e.getComponent().getParent();
                    // Check to see if the key pressed was a digit
                    if(Character.isDigit(e.getKeyChar())){
                        // Convert the key to a number 0 - 9
                        int selection = e.getKeyChar() - 48;
                        if(selection > 0){
                            boolean valid = true;
                            // Check to see if that digit was marked as invalid
                            // Step through all the components in Keypad
                            for(Component c:keypad.getComponents()){
                                // Check if it is a JLabel incase there are extras
                                if(c.getClass().equals(JLabel.class)){
                                    // Convert it to a label and test if the numbers match
                                    JLabel tempLabel = (JLabel)c;
                                    // Check to see if the selection digit was already grayed out
                                    if(tempLabel.getText().equals(Integer.toString(selection))){
                                        if(tempLabel.getForeground() == Color.gray)
                                            valid = false;
                                    }
                                }
                            }
                            if(valid){
                                source.setValue(selection);
                                source.showKeypad(false);
                                source.parent.currentTile = null;
                            }
                        }
                    }
                }
            });
            
            // Create a card layout, hold the two panels, and make the layout visable
            this.setLayout(new CardLayout());
            this.add(content, "content");
            this.add(keypad, "keypad");
        }
        
        // "T" brings the keypad forward while "F" brings the content forward
        void showKeypad(boolean key){
            CardLayout cl = (CardLayout)(this.getLayout());
            if(key){
                cl.show(this, "keypad");
                keypad.requestFocusInWindow();
            }
            else
                cl.show(this, "content");
        }
        
        // Change the value of the tile
        void setValue(int value){
            this.value = value;
            text.setText(Integer.toString(value));
            content.repaint();
        }
        
        // Get the current value of the tile
        int getValue(){
            return value;
        }
        
        // Change the color of the tile
        void mark(boolean key){
            if(key)
                content.setBackground(Color.red);
            else
                content.setBackground(Color.white);
        }
        
        // Blank the cell so we can reuse it
        void clear(){
            text.setText("");
        }
        
        // Lock the tile so it can't be changed
        void lock(){
            locked = true;
        }
        
        // Unlock the tile and make it editable
        void unlock(){
            locked = false;
        }
        
        // Convert to string for printing
        public String toString(){
            String printString;
            printString = value + ":";
            if(locked)
                printString += "T";
            else
                printString += "F";
            return printString;
        }
    }
    
    // Setup the  GUI
    Sudoku(){
        // Set Look and Feel
        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch (Exception e) {}

        setTitle("Sudoku");
        setSize(500, 500); // Set the initial size of the application window
        setLayout(new BorderLayout()); // Set the layout as border for better apperance
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Makes the window close when the "X" is pressed
        
        // Create the Sudoku Main Grid
        JPanel grid = new JPanel();
        grid.setLayout(new GridLayout(9,9));
        tiles = new Tile[9][9];

        // Create the 81 tiles and add them to the grid
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                // Intialize values for the borders to make the 9 sub-grids
                int top=1,left=1,bottom=1,right=1;
                switch(i){
                    case 0: 
                    case 3:
                    case 6: top = 3; break;
                    case 8: bottom = 3; break;
                    default: break;
                }
                switch(j){
                    case 0: 
                    case 3:
                    case 6: left = 3; break;
                    case 8: right = 3; break;
                    default: break;
                }
                // Intialize the text field and put it in the grid
                tiles[i][j] = new Tile(this);
                tiles[i][j].setBorder(new MatteBorder(top,left,bottom,right,Color.BLACK));
                grid.add(tiles[i][j]);
            }
        }
        
        // Create the Conrtol Buttons
        JPanel control = new JPanel();
        JButton btnNew = new JButton("New");
        JButton btnCheck = new JButton("Check");
        JButton btnSave = new JButton("Save");
        JButton btnLoad = new JButton("Load");
        control.add(btnNew);
        control.add(btnCheck);
        control.add(btnSave);
        control.add(btnLoad);
        
        // Here are the action listeners for the buttons
        // Generate a new puzzle
        btnNew.addActionListener((ActionEvent e) -> {
            generatePuzzle();
        });
        
        // Check if the puzzle was completed corectly
        btnCheck.addActionListener((ActionEvent e) -> {
            if(checkPuzzle())
                JOptionPane.showMessageDialog(this, "It is good");
            else
                JOptionPane.showMessageDialog(this, "It is bad");
        });
        
        // Save the current puzzle to a file
        btnSave.addActionListener((ActionEvent e) -> {
            save();
        });
        
        // Load a saved puzzle from a file
        btnLoad.addActionListener((ActionEvent e) -> {
            load();
        });
        
        // Add all the panels to the Frame
        add(grid, BorderLayout.CENTER);
        add(control, BorderLayout.SOUTH);
        
        //Make frame visible
        setVisible(true);
    }
    
    // This is the way to launch the GUI
    public static void main (String[] args){
        // Create the GUI to start the game
        Sudoku GUI = new Sudoku();
        
    }
}
