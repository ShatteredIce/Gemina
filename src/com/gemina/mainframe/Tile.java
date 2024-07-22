package com.gemina.mainframe;

import com.gemina.rendering.Texture;

public class Tile {

    static int tileWidth = 64;
    static int tileHeight = 64;

    //texture pack 1
    final static Texture tex1 = new Texture("road.png");
    final static Texture tex2 = new Texture("grass.png");
    final static Texture tex3 = new Texture("tree.png");
    final static Texture tex4 = new Texture("tower_neutral.png");
    final static Texture tex5 = new Texture("tower_red.png");
    final static Texture tex6 = new Texture("tower_blue.png");
    //texture pack 2
    final static Texture tex11 = new Texture("road.png");
    final static Texture tex12 = new Texture("forest.png");
    final static Texture tex13 = new Texture("rock.png");
    final static Texture tex14 = new Texture("tower_neutral.png");
    final static Texture tex15 = new Texture("tower_red.png");
    final static Texture tex16 = new Texture("tower_blue.png");

    double[] vertices = new double[8];

    int id;
    int x_pos;
    int y_pos;

    public Tile(int newid, int newx, int newy){
        id = newid;
        x_pos = newx;
        y_pos = newy;
        setVertices();
    }

    public void setVertices(){
        vertices[0] = tileWidth * x_pos;
        vertices[1] = tileHeight * y_pos;
        vertices[2] = tileWidth * x_pos;
        vertices[3] = tileHeight * y_pos + tileHeight;
        vertices[4] = tileWidth * x_pos + tileWidth;
        vertices[5] = tileHeight * y_pos;
        vertices[6] = tileWidth * x_pos + tileWidth;
        vertices[7] = tileHeight * y_pos + tileHeight;
    }

    public void setTexture(int pack){
        if(pack == 1) {
            switch (id) {
                case 1:
                    tex1.bind();
                    break;
                case 2:
                    tex2.bind();
                    break;
                case 3:
                    tex3.bind();
                    break;
                case 4:
                    tex4.bind();
                    break;
                case 5:
                    tex5.bind();
                    break;
                case 6:
                    tex6.bind();
                    break;
            }
        }
        else if(pack == 2) {
            switch (id) {
                case 1:
                    tex11.bind();
                    break;
                case 2:
                    tex12.bind();
                    break;
                case 3:
                    tex13.bind();
                    break;
                case 4:
                    tex14.bind();
                    break;
                case 5:
                    tex15.bind();
                    break;
                case 6:
                    tex16.bind();
                    break;
            }
        }
    }

    public int getMovement(){
        switch (id) {
            case 1:
                return 4;
            case 2 : case 4: case 5: case 6:
                return 1;
            case 3:
                return 2;
            default:
                return -1;
        }
    }

    public static int getWidth(){
        return tileWidth;
    }

    public static int getHeight(){
        return tileHeight;
    }

    public void setId(int newid) {
        id = newid;
    }

    public int getId(){
        return id;
    }

    public double[] getVertices(){
        return vertices;
    }

    public boolean isCapturePoint() {
        if(id == 4 || id == 5 || id == 6) {
            return true;
        }
        return false;
    }

}
