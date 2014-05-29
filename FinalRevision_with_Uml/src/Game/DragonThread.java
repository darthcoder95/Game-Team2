package Game;

public class  DragonThread extends Thread {

	public void run()
	{
		 final MP3 player = new MP3 ();
	     player.play("dragon.aiff");
	}
}
