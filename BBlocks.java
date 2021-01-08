// "CAP'N" Jyym Culpepper
// Program 3 - Building Blocks
// 10/15/2010

/* Description:
The building blocks include:

House - a cubic structure 4 by 4 by 4 units.  It has windows on two sides
Brick building - a cubic structure 4 by 4 by 4 units.  Solid brick on sides
Roof - pitched roof with triangle shape at each end.  Fits exactly on a house or brick building
Tower - a round building of any height with a cone shaped roof
Brick wall - 2 by 2 units at the base, either 2 units high or 4 units high
Steps - base 2 by 4 units, height 4 units
Doorway - a brick building with an archway in the middle of one side to the opposite side

You are to implement the 8 structures above (2 walls) and one structure of your own design.
The sizes are defined but you may decide on the textures, windows, etc.
Create a separate method for each structure.
Each method should be self-contained with all variables defined locally.
The center of the structure should be at the origin.

Use the RoomDemo program as a model for your program.
Create a “grass” floor that has 4 by 4 green squares.
Place an example of each structure in the middle of a green square (one per square).
You may put the roof on top of the house if you desire but they should be two different structures.  

You do not need to implement collision detection for this assignment.
*/

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import java.io.IOException;

import com.sun.opengl.util.texture.*;

class BBlocks
{
	private GLUquadric quadric;	// to control properties of quadric-based objects here
	private Texture MZ, GHZ, GHZG, GHZT1, GHZT2, Tree;				
	private GLCanvas canvas;		// for media tracker	 
	private GLU glu = new GLU();
	
	public final byte	house = 0x00,
							brickBuilding = 0x01,
							roof = 0x02,
							tower = 0x03,
							brickWall = 0x04,
							steps = 0x05,
							doorway = 0x06,
							loop = 0x07,
							floor = 0x08,
							wall = 0x09,
							trees = 0x0A;
	
	public BBlocks(GL gl, GLCanvas canvas) 
	{
		// Set parameters for quadrics
		this.canvas = canvas;
		quadric = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);	// GLU_POINT, GLU_LINE, GLU_FILL, GLU_SILHOUETTE
		glu.gluQuadricNormals  (quadric, GLU.GLU_NONE);	// GLU_NONE, GLU_FLAT, or GLU_SMOOTH
		glu.gluQuadricTexture  (quadric, false);			// use true to generate texture coordinates
		
		// Define textures
		GHZ = createTexture("GHZ2.jpg", ".jpg");			// the "blocks" texture
		MZ = createTexture("MZ.jpg", ".jpg");				// the "brick" texture
		GHZG = createTexture("GHZG2.jpg", ".jpg");		// the "grass" texture
		GHZT1 = createTexture("GHZT.jpg", ".jpg");		// the first "tower" texture
		GHZT2 = createTexture("GHZTwings.jpg", ".jpg");	// the second "tower" texture
		Tree = createTexture("GHZTree.png", ".png");		// the "tree" texture
	}
	
	// Method to create texture for use later - reads file, sets parameters
	private Texture createTexture(String filename, String FileType)
	{
		Texture t = null;
		try
		{
			t = TextureIO.newTexture(getClass().getResource(filename), false, FileType);
			t.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			t.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			t.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			t.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		} 
		catch (IOException e)
			{System.err.println("Error loading " + filename);}
		
		return t;
	}
	
	public void draw(GL gl, byte building)
	{
		switch (building)
		{
			case house:
				house(gl);
				break;
			case brickBuilding:
				brickBuilding(gl);
				break;
			case roof:
				roof(gl);
				break;
			case tower:
				tower(gl);
				break;
			case brickWall:
				brickWall(gl);
				break;
			case steps:
				steps(gl);
				break;
			case doorway:
				doorway(gl);
				break;
			case loop:
				loop(gl);
				break;
		}
	}
	
	// x, y, z and angle is where to place building
	// var1 is length, var2 is height, var3 is width when applicable
	// var4 and var5 are extras
	public void draw(GL gl, byte building, float x, float y, float z, float angle,
							float var1, float var2, float var3, float var4, float var5)
	{
		gl.glTranslatef(x, y, z);
		gl.glRotatef(-angle, 0, 1, 0);
		
		switch (building)
		{
			// Main "Building Blocks"
			case house:
				house(gl, var1-x, var2-y, var3-z, var4, var5);
				break;
			case brickBuilding:
				brickBuilding(gl, var1-x, var2-y, var3-z);
				break;
			case roof:
				roof(gl, var1-x, var2-y, var3-z);
				break;
			case tower:
				tower(gl, var1, var2);
				break;
			case brickWall:
				brickWall(gl, var1-x, var2-y, var3-z);
				break;
			case steps:
				steps(gl, (int)var4, var1-x, var2-y, var3-z);
				break;
			case doorway:
				doorway(gl, var1-x, var2-y, var3-z, (int)var4);
				break;
			case loop:
				loop(gl, var1, var2, var3, var4, var5);
				break;
			
			// Other "Building Blocks"
			case floor:
				floor(gl, var1-x, var2-z, var3);
				break;
			case wall:
				wall(gl, var1-x, var2-y, var3-z, var4);
				break;
			case trees:
				trees(gl, var1-x, var2-y, var3-z);
				break;
		}
		
		gl.glRotatef(angle, 0, 1, 0);
		gl.glTranslatef(-x, -y, -z);
	}
	
	public void draw(GL gl)
	{
		// house				(gl, l, h, w, t, x);
		// brickBuilding	(gl, l, h, w);
		// roof				(gl, l, h, w);
		// tower				(gl, r, h);
		// brickWall		(gl, l, h, w);
		// steps				(gl, n, l, h, w);
		// doorway			(gl, l, h, w, n);
		// loop				(gl, l, h, w, hs, ls);
	}
	
	private void house(GL gl)
	{
		house(gl, 4, 4, 4, 16, 3/2);
	}
	
	// l is the length, h is the height, and w is the width of the house
	// 	t is the thickness of the walls (ex. l/t, h/t, and w/t)
	// 	x is the size (length and height) of the window
	private void house(GL gl, float l, float h, float w, float t, float x)
	{
		GHZ.enable();
		GHZ.bind();
		
		// Front of House
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		// Back of House
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l, 0, 0);
		gl.glEnd();
		
		// Top of House
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, l);	gl.glVertex3f(0, h, w);
		gl.glEnd();
		
		// Inside Front of House
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(w/t, h);			gl.glVertex3f(l/t, 0, w/t);
		gl.glTexCoord2f(w/t, h/t);			gl.glVertex3f(l/t, (t-1)*h/t, w/t);
		gl.glTexCoord2f((t-1)*w/t, h/t);	gl.glVertex3f(l/t, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((t-1)*w/t, h);	gl.glVertex3f(l/t, 0, (t-1)*w/t);
		gl.glEnd();
		
		// Inside Back of House
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(w/t, h);			gl.glVertex3f((t-1)*l/t, 0, (t-1)*w/t);
		gl.glTexCoord2f(w/t, h/t);			gl.glVertex3f((t-1)*l/t, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((t-1)*w/t, h/t);	gl.glVertex3f((t-1)*l/t, (t-1)*h/t, w/t);
		gl.glTexCoord2f((t-1)*w/t, h);	gl.glVertex3f((t-1)*l/t, 0, w/t);
		gl.glEnd();
		
		// Inside Top of House
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(w/t, (t-1)*l/t);			gl.glVertex3f(l/t, (t-1)*h/t, w/t);
		gl.glTexCoord2f(w/t, l/t);					gl.glVertex3f((t-1)*l/t, (t-1)*h/t, w/t);
		gl.glTexCoord2f((t-1)*w/t, l/t);			gl.glVertex3f((t-1)*l/t, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((t-1)*w/t, (t-1)*l/t);	gl.glVertex3f(l/t, (t-1)*h/t, (t-1)*w/t);
		gl.glEnd();
		
		// Left of House (Front Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, h);	gl.glVertex3f((l-x)/2, 0, 0);
		gl.glTexCoord2f((l+x)/2, 0);	gl.glVertex3f((l-x)/2, h, 0);
		gl.glTexCoord2f(l, 0);			gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(l, h);			gl.glVertex3f(0, 0, 0);
		gl.glEnd();
		
		// Left of House (Back Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);			gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, 0);			gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f((l-x)/2, 0);	gl.glVertex3f((l+x)/2, h, 0);
		gl.glTexCoord2f((l-x)/2, h);	gl.glVertex3f((l+x)/2, 0, 0);
		gl.glEnd();
		
		// Left of House (Top Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, (h+x)/2);	gl.glVertex3f((l+x)/2, (h+x)/2, 0);
		gl.glTexCoord2f((l+x)/2, h);			gl.glVertex3f((l+x)/2, h, 0);
		gl.glTexCoord2f((l-x)/2, h);			gl.glVertex3f((l-x)/2, h, 0);
		gl.glTexCoord2f((l-x)/2, (h+x)/2);	gl.glVertex3f((l-x)/2, (h+x)/2, 0);
		gl.glEnd();
		
		// Left of House (Bottom Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, 0);			gl.glVertex3f((l+x)/2, 0, 0);
		gl.glTexCoord2f((l+x)/2, (h-x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, 0);
		gl.glTexCoord2f((l-x)/2, (h-x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, 0);
		gl.glTexCoord2f((l-x)/2, 0);			gl.glVertex3f((l-x)/2, 0, 0);
		gl.glEnd();
		
		// Inside Left of House (Front Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, h);		gl.glVertex3f((l-x)/2, 0, w/t);
		gl.glTexCoord2f((l+x)/2, h/t);	gl.glVertex3f((l-x)/2, (t-1)*h/t, w/t);
		gl.glTexCoord2f((t-1)*l/t, h/t);	gl.glVertex3f(l/t, (t-1)*h/t, w/t);
		gl.glTexCoord2f((t-1)*l/t, h);	gl.glVertex3f(l/t, 0, w/t);
		gl.glEnd();
		
		// Inside Left of House (Back Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(l/t, h);			gl.glVertex3f((t-1)*l/t, 0, w/t);
		gl.glTexCoord2f(l/t, h/t);			gl.glVertex3f((t-1)*l/t, (t-1)*h/t, w/t);
		gl.glTexCoord2f((l-x)/2, h/t);	gl.glVertex3f((l+x)/2, (t-1)*h/t, w/t);
		gl.glTexCoord2f((l-x)/2, h);		gl.glVertex3f((l+x)/2, 0, w/t);
		gl.glEnd();
		
		// Inside Left of House (Top Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, (h+x)/2);		gl.glVertex3f((l+x)/2, (h+x)/2, w/t);
		gl.glTexCoord2f((l+x)/2, (t-1)*h/t);	gl.glVertex3f((l+x)/2, (t-1)*h/t, w/t);
		gl.glTexCoord2f((l-x)/2, (t-1)*h/t);	gl.glVertex3f((l-x)/2, (t-1)*h/t, w/t);
		gl.glTexCoord2f((l-x)/2, (h+x)/2);		gl.glVertex3f((l-x)/2, (h+x)/2, w/t);
		gl.glEnd();
		
		// Inside Left of House (Bottom Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, 0);			gl.glVertex3f((l+x)/2, 0, w/t);
		gl.glTexCoord2f((l+x)/2, (h-x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, w/t);
		gl.glTexCoord2f((l-x)/2, (h-x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, w/t);
		gl.glTexCoord2f((l-x)/2, 0);			gl.glVertex3f((l-x)/2, 0, w/t);
		gl.glEnd();
		
		// Left Window (Front Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, (h-x)/2);		gl.glVertex3f((l-x)/2, (h-x)/2, 0);
		gl.glTexCoord2f(0, (h+x)/2);		gl.glVertex3f((l-x)/2, (h+x)/2, 0);
		gl.glTexCoord2f(1/2f-(int)x%2, (h+x)/2);	gl.glVertex3f((l-x)/2, (h+x)/2, w/t);
		gl.glTexCoord2f(1/2f-(int)x%2, (h-x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, w/t);
		gl.glEnd();
		
		// Left Window (Back Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, (h+x)/2);		gl.glVertex3f((l+x)/2, (h-x)/2, 0);
		gl.glTexCoord2f(0, (h-x)/2);		gl.glVertex3f((l+x)/2, (h+x)/2, 0);
		gl.glTexCoord2f(1/2f-(int)x%2, (h-x)/2);	gl.glVertex3f((l+x)/2, (h+x)/2, w/t);
		gl.glTexCoord2f(1/2f-(int)x%2, (h+x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, w/t);
		gl.glEnd();
		
		// Left Window (Top Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(1/2f-(int)x%2, (l+x)/2);	gl.glVertex3f((l-x)/2, (h+x)/2, 0);
		gl.glTexCoord2f(1/2f-(int)x%2, (l-x)/2);	gl.glVertex3f((l+x)/2, (h+x)/2, 0);
		gl.glTexCoord2f(0, (l-x)/2);		gl.glVertex3f((l+x)/2, (h+x)/2, w/t);
		gl.glTexCoord2f(0, (l+x)/2);		gl.glVertex3f((l-x)/2, (h+x)/2, w/t);
		gl.glEnd();
		
		// Left Window (Bottom Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(1/2f-(int)x%2, (l-x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, 0);
		gl.glTexCoord2f(1/2f-(int)x%2, (l+x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, 0);
		gl.glTexCoord2f(0, (l+x)/2);		gl.glVertex3f((l+x)/2, (h-x)/2, w/t);
		gl.glTexCoord2f(0, (l-x)/2);		gl.glVertex3f((l-x)/2, (h-x)/2, w/t);
		gl.glEnd();
		
		// Right of House (Front Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, h);	gl.glVertex3f((l-x)/2, h, w);
		gl.glTexCoord2f((l+x)/2, 0);	gl.glVertex3f((l-x)/2, 0, w);
		gl.glTexCoord2f(l, 0);			gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(l, h);			gl.glVertex3f(0, h, w);
		gl.glEnd();
		
		// Right of House (Back Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);			gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(0, 0);			gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f((l-x)/2, 0);	gl.glVertex3f((l+x)/2, 0, w);
		gl.glTexCoord2f((l-x)/2, h);	gl.glVertex3f((l+x)/2, h, w);
		gl.glEnd();
		
		// Right of House (Top Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l-x)/2, (h+x)/2);	gl.glVertex3f((l+x)/2, (h+x)/2, w);
		gl.glTexCoord2f((l-x)/2, h);			gl.glVertex3f((l+x)/2, h, w);
		gl.glTexCoord2f((l+x)/2, h);			gl.glVertex3f((l-x)/2, h, w);
		gl.glTexCoord2f((l+x)/2, (h+x)/2);	gl.glVertex3f((l-x)/2, (h+x)/2, w);
		gl.glEnd();
		
		// Right of House (Bottom Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l-x)/2, 0);			gl.glVertex3f((l+x)/2, 0, w);
		gl.glTexCoord2f((l-x)/2, (h-x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, w);
		gl.glTexCoord2f((l+x)/2, (h-x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, w);
		gl.glTexCoord2f((l+x)/2, 0);			gl.glVertex3f((l-x)/2, 0, w);
		gl.glEnd();
		
		// Inside Right of House (Front Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(l/t, h);			gl.glVertex3f(l/t, 0, (t-1)*w/t);
		gl.glTexCoord2f(l/t, h/t);			gl.glVertex3f(l/t, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((l-x)/2, h/t);	gl.glVertex3f((l-x)/2, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((l-x)/2, h);		gl.glVertex3f((l-x)/2, 0, (t-1)*w/t);
		gl.glEnd();
		
		// Inside Right of House (Back Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, h);		gl.glVertex3f((l+x)/2, 0, (t-1)*w/t);
		gl.glTexCoord2f((l+x)/2, h/t);	gl.glVertex3f((l+x)/2, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((t-1)*l/t, h/t);	gl.glVertex3f((t-1)*l/t, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((t-1)*l/t, h);	gl.glVertex3f((t-1)*l/t, 0, (t-1)*w/t);
		gl.glEnd();
		
		// Inside Right of House (Top Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, (h+x)/2);		gl.glVertex3f((l-x)/2, (h+x)/2, (t-1)*w/t);
		gl.glTexCoord2f((l+x)/2, (t-1)*h/t);	gl.glVertex3f((l-x)/2, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((l-x)/2, (t-1)*h/t);	gl.glVertex3f((l+x)/2, (t-1)*h/t, (t-1)*w/t);
		gl.glTexCoord2f((l-x)/2, (h+x)/2);		gl.glVertex3f((l+x)/2, (h+x)/2, (t-1)*w/t);
		gl.glEnd();
		
		// Inside Right of House (Bottom Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((l+x)/2, 0);	gl.glVertex3f((l-x)/2, 0, (t-1)*w/t);
		gl.glTexCoord2f((l+x)/2, (h-x)/2);			gl.glVertex3f((l-x)/2, (h-x)/2, (t-1)*w/t);
		gl.glTexCoord2f((l-x)/2, (h-x)/2);			gl.glVertex3f((l+x)/2, (h-x)/2, (t-1)*w/t);
		gl.glTexCoord2f((l-x)/2, 0);	gl.glVertex3f((l+x)/2, 0, (t-1)*w/t);
		gl.glEnd();
		
		// Right Window (Front Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, (h+x)/2);		gl.glVertex3f((l-x)/2, (h-x)/2, w);
		gl.glTexCoord2f(0, (h-x)/2);		gl.glVertex3f((l-x)/2, (h+x)/2, w);
		gl.glTexCoord2f(1/2f-(int)x%2, (h-x)/2);	gl.glVertex3f((l-x)/2, (h+x)/2, (t-1)*w/t);
		gl.glTexCoord2f(1/2f-(int)x%2, (h+x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, (t-1)*w/t);
		gl.glEnd();
		
		// Right Window (Back Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, (h-x)/2);		gl.glVertex3f((l+x)/2, (h-x)/2, w);
		gl.glTexCoord2f(0, (h+x)/2);		gl.glVertex3f((l+x)/2, (h+x)/2, w);
		gl.glTexCoord2f(1/2f-(int)x%2, (h+x)/2);	gl.glVertex3f((l+x)/2, (h+x)/2, (t-1)*w/t);
		gl.glTexCoord2f(1/2f-(int)x%2, (h-x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, (t-1)*w/t);
		gl.glEnd();
		
		// Right Window (Top Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(1/2f-(int)x%2, (l+x)/2);	gl.glVertex3f((l+x)/2, (h+x)/2, w);
		gl.glTexCoord2f(1/2f-(int)x%2, (l-x)/2);	gl.glVertex3f((l-x)/2, (h+x)/2, w);
		gl.glTexCoord2f(0, (l-x)/2);		gl.glVertex3f((l-x)/2, (h+x)/2, (t-1)*w/t);
		gl.glTexCoord2f(0, (l+x)/2);		gl.glVertex3f((l+x)/2, (h+x)/2, (t-1)*w/t);
		gl.glEnd();
		
		// Right Window (Bottom Piece)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(1/2f-(int)x%2, (l-x)/2);	gl.glVertex3f((l+x)/2, (h-x)/2, w);
		gl.glTexCoord2f(1/2f-(int)x%2, (l+x)/2);	gl.glVertex3f((l-x)/2, (h-x)/2, w);
		gl.glTexCoord2f(0, (l+x)/2);		gl.glVertex3f((l-x)/2, (h-x)/2, (t-1)*w/t);
		gl.glTexCoord2f(0, (l-x)/2);		gl.glVertex3f((l+x)/2, (h-x)/2, (t-1)*w/t);
		gl.glEnd();
		
		GHZ.disable();
	}
	
	private void brickBuilding(GL gl)
	{
		brickBuilding(gl, 4, 4, 4);
	}
	
	// l is the length, h is the height, and w is the width
	private void brickBuilding(GL gl, float l, float h, float w)
	{
		MZ.enable();
		MZ.bind();
		
		// Front of Building
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		// Left of Building
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(0, 0, 0);
		gl.glEnd();
		
		// Back of Building
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l, 0, 0);
		gl.glEnd();
		
		// Right of Building
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		// Top of Building
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, l);	gl.glVertex3f(0, h, w);
		gl.glEnd();
		
		MZ.disable();
	}
	
	private void roof(GL gl)
	{
		roof(gl, 4, 2, 4);
	}
	
	// l is the length, h is the height, and w is the width
	private void roof(GL gl, float l, float h, float w)
	{
		GHZG.enable();
		GHZG.bind();
		
		// Front of Roof
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l/2, h, 0);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l/2, h, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		// Back of Roof
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l/2, h, w);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l/2, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, 0, 0);
		gl.glEnd();
		
		// Left of Roof
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(l/2, h);	gl.glVertex3f(l/2, h, 0);
		gl.glEnd();
		
		// Right of Roof
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(l/2, h);	gl.glVertex3f(l/2, h, w);
		gl.glEnd();
		
		GHZG.disable();
	}
	
	private void tower(GL gl)
	{
		tower(gl, 2, 2*4);
	}
	
	// r is the radius, h is height of tower
	// 	f is the height of the cylinder, g is the height of the cone
	// detail is the level of detail, n is number of pieces in the cylinder/cone
	private void tower(GL gl, float r, float h)
	{
		final float f = 4*h/5, g = h/5;
		final int detail = 6, n = detail * (2 + (int)r);
		
		int i;
		double beginAngle, endAngle;
		float startL, endL, startW, endW;
		
		GHZT1.enable();
		GHZT1.bind();
		
		// Draws cylinder at height f, radius r, using n pieces
		for (i=0;i<n;i++)
		{
			beginAngle = Math.toRadians(360.0*(i-.5)/n);
			endAngle = Math.toRadians(360.0*(i+.5)/n);
			
			startL = r * (float) (1 + Math.sin(beginAngle));
			endL = r * (float) (1 + Math.sin(endAngle));
			startW = r * (float) (1 - Math.cos(beginAngle));
			endW = r * (float) (1 - Math.cos(endAngle));
			
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(2f*(i-.5f)/n, 1);			gl.glVertex3f(startL, 0, startW);
			gl.glTexCoord2f(2f*(i-.5f)/n, 0);			gl.glVertex3f(startL, f, startW);
			gl.glTexCoord2f(2f*(i+.5f)/n, 0);			gl.glVertex3f(endL, f, endW);
			gl.glTexCoord2f(2f*(i+.5f)/n, 1);			gl.glVertex3f(endL, 0, endW);
			gl.glEnd();
		}
		
		GHZT1.disable();
		GHZT2.enable();
		GHZT2.bind();
		
		// Draws cone at height h + g, radius r, using n pieces
		for (i=0;i<n;i++)
		{
			beginAngle = Math.toRadians(360.0*(i-.5)/n);
			endAngle = Math.toRadians(360.0*(i+.5)/n);
			
			startL = r * (float) (1 + Math.sin(beginAngle));
			endL = r * (float) (1 + Math.sin(endAngle));
			startW = r * (float) (1 + Math.cos(beginAngle));
			endW = r * (float) (1 + Math.cos(endAngle));
			
			gl.glBegin(GL.GL_TRIANGLES);
			gl.glTexCoord2f(1, 2);			gl.glVertex3f(startL, f, startW);
			gl.glTexCoord2f(0, 2);			gl.glVertex3f(endL, f, endW);
			gl.glTexCoord2f(1, 0);			gl.glVertex3f(r, h, r);
			gl.glEnd();
		}
		
		GHZT2.disable();
	}
	
	private void brickWall(GL gl)
	{
		brickWall(gl, 2, 2, 2);
	}
	
	// l is the length, h is the height, and w is the width
	private void brickWall(GL gl, float l, float h, float w)
	{
		MZ.enable();
		MZ.bind();
		
		// Front of Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		// Left of Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(0, 0, 0);
		gl.glEnd();
		
		// Back of Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l, 0, 0);
		gl.glEnd();
		
		// Right of Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		// Top of Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, l);	gl.glVertex3f(0, h, w);
		gl.glEnd();
		
		MZ.disable();
	}
	
	private void steps(GL gl)
	{
		steps(gl, 4, 4, 4, 2);
	}
	
	// n is the number of steps
	// l is the length, h is the height, and w is the width
	private void steps(GL gl, int n, float l, float h, float w)
	{
		int i;
		
		GHZ.enable();
		GHZ.bind();
			
		// Back of Steps
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l, 0, 0);
		gl.glEnd();
		
		for (i=n;i>0;i--)
		{
			if (i != n)
			{
				GHZ.enable();
				GHZ.bind();
			}
			
			// Front of Step
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0, h/n);	gl.glVertex3f((i-1)*l/n, (i-1)*h/n, 0);
			gl.glTexCoord2f(0, 0);	gl.glVertex3f((i-1)*l/n, i*h/n, 0);
			gl.glTexCoord2f(w, 0);	gl.glVertex3f((i-1)*l/n, i*h/n, w);
			gl.glTexCoord2f(w, h/n);	gl.glVertex3f((i-1)*l/n, (i-1)*h/n, w);
			gl.glEnd();
			
			// Left of Step
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0, i*h/n);		gl.glVertex3f(i*l/n, 0, 0);
			gl.glTexCoord2f(0, 0);		gl.glVertex3f(i*l/n, i*h/n, 0);
			gl.glTexCoord2f(l/n, 0);	gl.glVertex3f((i-1)*l/n, i*h/n, 0);
			gl.glTexCoord2f(l/n, i*h/n);	gl.glVertex3f((i-1)*l/n, 0, 0);
			gl.glEnd();
			
			// Right of Step
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0, i*h/n);		gl.glVertex3f((i-1)*l/n, 0, w);
			gl.glTexCoord2f(0, 0);		gl.glVertex3f((i-1)*l/n, i*h/n, w);
			gl.glTexCoord2f(l/n, 0);	gl.glVertex3f(i*l/n, i*h/n, w);
			gl.glTexCoord2f(l/n, i*h/n);	gl.glVertex3f(i*l/n, 0, w);
			gl.glEnd();
			
			GHZ.disable();
			GHZG.enable();
			GHZG.bind();
			
			// Top of Step
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0, l/n);	gl.glVertex3f((i-1)*l/n, i*h/n, 0);
			gl.glTexCoord2f(0, 0);		gl.glVertex3f(i*l/n, i*h/n, 0);
			gl.glTexCoord2f(w, 0);		gl.glVertex3f(i*l/n, i*h/n, w);
			gl.glTexCoord2f(w, l/n);	gl.glVertex3f((i-1)*l/n, i*h/n, w);
			gl.glEnd();
			
			GHZG.disable();
		}
	}
	
	private void doorway(GL gl)
	{
		doorway(gl, 4, 4, 4, 16);
	}
	
	// l is the length, h is the height, and w is the width
	// n is the number of pieces to be used to create the actual arch
	private void doorway(GL gl, float l, float h, float w, int n)
	{
		int j, k;
		double beginAngle, endAngle;
		float i, startH, endH, startW, endW;
		
		MZ.enable();
		MZ.bind();
		
		// Front of Left Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);		gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);		gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(w/4, 0);	gl.glVertex3f(0, h, 1/4f*w);
		gl.glTexCoord2f(w/4, h);	gl.glVertex3f(0, 0, 1/4f*w);
		gl.glEnd();
		
		// Back of Left Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);		gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, 0);		gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(-w/4, 0);	gl.glVertex3f(l, h, 1/4f*w);
		gl.glTexCoord2f(-w/4, h);	gl.glVertex3f(l, 0, 1/4f*w);
		gl.glEnd();
		
		// Left of Left Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(0, 0, 0);
		gl.glEnd();
		
		// Right of Left Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h/2);	gl.glVertex3f(0, 0, 1/4f*w);
		gl.glTexCoord2f(0, 0);		gl.glVertex3f(0, h/2, 1/4f*w);
		gl.glTexCoord2f(l, 0);		gl.glVertex3f(l, h/2, 1/4f*w);
		gl.glTexCoord2f(l, h/2);	gl.glVertex3f(l, 0, 1/4f*w);
		gl.glEnd();
		
		// Front of Right Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);		gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, 0);		gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(-w/4, 0);	gl.glVertex3f(0, h, 3/4f*w);
		gl.glTexCoord2f(-w/4, h);	gl.glVertex3f(0, 0, 3/4f*w);
		gl.glEnd();
		
		// Back of Right Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);		gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, 0);		gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w/4, 0);	gl.glVertex3f(l, h, 3/4f*w);
		gl.glTexCoord2f(w/4, h);	gl.glVertex3f(l, 0, 3/4f*w);
		gl.glEnd();
		
		// Left of Right Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h/2);	gl.glVertex3f(l, 0, 3/4f*w);
		gl.glTexCoord2f(0, 0);		gl.glVertex3f(l, h/2, 3/4f*w);
		gl.glTexCoord2f(l, 0);		gl.glVertex3f(0, h/2, 3/4f*w);
		gl.glTexCoord2f(l, h/2);	gl.glVertex3f(0, 0, 3/4f*w);
		gl.glEnd();
		
		// Right of Right Half
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		// Above the Arch (front)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(w/4, h/4);		gl.glVertex3f(0, 3/4f*h, 1/4f*w);
		gl.glTexCoord2f(w/4, 0);		gl.glVertex3f(0, h, 1/4f*w);
		gl.glTexCoord2f(3*w/4, 0);		gl.glVertex3f(0, h, 3/4f*w);
		gl.glTexCoord2f(3*w/4, h/4);	gl.glVertex3f(0, 3/4f*h, 3/4f*w);
		gl.glEnd();
		
		// Above the Arch (back)
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(w/4, h/4);		gl.glVertex3f(l, 3/4f*h, 3/4f*w);
		gl.glTexCoord2f(w/4, 0);		gl.glVertex3f(l, h, 3/4f*w);
		gl.glTexCoord2f(3*w/4, 0);		gl.glVertex3f(l, h, 1/4f*w);
		gl.glTexCoord2f(3*w/4, h/4);	gl.glVertex3f(l, 3/4f*h, 1/4f*w);
		gl.glEnd();
		
		// Then a series of n segments to create the arch
		for (j=0;j<n;j++)
		{
			beginAngle = Math.toRadians(180.0*j/n);
			endAngle = Math.toRadians(180.0*(j+1)/n);
			
			startW = (float) (1 - Math.cos(beginAngle));
			endW = (float) (1 - Math.cos(endAngle));
			startH = (float) (Math.sin(beginAngle));
			endH = (float) (Math.sin(endAngle));
			
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f((h+w-4)*j/2/n, l);		gl.glVertex3f(0, h/2+h/4*startH, w/4+w/4*startW);
			gl.glTexCoord2f((h+w-4)*j/2/n, 0);		gl.glVertex3f(l, h/2+h/4*startH, w/4+w/4*startW);
			gl.glTexCoord2f((h+w-4)*(j+1)/2/n, 0);	gl.glVertex3f(l, h/2+h/4*endH, w/4+w/4*endW);
			gl.glTexCoord2f((h+w-4)*(j+1)/2/n, l);	gl.glVertex3f(0, h/2+h/4*endH, w/4+w/4*endW);
			gl.glEnd();
			
			// cover the gap left
			if (2*j < n-1)
			{
				// Front Left
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glTexCoord2f(w/4, h/4);									gl.glVertex3f(0, h*3/4f, w*1/4f);
				gl.glTexCoord2f(w/4*(startW+1), h/4*(2-startH));	gl.glVertex3f(0, h/2+h/4*startH, w/4+w/4*startW);
				gl.glTexCoord2f(w/4*(endW+1), h/4*(2-endH));			gl.glVertex3f(0, h/2+h/4*endH, w/4+w/4*endW);
				gl.glEnd();
				
				// Back Left
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glTexCoord2f(3*w/4, h/4);								gl.glVertex3f(l, h*3/4f, w*1/4f);
				gl.glTexCoord2f(w/4*(3-startW), h/4*(2-startH));	gl.glVertex3f(l, h/2+h/4*startH, w/4+w/4*startW);
				gl.glTexCoord2f(w/4*(3-endW), h/4*(2-endH));			gl.glVertex3f(l, h/2+h/4*endH, w/4+w/4*endW);
				gl.glEnd();
			}
			else if (2*j > n-1)
			{
				// Front Right
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glTexCoord2f(3*w/4, h/4);								gl.glVertex3f(0, h*3/4f, w*3/4f);
				gl.glTexCoord2f(w/4*(endW+1), h/4*(2-endH));			gl.glVertex3f(0, h/2+h/4*endH, w/4+w/4*endW);
				gl.glTexCoord2f(w/4*(startW+1), h/4*(2-startH));	gl.glVertex3f(0, h/2+h/4*startH, w/4+w/4*startW);
				gl.glEnd();
				
				// Back Right
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glTexCoord2f(w/4, h/4);									gl.glVertex3f(l, h*3/4f, w*3/4f);
				gl.glTexCoord2f(w/4*(3-endW), h/4*(2-endH));			gl.glVertex3f(l, h/2+h/4*endH, w/4+w/4*endW);
				gl.glTexCoord2f(w/4*(3-startW), h/4*(2-startH));	gl.glVertex3f(l, h/2+h/4*startH, w/4+w/4*startW);
				gl.glEnd();
			}
			else // if (2*j == n-1)
			{
				// Front Center
				gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(w/4, h/4);									gl.glVertex3f(0, h*3/4f, w*1/4f);
				gl.glTexCoord2f(3*w/4, h/4);								gl.glVertex3f(0, h*3/4f, w*3/4f);
				gl.glTexCoord2f(w/4*(1+endW), h/4*(2-endH));			gl.glVertex3f(0, h/2+h/4*endH, w/4+w/4*endW);
				gl.glTexCoord2f(w/4*(1+startW), h/4*(2-startH));	gl.glVertex3f(0, h/2+h/4*startH, w/4+w/4*startW);
				gl.glEnd();
				
				// Back Center
				gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(3*w/4, h/4);								gl.glVertex3f(l, h*3/4f, w*1/4f);
				gl.glTexCoord2f(w/4, h/4);									gl.glVertex3f(l, h*3/4f, w*3/4f);
				gl.glTexCoord2f(w/4*(1+startW), h/4*(2-startH));	gl.glVertex3f(l, h/2+h/4*endH, w/4+w/4*endW);
				gl.glTexCoord2f(w/4*(1+endW), h/4*(2-endH));			gl.glVertex3f(l, h/2+h/4*startH, w/4+w/4*startW);
				gl.glEnd();
			}
		}
		
		// Top of Building
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, l);	gl.glVertex3f(0, h, w);
		gl.glEnd();
		
		MZ.disable();
	}
	
	private void loop(GL gl)
	{
		loop(gl, 4, 2.25f);
	}
	
	private void loop(GL gl, int size, float space)
	{
		loop(gl, 3*size, 3*size, 2*size, space, space);
	}
	
	// l is the length, h is the heigth, and w is the width of the loop area
	// 	proper dimensions are l = n*3, h = n*3, w = n*2, for any given n
	// 		size should be greater than 1
	// length_space is the space to be left for between the loop and the wall, 2 provides 0 space
	// height_space is the space to be left for between the loop and the ground/ceiling, 2 provides 0 space
	private void loop(GL gl, float l, float h, float w, float ls, float hs)
	{
		// startL, endL, startH, and endH are used to draw the pieces of the loop
		// start and end are the start and end points for the loop
		// beginAngle and endAngle are used to calculate the angle of the pieces
		float n = (int)(12+(l+h)/4), i, startL, endL, startH, endH, start = n/8 - n/(4*hs), end = n - start;
		double beginAngle, endAngle;
		
		GHZ.enable();
		GHZ.bind();
		
		// Draws Loop, using n segments
		for (i=start;i<end;i+=((n-2*start)/n)+.0000005f) // the .0000005f is to prevent rounding errors
		{
			beginAngle = Math.toRadians(360.0*i/n);
			endAngle = Math.toRadians(360.0*((i+((n-2*start)/n)+.0000005f))/n);
			
			startL = l/2f - l/ls + (l/ls) * (float) (1 + Math.sin(beginAngle));
			endL = l/2f - l/ls + (l/ls) * (float) (1 + Math.sin(endAngle));
			startH = h/2f - h/hs + (h/hs) * (float) (1 - Math.cos(beginAngle));
			endH = h/2f - h/hs + (h/hs) * (float) (1 - Math.cos(endAngle));
			
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0, (int)(3*l/n));					gl.glVertex3f(startL, startH, 9*w/16 - 9*i*w/16/n);
			gl.glTexCoord2f(0, 0);									gl.glVertex3f(endL, endH, 9*w/16 - 9*(i+1)*w/16/n);
			gl.glTexCoord2f((int)(7*w/16), 0);					gl.glVertex3f(endL, endH, w - 9*(i+1)*w/16/n);
			gl.glTexCoord2f((int)(7*w/16), (int)(3*l/n));	gl.glVertex3f(startL, startH, w - 9*i*w/16/n);
			gl.glEnd();
		}
		
		// Pre "Ramp" of Loop
		startL = l/2f - l/ls + (l/ls) * (float) (1 + Math.sin(Math.toRadians(360.0*start/n)));
		startH = h/2f - h/hs + (h/hs) * (float) (1 - Math.cos(Math.toRadians(360.0*start/n)));
		
		// The ramp
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, (int)(l/2));					gl.glVertex3f(0, 0, 9*w/16);
		gl.glTexCoord2f(0, 0);								gl.glVertex3f(startL, startH, 9*w/16 - 9*start*w/16/n);
		gl.glTexCoord2f((int)(7*w/16), 0);				gl.glVertex3f(startL, startH, w - 9*start*w/16/n);
		gl.glTexCoord2f((int)(7*w/16), (int)(l/2));	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		// Back of the ramp
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, ((int)(2*startH))/2f);					gl.glVertex3f(startL, 0, 9*w/16 - 9*start*w/16/n);
		gl.glTexCoord2f(0, 0);											gl.glVertex3f(startL, startH, 9*w/16 - 9*start*w/16/n);
		gl.glTexCoord2f((int)(7*w/16), 0);							gl.glVertex3f(startL, startH, w - 9*start*w/16/n);
		gl.glTexCoord2f((int)(7*w/16), ((int)(2*startH))/2f);	gl.glVertex3f(startL, 0, w - 9*start*w/16/n);
		gl.glEnd();
		
		// Left of the ramp
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glTexCoord2f(0, 0);											gl.glVertex3f(0, 0, 9*w/16);
		gl.glTexCoord2f(0, (int)(startL));							gl.glVertex3f(startL, 0, 9*w/16 - 9*start*w/16/n);
		gl.glTexCoord2f(((int)(2*startH))/2f, (int)(startL));	gl.glVertex3f(startL, startH, 9*w/16 - 9*start*w/16/n);
		gl.glEnd();
		
		// Right of the ramp
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glTexCoord2f(0, 0);											gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, (int)(-startL));							gl.glVertex3f(startL, 0, w - 9*start*w/16/n);
		gl.glTexCoord2f(((int)(2*startH))/2f, (int)(-startL));	gl.glVertex3f(startL, startH, w - 9*start*w/16/n);
		gl.glEnd();
		
		// Post "Ramp" of Loop
		endL = l/2f - l/ls + (l/ls) * (float) (1 + Math.sin(Math.toRadians(360.0*end/n)));
		endH = h/2f - h/hs + (h/hs) * (float) (1 - Math.cos(Math.toRadians(360.0*end/n)));
		
		// The ramp
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, (int)(l/2));					gl.glVertex3f(endL, endH, 9*w/16 - 9*end*w/16/n);
		gl.glTexCoord2f(0, 0);								gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f((int)(7*w/16), 0);				gl.glVertex3f(l, 0, 7*w/16);
		gl.glTexCoord2f((int)(7*w/16), (int)(l/2));	gl.glVertex3f(endL, endH, w - 9*end*w/16/n);
		gl.glEnd();
		
		// Back of the ramp
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, ((int)(2*endH))/2f);					gl.glVertex3f(endL, 0, w - 9*end*w/16/n);
		gl.glTexCoord2f(0, 0);											gl.glVertex3f(endL, endH, w - 9*end*w/16/n);
		gl.glTexCoord2f((int)(7*w/16), 0);							gl.glVertex3f(endL, endH, 9*w/16 - 9*end*w/16/n);
		gl.glTexCoord2f((int)(7*w/16), ((int)(2*endH))/2f);	gl.glVertex3f(endL, 0, 9*w/16 - 9*end*w/16/n);
		gl.glEnd();
		
		// Left of the ramp
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glTexCoord2f(0, 0);											gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, (int)(-startL));							gl.glVertex3f(endL, 0, 9*w/16 - 9*end*w/16/n);
		gl.glTexCoord2f(((int)(2*startH))/2f, (int)(-startL));	gl.glVertex3f(endL, endH, 9*w/16 - 9*end*w/16/n);
		gl.glEnd();

		// Right of the ramp
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glTexCoord2f(0, 0);											gl.glVertex3f(l, 0, 7*w/16);
		gl.glTexCoord2f(0, (int)(startL));							gl.glVertex3f(endL, 0, w - 9*end*w/16/n);
		gl.glTexCoord2f(((int)(2*startH))/2f, (int)(startL));	gl.glVertex3f(endL, endH, w - 9*end*w/16/n);
		gl.glEnd();
		
		// for use in how many and the size of the textures to use on the connectors
		float minusL = l/2f - l/ls, minusH = h/2f - h/hs, paste = ((int)(2*minusL+1))/2f;
		
		// Outside Front Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);					gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f((int)(9*w/16), 0);	gl.glVertex3f(0, h, 9*w/16);
		gl.glTexCoord2f((int)(9*w/16), h);	gl.glVertex3f(0, 0, 9*w/16);
		gl.glEnd();
		
		// Inside Front Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h-minusH);					gl.glVertex3f(minusL, h - minusH, 0);
		gl.glTexCoord2f(0, 0);							gl.glVertex3f(minusL, 0, 0);
		gl.glTexCoord2f((int)(9*w/16), 0);			gl.glVertex3f(minusL, 0, 9*w/16);
		gl.glTexCoord2f((int)(9*w/16), h-minusH);	gl.glVertex3f(minusL, h - minusH, 9*w/16);
		gl.glEnd();
		
		// Left-side "connecter"
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, h);					gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(paste, h-minusH);	gl.glVertex3f(minusL, h - minusH, 0);
		gl.glTexCoord2f(paste, 0);				gl.glVertex3f(minusL, 0, 0);
		gl.glEnd();
		
		// Right-side "connector"
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(paste, 0);		gl.glVertex3f(0, 0, 9*w/16);
		gl.glTexCoord2f(paste, h);		gl.glVertex3f(0, h, 9*w/16);
		gl.glTexCoord2f(0, h-minusH);	gl.glVertex3f(minusL, h - minusH, 9*w/16);
		gl.glTexCoord2f(0, 0);			gl.glVertex3f(minusL, 0, 9*w/16);
		gl.glEnd();
		
		// Outside Back Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((int)(9*w/16), h);	gl.glVertex3f(l, 0, 7*w/16);
		gl.glTexCoord2f((int)(9*w/16), 0);	gl.glVertex3f(l, h, 7*w/16);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(0, h);					gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		// Inside Back Wall
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f((int)(9*w/16), h - minusH);	gl.glVertex3f(l-minusL, h - minusH, 7*w/16);
		gl.glTexCoord2f((int)(9*w/16), 0);				gl.glVertex3f(l-minusL, 0, 7*w/16);
		gl.glTexCoord2f(0, 0);								gl.glVertex3f(l-minusL, 0, w);
		gl.glTexCoord2f(0, h-minusH);						gl.glVertex3f(l-minusL, h - minusH, w);
		gl.glEnd();
		
		// Left-side "connecter"
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(paste, 0);		gl.glVertex3f(l, 0, 7*w/16);
		gl.glTexCoord2f(paste, h);		gl.glVertex3f(l, h, 7*w/16);
		gl.glTexCoord2f(0, h-minusH);	gl.glVertex3f(l-minusL, h - minusH, 7*w/16);
		gl.glTexCoord2f(0, 0);			gl.glVertex3f(l-minusL, 0, 7*w/16);
		gl.glEnd();
		
		// Right-side "connector"
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, h);					gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(paste, h-minusH);	gl.glVertex3f(l-minusL, h - minusH, w);
		gl.glTexCoord2f(paste, 0);				gl.glVertex3f(l-minusL, 0, w);
		gl.glEnd();
		
		paste = ((int)(2*minusH+1))/2f;
		
		// Inside Ceiling
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);					gl.glVertex3f(minusL, h - minusH, 0);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(l - minusL, h - minusH, 7*w/16);
		gl.glTexCoord2f((int)(9*w/16), 0);	gl.glVertex3f(l - minusL, h - minusH, w);
		gl.glTexCoord2f((int)(9*w/16), l);	gl.glVertex3f(minusL, h - minusH, 9*w/16);
		gl.glEnd();
		
		// Left-side "connector"
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);					gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(l, h, 7*w/16);
		gl.glTexCoord2f(paste, minusL);		gl.glVertex3f(l - minusL, h - minusH, 7*w/16);
		gl.glTexCoord2f(paste, l-minusL);	gl.glVertex3f(minusL, h - minusH, 0);
		gl.glEnd();
		
		// Right-side "connector"
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(paste, l);		gl.glVertex3f(0, h, 9*w/16);
		gl.glTexCoord2f(paste, 0);		gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(0, minusL);	gl.glVertex3f(l - minusL, h - minusH, w);
		gl.glTexCoord2f(0, l-minusL);	gl.glVertex3f(minusL, h - minusH, 9*w/16);
		gl.glEnd();
		
		GHZ.disable();
		GHZG.enable();
		GHZG.bind();
		
		// Outside Ceiling
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);					gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);					gl.glVertex3f(l, h, 7*w/16);
		gl.glTexCoord2f((int)(9*w/16), 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f((int)(9*w/16), l);	gl.glVertex3f(0, h, 9*w/16);
		gl.glEnd();
		
		GHZG.disable();
	}
	
	// l is the length, h is the height, and w is the width
	// texture is the number texture to use
	private void floor(GL gl, float l, float w, float texture)
	{
		if ((int)texture == 1)
		{
			GHZ.enable();
			GHZ.bind();
		}
		else if ((int)texture == 2)
		{
			MZ.enable();
			MZ.bind();
		}
		else if ((int)texture == 3)
		{
			GHZG.enable();
			GHZG.bind();
		}
		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(l, w);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, w);	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		if (texture == 1)
			GHZ.disable();
		else if (texture == 2)
			MZ.disable();
		else if (texture == 3)
			GHZG.disable();
	}
	
	// l is the length, h is the height, and w is the width
	// texture is the number texture to use
	private void wall(GL gl, float l, float h, float w, float texture)
	{
		// number of blocks
		float n = Math.max(l, w);
		
		if ((int)texture == 1)
		{
			GHZ.enable();
			GHZ.bind();
		}
		else if ((int)texture == 2)
		{
			MZ.enable();
			MZ.bind();
		}
		else if ((int)texture == 3)
		{
			GHZG.enable();
			GHZG.bind();
		}
		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(n, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(n, h);	gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		if (texture == 1)
			GHZ.disable();
		else if (texture == 2)
			MZ.disable();
		else if (texture == 3)
			GHZG.disable();
	}
	
	// l is the length, h is the height, and w is the width
	private void trees(GL gl, float l, float h, float w)
	{
		// number of trees
		float n = (int) (.5f + 594f * Math.abs(l + w) / h / 241f);
		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		Tree.enable();
		Tree.bind();
		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 1);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(n, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(n, 1);	gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		Tree.disable();
		
		gl.glDisable(GL.GL_BLEND);
	}
	
	private void box(GL gl)
	{
		// length, height, and width
		final float l = 10, h = 10, w = 10;
		
		GHZ.enable();
		GHZ.bind();
		
		// Front of Box
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(0, 0, w);
		gl.glEnd();
		
		// Back of Box
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, h);	gl.glVertex3f(l, 0, 0);
		gl.glEnd();
		
		// Left of Box
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(0, 0, 0);
		gl.glEnd();
		
		// Right of Box
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, h);	gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(0, h, w);
		gl.glTexCoord2f(l, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(l, h);	gl.glVertex3f(l, 0, w);
		gl.glEnd();
		
		// Top of Box
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);	gl.glVertex3f(0, h, 0);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, h, 0);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, h, w);
		gl.glTexCoord2f(w, l);	gl.glVertex3f(0, h, w);
		gl.glEnd();
		
		// Bottom of Box
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, l);	gl.glVertex3f(0, 0, w);
		gl.glTexCoord2f(0, 0);	gl.glVertex3f(l, 0, w);
		gl.glTexCoord2f(w, 0);	gl.glVertex3f(l, 0, 0);
		gl.glTexCoord2f(w, l);	gl.glVertex3f(0, 0, 0);
		gl.glEnd();
		
		GHZ.disable();
	}
}
