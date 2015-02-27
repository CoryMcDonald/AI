// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import javax.swing.JFrame;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import java.awt.event.WindowEvent;

public class View extends JFrame implements ActionListener {
	Controller controller;
	Model model;
	private Object secret_symbol; // used to limit access to methods that agents could potentially use to cheat
	private MyPanel panel;

	public View(Controller c, Model m, Object symbol) throws Exception {
		this.controller = c;
		this.model = m;
		secret_symbol = symbol;

		// Make the game window
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("AI Tournament");
		this.setSize(1203, 626);
		this.panel = new MyPanel(c, m, symbol);
		this.getContentPane().add(this.panel);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) {
		repaint(); // indirectly calls MyPanel.paintComponent
	}

	class MyPanel extends JPanel {
		public static final int FLAG_IMAGE_HEIGHT = 25;

		Controller controller;
		private Object secret_symbol; // used to limit access to methods that agents could potentially use to cheat
		Model model;
		Image image_robot_blue;
		Image image_robot_red;
		Image image_broken;
		Image image_flag_blue;
		Image image_flag_red;
		MySoundClip sound_doing;

		MyPanel(Controller c, Model m, Object symbol) throws Exception {
			this.controller = c;
			this.model = m;
			this.secret_symbol = symbol;
			this.addMouseListener(c);
			this.image_robot_blue = ImageIO.read(new File("robot_blue.png"));
			this.image_robot_red = ImageIO.read(new File("robot_red.png"));
			this.image_broken = ImageIO.read(new File("broken.png"));
			this.image_flag_blue = ImageIO.read(new File("flag_blue.png"));
			this.image_flag_red = ImageIO.read(new File("flag_red.png"));
			// this.sound_doing = new MySoundClip("metal_doing.wav", 3);
		}

		void drawTerrain(Graphics g) {
			byte[] terrain = this.model.getTerrain(secret_symbol);
			int posBlue = 0;
			int posRed = (60 * 60 - 1) * 4;
			for(int y = 0; y < 60; y++) {
				for(int x = 0; x < 60; x++) {
					int bb = terrain[posBlue + 1] & 0xff;
					int gg = terrain[posBlue + 2] & 0xff;
					int rr = terrain[posBlue + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posBlue += 4;
				}
				for(int x = 60; x < 120; x++) {
					int bb = terrain[posRed + 1] & 0xff;
					int gg = terrain[posRed + 2] & 0xff;
					int rr = terrain[posRed + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posRed -= 4;
				}
			}
		}

		void drawSprites(Graphics g) {
			ArrayList<Model.Sprite> sprites_blue = this.model.getSpritesBlue(this.secret_symbol);
			for(int i = 0; i < sprites_blue.size(); i++) {

				// Draw the robot image
				Model.Sprite s = sprites_blue.get(i);
				if(s.energy >= 0) {
					g.drawImage(image_robot_blue, (int)s.x - 12, (int)s.y - 32, null);

					// Draw energy bar
					g.setColor(new Color(0, 0, 128));
					g.drawRect((int)s.x - 18, (int)s.y - 32, 3, 32);
					int energy = (int)(s.energy * 32.0f);
					g.fillRect((int)s.x - 17, (int)s.y - energy, 2, energy);
				}
				else
					g.drawImage(image_broken, (int)s.x - 12, (int)s.y - 32, null);

				// Draw selection box
				if(i == controller.getSelectedSprite())
				{
					g.setColor(new Color(100, 0, 0));
					g.drawRect((int)s.x - 22, (int)s.y - 42, 44, 57);
				}
			}
			ArrayList<Model.Sprite> sprites_red = this.model.getSpritesRed(this.secret_symbol);
			for(int i = 0; i < sprites_red.size(); i++) {

				// Draw the robot image
				Model.Sprite s = sprites_red.get(i);
				if(s.energy >= 0) {
					g.drawImage(image_robot_red, (int)(Model.XMAX - 1 - s.x) - 12, (int)(Model.YMAX - 1 - s.y) - 32, null);

					// Draw energy bar
					g.setColor(new Color(128, 0, 0));
					g.drawRect((int)(Model.XMAX - 1 - s.x) + 14, (int)(Model.YMAX - 1 - s.y) - 32, 3, 32);
					int energy = (int)(s.energy * 32.0f);
					g.fillRect((int)(Model.XMAX - 1 - s.x) + 15, (int)(Model.YMAX - 1 - s.y) - energy, 2, energy);
				}
				else
					g.drawImage(image_broken, (int)(Model.XMAX - 1 - s.x) - 12, (int)(Model.YMAX - 1 - s.y) - 32, null);
			}
		}

		void drawBombs(Graphics g) {
			ArrayList<Model.Bomb> bombs = this.model.getBombsFlying(this.secret_symbol);
			for(int i = 0; i < bombs.size(); i++) {
				Model.Bomb b = bombs.get(i);
				int x = (int)b.getX();
				int y = (int)b.getY();
				int height = (int)(0.01 * b.position * (b.distance - b.position));
				g.setColor(new Color(128, 64, 192));
				g.fillOval(x - 5, y - 5 - height, 10, 10);
				g.setColor(new Color(100, 100, 100));
				g.fillOval(x - 5, y - 5, 10, 10);
			}
			bombs = this.model.getBombsExploding(this.secret_symbol);
			for(int i = 0; i < bombs.size(); i++) {
				Model.Bomb b = bombs.get(i);
				int x = (int)b.getX();
				int y = (int)b.getY();
				if(b.isDetonating())
					sound_doing.play();
				g.setColor(new Color(128, 0, 64));
				int r = (int)(b.position - b.distance);
				g.drawOval(x - r, y - r, 2 * r, 2 * r);
				r = (int)Model.BLAST_RADIUS;
				g.drawOval(x - r, y - r, 2 * r, 2 * r);
			}
		}

		void drawFlags(Graphics g) {
			// Blue
			g.drawImage(image_flag_blue, (int)Model.XFLAG, (int)Model.YFLAG - FLAG_IMAGE_HEIGHT, null);
			g.setColor(new Color(0, 0, 128));
			g.drawRect((int)Model.XFLAG - 3, (int)Model.YFLAG - 25, 3, 32);
			int energy = (int)(model.getFlagEnergySelf() * 32.0f);
			g.fillRect((int)Model.XFLAG - 2, (int)Model.YFLAG + 7 - energy, 2, energy);

			// Red
			g.drawImage(image_flag_red, (int)Model.XFLAG_OPPONENT,  (int)Model.YFLAG_OPPONENT - FLAG_IMAGE_HEIGHT, null);
			g.setColor(new Color(128, 0, 0));
			g.drawRect((int)Model.XFLAG_OPPONENT - 3, (int)Model.YFLAG_OPPONENT - 25, 3, 32);
			energy = (int)(model.getFlagEnergyOpponent() * 32.0f);
			g.fillRect((int)Model.XFLAG_OPPONENT - 2, (int)Model.YFLAG_OPPONENT + 7 - energy, 2, energy);
		}

		public void paintComponent(Graphics g) {
			// Give the agents a chance to make decisions
			if(!this.controller.update()) {
				model.setPerspectiveBlue(secret_symbol);
				if(model.getFlagEnergySelf() < 0.0f)
					System.out.println("\nRed wins!");
				else if(model.getFlagEnergyOpponent() < 0.0f)
					System.out.println("\nBlue wins!");
				else
					System.out.println("\nTie.");
				View.this.dispatchEvent(new WindowEvent(View.this, WindowEvent.WINDOW_CLOSING)); // The game is over, so close this window
			}

			// Draw the view
			model.setPerspectiveBlue(secret_symbol);
			drawTerrain(g);
			drawFlags(g);
			drawSprites(g);
			drawBombs(g);
		}
	}

	class MySoundClip {
		Clip[] clips;
		int pos;

		MySoundClip(String filename, int copies) throws Exception {
			clips = new Clip[copies];
			for(int i = 0; i < copies; i++) {
				clips[i] = AudioSystem.getClip();
				AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
				clips[i].open(ais);
			}
			pos = 0;
		}

		void play() {
			clips[pos].setFramePosition(0);
			clips[pos].loop(0);
			if(++pos >= clips.length)
				pos = 0;
		}
	}
}
