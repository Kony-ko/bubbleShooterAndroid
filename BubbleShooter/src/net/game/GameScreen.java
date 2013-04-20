package net.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Delayed;

import net.game.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;

import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameScreen extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = GameScreen.class.getSimpleName();
	// static Point originalPoint;
	private GameThread thread;
	private Bubble[][] bubbles;
	private Bitmap[] bubbles_normal = new Bitmap[8];
	private Bitmap backgroundBMP;
	// variables for destruction
	Queue<Point> bubblePositionsToDestroy;
	int colorIndexDest = 0;

	static int width;
	static int height;
	static int bubbleHight;
	static int bubblewidth;
	static int bubbleSize;
	static int numOfBubble = 0;
	LinkedList<Bubble> waitingBubbles = new LinkedList<Bubble>();
	int numOfWaitingBubbles = 4;
	Bubble movingBubble;
	float slope;
	float deltaX = 0;
	boolean moving = false;
	final int noOfSteps = 50;
	boolean changingWaiting = false;
	int noOfShiftedRows = 0;
	int t1 = 0;
	private SoundPool sounds;
	private int collide;
	private int destroyGroup;
	private int loseSound;
	private int initialYDis = 60;
	private int initialXdis = 50;
	int noOfSameColor = 0;
	Random random = new Random();
	Paint paint;
	int score;
	boolean lose = false;

	Bitmap losePanel;
	Bitmap winPanel;
	Bitmap compressor;

	private void initialize() {
		// num of bubbles should be initialized according to level no

		// generating pics of bubbles
		bubbles = new Bubble[20][9];
		bubblePositionsToDestroy = new LinkedList<Point>();
		bubbleHight = bubbles.length;
		bubblewidth = bubbles[0].length;

		// movingBubble = new Bubble();

		BitmapFactory.Options options = new BitmapFactory.Options();

		bubbles_normal[0] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_1, options);
		bubbles_normal[1] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_2, options);
		bubbles_normal[2] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_3, options);
		bubbles_normal[3] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_4, options);
		bubbles_normal[4] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_5, options);
		bubbles_normal[5] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_6, options);
		bubbles_normal[6] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_7, options);
		bubbles_normal[7] = BitmapFactory.decodeResource(getResources(),
				R.drawable.bubble_8, options);
		bubbleSize = bubbles_normal[0].getWidth();
		backgroundBMP = BitmapFactory.decodeResource(getResources(),
				R.drawable.background, options);
		losePanel = BitmapFactory.decodeResource(getResources(),
				R.drawable.lose_panel, options);
		winPanel = BitmapFactory.decodeResource(getResources(),
				R.drawable.win_panel, options);
		paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setTextSize(20);
		noOfSameColor = 4;
		movingBubble = new Bubble();
		compressor = BitmapFactory.decodeResource(getResources(),
				R.drawable.compressor, options);

	}

	private void reInit() {
		noOfShiftedRows = 0;
		score = 0;
		numOfBubble = 6;
		int numOfBubblesGen = 0;

		int index = 0;
		for (int i = 0; i < bubbleHight; i++) {

			for (int j = 0; j < bubblewidth; j++) {
				if (numOfBubblesGen % noOfSameColor == 0) {
					index = random.nextInt(noOfSameColor);
				}
				if (numOfBubblesGen < numOfBubble) {
					if (i % 2 == 0) {

						bubbles[i][j] = new Bubble(bubbles_normal[index],
								initialXdis + (int) j * 30, initialYDis + i
										* 30);

						// positions[i] = new Point(30 + (int) j * 30, i* 30);

					} else {
						bubbles[i][j] = new Bubble(bubbles_normal[index],
								initialXdis + 15 + (int) j * 30, initialYDis
										+ i * 30);
						// positions[i] = new Point(45 + (int) j * 30, i * 30);
					}
					bubbles[i][j].colorIndex = index;
				} else {

					bubbles[i][j] = new Bubble();

				}
				numOfBubblesGen++;
			}

		}

	}

	public GameScreen(Context context) {

		super(context);
		initialize();
		width = 300;
		height = 500;

		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		// create droid and load bitmap
		// boolean odd = false;
		// float x = 1;
		// int y = 1;
		// for (int i = 0; i < bubbles.length; i++) {
		// int index = random.nextInt(8);
		// if (i % 8 == 0) {
		// x = 0;
		// y++;
		// odd = !odd;
		//
		// }
		// if (!odd) {
		// bubbles[i] = new Bubble(BitmapFactory.decodeResource(
		// getResources(), bubbles_normal[index]),
		// 30 + (int) x * 30, y * 30);
		// positions[i] = new Point(30 + (int) (x * 30), y * 30);
		//
		// } else {
		// bubbles[i] = new Bubble(BitmapFactory.decodeResource(
		// getResources(), bubbles_normal[index]),
		// 45 + (int) x * 30, y * 30);
		// positions[i] = new Point(45 + (int) (x * 30), y * 30);
		// }
		// x++;
		// }
		reInit();
		int index = 0;
		for (int i = 0; i < numOfWaitingBubbles; i++) {
			index = random.nextInt(noOfSameColor);
			Bubble curr = new Bubble(bubbles_normal[index], 2 * initialXdis + i
					* 30, height - initialYDis);
			curr.colorIndex = index;
			waitingBubbles.addFirst(curr);
		}
		// index = random.nextInt(8);
		// Bubble First = new Bubble(bubbles_normal[index], width / 2, height);
		// First.colorIndex = index;
		// waitingBubbles.add(First);
		// create the game loop thread
		thread = new GameThread(getHolder(), this);

		// make the GamePanel focusable so it can handle events
		setFocusable(true);
		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		collide = sounds.load(context, R.raw.rebound, 1);
		destroyGroup = sounds.load(context, R.raw.destroy_group, 1);
		loseSound = sounds.load(context, R.raw.lose, 1);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop
		// thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (lose) {
			lose = false;
			reInit();
		}
		if (!moving) {
			Bubble removed = waitingBubbles.removeFirst();
			// changing bubble position

			// int tempY = removed.y;

			synchronized (waitingBubbles) {
				for (int i = 0; i < waitingBubbles.size(); i++) {
					waitingBubbles.get(i).x = waitingBubbles.get(i).x + 30;

				}
			}

			int index = random.nextInt(8);
			Bubble curr = new Bubble(bubbles_normal[index], 2 * initialXdis,
					height - initialYDis);
			curr.colorIndex = index;
			waitingBubbles.addLast(curr);
			// add new Bubble
			movingBubble.bitmap = bubbles_normal[removed.colorIndex];
			movingBubble.x = removed.x;
			movingBubble.y = removed.y;
			// movingBubble = new Bubble(bubbles_normal[removed.colorIndex],
			// removed.x, removed.y);
			movingBubble.colorIndex = removed.colorIndex;
			movingBubble.destroy = false;
			float desiredX = event.getX();
			deltaX = ((desiredX - movingBubble.x) / noOfSteps);
			// if (desiredX > width / 2) {
			// deltaX = 1;
			// } else {
			// deltaX = -1;
			// }
			float desiredY = event.getY();
			slope = (desiredY - movingBubble.y) / (desiredX - movingBubble.x);
			moving = true;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);

		// fills the canvas with black

		canvas.drawBitmap(backgroundBMP, 165 - (backgroundBMP.getWidth() / 2),
				10, null);
		if (numOfBubble == 0) {
			canvas.drawBitmap(winPanel, initialXdis, 100, paint);
		} else if (!lose) {
			drawBubbles(canvas);

			if (!movingBubble.destroy)
				movingBubble.draw(canvas);
			update();
			// draw compressor
			canvas.drawBitmap(compressor, 5, -30 + (30 * noOfShiftedRows),
					paint);
			canvas.drawText("score", 20, height - 70, paint);
			canvas.drawText(score + "", 30, height - 50, paint);
		} else {
			canvas.drawBitmap(losePanel, initialXdis, 100, paint);
		}

	}

	/**
	 * while queue is not empty poll a bubble from the queue
	 */

	private void checkBubbleToDestroy() {
		int numOfBubblesChecked = 1;
		while (bubblePositionsToDestroy.size() > 0) {
			Log.d("Color", colorIndexDest + "");
			Point curr = bubblePositionsToDestroy.poll();
			int posX = curr.x;
			int posY = curr.y;
			Log.d("Points ", curr.x + " " + curr.y);
			// check its neighbor
			// check the right position
			if (posX < bubblewidth - 1) {
				if (!bubbles[posY][posX + 1].destroy
						&& !bubbles[posY][posX + 1].markedCheck) {
					if (bubbles[posY][posX + 1].colorIndex == colorIndexDest) {
						bubbles[posY][posX + 1].markedCheck = true;
						bubblePositionsToDestroy.add(new Point(posX + 1, posY));
						numOfBubblesChecked++;
					}
				}
			}
			// check the left position
			if (posX > 0) {
				if (!bubbles[posY][posX - 1].destroy
						&& !bubbles[posY][posX - 1].markedCheck) {
					if (bubbles[posY][posX - 1].colorIndex == colorIndexDest) {
						bubbles[posY][posX - 1].markedCheck = true;
						bubblePositionsToDestroy.add(new Point(posX - 1, posY));
						numOfBubblesChecked++;
					}
				}
			}
			// check the top and down
			if (posY % 2 == 0) {
				// even row

				if (posY < bubbleHight - 1) {
					// check bottom right
					if (!bubbles[posY + 1][posX].destroy
							&& !bubbles[posY + 1][posX].markedCheck) {
						if (bubbles[posY + 1][posX].colorIndex == colorIndexDest) {
							bubbles[posY + 1][posX].markedCheck = true;
							bubblePositionsToDestroy.add(new Point(posX,
									posY + 1));
							numOfBubblesChecked++;
						}
					}
					// check bottom left
					if (posX > 0) {
						if (!bubbles[posY + 1][posX - 1].destroy
								&& !bubbles[posY + 1][posX - 1].markedCheck) {
							if (bubbles[posY + 1][posX - 1].colorIndex == colorIndexDest) {
								bubbles[posY + 1][posX - 1].markedCheck = true;
								bubblePositionsToDestroy.add(new Point(
										posX - 1, posY + 1));
								numOfBubblesChecked++;
							}
						}
					}
				}

				if (posY > 0) {
					// check top right
					if (!bubbles[posY - 1][posX].destroy
							&& !bubbles[posY - 1][posX].markedCheck) {
						if (bubbles[posY - 1][posX].colorIndex == colorIndexDest) {
							bubbles[posY - 1][posX].markedCheck = true;
							bubblePositionsToDestroy.add(new Point(posX,
									posY - 1));
							numOfBubblesChecked++;
						}
					}
					// check top left
					if (posX > 0) {
						if (!bubbles[posY - 1][posX - 1].destroy
								&& !bubbles[posY - 1][posX - 1].markedCheck) {
							if (bubbles[posY - 1][posX - 1].colorIndex == colorIndexDest) {
								bubbles[posY - 1][posX - 1].markedCheck = true;
								bubblePositionsToDestroy.add(new Point(
										posX - 1, posY - 1));
								numOfBubblesChecked++;
							}
						}
					}
				}

			} else {
				// odd row
				if (posY < bubbleHight - 1) {
					// check bottom right
					if (posX < bubblewidth - 1) {
						if (!bubbles[posY + 1][posX + 1].destroy
								&& !bubbles[posY + 1][posX + 1].markedCheck) {
							if (bubbles[posY + 1][posX + 1].colorIndex == colorIndexDest) {
								bubbles[posY + 1][posX + 1].markedCheck = true;
								bubblePositionsToDestroy.add(new Point(
										posX + 1, posY + 1));
								numOfBubblesChecked++;
							}
						}
					}
					// check bottom left
					if (!bubbles[posY + 1][posX].destroy
							&& !bubbles[posY + 1][posX].markedCheck) {
						if (bubbles[posY + 1][posX].colorIndex == colorIndexDest) {
							bubbles[posY + 1][posX].markedCheck = true;
							bubblePositionsToDestroy.add(new Point(posX,
									posY + 1));
							numOfBubblesChecked++;
						}
					}
				}
				if (posY > 0) {
					// check top right
					if (posX < bubblewidth - 1) {
						if (!bubbles[posY - 1][posX + 1].destroy
								&& !bubbles[posY - 1][posX + 1].markedCheck) {
							if (bubbles[posY - 1][posX + 1].colorIndex == colorIndexDest) {
								bubbles[posY - 1][posX + 1].markedCheck = true;
								bubblePositionsToDestroy.add(new Point(
										posX + 1, posY - 1));
								numOfBubblesChecked++;
							}
						}
					}
					// check top left
					if (!bubbles[posY - 1][posX].destroy
							&& !bubbles[posY - 1][posX].markedCheck) {
						if (bubbles[posY - 1][posX].colorIndex == colorIndexDest) {
							bubbles[posY - 1][posX].markedCheck = true;
							bubblePositionsToDestroy.add(new Point(posX,
									posY - 1));
							numOfBubblesChecked++;
						}
					}
				}

			}

		}

		// check to destroy
		if (numOfBubblesChecked > 2) {
			score = score + numOfBubblesChecked;
			sounds.play(destroyGroup, 5.0f, 5.0f, 1, 0, 1.5f);
			for (int i = 0; i < bubbleHight; i++) {
				for (int j = 0; j < bubblewidth; j++) {
					if (!bubbles[i][j].destroy && bubbles[i][j].markedCheck) {

						bubbles[i][j].destroy = true;
						numOfBubble--;
					}
				}
			}
			// check if the ball should fall (no bubbles hold it)
			for (int i = 0; i < bubbleHight; i++) {
				for (int j = 0; j < bubblewidth; j++) {
					if (!bubbles[i][j].destroy && i > 0) {
						if (i % 2 == 0) {
							if (j > 0) {
								if (bubbles[i - 1][j - 1].destroy
										&& bubbles[i - 1][j].destroy) {
									bubbles[i][j].destroy = true;
									numOfBubble--;
								}
							}
						} else {
							if (j < bubblewidth - 1) {
								if (bubbles[i - 1][j + 1].destroy
										&& bubbles[i - 1][j].destroy) {
									bubbles[i][j].destroy = true;
									numOfBubble--;
								}
							}
						}

					}
				}

			}
		} else {
			for (int i = 0; i < bubbleHight; i++) {
				for (int j = 0; j < bubblewidth; j++) {
					if (!bubbles[i][j].destroy) {
						bubbles[i][j].markedCheck = false;
					}
				}
			}
		}

	}

	public void update() {
		if (!movingBubble.destroy) {
			if (moving) {
				if (movingBubble.x >= 30 * bubblewidth) {
					deltaX = -deltaX;
				}
				if (movingBubble.x <= initialXdis) {
					deltaX = -1 * deltaX;
				}

				movingBubble.x = (int) (movingBubble.x + deltaX);
				int deltaY = Math.abs((int) (slope * deltaX));

				movingBubble.y = movingBubble.y - deltaY;

				checkCollision();

				checkBubbleToDestroy();
			}
		}
		checkLoser();
	}

	private void checkLoser() {
		for (int i = bubbleHight - 1; i >= 0; i--) {
			for (int j = bubblewidth - 1; j >= 0; j--) {
				if (!bubbles[i][j].destroy) {
					if ((height - initialYDis - bubbles[i][j].y) < 5) {
						sounds.play(loseSound, 5.0f, 5.0f, 1, 0, 1.5f);
						lose = true;
					}
				}
			}
		}
	}

	private void drawBubbles(Canvas canvas) {
		boolean change = false;
		if (t1 >= 100 && t1 % 100 == 0) {
			change = true;
			t1 = 0;
			noOfShiftedRows++;

			Log.d("no Of shifted Rows", noOfShiftedRows + "");
		}
		for (int i = 0; i < bubbleHight; i++) {
			for (int j = 0; j < bubblewidth; j++) {

				if (!bubbles[i][j].destroy) {
					if (change) {

						bubbles[i][j].noOfShiftedRows = 1;
						bubbles[i][j].y += bubbles[i][j].noOfShiftedRows * 30;
					}
					bubbles[i][j].draw(canvas);
				}
			}
		}
		if (!changingWaiting) {
			synchronized (waitingBubbles) {

				Iterator<Bubble> it = waitingBubbles.iterator();
				while (it.hasNext()) {
					Bubble curr = it.next();
					curr.draw(canvas);
				}
			}
		}
		t1++;

	}

	private ArrayList<Bubble> getBubblesFrame() {
		ArrayList<Bubble> myBubbles = new ArrayList<Bubble>();
		// for (int i = 0; i < bubbleHight; i++) {
		// for (int j = 0; j < bubbleHight; j++) {
		// Bubble curr = bubbles[i][j];
		// if(curr==null){
		// //check Right
		// if(j<bubbleHight-1){
		// Bubble bubbleFrame = bubbles[i][j+1];
		// if(bubbleFrame!=null){
		//
		// }
		// }
		//
		// //check left
		//
		//
		// //check up
		// }
		// }
		// }
		return myBubbles;
	}

	/**
	 * check the collision for the moving bubble with the static bubble and put
	 * it in a suitable position on the array if it collides then put it on the
	 * queue (bubble to be destroyed)
	 */
	private void checkCollision() {

		if (moving) {
			// check if hit the ceil
			if (movingBubble.y <= (initialYDis + (noOfShiftedRows * 30))) {
				int posX = (movingBubble.x - 30) / 30;
				Log.d("no Of shifted Rows", noOfShiftedRows + "");
				if (bubbles[0][posX].destroy) {
					bubbles[0][posX].destroy = false;
					bubbles[0][posX].bitmap = bubbles_normal[movingBubble.colorIndex];
					bubbles[0][posX].x = posX * 30 + initialXdis;
					// bubbles[0][posX] = new Bubble(
					//
					// bubbles_normal[movingBubble.colorIndex],
					//
					// posX * 30 + initialXdis, initialYDis
					// + (noOfShiftedRows * 30));
					bubbles[0][posX].markedCheck = true;
					bubbles[0][posX].y = initialYDis + (noOfShiftedRows * 30);
					bubbles[0][posX].colorIndex = movingBubble.colorIndex;
					bubbles[0][posX].noOfShiftedRows = noOfShiftedRows;
					moving = false;
					movingBubble.destroy = true;
					numOfBubble++;

					// VIP note in this case x and y in the bubble represents
					// their index in the array
					bubblePositionsToDestroy.add(new Point(

					posX, 0));
					colorIndexDest = movingBubble.colorIndex;
					sounds.play(collide, 5.0f, 5.0f, 1, 0, 1.5f);

				}
			}
			for (int i = bubbleHight - 1; i >= 0 && moving; i--) {
				for (int j = bubblewidth - 1; j >= 0 && moving; j--) {
					Bubble curr = bubbles[i][j];
					if (!curr.destroy) {

						// int diffY = Math.abs(movingBubble.y - curr.y);

						// check if hit from right
						// if ((curr.x + 30 - movingBubble.x) < 5
						// && (curr.x + 30 - movingBubble.x) > -5
						// && (diffY < 5)) {
						// if (j < bubblewidth - 1) {
						// if (bubbles[i][j + 1] == null) {
						// bubbles[i][j + 1] = new
						//
						// Bubble(
						//
						// bubbles_normal[movingBubble.colorIndex],
						//
						// curr.x + 30, curr.y);
						// moving = false;
						// Log.d("Hi", "rightttt  ");
						// movingBubble.destroy=true;
						// numOfBubble++;
						// }
						// }
						// // check if hit from left
						// } else if ((movingBubble.x + 30 - curr.x) < 5
						// && (movingBubble.x + 30 - curr.x) > -5 &&
						//
						// (diffY < 5)) {
						// if (j > 0) {
						// if (bubbles[i][j - 1] == null) {
						// bubbles[i][j - 1] = new
						//
						// Bubble(
						//
						// bubbles_normal[movingBubble.colorIndex],
						//
						// curr.x - 30, curr.y);
						// Log.d("Hi", "lefttt  ");
						// moving = false;
						// movingBubble.destroy=true;
						// numOfBubble++;
						// }
						// }
						// check if hit from down
						// }
						if ((curr.y + 30) >= movingBubble.y) {
							if (i < bubbleHight - 1) {
								if ((movingBubble.x - curr.x) >= -15
										&& (movingBubble.x - curr.x) <= 12) {// put

									// in

									// the

									// left

									// bottom
									Log.d("Hi", "bottom left  ");
									if (i % 2 == 0)

									{// for even rows
										if (j

										> 0) {
											if (bubbles[i + 1][j - 1].destroy) {
												bubbles[i + 1][j - 1].bitmap = bubbles_normal[movingBubble.colorIndex];
												bubbles[i + 1][j - 1].destroy = false;
												bubbles[i + 1][j - 1].x = curr.x - 15;
												bubbles[i + 1][j - 1].y = curr.y + 30;
												// bubbles[i + 1][j - 1] = new
												// Bubble(
												//
												// bubbles_normal[movingBubble.colorIndex],
												//
												// curr.x - 15,
												// curr.y + 30);

												bubbles[i + 1][j - 1].markedCheck = true;
												bubbles[i + 1][j - 1].colorIndex = movingBubble.colorIndex;
												moving = false;
												movingBubble.destroy = true;
												numOfBubble++;
												bubblePositionsToDestroy
														.add(new Point(j - 1,
																i + 1));
												colorIndexDest = movingBubble.colorIndex;
												sounds.play(collide, 1.0f,
														1.0f, 0, 0, 1.5f);

											}
										}
									} else {// for odd rows
										if (bubbles[i + 1][j].destroy) {
											bubbles[i + 1][j].destroy = false;
											bubbles[i + 1][j].bitmap = bubbles_normal[movingBubble.colorIndex];
											bubbles[i + 1][j].x = curr.x - 15;
											bubbles[i + 1][j].y = curr.y + 30;
											// bubbles[i + 1][j] = new Bubble(
											//
											// bubbles_normal[movingBubble.colorIndex],
											//
											// curr.x - 15, curr.y + 30);
											bubbles[i + 1][j].colorIndex = movingBubble.colorIndex;
											bubbles[i + 1][j].markedCheck = true;
											moving = false;
											movingBubble.destroy = true;
											numOfBubble++;
											bubblePositionsToDestroy
													.add(new Point(j,

													i + 1));
											colorIndexDest = movingBubble.colorIndex;
											sounds.play(collide, 1.0f, 1.0f, 0,
													0, 1.5f);

										}
									}

								} else if ((movingBubble.x - curr.x) >= 12 &&

								(movingBubble.x - curr.x) <= 30) {// put

									// in

									// the

									// right

									// bottom
									Log.d("Hi", "bottom right  ");
									if (i % 2 == 0)

									{
										if (bubbles[i + 1][j].destroy) {
											bubbles[i + 1][j].destroy = false;
											bubbles[i + 1][j].bitmap = bubbles_normal[movingBubble.colorIndex];
											bubbles[i + 1][j].x = curr.x + 15;
											bubbles[i + 1][j].y = curr.y + 30;
											// bubbles[i + 1][j] = new Bubble(
											//
											// bubbles_normal[movingBubble.colorIndex],
											//
											// curr.x + 15, curr.y + 30);
											bubbles[i + 1][j].markedCheck = true;
											bubbles[i + 1][j].colorIndex = movingBubble.colorIndex;
											moving = false;
											movingBubble.destroy = true;
											numOfBubble++;
											bubblePositionsToDestroy
													.add(new Point(j,

													i + 1));
											colorIndexDest = movingBubble.colorIndex;
											sounds.play(collide, 1.0f, 1.0f, 0,
													0, 1.5f);

										}
									} else {

										if (j

										< bubblewidth - 1) {
											if (bubbles[i + 1][j + 1].destroy) {
												bubbles[i + 1][j + 1].destroy = false;
												bubbles[i + 1][j + 1].bitmap = bubbles_normal[movingBubble.colorIndex];
												bubbles[i + 1][j + 1].x = curr.x + 15;
												bubbles[i + 1][j + 1].y = curr.y + 30;

												// bubbles[i + 1][j + 1] = new
												// Bubble(
												//
												// bubbles_normal[movingBubble.colorIndex],
												//
												// curr.x + 15,
												// curr.y + 30);
												bubbles[i + 1][j + 1].colorIndex = movingBubble.colorIndex;
												bubbles[i + 1][j + 1].markedCheck = true;
												moving = false;
												movingBubble.destroy = true;
												numOfBubble++;
												bubblePositionsToDestroy
														.add(new Point(j + 1,

														i + 1));
												colorIndexDest = movingBubble.colorIndex;
												sounds.play(collide, 1.0f,
														1.0f, 0, 0, 1.5f);

											}
										}
									}
								}
							}
						}
						// if (curr.x + 30 == movingBubble.x &&
						// (movingBubble.y-curr.y)<10) {
						// if (j < bubblewidth - 1) {
						// bubbles[i][j + 1] = new Bubble(
						// bubbles_normal[movingBubble.colorIndex],
						// curr.x +30 , curr.y);
						// moving = false;
						// Log.d("Hi", "henaaaa   "+movingBubble.colorIndex);
						// }
						// } else if (curr.x - 30 ==
						// movingBubble.x&&((movingBubble.y-curr.y)<10)) {
						// if (j > 0) {
						// bubbles[i][j - 1] = new Bubble(
						// bubbles_normal[movingBubble.colorIndex],
						// curr.x - 30, curr.y);
						// Log.d("Hi", "henaaaa   "+movingBubble.colorIndex);
						// moving = false;
						// }
						// }else if(curr.y+30 == movingBubble.y &&
						// (movingBubble.x-curr.x )<5 && (movingBubble.x-curr.x
						// )>-5){
						// if(i<bubbleHight){
						// bubbles[i+1][j]= new Bubble(
						// bubbles_normal[movingBubble.colorIndex],
						// curr.x+15 , curr.y+30);
						// Log.d("Hi", "henaaaa   "+movingBubble.colorIndex);
						// moving = false;
						// }
						// }else if((movingBubble.y-(curr.y+30))<5 &&
						// (curr.x-movingBubble.x )<5 &&
						// (curr.x-movingBubble.x)>-5){
						// if(i<bubbleHight && j>0){
						// bubbles[i+1][j-1]= new Bubble(
						// bubbles_normal[movingBubble.colorIndex],
						// curr.x-15 , curr.y+30);
						// Log.d("Hi", "henaaaa   "+movingBubble.colorIndex);
						// moving = false;
						// }
						// }

					}
				}
			}

		}

	}

}
