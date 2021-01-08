import java.awt.*;
import java.awt.image.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import java.io.IOException;

class Environment
{
	private BBlocks bb;				// Building blocks
	private GLCanvas canvas; 		// for media tracker    
	
	private final int	x1 = 0, y1 = 0, z1 = -25, x2 = 200, y2 = 32, z2 = 25,
							halfY = (y1 + y2) / 2, halfX = (x1 + x2) / 2, halfZ = (z1 + z2) / 2;
	
	public Environment(GL gl, GLCanvas canvas) 
	{
		this.canvas = canvas;
		bb = new BBlocks(gl, canvas);
	}
	
	// moving ball as Sonic
	
	// FALSE WALL!!!!!
	// in caves area
	
	// floating platforms for first boss area
	
	public void draw(GL gl)
	{
		int i, j;
		
		gl.glColor3f(1, 1, 1);
		
		// Draws Loop
		bb.draw(gl, bb.loop, 3*halfX/2, halfY, halfZ-halfY/3, 0, halfY, halfY, 2*halfY/3, 2.25f, 2.25f);
		
		// Draws Houses
		bb.draw(gl, bb.house, halfX+halfY+16, halfY, halfZ-halfY/3, 90, halfX+5*halfY/3+16, 3*halfY/2, halfZ-halfY-1, 16, 5/2);
		bb.draw(gl, bb.house, halfX+halfY+34, halfY, z1+2, 90, halfX+halfY+34+halfZ-z1-halfY/3-4, 3*halfY/2, z1-2*halfY/3-4, 16, 5/2);
		bb.draw(gl, bb.house, halfX+halfY+34, halfY, halfZ+halfY/3+2, 90, halfX+halfY+34+z2-halfY/3-4, 3*halfY/2, halfZ-halfY/3-4, 16, 5/2);
		
		// Draws Towers
		bb.draw(gl, bb.tower, halfX+halfY+35, 3*halfY/2, z1+3, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+35, 3*halfY/2, z1+11, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+43, 3*halfY/2, z1+3, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+43, 3*halfY/2, z1+11, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+35, 3*halfY/2, halfZ+halfY/3+3, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+35, 3*halfY/2, halfZ+halfY/3+11, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+43, 3*halfY/2, halfZ+halfY/3+3, 0, 3, halfY/2, 0, 0, 0);
		bb.draw(gl, bb.tower, halfX+halfY+43, 3*halfY/2, halfZ+halfY/3+11, 0, 3, halfY/2, 0, 0, 0);
		
		// Draws Platforms
		//bb.draw(gl, 
		
		// Draws Stairs
		bb.draw(gl, bb.steps, halfX-halfY, y1, z1, 0, halfX+halfY, halfY, z2, halfY, 0);
		bb.draw(gl, bb.wall, halfX, y1, z1, 0, halfX+halfY, halfY, z1, 1, 0);
		bb.draw(gl, bb.wall, halfX, y1, z2, 0, halfX+halfY, halfY, z2, 1, 0);
		
		// Draws Pit and Ground above Marble Zone
		bb.draw(gl, bb.floor, x1, halfY, z1, 0, x1+30, z2, 2, 0, 0);
		bb.draw(gl, bb.wall, x1+30, halfY, z1, 0, x1+30, 3*halfY/2, z2, 2, 0);
		bb.draw(gl, bb.floor, x1+30, 3*halfY/2-2, z1, 0, halfX, z2, 2, 0, 0);
		bb.draw(gl, bb.wall, halfX, 3*halfY/2-2, z1, 0, halfX, 3*halfY/2, z2, 2, 0);
		bb.draw(gl, bb.floor, x1+30, 3*halfY/2, z1, 0, halfX, z2, 3, 0, 0);
		bb.draw(gl, bb.floor, x1, 5*halfY/4, z1, 0, x1+30, z2, 2, 0, 0);
		bb.draw(gl, bb.floor, x1, 3*halfY/2, z1, 0, x1+15, z1+15, 2, 0, 0);
		bb.draw(gl, bb.floor, x1, 3*halfY/2, z2-15, 0, x1+15, z2, 2, 0, 0);
		bb.draw(gl, bb.wall, x1+15, halfY, z1, 0, x1+15, 3*halfY/2, z1+15, 2, 0);
		bb.draw(gl, bb.wall, x1, halfY, z1+15, 0, x1+15, 3*halfY/2, z1+15, 2, 0);
		bb.draw(gl, bb.wall, x1, halfY, z2-15, 0, x1+15, 3*halfY/2, z2-15, 2, 0);
		bb.draw(gl, bb.wall, x1+15, halfY, z2-15, 0, x1+15, 3*halfY/2, z2, 2, 0);
		
		// Draws stuuf in Marble Zone
		
		// Draws Floor and Walls Around Marble Zone
		bb.draw(gl, bb.floor, x1, y1, z1, 0, halfX-halfY, z2, 2, 0, 0);
		bb.draw(gl, bb.wall, x1, y1, z1, 0, x1, 3*halfY/2, z2, 2, 0);
		bb.draw(gl, bb.wall, x1, y1, z1, 0, halfX, 3*halfY/2, z1, 2, 0);
		bb.draw(gl, bb.wall, x1, y1, z2, 0, halfX, 3*halfY/2, z2, 2, 0);
		
		// Draws Grass and Trees Around Green Hill Zone
		bb.draw(gl, bb.floor, halfX+halfY, halfY, z1, 0, x2, z2, 3, 0, 0);
		bb.draw(gl, bb.trees, x2, halfY, z1, 0, x2, y2, z2, 0, 0);
		bb.draw(gl, bb.trees, halfX, halfY, z1, 0, x2, y2, z1, 0, 0);
		bb.draw(gl, bb.trees, x2, halfY,  z2, 0, halfX, y2, z2, 0, 0);
		
		
		// Draws Floors/Ceilings
		// (gl, bb.floor, x1, y1, z1, 0, x2, z2, texture, 0, 0)
		
		// Draws Walls
		// (gl, bb.wall, x1, y1, z1, 0, x2, y2, z2, texture, 0)
		
		// Draws Trees
		// (gl, bb.trees, x1, y1, z1, 0, x2, y2, z2, 0, 0)
	}
}
