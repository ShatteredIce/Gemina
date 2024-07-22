package com.gemina.mainframe;

import com.gemina.rendering.Model;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static java.sql.Types.NULL;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Game {

    //setup window variables
    int WINDOW_WIDTH = 840;
    int WINDOW_HEIGHT = 640;
    int gameScreenWidth = 840;
    int gameScreenHeight = 640;
    int worldWidth = 640;
    int worldHeight = 640;
    int tileLength = 64;

    //setup camera variables
    public double viewX = 0;
    public double viewY = 0;
    public double cameraSpeed = 1;
    public double cameraWidth = 840;
    public double cameraHeight = 640;
    public int windowXOffset = 0;
    public int windowYOffset = 0;
    boolean panLeft = false;
    boolean panRight = false;
    boolean panUp = false;
    boolean panDown = false;

    //setup map variables
    int mapWidth;
    int mapHeight;
    int[][] map;
    Tile[][] tiles = null;
    int texturePack = 1;
    //Random map generation
    Random random = new Random();

    //WASD Key press state
    boolean aPressed = false;
    boolean wPressed = false;
    boolean dPressed = false;
    boolean sPressed = false;

    // The window handle
    private long window;
    //Rendering variables
    final double[] textureCoords = {0, 1, 0, 0, 1, 1, 1, 0};
    final int[] indices = {0, 1, 2, 2, 1, 3};
    final double[] placeholder = {0, 0, 0, 0, 0, 0, 0, 0};

    public void run(){
        //Initialize game engine and game loop
        initGLFW();
        loop();

        //Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        //Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void initGLFW() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Gemina Project", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");


        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            //camera controls
            if (key == GLFW_KEY_MINUS && action == GLFW_PRESS)
                System.out.println("minus pressed");
            if (key == GLFW_KEY_EQUAL && action == GLFW_PRESS)
                System.out.println("equal pressed");
            //pan camera
            if (key == GLFW_KEY_LEFT && action == GLFW_PRESS)
                panLeft = true;
            if (key == GLFW_KEY_LEFT && action == GLFW_RELEASE)
                panLeft = false;
            if (key == GLFW_KEY_RIGHT && action == GLFW_PRESS)
                panRight = true;
            if (key == GLFW_KEY_RIGHT && action == GLFW_RELEASE)
                panRight = false;
            if (key == GLFW_KEY_UP && action == GLFW_PRESS)
                panUp = true;
            if (key == GLFW_KEY_UP && action == GLFW_RELEASE)
                panUp = false;
            if (key == GLFW_KEY_DOWN && action == GLFW_PRESS)
                panDown = true;
            if (key == GLFW_KEY_DOWN && action == GLFW_RELEASE)
                panDown = false;

        });
        //mouse clicks
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            DoubleBuffer xpos = BufferUtils.createDoubleBuffer(3);
            DoubleBuffer ypos = BufferUtils.createDoubleBuffer(3);
            glfwGetCursorPos(window, xpos, ypos);
            //convert the glfw coordinate to our coordinate system
            xpos.put(0, Math.min(Math.max(xpos.get(0), windowXOffset), WINDOW_WIDTH + windowXOffset));
            ypos.put(0, Math.min(Math.max(ypos.get(0), windowYOffset), WINDOW_HEIGHT + windowYOffset));
            //relative camera coordinates
            xpos.put(1, getWidthScalar() * (xpos.get(0) - windowXOffset) + viewX);
            ypos.put(1, getHeightScalar() * (ypos.get(0) - windowYOffset) + viewY);
            //true window coordinates
            xpos.put(2, xpos.get(0) - windowXOffset);
            ypos.put(2, ypos.get(0) - windowYOffset);
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                System.out.println(xpos.get(1) + "," + ypos.get(1));
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop(){
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        projectTrueWindowCoordinates();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //Enable transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        tiles = generateMap();

        Model model = new Model(placeholder, textureCoords, indices);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {

            // Poll for window events (key callbacks)
            glfwPollEvents();

            glEnable(GL_TEXTURE_2D);

            drawGame(model);

            //move camera
            if (panLeft) {
                viewX = Math.max(0, viewX - cameraWidth * 0.01 * cameraSpeed);
            }
            if (panRight) {
                viewX = Math.min(worldWidth - cameraWidth * (double) gameScreenWidth / (double) WINDOW_WIDTH, viewX + cameraWidth * 0.01 * cameraSpeed);
            }
            if (panDown) {
                viewY = Math.max(0, viewY - cameraHeight * 0.01 * cameraSpeed);
            }
            if (panUp) {
                viewY = Math.min(worldHeight - cameraHeight * (double) gameScreenHeight / (double) WINDOW_HEIGHT, viewY + cameraHeight * 0.01 * cameraSpeed);
            }

            glfwSwapBuffers(window);
        }
    }


    public static void main(String[] args) {
        new Game().run();
    }

    //generate map
    public Tile[][] generateMap(){
        mapWidth = 30;
        mapHeight = 20;
        worldWidth = mapWidth * tileLength;
        worldHeight = mapHeight * tileLength;
        map = new int[mapWidth][mapHeight];
        //set a random terrain for each tile
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                int seed = random.nextInt(20);
                if(seed <= 3) {
                    map[i][j] = 1;
                }
                else if(seed <= 15) {
                    map[i][j] = 2;
                }
                else if(seed <= 18) {
                    map[i][j] = 3;
                }
                else if(seed <= 19) {
                    map[i][j] = 4;
                }
            }
        }
        Tile[][] tiles = new Tile[mapWidth][mapHeight];
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                tiles[x][y] = new Tile(map[x][y], x, y);
            }
        }
        return tiles;
    }

    public void drawMap(Model model) {
        projectRelativeCameraCoordinates();
        //display tiles
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                tiles[i][j].setTexture(texturePack);
                model.render(tiles[i][j].getVertices());
                //TODO - optimize by not rendering tiles offscreen
            }
        }
    }


    //Calls all the draw functions in order
    public void drawGame(Model model) {
        drawMap(model);
    }

    //Screen projections based on camera or window
    public void projectRelativeCameraCoordinates(){
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity(); // Resets any previous projection matrices
        glOrtho((-windowXOffset * getWidthScalar()) + viewX, viewX + cameraWidth + (windowXOffset * getWidthScalar()), viewY + ((-windowYOffset) * getHeightScalar()), viewY + cameraHeight + ((windowYOffset)* getHeightScalar()), 1, -1);
        glMatrixMode(GL_MODELVIEW);
    }
    public void projectTrueWindowCoordinates(){
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity(); // Resets any previous projection matrices
        glOrtho(-windowXOffset, WINDOW_WIDTH + windowXOffset, WINDOW_HEIGHT + windowYOffset, -windowYOffset, 1, -1);
        glMatrixMode(GL_MODELVIEW);
    }

    //Scalars to help calculation
    public double getWidthScalar(){
        return(double) cameraWidth / (double) WINDOW_WIDTH;
    }

    public double getHeightScalar(){
        return(double) cameraHeight / (double) WINDOW_HEIGHT;
    }

    public double mapWidthScalar() {
        return (double) gameScreenWidth / (double) WINDOW_WIDTH;
    }

    public double mapHeightScalar() {
        return (double) gameScreenHeight / (double) WINDOW_HEIGHT;
    }


    //Zoom camera in or out
    public void updateZoomLevel(boolean zoomOut){
        DoubleBuffer xpos = BufferUtils.createDoubleBuffer(3);
        DoubleBuffer ypos = BufferUtils.createDoubleBuffer(3);
        glfwGetCursorPos(window, xpos, ypos);
        //Convert the glfw coordinate to our coordinate system
        xpos.put(0, Math.min(Math.max(xpos.get(0), windowXOffset), WINDOW_WIDTH + windowXOffset));
        ypos.put(0, Math.min(Math.max(ypos.get(0), windowYOffset), WINDOW_HEIGHT + windowYOffset));
        //Relative camera coordinates
        xpos.put(1, getWidthScalar() * (xpos.get(0) - windowXOffset) + viewX);
        ypos.put(1, getHeightScalar() * (ypos.get(0) - windowYOffset) + viewY);
        //True window coordinates
        xpos.put(2, xpos.get(0) - windowXOffset);
        ypos.put(2, ypos.get(0) - windowYOffset);

        boolean mouseInFrame = false;
        double oldX = xpos.get(1);
        double oldY = ypos.get(1);
        double xAxisDistance = 0;
        double yAxisDistance = 0;

        if(xpos.get(2) > 0 && xpos.get(2) < gameScreenWidth && ypos.get(2) > 0 && ypos.get(2) < gameScreenHeight){
            mouseInFrame = true;
            xAxisDistance = xpos.get(2)/WINDOW_WIDTH;
            yAxisDistance = ypos.get(2)/WINDOW_HEIGHT;
        }

        int MIN_WIDTH = 100;
        int MIN_HEIGHT = 100;
        int MAX_WIDTH = worldWidth * WINDOW_WIDTH / gameScreenWidth;
        int MAX_HEIGHT = worldHeight * WINDOW_HEIGHT / gameScreenHeight;

        double zoomLevel = 4d/3d;

        if(!mouseInFrame) {
            oldX = viewX + (cameraWidth * gameScreenWidth/WINDOW_WIDTH)/2;
            oldY = viewY + (cameraHeight * gameScreenHeight/WINDOW_HEIGHT)/2;
            xAxisDistance = (gameScreenWidth/2d/WINDOW_WIDTH);
            yAxisDistance = (gameScreenHeight/2d/WINDOW_HEIGHT);
        }

        //Zooms out camera
        if(zoomOut){
            if(cameraWidth * zoomLevel <= MAX_WIDTH && cameraHeight * zoomLevel <= MAX_HEIGHT){
                cameraWidth *= zoomLevel;
                cameraHeight *= zoomLevel;
                viewX = oldX - cameraWidth * xAxisDistance;
                viewY = oldY - cameraHeight * yAxisDistance;
//				System.out.println(viewX + " " + cameraWidth);
                double gameScreenCameraWidth = cameraWidth * gameScreenWidth / WINDOW_WIDTH;
                double gameScreenCameraHeight = cameraHeight * gameScreenHeight / WINDOW_HEIGHT;
                if(viewX + gameScreenCameraWidth > worldWidth){
                    viewX = worldWidth - gameScreenCameraWidth;
                }
                if(viewY + gameScreenCameraHeight > worldHeight){
                    viewY = worldHeight - gameScreenCameraHeight;
                }
                if(viewX < 0){
                    viewX = 0;
                }
                if(viewY < 0){
                    viewY = 0;
                }
            }
        }
        else{ // Zooms in camera
            if(cameraWidth / zoomLevel >= MIN_WIDTH && cameraHeight / zoomLevel >= MIN_HEIGHT){
                cameraWidth /= zoomLevel;
                cameraHeight /= zoomLevel;
                viewX = oldX - cameraWidth * xAxisDistance;
                viewY = oldY - cameraHeight * yAxisDistance;
            }
        }
    }
}