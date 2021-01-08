BACKSTORY
---------
We're going back to a time before CAP'N Jyym was even born to realize Dr. Robotnik's first ever ambition: taking over South Island.
But, unfortunately for him, Sonic the Hedgehog lurks out there ready to unravel his great plans.
You must seek out and destroy Sonic before time runs out, so that you may proceed with your master plan.

You must navigate through the not-so-treacherous (and incomplete) caves of Marble Zone and weave your way through the twisting, turning, looping Green Hill Zone and locate Sonic the Hedgehog.
Once you have found the small, blue hedgehog, you must drop 3 fireballs on him.  But be accurate!  Sonic is (not) very fast!
If you succeed, Dr. Robotnik will finally be rid of his archenemy and nobody (or so we think) will be able to stop him.
However, if you cannot defeat Sonic in the allotted time (10:00), Dr. Robotnik's Eggpod will crash and his plans (and my hard work) will be all for naught.


CONTROLS
--------
W = move forward
S = move backward
A = move left (strafe)
D = move right(strafe)

Up    = Increase Altitude
Down  = Decrease Altitude
Left  = Rotate Left
Right = Rotate Right

Spacebar = Fire fireball
	* You must be stationery to fire; if you are moving, you cannot fire
	* You can only fire 1 fireball at a time.

Shift (hold) = move quickly
	* This is used to speed up the process of finding Sonic.
Backspace (hold) = time passes more quickly
	* This is used to speed up the process of Dr. Robotnik's Eggpod crashing.
Escape = Exit


MECHANICS
---------
Download and compile all files and run Game (java Game).

Dr. Robotnik is not affected by gravity.
His Eggpod allows him to float above the ground.
However, he is limited in how high he can fly.

Upon starting the game, a clock will start in the upper-left corner of the screen.
You have 10 minutes to find Sonic; if you cannot find Sonic in 10 minutes, then the "Time Over" screen will appear.
During the final minute, the clock will flash to alert you that you need to hurry up!
Once you have found Sonic, you must position yourself correctly so that your fireballs will hit him.
Once 3 fireballs have collided with Sonic, the "Win" screen will appear.
After a few seconds of viewing either the "Time Over" screen or the "Win" screen, the game will exit.

On the right hand side of the screen, you will notice the overhead map.
Your position is indicated by the red triangle, and Sonic is indicated by the blue dot.
The red triangle points in the direction you are facing.
Depending on your altitude, you may see different areas of the map.
This happens because directly above the caves is more land to explore.

You will collide with objects if you get close enough.
For example, you cannot pass through any walls or buildings.
However, if there is an opening wide enough, you may be able to fly through it. (hint hint: loop)


OTHER NOTES
-----------
Not all the walls on the loop will cause collisions, this is done to avoid "colliding with air."
Though gravity is off, it can be turned on.
	To change gravity, change the if statements on lines 210 and 215 (to false and true, respectively).
Although I did not have time, I wanted to add a false wall and some music and sound effects...


"CAP'N" Jyym Culpepper
