/***********************************************************************************************\
|* Sudoku is a game to fill in tiles of the board without repeating numbers in rows or columns *|
\***********************************************************************************************/

package Sudoku;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Sudoku extends JFrame {

    // Class wide variables
    static Tile[][] tiles;
    static int[][] puzzle = new int[9][9]; //Holds the puzzle values
    static Tile currentTile;
    
    // Tile class for the individual puzzle elements
    private class Tile extends JPanel{
        private Sudoku parent;
        private JPanel content,keypad;
        private JLabel text;
        private String value = "";
        private boolean locked = false;
        
        Tile(Sudoku p){
            // Set the parent frame for functionality between tiles
            parent = p;
            
            // Create the content panel to show the curent value
            content = new JPanel();
            text = new JLabel("");
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
                                source.setText(digit.getText());
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
                                source.setText(Integer.toString(selection));
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
        void setText(String value){
            this.value = value;
            text.setText(value);
            content.repaint();
        }
        
        // Get the current value of the tile
        String getText(){
            return value;
        }
        
        // Change the color of the tile
        void mark(boolean key){
            if(key)
                content.setBackground(Color.red);
            else
                content.setBackground(Color.white);
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
        JButton btnShow = new JButton("Show Solution");
        JButton btnSave = new JButton("Save");
        JButton btnLoad = new JButton("Load");
        control.add(btnNew);
        control.add(btnCheck);
        control.add(btnShow);
        control.add(btnSave);
        control.add(btnLoad);
        
        // Here are the action listeners for the buttons
        // Generate a new puzzle
        btnNew.addActionListener((ActionEvent e) -> {
            
        });
        
        // Check if the puzzle was completed corectly
        btnCheck.addActionListener((ActionEvent e) -> {
            
        });
        
        // Show the answer to the current puzzle
        btnShow.addActionListener((ActionEvent e) -> {
            
        });
        
        // Save the current puzzle to a file
        btnSave.addActionListener((ActionEvent e) -> {
            
        });
        
        // Load a saved puzzle from a file
        btnLoad.addActionListener((ActionEvent e) -> {
            
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
