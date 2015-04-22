// package crappyBird;

import java.awt.Graphics;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/*
 * This screen just waits for a mouseclick, whereupon it is removed from the JFrame and replaced with the game
 * I'd like not have to wait for the picture to download though, so I need to figure out how to package the images
 * within the Jar file. 
 */

public class Menu extends JPanel{
	private static final long serialVersionUID = 1L;
	int highscore;
	
	//gets the background from imgur
	static BufferedImage img = null;{
		try {
			img = ImageIO.read(new URL("http://i.imgur.com/U6KEwxe.png"));
		} catch (IOException e) {
			System.out.println("WRONG MENU");
		}}
	
	boolean startGame = false;						//the boolean toggle that starts the game over in ExecuteMe
	
	
	public Menu(){
		setFocusable(true);							//waits for a mouseclick, then toggles startGame
		addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				startGame = true;
			}

			});
	}
	
	public void paint (Graphics g){
		super.paint(g);
	
		g.drawImage(img, 0, 0, null);				//paints background
	
		
	}
}
	
