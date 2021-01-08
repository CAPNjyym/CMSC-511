// "CAP'N" Jyym Culpepper
// Creature

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import java.io.IOException;

import com.sun.opengl.util.texture.*;

class Creature
{
	private GLUquadric quadric;	// to control properties of quadric-based objects here
	private Texture Pod, Suit, Windshield;				
	private GLCanvas canvas;		// for media tracker	 
	private GLU glu = new GLU();
	
	// size is the radius of the creature, d is the level of detail
	// flux is how much movement occurs during the movement animation, rate is how quickly it is animated
	private final float size = 1;
	private final int d = 24;
	private final float flux = .374f, rate = 3f;
	
	private int fallcount = 0, walkcount = 0, firecount = 0, moveAccel[] = new int[4];;
	private float x, y, z, angle, firex, firey, firez, fireangle;
	private boolean falling = false, moving = false, firing = false, moveDir[] = new boolean[4], colBox = false;
	
	private final float FALL_ANGLE = -45;
	
	public Creature(GL gl, GLCanvas canvas) 
	{
		// Set parameters for quadrics
		this.canvas = canvas;
		quadric = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);	// GLU_POINT, GLU_LINE, GLU_FILL, GLU_SILHOUETTE
		glu.gluQuadricNormals  (quadric, GLU.GLU_NONE);	// GLU_NONE, GLU_FLAT, or GLU_SMOOTH
		glu.gluQuadricTexture  (quadric, false);			// use true to generate texture coordinates
		
		// Define textures
		Pod = createTexture("Pod.jpg", ".jpg");
		Suit = createTexture("Suit.jpg", ".jpg");
		Windshield = createTexture("Windshield.png", ".png");
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
	
	public void draw(GL gl)
	{
		if (colBox)
			drawColBox(gl);
		
		if (firing)
			drawFireball(gl);
		
		gl.glColor3f(1f, 1f, 1f);
		gl.glPushMatrix();
		
		// increment falling animation
		if (falling)
		{
			float i = fallcount / 90f, a = (float) Math.toRadians(angle);
			
			gl.glTranslatef(x, y-1, z);
			gl.glRotatef(i*FALL_ANGLE, (float) -Math.cos(a), 0, (float) Math.sin(a));
			gl.glRotatef(i*FALL_ANGLE, (float) -Math.sin(a), 0, (float) -Math.cos(a));
			gl.glTranslatef(-x, 1-y, -z);
		}
		else
		{
			float a = (float) Math.toRadians(angle), xdir = 0, zdir = 0;
			
			gl.glTranslatef(x, y, z);
			
			//if (moveDir[0]) // forward
			gl.glRotatef(moveAccel[0], (float) -Math.sin(a), 0, (float) -Math.cos(a));
			//if (moveDir[1]) // backward
			gl.glRotatef(moveAccel[1], (float) Math.sin(a), 0, (float) Math.cos(a));
			//if (moveDir[2]) // left
			gl.glRotatef(moveAccel[2], (float) -Math.cos(a), 0, (float) Math.sin(a));
			//if (moveDir[3]) // right
			gl.glRotatef(moveAccel[3], (float) Math.cos(a), 0, (float) -Math.sin(a));
			
			gl.glTranslatef(-x, flux*(float)Math.sin(Math.toRadians(rate*walkcount)) - y, -z);
		}
		gl.glTranslatef(x, y, z);
		gl.glRotatef(angle-180, 0, 1, 0);
		drawPod(gl);
		drawRobotnik(gl);
		drawWindshield(gl);
		
		gl.glPopMatrix();
	}
	
	private void drawPod(GL gl)
	{
		// half is .5/16, one is 1/16 of size, two is 2/16 of size, four is 4/16 of size, six is 6/16 of size, eight is 8/16
		float half = size/32f, one = 2*half, two = 2*one, four = 2*two, six = 3*two, eight = 2*four; // heights of segments of pod
		
		double	clip1[] = {0, 0, -1, two},							// clips main pod where y > two
					clip2[] = {0, 0, 1, six},							// clips main pod where y < -six
					clip3[] = {0, -1, 0, -size+four-23*half/8},	// clips top of chair where z < ends of chair
					clip4[] = {1, 0, 0, eight},						// clips chair where x < eight
					clip5[] = {-1, 0, 0, eight},						// clips chair where x > eight
					clip6[] = {0, -1, 0, 0},							// clips chair where z < 0
					clip7[] = {0, 0, 1, 0},								// clips chair where y < -six
					clip8[] = {0, 0, -1, size-two},					// clips chair where y > size-eight,
					clip9[] = {4, 1, 0, .8*four},						// clips right "sidepod" where 4y - x > size/5
					clip10[] = {-4, 1, 0, .8*four};					// clips left "sidepod" where 4y + x > size/5
		
		glu.gluQuadricTexture(quadric, true);
		
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 1, 0);
		gl.glRotatef(-90, 1, 0, 0);
		
		// Main pod
		gl.glClipPlane(GL.GL_CLIP_PLANE0, clip1, 0);
		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glColor3f(0, 0, 0);
		glu.gluSphere(quadric, size-one, d, d); // Underside / interior of pod
		gl.glClipPlane(GL.GL_CLIP_PLANE1, clip2, 0);
		gl.glEnable(GL.GL_CLIP_PLANE1);
		gl.glColor3f(1, 1, 1);
		Pod.enable();
		Pod.bind();
		glu.gluSphere(quadric, size, d, d); // Main exterior piece
		Pod.disable();
		gl.glDisable(GL.GL_CLIP_PLANE0);
		gl.glDisable(GL.GL_CLIP_PLANE1);
		
		// fills gaps between pieces of pod
		gl.glColor3f(.6f, .6f, .6f);
		gl.glTranslatef(0, 0, two);
		glu.gluDisk(quadric, Math.sqrt((size-one)*(size-one)-two*two), Math.sqrt(size*size-two*two), d, d);
		gl.glTranslatef(0, 0, -eight);
		glu.gluPartialDisk(quadric, Math.sqrt((size-one)*(size-one)-six*six), Math.sqrt(size*size-six*six), d-d/12, d-d/12, 15, 330);
		gl.glColor3f(0, 0, 0);
		glu.gluPartialDisk(quadric, Math.sqrt((size-one)*(size-one)-six*six), Math.sqrt(size*size-six*six), d/12, d/12, -15, 30);
		
		// Underside fireball "cannon"
		gl.glTranslatef(0, 0, -eight-four);
		gl.glColor3f(.1f, .1f, .1f);
		glu.gluCylinder(quadric, size/6, size/6, eight, d, d);
		gl.glColor3f(.6f, .6f, .6f);
		glu.gluCylinder(quadric, 3*size/16, 3*size/16, eight, d, d);
		glu.gluDisk(quadric, size/6, 3*size/16, d, d);
		gl.glColor3f(.5f, .5f, .5f);
		gl.glTranslatef(0, 0, two);
		glu.gluDisk(quadric, 3*size/16, size/4, d, d);
		glu.gluCylinder(quadric, size/4, size/4, six, d, d);
		
		gl.glTranslatef(0, 0, eight+two);
		
		// Bottom of Chair
		gl.glColor3f(.3f, .3f, .7f);
		glu.gluDisk(quadric, 0, Math.sqrt((size-one)*(size-one)-six*six), d, d);
		
		gl.glClipPlane(GL.GL_CLIP_PLANE0, clip3, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE1, clip4, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE2, clip5, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE3, clip6, 0);
		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glEnable(GL.GL_CLIP_PLANE1);
		gl.glEnable(GL.GL_CLIP_PLANE2);
		gl.glEnable(GL.GL_CLIP_PLANE3);
		
		// use clipped disk to create top of chair
		gl.glColor3f(.6f, .6f, 1);
		gl.glTranslatef(0, -four+8*half/5, size-two);
		gl.glRotatef(d/360, 0, 1, 0);
		glu.gluDisk(quadric, size-four+half, size-two+half, d, d);
		gl.glRotatef(-d/360, 0, 1, 0);
		gl.glTranslatef(0, eight+four-3*half/5, two-size);
		
		gl.glDisable(GL.GL_CLIP_PLANE0);
		gl.glClipPlane(GL.GL_CLIP_PLANE4, clip7, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE5, clip8, 0);
		gl.glEnable(GL.GL_CLIP_PLANE4);
		gl.glEnable(GL.GL_CLIP_PLANE5);
		
		// Front of Chair
		gl.glColor3f(.3f, .3f, .7f);
		gl.glRotatef(25, 1, 0, 0);
		glu.gluCylinder(quadric, size, size, 2*size, d, d);
		gl.glRotatef(-25, 1, 0, 0);
		gl.glTranslatef(0, -two, 0);
		
		// Back of Chair
		gl.glRotatef(25, 1, 0, 0);
		glu.gluCylinder(quadric, size, size, 2*size, d, d);
		
		gl.glDisable(GL.GL_CLIP_PLANE1);
		gl.glDisable(GL.GL_CLIP_PLANE2);
		gl.glDisable(GL.GL_CLIP_PLANE3);
		gl.glTranslatef(-eight, (float)-Math.sqrt(size*size-eight*eight), 0);
		
		// Left side of Chair
		gl.glColor3f(.15f, .15f, .6f);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, two*(float)Math.cos(Math.toRadians(25)), 0);
		gl.glVertex3f(0, two*(float)Math.cos(Math.toRadians(25)), 2*size);
		gl.glVertex3f(0, 0, 2*size);
		gl.glEnd();
		
		// Right side of Chair
		gl.glTranslatef(size, 0, 0);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, two*(float)Math.cos(Math.toRadians(25)), 0);
		gl.glVertex3f(0, two*(float)Math.cos(Math.toRadians(25)), 2*size);
		gl.glVertex3f(0, 0, 2*size);
		gl.glEnd();
		
		gl.glDisable(GL.GL_CLIP_PLANE4);
		gl.glDisable(GL.GL_CLIP_PLANE5);
		gl.glTranslatef(-eight, (float)Math.sqrt(size*size-eight*eight), 0);
		gl.glRotatef(-25, 1, 0, 0);
		gl.glTranslatef(0, -six-half, 0);
		
		// use "cut" triangles to fill sides
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(-eight, (float)-Math.sqrt((size-one)*(size-one)-eight*eight-two*two), eight);
		gl.glVertex3f(-eight, (float)-Math.sqrt((size-one)*(size-one)-eight*eight-two*two)-half, four);
		gl.glVertex3f(-eight, (float)-Math.sqrt((size-one)*(size-one)-eight*eight-two*two)+half, 0);
		gl.glVertex3f(-eight, -eight-half, 0);
		
		gl.glVertex3f(eight, (float)-Math.sqrt((size-one)*(size-one)-eight*eight-two*two), eight);
		gl.glVertex3f(eight, (float)-Math.sqrt((size-one)*(size-one)-eight*eight-two*two)-half, four);
		gl.glVertex3f(eight, (float)-Math.sqrt((size-one)*(size-one)-eight*eight-two*two)+half, 0);
		gl.glVertex3f(eight, -eight-half, 0);
		gl.glEnd();
		
		// right "sidepod"
		gl.glRotatef(-90, 1, 0, 0);
		gl.glColor3f(.6f, .6f, .6f);
		gl.glTranslatef((float)Math.sqrt(size*size-four*four), -two-one, -two-one);
		gl.glClipPlane(GL.GL_CLIP_PLANE0, clip9, 0);
		gl.glEnable(GL.GL_CLIP_PLANE0);
		glu.gluCylinder(quadric, two+half, two+half, six, 3*d/8, 3*d/8);
		gl.glColor3f(.1f, .1f, .1f);
		gl.glTranslatef(0, 0, half/5);
		glu.gluDisk(quadric, 0, two+half, 3*d/8, 3*d/8);
		gl.glTranslatef(0, 0, six-2*half/5);
		glu.gluDisk(quadric, 0, two+half, 3*d/8, 3*d/8);
		gl.glDisable(GL.GL_CLIP_PLANE0);
		
		// left "sidepod"
		gl.glTranslatef(-2*(float)Math.sqrt(size*size-four*four), 0, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE0, clip10, 0);
		gl.glEnable(GL.GL_CLIP_PLANE0);
		glu.gluDisk(quadric, 0, two+half, 3*d/8, 3*d/8);
		gl.glTranslatef(0, 0, 2*half/5-six);
		glu.gluDisk(quadric, 0, two+half, 3*d/8, 3*d/8);
		gl.glColor3f(.6f, .6f, .6f);
		glu.gluCylinder(quadric, two+half, two+half, six, 3*d/8, 3*d/8);
		gl.glDisable(GL.GL_CLIP_PLANE0);
		
		// left "bars"
		gl.glColor3f(.5f, .5f, .5f);
		gl.glTranslatef(-four, 0, -one-half/5);
		glu.gluCylinder(quadric, half, half, eight, d/4, d/4);
		gl.glRotatef(125, 0, 1, 0);
		glu.gluCylinder(quadric, half, half, eight+one, d/4, d/4);
		glu.gluSphere(quadric, half, d/4, d/4);
		gl.glRotatef(-125, 0, 1, 0);
		gl.glTranslatef(0, 0, eight);
		gl.glRotatef(55, 0, 1, 0);
		glu.gluCylinder(quadric, half, half, eight+one, d/4, d/4);
		glu.gluSphere(quadric, half, d/4, d/4);
		gl.glRotatef(-55, 0, 1, 0);
		
		// right "bars"
		gl.glTranslatef(2*(float)Math.sqrt(size*size-four*four)+eight, 0, 0);
		gl.glRotatef(-55, 0, 1, 0);
		glu.gluCylinder(quadric, half, half, eight+one, d/4, d/4);
		glu.gluSphere(quadric, half, d/4, d/4);
		gl.glRotatef(55, 0, 1, 0);
		gl.glTranslatef(0, 0, -eight);
		glu.gluCylinder(quadric, half, half, eight, d/4, d/4);
		gl.glRotatef(-125, 0, 1, 0);
		glu.gluCylinder(quadric, half, half, eight+one, d/4, d/4);
		glu.gluSphere(quadric, half, d/4, d/4);
		gl.glRotatef(125, 0, 1, 0);
		
		gl.glPopMatrix();
		
		glu.gluQuadricTexture(quadric, false);
	}
	
	public void drawRobotnik(GL gl)
	{
		// ~1/4 head, ~5/8 body
		float body = 3*size/4, head = 11*size/32; // size of head and body in relation to size
		
		double clip[] = {0, 1, 0, size/8}; // clips body where y < chair
		
		glu.gluQuadricTexture(quadric, true);
		
		gl.glPushMatrix();
		
		// Body and Head
		gl.glColor3f(1, 1, 1);
		gl.glTranslatef(0, -size/4, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE0, clip, 0);
		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glRotatef(-90, 1, 0, 0);
		gl.glRotatef(-90, 0, 0, 1);
		Suit.enable();
		Suit.bind();
		glu.gluSphere(quadric, body, d/2, d/2);
		Suit.disable();
		gl.glRotatef(90, 0, 0, 1);
		gl.glRotatef(90, 1, 0, 0);
		gl.glDisable(GL.GL_CLIP_PLANE0);
		
		if (!falling)
			gl.glColor3f(1, .7f, .6f);
		else
			gl.glColor3f(.6f, 0, 0);
		gl.glTranslatef(0, 15*size/16, 0);
		glu.gluSphere(quadric, head, 7*d/16, 7*d/16);
		
		// Stash
		gl.glTranslatef(-15*head/16, -7*head/16, 0);
		
		// Normal stash
		if (!falling)
		{
			gl.glColor3f(.6f, 0, 0);
			gl.glRotatef(18, 0, 0, 1);
			
			for (int i=-1;i<2;i+=2)
			{
				gl.glBegin(GL.GL_QUADS);
				
				// Part 1: Front
				gl.glVertex3f(-head/16, -head/32, 0);			// p1v1
				gl.glVertex3f(-head/16, 5*head/32, 0);			// p1v2
				gl.glVertex3f(0, head/4, i*head/2);				// p1v6
				gl.glVertex3f(0, -head/16, i*head/2);			// p1v5
				// Part 1: Bottom
				gl.glVertex3f(-head/16, -head/32, 0);			// p1v1
				gl.glVertex3f(head/8, 0, 0);						// p1v3
				gl.glVertex3f(5*head/16, -head/16, i*head/2);// p1v7
				gl.glVertex3f(0, -head/16, i*head/2);			// p1v5
				// Part 1: Top
				gl.glVertex3f(-head/16, 5*head/32, 0);			// p1v2
				gl.glVertex3f(head/8, head/8, 0);				// p1v4
				gl.glVertex3f(5*head/16, head/4, i*head/2);	// p1v8
				gl.glVertex3f(0, head/4, i*head/2);				// p1v6
				
				// p1v5 = p2v1, p1v6 = p2v2, p1v7 = p2v3, p1v8 = p2v4
				// Part 2: Front
				gl.glVertex3f(0, -head/16, i*head/2);					// p2v1
				gl.glVertex3f(0, head/4, i*head/2);						// p2v2
				gl.glVertex3f(7*head/16, 7*head/16, i*7*head/8);	// p2v6
				gl.glVertex3f(7*head/16, -7*head/32, i*7*head/8);	// p2v5
				// Part 2: Top
				gl.glVertex3f(0, -head/16, i*head/2);					// p2v1
				gl.glVertex3f(5*head/16, -head/16, i*head/2);		// p2v3
				gl.glVertex3f(head/2, -7*head/32, i*13*head/16);	// p2v7
				gl.glVertex3f(7*head/16, -7*head/32, i*7*head/8);	// p2v5
				// Part 2: Bottom
				gl.glVertex3f(0, head/4, i*head/2);						// p2v2
				gl.glVertex3f(5*head/16, head/4, i*head/2);			// p2v4
				gl.glVertex3f(head/2, 7*head/16, i*13*head/16);		// p2v8
				gl.glVertex3f(7*head/16, 7*head/16, i*7*head/8);	// p2v6
				
				// p2v5 = p3v1, p2v6 = p3v2, p2v7 = p3v3, p2v8 = p3v4
				// Part 3: Front
				gl.glVertex3f(7*head/16, -7*head/32, i*7*head/8);	// p3v1
				gl.glVertex3f(7*head/16, 7*head/16, i*7*head/8);	// p3v2
				gl.glVertex3f(9*head/16, 9*head/32, i*head);			// p3v6
				gl.glVertex3f(9*head/16, -head/16, i*head);			// p3v5
				// Part 3: Back
				gl.glVertex3f(head/2, -7*head/32, i*13*head/16);	// p3v3
				gl.glVertex3f(head/2, 7*head/16, i*13*head/16);		// p3v4
				gl.glVertex3f(5*head/8, 9*head/32, i*15*head/16);	// p3v8
				gl.glVertex3f(5*head/8, -head/16, i*15*head/16);	// p3v7
				
				gl.glEnd();
				gl.glBegin(GL.GL_TRIANGLES);
				
				// p3v1 = e1p1, p3v5 = e1p2, p3v3 = e1p3, p3v7 = e1p4
				// End 1: Front
				gl.glVertex3f(7*head/16, -7*head/32, i*7*head/8);	// e1v1
				gl.glVertex3f(9*head/16, -head/16, i*head);			// e1v2
				gl.glVertex3f(3*head/4, -5*head/16, i*head);			// e1v5
				// End 1: Back
				gl.glVertex3f(head/2, -7*head/32, i*13*head/16);	// e1v3
				gl.glVertex3f(5*head/8, -head/16, i*15*head/16);	// e1v4
				gl.glVertex3f(3*head/4, -5*head/16, i*head);			// e1v5
				// End 1: Top
				gl.glVertex3f(9*head/16, -head/16, i*head);			// e1v2
				gl.glVertex3f(5*head/8, -head/16, i*15*head/16);	// e1v4
				gl.glVertex3f(3*head/4, -5*head/16, i*head);			// e1v5
				// End 1: Bottom
				gl.glVertex3f(7*head/16, -7*head/32, i*7*head/8);	// e1v1
				gl.glVertex3f(head/2, -7*head/32, i*13*head/16);	// e1v3
				gl.glVertex3f(3*head/4, -5*head/16, i*head);			// e1v5
				
				// p3v5 = e2p1, p3v6 = e2p2, p3v7 = e2p3, p3v8 = e2p4
				// End 2: Front
				gl.glVertex3f(9*head/16, -head/16, i*head);			// e2v1
				gl.glVertex3f(9*head/16, 9*head/32, i*head);			// e2v2
				gl.glVertex3f(3*head/4, head/8, i*17*head/16);		// e3v5
				// End 2: Back
				gl.glVertex3f(5*head/8, -head/16, i*15*head/16);	// e2v3
				gl.glVertex3f(5*head/8, 9*head/32, i*15*head/16);	// e2v4
				gl.glVertex3f(3*head/4, head/8, i*17*head/16);		// e3v5
				// End 2: Top
				gl.glVertex3f(9*head/16, 9*head/32, i*head);			// e2v2
				gl.glVertex3f(5*head/8, 9*head/32, i*15*head/16);	// e2v4
				gl.glVertex3f(3*head/4, head/8, i*17*head/16);		// e3v5
				// End 2: Bottom
				gl.glVertex3f(9*head/16, -head/16, i*head);			// e2v1
				gl.glVertex3f(5*head/8, -head/16, i*15*head/16);	// e2v3
				gl.glVertex3f(3*head/4, head/8, i*17*head/16);		// e3v5
				
				// p3v6 = e3p1, p3v2 = e3p2, p3v8 = e3p3, p3v4 = e3p4
				// End 3: Front
				gl.glVertex3f(9*head/16, 9*head/32, i*head);			// e3v1
				gl.glVertex3f(7*head/16, 7*head/16, i*7*head/8);	// e3v2
				gl.glVertex3f(3*head/4, 9*head/16, i*head);			// e3v5
				// End 3: Back
				gl.glVertex3f(5*head/8, 9*head/32, i*15*head/16);	// e3v3
				gl.glVertex3f(4*head/8, 7*head/16, i*13*head/16);	// e3v4
				gl.glVertex3f(3*head/4, 9*head/16, i*head);			// e3v5
				// End 3: Top
				gl.glVertex3f(7*head/16, 7*head/16, i*7*head/8);	// e3v2
				gl.glVertex3f(head/2, 7*head/16, i*13*head/16);		// e3v4
				gl.glVertex3f(3*head/4, 9*head/16, i*16*head/16);	// e3v5
				// End 3: Bottom
				gl.glVertex3f(9*head/16, 9*head/32, i*head);			// e3v1
				gl.glVertex3f(5*head/8, 9*head/32, i*15*head/16);	// e3v3
				gl.glVertex3f(3*head/4, 9*head/16, i*head);			// e3v5
				
				gl.glEnd();
			}
		
		gl.glRotatef(-18, 0, 0, 1);
		}
		
		// Falling stash
		else
		{
			gl.glColor3f(.3f, 0, 0);
			for (int i=-1;i<2;i+=2)
			{
				gl.glBegin(GL.GL_QUADS);
				
				// Main: Front
				gl.glVertex3f(-head/16, head/16, 0);			// mv1
				gl.glVertex3f(-head/16, 3*head/16, 0);			// mv2
				gl.glVertex3f(head/16, 5*head/8, i*head);		// mv6
				gl.glVertex3f(head/16, 0, i*head);				// mv5
				// Main: Back
				gl.glVertex3f(head/16, head/16, 0);				// mv3
				gl.glVertex3f(head/16, 3*head/16, 0);			// mv4
				gl.glVertex3f(3*head/16, 5*head/8, i*head);	// mv8
				gl.glVertex3f(3*head/16, 0, i*head);			// mv7
				// Main: Bottom
				gl.glVertex3f(-head/16, head/16, 0);			// mv1
				gl.glVertex3f(head/16, head/16, 0);				// mv3
				gl.glVertex3f(3*head/16, 0, i*head);			// mv7
				gl.glVertex3f(head/16, 0, i*head);				// mv5
				// Main: Top
				gl.glVertex3f(-head/16, 3*head/16, 0);			// mv2
				gl.glVertex3f(head/16, 3*head/16, 0);			// mv4
				gl.glVertex3f(3*head/16, 5*head/8, i*head);	// mv8
				gl.glVertex3f(head/16, 5*head/8, i*head);		// mv6
				
				gl.glEnd();
				gl.glBegin(GL.GL_TRIANGLES);
				
				// mv5 = e1v1, e1p2 = e2v1, mv7 = e1v3, e1p4 = e2v3
				// End 1: Front
				gl.glVertex3f(head/16, 0, i*head);					// e1v1
				gl.glVertex3f(head/16, 3*head/16, i*head);		// e1v2
				gl.glVertex3f(3*head/16, -head/32, i*3*head/2);	// e1v5
				// End 1: Back
				gl.glVertex3f(3*head/16, 0, i*head);				// e1v3
				gl.glVertex3f(3*head/16, 3*head/16, i*head);		// e1v4
				gl.glVertex3f(3*head/16, -head/32, i*3*head/2);	// e1v5
				// End 1: Top
				gl.glVertex3f(head/16, 3*head/16, i*head);		// e1v2
				gl.glVertex3f(3*head/16, 3*head/16, i*head);		// e1v4
				gl.glVertex3f(3*head/16, -head/32, i*3*head/2);	// e1v5
				// End 1: Bottom
				gl.glVertex3f(head/16, 0, i*head);					// e1v1
				gl.glVertex3f(3*head/16, 0, i*head);				// e1v3
				gl.glVertex3f(3*head/16, -head/32, i*3*head/2);	// e1v5
				
				// e1v2 = e2v1, e2v2 = e3v1, e1v4 = e2v3, e2v4 = e3v4
				// End 2: Front
				gl.glVertex3f(head/16, 3*head/16, i*head);			// e2v1
				gl.glVertex3f(head/16, 7*head/16, i*head);			// e2v2
				gl.glVertex3f(3*head/16, 13*head/32, i*3*head/2);	// e2v5
				// End 2: Back
				gl.glVertex3f(3*head/16, 3*head/16, i*head);			// e2v3
				gl.glVertex3f(3*head/16, 7*head/16, i*head);			// e2v4
				gl.glVertex3f(3*head/16, 13*head/32, i*3*head/2);	// e2v5
				// End 2: Top
				gl.glVertex3f(head/16, 7*head/16, i*head);			// e2v2
				gl.glVertex3f(3*head/16, 7*head/16, i*head);			// e2v4
				gl.glVertex3f(3*head/16, 13*head/32, i*3*head/2);	// e2v5
				// End 2: Bottom
				gl.glVertex3f(head/16, 3*head/16, i*head);			// e2v1
				gl.glVertex3f(3*head/16, 3*head/16, i*head);			// e2v3
				gl.glVertex3f(3*head/16, 13*head/32, i*3*head/2);	// e2v5
				
				// e2v2 = e3v1, mv6 = e3v2, e2v4 = e3v3, mv8 = e3v4
				// End 3: Front
				gl.glVertex3f(head/16, 7*head/16, i*head);			// e3v1
				gl.glVertex3f(head/16, 5*head/8, i*head);				// e3v2
				gl.glVertex3f(3*head/16, 27*head/32, i*3*head/2);	// e2v5
				// End 3: Back
				gl.glVertex3f(3*head/16, 7*head/16, i*head);			// e3v3
				gl.glVertex3f(3*head/16, 5*head/8, i*head);			// e3v4
				gl.glVertex3f(3*head/16, 27*head/32, i*3*head/2);	// e2v5
				// End 3: Top
				gl.glVertex3f(head/16, 5*head/8, i*head);				// e3v2
				gl.glVertex3f(3*head/16, 5*head/8, i*head);			// e3v4
				gl.glVertex3f(3*head/16, 27*head/32, i*3*head/2);	// e2v5
				// End 3: Bottom
				gl.glVertex3f(head/16, 7*head/16, i*head);			// e3v1
				gl.glVertex3f(3*head/16, 7*head/16, i*head);			// e3v3
				gl.glVertex3f(3*head/16, 27*head/32, i*3*head/2);	// e2v5
				
				gl.glEnd();
			}
		}
		
		// Nose
		gl.glColor3f(1, .08f, .06f);
		gl.glTranslatef(-head/16, 5*head/16, 0);
		glu.gluSphere(quadric, 3*head/32, 7*d/32, 7*d/32);
		
		// Shades
		if (!falling)
			gl.glColor3f(.15f, .15f, .6f);
		else
			gl.glColor3f(.6f, .6f, 1);
		gl.glRotatef(-90, 0, 1, 0);
		gl.glTranslatef(0, 3*head/32, -head/21);
		glu.gluPartialDisk(quadric, 7*head/32/Math.sqrt(2.0), 7*head/32/Math.sqrt(2.0)+head/64, 7*d/32, 7*d/32, 300, 120);
		
		gl.glTranslatef(-15*head/64, 3*head/16, 0);
		glu.gluDisk(quadric, 0, 5*head/32, 3*d/8, 3*d/8);
		gl.glTranslatef(15*head/32, 0, 0);
		glu.gluDisk(quadric, 0, 5*head/32, 3*d/8, 3*d/8);
		
		// Mouth (for falling)
		if (falling)
		{
			gl.glTranslatef(-15*head/64, -11*head/16, -5*head/32);
			gl.glColor3f(1, 1, 1);
			gl.glRotatef(35, 1, 0, 0);
			glu.gluPartialDisk(quadric, 0, head/4, 3*d/16, 3*d/16, 270, 180);
			gl.glRotatef(-35, 1, 0, 0);
		}
		
		gl.glPopMatrix();
		
		glu.gluQuadricTexture(quadric, false);
	}
	
	public void drawFireball(GL gl)
	{
		//double clip[] = {0, -1, 0, y}; // clips fireball before the chair
		
		//gl.glClipPlane(GL.GL_CLIP_PLANE0, clip, 0);
		//gl.glEnable(GL.GL_CLIP_PLANE0);
		
		gl.glPushMatrix();
		
		gl.glTranslatef(firex, firey, firez);
		gl.glColor3f(1, .6f, 0);
		gl.glRotatef(-90, 1, 0, 0);
		glu.gluSphere(quadric, size/6, d/3, d/3);
		glu.gluCylinder(quadric, size/6, 0, size/3, d/3, d/3);
		
		gl.glPopMatrix();
	}
	
	// this must be drawn after everything else so the transparency works correctly
	public void drawWindshield(GL gl)
	{
		double	clip1[] = {0, 0, 1, 3*size/8},		// clips where y < -six
					clip2[] = {0, 1, -1, -5*size/16};	// clips where z + y > -eight
		
		glu.gluQuadricTexture(quadric, true);
		
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 1, 0);
		gl.glRotatef(-90, 1, 0, 0);
		gl.glColor3f(1, 1, 1);
		
		gl.glClipPlane(GL.GL_CLIP_PLANE0, clip1, 0);
		gl.glClipPlane(GL.GL_CLIP_PLANE1, clip2, 0);
		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glEnable(GL.GL_CLIP_PLANE1);
		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		Windshield.enable();
		Windshield.bind();
		
		gl.glRotatef(180, 1, 0, 0);
		glu.gluSphere(quadric, 31*size/32, d, d);
		gl.glRotatef(180, 1, 0, 0);
		
		Windshield.disable();
		
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_CLIP_PLANE0);
		gl.glDisable(GL.GL_CLIP_PLANE1);
		
		gl.glPopMatrix();
	}
	
	// the "minus X" of the collision box
	public int minusX(float dx)
	{
		return diffX(dx, -2);
	}
	
	// the "plus X" of the collision box
	public int plusX(float dx)
	{
		return diffX(dx, 1);
	}
	
	// the "minus Y" of the collision box
	public int minusY(float dy)
	{
		return diffY(dy, -1);
	}
	
	// the "plus Y" of the collision box
	public int plusY(float dy)
	{
		return diffY(dy, 2);
	}
	
	// the "minus Z" of the collision box
	public int minusZ(float dz)
	{
		return diffZ(dz, -2);
	}
	
	// the "plus Z" of the collision box
	public int plusZ(float dz)
	{
		return diffZ(dz, 1);
	}
	
	// returns "minusX" or "plusX" depending on the diff sent
	public int diffX(float dx, int diff)
	{
		float diffX = x + dx + diff;
		
		if (diffX >= 0)
			diffX += .5f;
		else
			diffX -= .5f;
		
		return (int) diffX;
	}
	
	// returns "minusY" or "plusY" depending on the diff sent
	public int diffY(float dy, int diff)
	{
		float diffY = y + dy + diff;
		
		if (diffY >= 0)
			diffY += .5f;
		else
			diffY -= .5f;
		
		return (int) diffY;
	}
	
	// returns "minusZ" or "plusZ" depending on the diff sent
	public int diffZ(float dz, int diff)
	{
		float diffZ = z + dz + diff;
		
		if (diffZ >= 0)
			diffZ += .5f;
		else
			diffZ -= .5f;
		
		return (int) diffZ;
	}
	
	public void drawColBox(GL gl)
	{
		float plusX, minusX, plusY, minusY, plusZ, minusZ;
		
		minusX = minusX(0);
		plusX = plusX(0) + 1;
		minusY = minusY(0) - 1;
		plusY = plusY(0);
		minusZ = minusZ(0);
		plusZ = plusZ(0) + 1;
		
		// X-Z Plane
		gl.glColor3f(1, 0, 0);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(minusX, minusY, plusZ);
		gl.glVertex3f(plusX, minusY, plusZ);
		gl.glVertex3f(plusX, minusY, minusZ);
		gl.glVertex3f(minusX, minusY, minusZ);
		
		gl.glVertex3f(minusX, plusY, plusZ);
		gl.glVertex3f(plusX, plusY, plusZ);
		gl.glVertex3f(plusX, plusY, minusZ);
		gl.glVertex3f(minusX, plusY, minusZ);
		gl.glEnd();
		
		// Y-Z Plane
		gl.glColor3f(0, 1, 0);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(plusX, minusY, minusZ);
		gl.glVertex3f(plusX, plusY, minusZ);
		gl.glVertex3f(plusX, plusY, plusZ);
		gl.glVertex3f(plusX, minusY, plusZ);
		
		gl.glVertex3f(minusX, minusY, minusZ);
		gl.glVertex3f(minusX, plusY, minusZ);
		gl.glVertex3f(minusX, plusY, plusZ);
		gl.glVertex3f(minusX, minusY, plusZ);
		gl.glEnd();
		
		// X-Y Plane
		gl.glColor3f(0, 0, 1);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(minusX, minusY, minusZ);
		gl.glVertex3f(plusX, minusY, minusZ);
		gl.glVertex3f(plusX, plusY, minusZ);
		gl.glVertex3f(minusX, plusY, minusZ);
		
		gl.glVertex3f(minusX, minusY, plusZ);
		gl.glVertex3f(plusX, minusY, plusZ);
		gl.glVertex3f(plusX, plusY, plusZ);
		gl.glVertex3f(minusX, plusY, plusZ);
		gl.glEnd();
	}
	
	public boolean firing()
	{
		return firing;
	}
	
	public void fired()
	{
		firecount = 0;
		firing = false;
	}
	
	public float[] fireXYZ()
	{
		float f[] = new float[3];
		f[0] = firex;
		f[1] = firey;
		f[2] = firez;
		return f;
	}
	
	// increments all time-related (count) variables
	public void inc()
	{
		accelerate();
		if (firing)
		{
			firecount++;
			firey -= 1/6f;
		}
		
		if (falling)
			fallcount++;
		else
			walkcount++;
		
		if (fallcount >= 90)
			fallcount = 90;
		if (firecount >= 100)
		{
			firecount = 0;
			firing = false;
		}
		if (walkcount >= 360.0)
			walkcount -= 360.0;
	}
	
	public boolean incfalling()
	{
		falling = true;
		fallcount++;
		if (fallcount >= 90)
		{
			fallcount = 90;
			return true;
		}
		return false;
	}
	
	// sets creature at a location
	public void set(float dx, float dy, float dz, float dangle)
	{
		x = dx;
		y = dy;
		z = dz;
		angle = dangle;
	}
	
	// activates falling animation variable
	public void fall()
	{
		if (falling && fallcount >= 90)
		{
			falling = false;
			fallcount = 0;
		}
		else
			falling = true;
	}
	
	public void fire()
	{
		firing = true;
		firex = x;
		firey = y-size/2;
		firez = z;
	}
	
	public void accelerate()
	{
		for (int ddir=0;ddir<4;ddir++)
		{
			if (moveDir[ddir])
			{
				moveAccel[ddir]++;
				if (moveAccel[ddir] > 20)
					moveAccel[ddir] = 20;
			}
			
			else
			{
				moveAccel[ddir]--;
				if (moveAccel[ddir] < 0)
					moveAccel[ddir] = 0;
			}
		}
	}
	
	public void move(float dx, float dz, int ddir)
	{
		x = dx;
		z = dz;
			
		moveDir[ddir] = true;
	}
	
	public void stop(int ddir)
	{
		moveDir[ddir] = false;
	}
	
	public void move(float dy)
	{
		y = dy;
	}
	
	public void rotate(float viewangle)
	{
		angle = viewangle;
	}
	
	public boolean idle()
	{
		return !moveDir[0]&&!moveDir[1]&&!moveDir[2]&&!moveDir[3]&&!falling&&!firing;
	}
	
	public void stop()
	{
		for (int i=0;i<4;i++)
			moveDir[i] = false;
	}
	
	public void col()
	{
		colBox = !colBox;
	}
}
