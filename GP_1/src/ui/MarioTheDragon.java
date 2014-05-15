/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ui;

/**
 *
 * @ Created by Gabriel Simón Alarcón
 */
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public final class MarioTheDragon implements MouseListener
{
	public static void main (String [] args)
	{
		MarioTheDragon a = new MarioTheDragon();
	}
	
	private JFrame background;
	private Container container;
	private JButton button;
	private ImagePanel back;
	
	public static boolean paused;
	public static boolean crashed;
	public static boolean started;
	public static boolean playedOnce;	
	
	public boolean goingUp;
	private double upCount;
	
	public static int distance;
	public static int maxDistance;
	
	public final int XPOS;
	public final int NUMRECS;
	public final int RECHEIGHT;
	public final int RECWIDTH;
	
	private int moveIncrement;
	private int numSmoke;
	
	private ArrayList<MovingImage> toprecs;
	private ArrayList<MovingImage> bottomrecs;
	private ArrayList<MovingImage> middlerecs;
	private ArrayList<MovingImage> recs;
	private ArrayList<MovingImage> smoke;
	private MovingImage dragon;
	
	//private MP3 move = new MP3("HelicopterSound.mp3");
	
	/*Graphics information:
	 *Background is 812 x 537
	 *Floor is 74 and Ceiling is 72 pixels high
	 *28 rectangles across that are 29 x 73
	 */
	
	
	public MarioTheDragon()
	{
		NUMRECS = 28;
		RECHEIGHT = 73;
		RECWIDTH = 29;
		XPOS = 200;
		playedOnce = false;
		maxDistance = 0;
		
		load(new File("Best.txt"));
		
		initiate();
	}
	
	public void load(File file)
	{
		try
		{
			Scanner reader = new Scanner(file);
			while(reader.hasNext())
			{
				int value = reader.nextInt();
				if(value > maxDistance)
					maxDistance = value;
			}
		}
		catch(IOException i )
		{
			System.out.println("Error. "+i);
		}
	}
		
	public void save()
	{
		FileWriter out;
		try
		{
			out = new FileWriter("Best.txt");
			out.write("" + maxDistance);
			out.close();
		}
		catch(IOException i)
		{
			System.out.println("Error: "+i.getMessage());
		}
	}
	
	public void initiate()
	{
		if(!playedOnce)
		{
			background = new JFrame("Mario the Dragon"); 
			background.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //closes the program when the window is closed
			background.setResizable(false); //don't allow the user to resize the window
			background.setSize(new Dimension(818,568));
			background.setVisible(true);
			
			back = new ImagePanel("back.JPG");
			background.add(back);
			
			back.addMouseListener(this);
		}
		playedOnce = true;
		goingUp = false;
		paused = false;
		crashed = false;
		started = false;
		
		distance = 0;
		upCount = 0;
		
		moveIncrement = 2;
		numSmoke = 1000;
		
		recs = new ArrayList<>();
		toprecs = new ArrayList<>();
		middlerecs = new ArrayList<>();
		bottomrecs = new ArrayList<>();
		smoke = new ArrayList<>();
		
		dragon = new MovingImage("helicopter.GIF",XPOS,270);
		
		for(int x = 0; x < NUMRECS; x++)
			toprecs.add(new MovingImage("rec2.JPG",RECWIDTH*x,30));
		for(int x = 0; x < NUMRECS; x++)
			bottomrecs.add(new MovingImage("rec2.JPG",RECWIDTH*x,450));
			
		middlerecs.add(new MovingImage("rec1.JPG",1392,randomMidHeight()));
		middlerecs.add(new MovingImage("rec1.JPG",1972,randomMidHeight()));
	
			
		drawRectangles();
	}
	
	public void drawRectangles()
	{
		long last = System.currentTimeMillis();
		long lastCopter = System.currentTimeMillis();
		long lastSmoke = System.currentTimeMillis();
		long lastSound = System.currentTimeMillis();
		int firstUpdates = 0;
		double lastDistance = (double)System.currentTimeMillis();
		while(true)
		{
			if(!paused && !crashed && started && (double)System.currentTimeMillis() - (double)(2900/40) > lastDistance)
			{	
				lastDistance = System.currentTimeMillis();
				distance++;
			}	
			
		/*	if(!paused && !crashed && started && System.currentTimeMillis() - 1300 > lastSound)
			{
				lastSound = System.currentTimeMillis();
				move.play();
			}
		*/
		
			if(!paused && !crashed && started && System.currentTimeMillis() - 10 > lastCopter)
			{
				lastCopter = System.currentTimeMillis();
				updateCopter();
				updateMiddle();
			}
			if(!paused && !crashed && started && System.currentTimeMillis() - 100 > last)
			{
				last = System.currentTimeMillis();
				updateRecs();
			}
			if(!paused && !crashed && started && System.currentTimeMillis() - 75 > lastSmoke)
			{
				lastSmoke = System.currentTimeMillis();
				if (firstUpdates < numSmoke)
				{
					firstUpdates++;
					smoke.add(new MovingImage("smoke.GIF",187,dragon.getY()));
					for(int x = 0; x < firstUpdates; x++)
						smoke.set(x,new MovingImage("smoke.GIF",smoke.get(x).getX() - 12, smoke.get(x).getY()));
				}
				else
				{
					for(int x = 0; x < numSmoke - 1; x++)
						smoke.get(x).setY(smoke.get(x+1).getY());
					smoke.set(numSmoke - 1,new MovingImage("smoke.GIF",187,dragon.getY()));
				}
					}
					back.updateImages(toprecs,middlerecs,bottomrecs,dragon,smoke);
				}
	}
	
	public void updateRecs()
	{
		for(int x = 0; x < (NUMRECS - 1); x++) //move all but the last rectangle 1 spot to the left
		{
			toprecs.set(x,new MovingImage("rec2.JPG",RECWIDTH*x,toprecs.get(x+1).getY()));
			bottomrecs.set(x,new MovingImage("rec2.JPG",RECWIDTH*x,bottomrecs.get(x+1).getY()));
		}
		lastRec();
	}
	
	public void lastRec()
	{
		if(distance % 400 == 0)
			moveIncrement++;
		if(toprecs.get(26).getY() < 2) //if too high, move down
			moveDown();
		else if (bottomrecs.get(26).getY() > 463) //else if too low, move up
			moveUp();
		else //else move randomly
		{
			if((int)(Math.random() * 60) == 50)
				randomDrop();
			else
			{
				if((int)(Math.random() * 2) == 1)
					moveUp();
				else
					moveDown();
			}
		}
	}
	
	public void randomDrop()
	{
		toprecs.get(26).setY(toprecs.get(26).getY() + (463 - bottomrecs.get(26).getY()));
		bottomrecs.get(26).setY(463);
	}
	
	public void moveDown()
	{
		toprecs.set((NUMRECS - 1),new MovingImage("rec2.JPG",RECWIDTH*(NUMRECS - 1),toprecs.get(26).getY() + moveIncrement));
		bottomrecs.set((NUMRECS - 1),new MovingImage("rec2.JPG",RECWIDTH*(NUMRECS - 1),bottomrecs.get(26).getY() + moveIncrement));
	}
	
	public void moveUp()
	{
		bottomrecs.set((NUMRECS - 1),new MovingImage("rec2.JPG",RECWIDTH*(NUMRECS - 1),bottomrecs.get(26).getY() - moveIncrement));
		toprecs.set((NUMRECS - 1),new MovingImage("rec2.JPG",RECWIDTH*(NUMRECS - 1),toprecs.get(26).getY() - moveIncrement));
	}
	
	public int randomMidHeight()
	{
		int max = 10000;
		int min = 0;
		
		for(int x = 0; x < NUMRECS; x++)
		{
			if(toprecs.get(x).getY() > min)
				min = (int)toprecs.get(x).getY();
			if(bottomrecs.get(x).getY() < max)
				max = (int)bottomrecs.get(x).getY();
		}
		min += RECHEIGHT;
		max -= (RECHEIGHT + min);
		return min + (int)(Math.random() * max);
	}
	
	//moves the randomly generated middle rectangles
	public void updateMiddle()
	{
		if(middlerecs.get(0).getX() > -1 * RECWIDTH)
		{
			middlerecs.set(0,new MovingImage("rec2.JPG",middlerecs.get(0).getX() - (RECWIDTH/5), middlerecs.get(0).getY()));
			middlerecs.set(1,new MovingImage("rec2.JPG",middlerecs.get(1).getX() - (RECWIDTH/5), middlerecs.get(1).getY()));
		}
		else
		{
			middlerecs.set(0,new MovingImage("rec2.JPG",middlerecs.get(1).getX() - (RECWIDTH/5), middlerecs.get(1).getY()));
			middlerecs.set(1,new MovingImage("rec2.JPG",middlerecs.get(0).getX() + 580,randomMidHeight()));
		}
	}
	
	public boolean shoot()
	{
		for(int x = 3; x <= 7; x++)
			if(dragon.getY() + 60 >= bottomrecs.get(x).getY())
				return true;
		
		for(int y = 3; y <= 7; y++)
				if(dragon.getY() <= toprecs.get(y).getY() + RECHEIGHT)
					return true;
		for(int z = 0; z <= 1; z++)
			if(isInMidRange(z))
				return true;
		return false;
	}
	
	public boolean isInMidRange(int num)
	{
		Rectangle middlecheck = new Rectangle((int)middlerecs.get(num).getX(),(int)middlerecs.get(num).getY(),RECWIDTH,RECHEIGHT);
		Rectangle coptercheck = new Rectangle((int)dragon.getX(),(int)dragon.getY(),106,48);
		return middlecheck.intersects(coptercheck);
	}
	
	public void crash()
	{
		crashed = true;
		if(distance > maxDistance) 
		{
			maxDistance = distance;
			save();
		}
			
		initiate();
	}
	
	//moves the helicopter
	public void updateCopter()
	{
		upCount += .08;
		if(goingUp)
		{
			if(upCount < 3.5)
				dragon.setPosition(XPOS,(double)(dragon.getY() - (.3 + upCount)));
			else
				dragon.setPosition(XPOS,(double)(dragon.getY() - (1.2 + upCount)));
			dragon.setImage("upCopter.GIF");	
		}
		else
		{
			if(upCount < 1)
				dragon.setPosition(XPOS,(double)(dragon.getY() + upCount));
			else
				dragon.setPosition(XPOS,(double)(dragon.getY() + (1.2 + upCount)));
			dragon.setImage("helicopter.GIF");
		}
		if(shoot())
			crash();
	}
	
	//Called when the mouse exits the game window
        @Override
	public void mouseExited(MouseEvent e)
	{		
		if(started)
		{
			paused = true;
			//move.close();	
		}		
	}
	
	//Called when the mouse enters the game window
        @Override
	public void mouseEntered(MouseEvent e){}
	
	//Called when the mouse is released
        @Override
	public void mouseReleased(MouseEvent e)
	{
		goingUp = false;
		upCount = 0;
		if(paused)
			paused = false;
	}
	
	//Called when the mouse is pressed
        @Override
	public void mousePressed(MouseEvent e)
	{
		if (!started)
			started = true;
		goingUp = true;
		upCount = 0;
	}
	
	//Called when the mouse is clicked
        @Override
	public void mouseClicked(MouseEvent e){}
}
class ImagePanel extends JPanel {

  	private Image background;		//The background image
  	private ArrayList<MovingImage> top;	//An array list of foreground images
	private ArrayList<MovingImage> bottom;
	private ArrayList<MovingImage> middle;
	private MovingImage mario;
	private ArrayList<MovingImage> smoke;
	
	//Constructs a new ImagePanel with the background image specified by the file path given
  	public ImagePanel(String img) 
  	{
  		this(new ImageIcon(img).getImage());	
  		//The easiest way to make images from file paths in Swing
  	}

	//Constructs a new ImagePanel with the background image given
  	public ImagePanel(Image img)
  	{
    	background = img;
    	Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));	
    	//Get the size of the image
    	//Thoroughly make the size of the panel equal to the size of the image
    	//(Various layout managers will try to mess with the size of things to fit everything)
    	setPreferredSize(size);
    	setMinimumSize(size);
    	setMaximumSize(size);
    	setSize(size);
    	
    	top = new ArrayList<>();
    	middle = new ArrayList<>();
    	bottom = new ArrayList<>();
    	
    	//smoke = new ArrayList<>();
  	}

	//This is called whenever the computer decides to repaint the window
	//It's a method in JPanel that I've overwritten to paint the background and foreground images
          @Override
  	public void paintComponent(Graphics g) 
  	{
  		//Paint the background with its upper left corner at the upper left corner of the panel
    	g.drawImage(background, 0, 0, null); 
    	//Paint each image in the foreground where it should go
    	for(MovingImage img : top)
    		g.drawImage(img.getImage(), (int)(img.getX()), (int)(img.getY()), null);
    	for(MovingImage img : middle)
    		g.drawImage(img.getImage(), (int)(img.getX()), (int)(img.getY()), null);
    	for(MovingImage img : bottom)
    		g.drawImage(img.getImage(), (int)(img.getX()), (int)(img.getY()), null);
    	for(MovingImage img : smoke)
    		g.drawImage(img.getImage(), (int)(img.getX()), (int)(img.getY()), null);
    	if(mario != null)
    		g.drawImage(mario.getImage(), (int)(mario.getX()), (int)(mario.getY()), null);
    	drawStrings(g);
  	}
 	
 	public void drawStrings(Graphics g)
 	{
            g.setFont(new Font("Arial",Font.BOLD,20));
            g.drawString("Distance: " + MarioTheDragon.distance,30,500);
            g.setFont(new Font("Arial",Font.BOLD,20));
            
            if (MarioTheDragon.distance > MarioTheDragon.maxDistance)
                g.drawString("Best: " + MarioTheDragon.distance,650,500);
            else
                g.drawString("Best: " + MarioTheDragon.maxDistance,650,500);
            
            if(MarioTheDragon.paused)
            {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Chiller",Font.BOLD,72));
                g.drawString("Paused",325,290);
                g.setFont(new Font("Chiller",Font.BOLD,30));
                g.drawString("Click to unpause.",320,340);
            }
 	}
 	
  	//Replaces the list of foreground images with the one given, and repaints the panel
  	public void updateImages(ArrayList<MovingImage> newTop,ArrayList<MovingImage> newMiddle,ArrayList<MovingImage> newBottom,MovingImage newCopter,ArrayList<MovingImage> newSmoke)
  	{
  		top = newTop;
  		mario = newCopter;
  		middle = newMiddle;
  		bottom = newBottom;
  		smoke = newSmoke;
  		repaint();	//This repaints stuff... you don't need to know how it works
  	}
}


class MovingImage
{
	private Image image;		//The picture
	private double x;			//X position
	private double y;			//Y position
	
	//Construct a new Moving Image with image, x position, and y position given
	public MovingImage(Image img, double xPos, double yPos)
	{
		image = img;
		x = xPos;
		y = yPos;
	}
	
	//Construct a new Moving Image with image (from file path), x position, and y position given
	public MovingImage(String path, double xPos, double yPos)
	{
		this(new ImageIcon(path).getImage(), xPos, yPos);	
			//easiest way to make an image from a file path in Swing
	}
	
	//They are set methods.  I don't feel like commenting them.
	public void setPosition(double xPos, double yPos)
	{
		x = xPos;
		y = yPos;
	}
	
	public void setImage(String path)
	{
		image = new ImageIcon(path).getImage();
	}
	
	public void setY(double newY)
	{
		y = newY;
	}
	
	public void setX(double newX)
	{
		x = newX;
	}
	
	//Get methods which I'm also not commenting
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	
	public Image getImage()
	{
		return image;
	}
}
/*
class MP3 {
    private String filename;
    private Player player; 

    // constructor that takes the name of an MP3 file
    public MP3(String filename) {
        this.filename = filename;
    }

    public void close() { if (player != null) player.close(); }

    // play the MP3 file to the sound card
    public void play() {
        try {
            FileInputStream fis     = new FileInputStream(filename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            player = new Player(bis);
        }
        catch (Exception e) {
            System.out.println("Problem playing file " + filename);
            System.out.println(e);
        }

        // run in new thread to play in background
        new Thread() {
            public void run() {
                try { player.play(); }
                catch (Exception e) { System.out.println(e); }
            }
        }.start();
    }

}
*/