/*
	"CAP'N" Jyym Culpepper
*/
import java.awt.event.*;
import javax.swing.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;

import java.awt.Font;
import com.sun.opengl.util.j2d.TextRenderer;

public class Game implements GLEventListener, KeyListener
{
	private Environment gr;
	private Map map;
	private Creature creature;
	private GLCanvas canvas;
	private GLU glu = new GLU();
	private TextRenderer Time;
	private int winlose, winlosecount, sonicHealth;
	private float x, y, z, eyex, eyey, eyez, viewangle, topangle, stepsize, pan, sonicPos, sonicDir;
	private boolean f1, keys[] = new boolean[256];	// keys currently pressed
	private long startTime, time;
	
	public Game(GLCanvas canvas)
	{
		x = 4; y = 2; z = 0;		// initial location of creature (center/origin)
		viewangle = 0;				// initial direction for viewing
		topangle = 40;				// angle of viewing downwards
		calcEyeLoc();				// calculates eye location based on x, y, z
		stepsize = 1/8f;			// distance to move when walking
		pan = 2;						// how far to turn when rotating
		sonicHealth = 3;			// Sonic's health
		sonicPos = 0;				// position Sonic initially stands at
		sonicDir = 1/16f;			// direction Sonic initially faces
		winlose = 0;				// 0 means play, -1 means lose, 1 means win
		this.canvas = canvas;
	}
	
	public void init(GLAutoDrawable drawable) 
	{
		GL gl = drawable.getGL();
		gl.glClearColor(37f/255, 0, 184f/255, 1);		// background
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		gr = new Environment(gl, canvas);
		initMap();
		creature = new Creature(gl, canvas);
		creature.set(x, y, z, viewangle);
		
		Time = new TextRenderer(new Font("Arial", Font.BOLD, 50));
		startTime = System.currentTimeMillis();// - 599000;
	}
	
	public void initMap()
	{
		map = new Map(0, 0, -25, 200, 32, 25);
		int steps;
		
		// Marble Zone Ceiling
		map.add(0, 16, -25, 30, 20, 25);
		map.add(0, 20, -25, 15, 24, -10);
		map.add(0, 20, 10, 15, 24, 25);
		map.add(30, 22, -25, 100, 24, 25);
		
		// Green Hill Zone Ground
		map.add(116, 0, -25, 200, 16, 25);
		for (steps=1;steps<16;steps++)
			map.add(82+2*steps, 0, -25, 83+2*steps, steps, 25);
		
		// Green Hill Zone Buildings
		map.add(132, 16, -5, 144, 24, 5);
		map.add(150, 16, -23, 166, 24, -7);
		map.add(150, 16, 7, 166, 24, 23);
		
		// Green Hill Zone Towers
		map.add(151, 24, -21, 157, 31, -17);
		map.add(152, 24, -22, 156, 31, -21);
		map.add(152, 24, -17, 156, 31, -16);
		map.add(151, 24, -13, 157, 31, -9);
		map.add(152, 24, -14, 156, 31, -13);
		map.add(152, 24, -9, 156, 31, -8);
		map.add(159, 24, -21, 165, 31, -17);
		map.add(160, 24, -22, 164, 31, -21);
		map.add(160, 24, -17, 164, 31, -16);
		map.add(159, 24, -13, 165, 31, -9);
		map.add(160, 24, -14, 164, 31, -13);
		map.add(160, 24, -9, 164, 31, -8);
		
		map.add(151, 24, 9, 157, 31, 13);
		map.add(152, 24, 8, 156, 31, 9);
		map.add(152, 24, 13, 156, 31, 14);
		map.add(151, 24, 17, 157, 31, 21);
		map.add(152, 24, 16, 156, 31, 17);
		map.add(152, 24, 21, 156, 31, 22);
		map.add(159, 24, 9, 165, 31, 13);
		map.add(160, 24, 8, 164, 31, 9);
		map.add(160, 24, 13, 164, 31, 14);
		map.add(159, 24, 17, 165, 31, 21);
		map.add(160, 24, 16, 164, 31, 17);
		map.add(160, 24, 21, 164, 31, 22);
		
		// Green Hill Zone Loop
		map.add(150, 16, -5, 151, 31, 1);
		map.add(165, 16, -1, 166, 31, 5);
		map.add(150, 16, 1, 159, 17, 5);
		map.add(157, 16, -5, 165, 17, -1);
	}
	
	public void display(GLAutoDrawable drawable)
	{
		if (winlose == 0)
		{
			increment();
			move();
		}
		else if (winlose == -1)
			creature.incfalling();
		GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		// Main Screen
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glViewport(0, 0, 768, 768);		// bottomx, bottomy, topx, topy
		glu.gluPerspective(90, 1, .5, 200);	// fov, aspect, near-clip, far clip
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		glu.gluLookAt(eyex, eyey, eyez,					// eye location
			eyex+Math.cos(Math.toRadians(viewangle)),	// point to look at (near middle)
			eyey-Math.tan(Math.toRadians(topangle)),
			eyez-Math.sin(Math.toRadians(viewangle)),
			0f,1f,0f);											// the "up" direction
		
		drawSonic(gl);
		gr.draw(gl);
		creature.draw(gl);
		if (f1)
		{
			map.draw(gl);
			creature.drawColBox(gl);
		}
		
		time(drawable);
		
		drawMap(gl);
		
		// check for errors
		int error = gl.glGetError();
		if (error != GL.GL_NO_ERROR)
			System.out.println("OpenGL Error: " + error);
	}
	
	// increments time-related (count) variables
	public void increment()
	{
		creature.inc();
		
		if (sonicPos >= 20)
			sonicDir = -1/16f;
		else if (sonicPos <= -20)
			sonicDir = 1/16f;
		
		sonicPos += sonicDir;
		
		// if firing
		if (creature.firing())
		{
			float f[] = creature.fireXYZ();
			
			if (hitSonic(f))
			{
				sonicHealth--;
				creature.fired();
				if (sonicHealth == 0)
					winlose = 1;
			}
			else if (map.collision(f[0], f[1], f[2]))
				creature.fired();
		}
		
		time = System.currentTimeMillis();	// current time
		time = time - startTime;				// milliseconds
		time = time / 1000;						// seconds
		
		if (time >= 600)
		{
			time = 599;
			creature.fall();
			winlose = -1;
		}
	}
	
	public boolean hitSonic(float[] f)
	{
		return	(f[0] > 194 && f[0] < 196 &&
					 f[1] > 16 && f[1] < 18 &&
					 f[2] > sonicPos-1 && f[2] < sonicPos+1);
	}
		
	public void move()
	{
		float dx, dy, dz;
		boolean moved = false;
		
		// "Gravity"
		if (keys[KeyEvent.VK_UP] && !keys[KeyEvent.VK_DOWN]) // (false)
		{
			dy = stepsize;
			moved = moveY(dy) || moved;
		}
		if (keys[KeyEvent.VK_DOWN] && !keys[KeyEvent.VK_UP]) // (true)
		{
			dy = -stepsize;
			moved = moveY(dy) || moved;
		}
		
		if (keys[KeyEvent.VK_LEFT] && !keys[KeyEvent.VK_RIGHT])
		{
			viewangle += pan;
			if (viewangle > 360)
				viewangle -= 360;
			creature.rotate(viewangle);
			moved = true;
		}
		if (keys[KeyEvent.VK_RIGHT] && !keys[KeyEvent.VK_LEFT])
		{
			viewangle -= pan;
			if (viewangle < 0)
				viewangle += 360;
			creature.rotate(viewangle);
			moved = true;
		}
		if (keys[KeyEvent.VK_W] && !keys[KeyEvent.VK_S])
		{
			dx = stepsize * (float) Math.cos(Math.toRadians(viewangle));
			dz = -stepsize * (float) Math.sin(Math.toRadians(viewangle));
			moved = moveXZ(dx, dz, 0) || moved;
		}
		if (keys[KeyEvent.VK_A] && !keys[KeyEvent.VK_D])
		{
			dx = -stepsize * (float) Math.sin(Math.toRadians(viewangle));
			dz = -stepsize * (float) Math.cos(Math.toRadians(viewangle));
			moved = moveXZ(dx, dz, 2) || moved;
		}
		if (keys[KeyEvent.VK_S] && !keys[KeyEvent.VK_W])
		{
			dx = -stepsize * (float) Math.cos(Math.toRadians(viewangle));
			dz = stepsize * (float) Math.sin(Math.toRadians(viewangle));
			moved = moveXZ(dx, dz, 1) || moved;
		}
		if (keys[KeyEvent.VK_D] && !keys[KeyEvent.VK_A])
		{
			dx = stepsize * (float) Math.sin(Math.toRadians(viewangle));
			dz = stepsize * (float) Math.cos(Math.toRadians(viewangle));
			moved = moveXZ(dx, dz, 3) || moved;
		}
		if (keys[KeyEvent.VK_W] && keys[KeyEvent.VK_S])
		{
			creature.stop(0);
			creature.stop(1);
		}
		if (keys[KeyEvent.VK_A] && keys[KeyEvent.VK_D])
		{
			creature.stop(2);
			creature.stop(3);
		}
		
		if (moved)
		{
			calcEyeLoc();
		}
		else
			canvas.repaint();
	}
	
	private boolean moveY(float dy)
	{
		if (!collision(0, dy, 0))
		{
			y += dy;
			creature.move(y);
			return true;
		}
		else
		{
			y = map.slideY(y);
			return false;
		}
	}
	
	private boolean moveXZ(float dx, float dz, int dir)
	{
		if (!collision(dx, 0, dz))
		{
			x += dx;
			z += dz;
			creature.move(x, z, dir);
			return true;
		}
		else if (canStep(dx, dz))
		{
			y += 1;
			x += dx;
			z += dz;
			creature.move(y);
			creature.move(x, z, dir);
			return true;
		}
		else if (!collision(dx, 0, 0))
		{
			x += dx;
			creature.move(x, z, dir);
			return true;
		}
		else if (!collision(0, 0, dz))
		{
			z += dz;
			creature.move(x, z, dir);
			return true;
		}
		else if (canStep(0, dz))
		{
			y += 1;
			z += dz;
			creature.move(y);
			creature.move(x, z, dir);
			return true;
		}
		else if (canStep(dx, 0))
		{
			y += 1;
			x += dx;
			creature.move(y);
			creature.move(x, z, dir);
			return true;
		}
		else
			return false;
	}
	
	private void calcEyeLoc()
	{
		eyex = x - 5 * (float) Math.cos(Math.toRadians(topangle)) * (float) Math.cos(Math.toRadians(viewangle));
		eyey = y + 5 * (float) Math.sin(Math.toRadians(topangle));
		eyez = z + 5 * (float) Math.cos(Math.toRadians(topangle)) * (float) Math.sin(Math.toRadians(viewangle));
	}
	
	public boolean collision(float dx, float dy, float dz)
	{
		return map.collision(creature.minusX(dx), creature.minusY(dy), creature.minusZ(dz),
		                     creature.plusX(dx), creature.plusY(dy), creature.plusZ(dz));
	}
	
	public boolean canStep(float dx, float dz)
	{
		return !map.collision(creature.minusX(dx), creature.minusY(1), creature.minusZ(dz),
		                     creature.plusX(dx), creature.plusY(1), creature.plusZ(dz));
	}
	
	public void drawSonic(GL gl)
	{
		GLUquadric quadric = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);	// GLU_POINT, GLU_LINE, GLU_FILL, GLU_SILHOUETTE
		glu.gluQuadricNormals  (quadric, GLU.GLU_NONE);	// GLU_NONE, GLU_FLAT, or GLU_SMOOTH
		glu.gluQuadricTexture  (quadric, false);			// use true to generate texture coordinates
		
		gl.glPushMatrix();
		gl.glColor3f(.3f, .3f, .7f);
		gl.glTranslatef(195, 17, sonicPos);
		glu.gluSphere(quadric, 1, 8, 8);
		gl.glTranslatef(-195, -17, -sonicPos);
		gl.glPopMatrix();
	}
	
	public void time(GLAutoDrawable drawable)
	{
		String t = time/60 + ":";
		if (time%60 < 10)
			t += "0";
		t += time%60;
		
		//int size = Math.min(drawable.getWidth(), drawable.getHeight());
		
		Time.beginRendering(1024, 768);
		Time.setColor(0, 0, 0, 1);
		Time.draw("TIME " + t, 14, 720);
		if (time < 540 || System.currentTimeMillis() % 300 < 150)
			Time.setColor(1, 1, 0, 1);
		else
			Time.setColor(1, 0, 0, 1);
		Time.draw("TIME", 10, 724);
		Time.setColor(1, 1, 1, 1);
		Time.draw(t, 143, 724);
		
		if (winlose == 1)
		{
			winlosecount++;
			
			Time.setColor(0, 0, 0, 1);
			Time.draw("DR. ROBOTNIK", -1666+20*Math.min(winlosecount, 100), 476);
			Time.draw("HAS DEFEATED", -2473+20*Math.min(winlosecount, 140), 406);
			Time.draw("HIS ARCHENEMY", 3914-20*Math.min(winlosecount, 180), 336);
			Time.draw("SONIC the HEDGEHOG!", 4634-20*Math.min(winlosecount, 220), 266);
			Time.setColor(1, 1, 1, 1);
			Time.draw("DR. ROBOTNIK", -1670+20*Math.min(winlosecount, 100), 480);
			Time.draw("HAS DEFEATED", -2477+20*Math.min(winlosecount, 140), 410);
			Time.draw("HIS ARCHENEMY", 3910-20*Math.min(winlosecount, 180), 340);
			Time.draw("SONIC the HEDGEHOG!", 4630-20*Math.min(winlosecount, 220), 270);
			
			if (winlosecount >= 600)
				System.exit(0);
		}
		else if (winlose == -1)
		{
			winlosecount++;
			
			Time.setColor(37f/255, 0, 184f/255, 1);
			Time.draw("TIME", -1996+20*Math.min(winlosecount, 120), 406);
			Time.draw("OVER", 2937-20*Math.min(winlosecount, 120), 406);
			Time.setColor(1, 1, 1, 1);
			Time.draw("TIME ", -2000+20*Math.min(winlosecount, 120), 410);
			Time.draw("OVER", 2933-20*Math.min(winlosecount, 120), 410);
			
			if (winlosecount >= 500)
				System.exit(0);
		}
		
    	Time.endRendering();
	}
	
	public void drawMap(GL gl)
	{
		GLUquadric quadric = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);	// GLU_POINT, GLU_LINE, GLU_FILL, GLU_SILHOUETTE
		glu.gluQuadricNormals  (quadric, GLU.GLU_NONE);	// GLU_NONE, GLU_FLAT, or GLU_SMOOTH
		glu.gluQuadricTexture  (quadric, false);			// use true to generate texture coordinates
		
		float depth = 40;
		
		// Map (Upper)
		gl.glViewport(800, 384, 192, 768);			// bottomx, bottomy, topx, topy
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-25, 25, 100, 300, -depth, 0);	// left, right, bottom, top
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 0, 1);
		gl.glRotatef(90, 1, 0, 0);
		drawSonic(gl);
		gr.draw(gl);
		gl.glColor3f(1, 0, 0);
		if (x > 97)
		{
			//creature.draw(gl);
			gl.glTranslatef(x, depth-.001f, z);
			gl.glRotatef(90, 1, 0, 0);
			gl.glRotatef(30-viewangle, 0, 0, 1);
			glu.gluDisk(quadric, 0, 3, 3, 3);
		}
		gl.glPopMatrix();
		
		// Map (Middle)
		gl.glViewport(800, 115, 192, 269);			// bottomx, bottomy, topx, topy
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		if (y < 16 || x > 30 && y < 24)
			depth = 23.998f;
		else
			depth = 40;
		gl.glOrtho(-25, 25, 30, 100, -depth, 0);	// left, right, bottom, top
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 0, 1);
		gl.glRotatef(90, 1, 0, 0);
		gr.draw(gl);
		gl.glColor3f(1, 0, 0);
		if (x < 103 && x > 27)
		{
			//creature.draw(gl);
			gl.glTranslatef(x, depth-.001f, z);
			gl.glRotatef(90, 1, 0, 0);
			gl.glRotatef(30-viewangle, 0, 0, 1);
			glu.gluDisk(quadric, 0, 3, 3, 3);
		}
		gl.glPopMatrix();
		
		// Map (Lower)
		gl.glViewport(800, 0, 192, 115);			// bottomx, bottomy, topx, topy
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		if (y < 16)
			depth = 15.999f;
		else
			depth = 40;
		gl.glOrtho(-25, 25, 0, 30, -depth, 0);	// left, right, bottom, top
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 0, 1);
		gl.glRotatef(90, 1, 0, 0);
		gr.draw(gl);
		gl.glColor3f(1, 0, 0);
		if (x < 33)
		{
			//creature.draw(gl);
			gl.glTranslatef(x, depth-.001f, z);
			gl.glRotatef(90, 1, 0, 0);
			gl.glRotatef(30-viewangle, 0, 0, 1);
			glu.gluDisk(quadric, 0, 3, 3, 3);
		}
		gl.glPopMatrix();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		// System.out.println("Reshaping to " + width + "x" + height);
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		width = height = Math.min(4*width/3, height);		// prevents different x, y scaling
		width *= 4f/3;
		
		// Window 1
		gl.glViewport(0, 0, width/2, height);
		glu.gluPerspective(90, 1, .5, 200);					// fov, aspect, near-clip, far clip
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		glu.gluLookAt(eyex, eyey, eyez,						// eye location
			 eyex+Math.cos(Math.toRadians(viewangle)),	// point to look at (near middle)
			 eyey, 
			 eyez-Math.sin(Math.toRadians(viewangle)),	
			 0f,1f,0f);												// the "up" direction
		
		// Window 2
		gl.glViewport(width/2, 0, width/2, height);
		gl.glOrtho(-10f, 20f, -10f, 10f, 0f, 10f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		glu.gluLookAt(eyex, 16, eyez,	// eye location
			 eyex,							// point to look at (near middle)
			 0, 
			 eyez,	
			 0f,1f,0f);						// the "up" direction
	}
	
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
		{System.out.println("displayChanged() entered\n");}
	
	public static void main(String args[])
	{
		JFrame mainFrame = new JFrame("Dr. Robotnik's Revenge");
		GLCanvas canvas = new GLCanvas();
		
		Game tex = new Game(canvas);
		canvas.addGLEventListener(tex);
		canvas.addKeyListener(tex);
		mainFrame.getContentPane().add(canvas);
		//canvas.setSize(600,600);
		canvas.setSize(1024, 768);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setVisible(true);
		canvas.requestFocus();
		
		(new Animator(canvas)).start();
	}
	
	public void keyPressed(KeyEvent e)
	{
		keys[e.getKeyCode()] = true;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			if (creature.idle())
				creature.fire();
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			stepsize = 1;
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			startTime -= 1000;
		// System.out.println("Key Pressed: " + e.getKeyCode());
	}
	
	public void keyReleased(KeyEvent e)
	{
		keys[e.getKeyCode()] = false;
		
		if (e.getKeyCode() == KeyEvent.VK_W)
			creature.stop(0);
		if (e.getKeyCode() == KeyEvent.VK_A)
			creature.stop(2);
		if (e.getKeyCode() == KeyEvent.VK_S)
			creature.stop(1);
		if (e.getKeyCode() == KeyEvent.VK_D)
			creature.stop(3);
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			stepsize = 1/8f;
		
		// System.out.println("Key Released: " + e.getKeyCode());
	}
	
	public void keyTyped(KeyEvent e){}
}

// "Situational"Map
//		where use byte, short, int as needed
// 	long is not supported because numbers greater than the maximum integer value
//		will cause an error, as such using long is very unwieldy
class Map
{
	// 0, 0, 0 of the ByteMap will be mapped to the x_offset, y_offset, z_offset of the environment
	// length, height, and width of environment
	// stacks is the number of numbers needed to represent the y-axis
	// totalBits is the number of bits used in a number
	private final int x_offset, y_offset, z_offset;
	private final int length, height, width, stacks;
	private final int totalBits;
	
	private Number Map[][][];
	
	// pass in the X1, Y1, Z1, and X2, Y2, Z2 of the environment to be mapped
	// where X1, Y1, Z1 is the near corner and X2, Y2, Z2 is the far corner
	// the ByteMap will be oriented in the fasion of X1...X2, Y1...Y2, Z1...Z2
	public Map(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		x_offset = x1;
		y_offset = y1;
		z_offset = z1;
		length = Math.abs(x2-x1);
		height = Math.abs(y2-y1);
		width = Math.abs(z2-z1);
		stacks = (height+31)/32;
		if (height < 9)
			totalBits = 8;
		else if (height < 17)
			totalBits = 16;
		else
			totalBits = 32;
		
		createMap();
	}
	
	public Map(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		x_offset = (int)x1;
		y_offset = (int)y1;
		z_offset = (int)z1;
		length = Math.abs((int)x2-(int)x1+1);
		height = Math.abs((int)y2-(int)y1+1);
		width = Math.abs((int)z2-(int)z1+1);
		stacks = (height+31)/32;
		if (height < 9)
			totalBits = 8;
		else if (height < 17)
			totalBits = 16;
		else
			totalBits = 32;
		
		createMap();
	}
	
	// Creates map with dimensions length, width, and stacks
	private void createMap()
	{
		int x, z, y;
		if (height < 9)
		{
			Map = new Byte[length][width][1];
			
			for (x=0;x<length;x++)
				for (z=0;z<width;z++)
					for (y=0;y<stacks;y++)
						Map[x][z][y] = new Byte((byte)0);
		}
		else if (height < 17)
		{
			Map = new Short[length][width][1];
			
			for (x=0;x<length;x++)
				for (z=0;z<width;z++)
					for (y=0;y<stacks;y++)
						Map[x][z][y] = new Short((short)0);
		}
		else
		{
			Map = new Integer[length][width][stacks];
			
			for (x=0;x<length;x++)
				for (z=0;z<width;z++)
					for (y=0;y<stacks;y++)
						Map[x][z][y] = new Integer(0);
		}
	}
	
	// checks for collision(s) from x1 to x2, y1 to y2, z1 to z2
	public boolean collision(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		return collision((int) x1, (int) y1, (int) z1, (int) x2, (int) y2, (int) z2);
	}
	
	public boolean collision(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		int x, y, z;
		for (x=x1;x<=x2;x++)
			for (z=z1;z<=z2;z++)
				for (y=y1;y<=y2;y++)
					if (collision(x, y, z))
						return true;
		return false;
	}
	
	// checks for collision(s) at x, y, z
	public boolean collision(float x, float y, float z)
	{
		return collision((int) x, (int) y, (int) z);
	}
	
	public boolean collision(int x, int y, int z)
	{
		// Prevents you from leaving the boundries
		if (	(x < x_offset || x >= length + x_offset) ||
				(y <= y_offset || y > height + y_offset) ||
				(z < z_offset || z >= width + z_offset))
			return true;
		
		// Simple collision detection against objects
		try
		{
			if (bitRep(Map[x-x_offset][z-z_offset][(y-y_offset)/32], (y-y_offset), (y-y_offset)).equals("1"))
				return true;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return true;
		}
		return false;
	}
	
	// called if a collision has been detected along the y-axis
		// moves character as close the collision as it can without a collision
	public float slideY(float y)
	{
		return ((int) (y + .75f)) - .5f;
	}
	
	// adds collision walls to the ByteMap from x1, y1, z1 to x2, y2, z2
		// NOTE: has not been tested for Maps with more than 32 layers
	public void add(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		int x, z, y, beg = y1/32, end = y2/32;
		for (x=x1;x<x2;x++)
			for (z=z1;z<z2;z++)
			{
				if (beg == end)
					add(x-x_offset, z-z_offset, beg, (y1-y_offset)%32, (y2-y_offset)%32);
				else
				{
					add(x-x_offset, z-z_offset, beg, (y1-y_offset)%32, (32*beg+32-y_offset)%32);
					for (y=beg+1;y<end;y++)
						add(x-x_offset, z-z_offset, y, (32*y-y_offset)%32, (32*y+32-y_offset)%32);
					add(x-x_offset, z-z_offset, end, (32*end-y_offset)%32, (y2-y_offset)%32);
				}
			}
	}
	
	public void add(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		add((int) x1, (int) y1, (int) z1, (int) x2, (int) y2, (int) z2);
	}
	
	// sets bits in base from beg to end to 1
	private void add(int x, int z, int y, int beg, int end)
	{
		Number base = Map[x][z][y];
		
		if (beg > end)
			return;
		if (beg < 0)
			beg = 0;
		if (end > totalBits)
			end = totalBits;
		
		String bits = "";
		for (int i=beg;i<end;i++)
			bits += "1";
		
		Map[x][z][y] = parseBits(bitRep(base, 1, beg) + bits + bitRep(base, end+1, totalBits));
	}
	
	// removes collision walls from the ByteMap from x1, y1, z1 to x2, y2, z2
	public void remove(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		int x, z, y, beg = y1/32, end = y2/32;
		for (x=x1-1;x<x2;x++)
			for (z=z1-1;z<z2;z++)
			{
				if (beg == end)
					remove(x-x_offset, z-z_offset, beg, (y1-y_offset)%32, (y2-y_offset)%32);
				else
				{
					remove(x-x_offset, z-z_offset, beg, (y1-y_offset)%32, (32*beg+32-y_offset)%32);
					for (y=beg+1;y<end;y++)
						remove(x-x_offset, z-z_offset, y, (32*y-y_offset)%32, (32*y+32-y_offset)%32);
					remove(x-x_offset, z-z_offset, end, (32*end-y_offset)%32, (y2-y_offset)%32);
				}
			}
	}
	
	public void remove(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		remove((int) x1, (int) y1, (int) z1, (int) x2+1, (int) y2+1, (int) z2+1);
	}
	
	// sets bits in base from beg to end to 0
	private void remove(int x, int z, int y, int beg, int end)
	{
		Number base = Map[x][z][y];
		
		if (beg > end)
			return;
		if (beg < 0)
			beg = 0;
		if (end > totalBits)
			end = totalBits;
		
		String bits = "";
		for (int i=beg;i<end;i++)
			bits += "0";
		
		Map[x][z][y] = parseBits(bitRep(base, 1, beg) + bits + bitRep(base, end+1, totalBits));
	}
	
	public String bitRep(Number N)
	{
		return bitRep(N, 1, totalBits);
	}
	
	// returns a String of the binary representation for a number
	private String bitRep(Number N, int beg, int end)
	{
		if (beg > end)
			return "";
		if (beg < 1)
			beg = 1;
		if (end > totalBits)
			end = totalBits;
		
		String bitRep = "";
		int bit, n = N.intValue();
		
		if (n < 0)
		{
			n += Math.pow(2, totalBits-1);
			if (beg <= 1)
				bitRep = "1";
		}
		else if (beg <= 1)
			bitRep = "0";
		
		if (beg < 2)
			beg = 2;
		
		for (bit=beg;bit<=end;bit++)
		{
			n %= Math.pow(2, totalBits-bit+1);
			
			if (n < Math.pow(2, totalBits-bit))
				bitRep += "0";
			else
				bitRep += "1";
		}
		
		return bitRep;
	}
	
	// returns a Number equivalent to the binary representation given
	private Number parseBits(String bitRep)
	{
		Number N = null;
		long n = 0;
		int bit = bitRep.lastIndexOf('1', totalBits-1);
		
		while (bit > -1)
		{
			n += Math.pow(2, totalBits-bit-1);
			bit = bitRep.lastIndexOf('1', bit-1);
		}
		
		if (totalBits == 8)
			N = new Byte((byte)n);
		else if (totalBits == 16)
			N = new Short((short)n);
		else
			N = new Integer((int)n);
		
		return N;
	}
	
	// prints a given layer of the Map
	public String printLayer(int slice)
	{
		int x, z;
		String MapSlice = "";
		
		for (x=length-1;x>=0;x--)
		{
			for (z=0;z<width;z++)
			{
				MapSlice += bitRep(Map[x][z][slice/32], slice%32, slice%32);
			}
			MapSlice += (char) 13;
		}
		
		return MapSlice;
	}
	
	// returns a text form of the Map
	public String toString()
	{
		int x, z, y;
		String TextMap = "From " + y_offset + " to " + (y_offset+height) + ":" + (char) 13;
		
		for (x=length-1;x>=0;x--)
		{
			for (y=1;y<=height;y++)
			{
				for (z=0;z<width;z++)
				{
					TextMap += bitRep(Map[x][z][y/32], y%32, y%32);
				}
				TextMap += " ";
			}
			TextMap += (char) 13;
		}
		
		return TextMap;
	}
	
	// Draws collision boxes
	public void draw(GL gl)
	{
		int x, z, y, s;
		String stack;
		for (x=0;x<length;x++)
		{
			for (z=0;z<width;z++)
			{
				for (s=0;s<stacks;s++)
				{
					stack = bitRep(Map[x][z][s], 0, 32);
					for (y=0;y<stack.length();y++)
					{
						if (stack.charAt(y) == '1')
						{
							gl.glColor3f((float)Math.random(), (float)Math.random(), (float)Math.random());
							gl.glBegin(GL.GL_QUADS);
							// Front
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset+1, z+z_offset);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset, z+z_offset);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset, z+z_offset+1);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset+1, z+z_offset+1);
							// Back
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset+1, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset+1, z+z_offset+1);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset, z+z_offset+1);
							// Left
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset+1, z+z_offset);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset+1, z+z_offset);
							// Right
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset, z+z_offset+1);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset, z+z_offset+1);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset+1, z+z_offset+1);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset+1, z+z_offset+1);
							// Top
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset, z+z_offset+1);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset, z+z_offset+1);
							// Bottom
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset+1, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset+1, z+z_offset);
							gl.glVertex3f(x+x_offset+1, 32*s+y+y_offset+1, z+z_offset+1);
							gl.glVertex3f(x+x_offset, 32*s+y+y_offset+1, z+z_offset+1);
							gl.glEnd();
						}
					}
				}
			}
		}
	}
}
