package com.example.Ev3Proj3;

public class Press {
    private String buttonId;
    private int speed;
    private int motors;
    private boolean spin;
    private long duration;


    public Press(String buttonId, int speed, int motors, boolean spin, long duration) {
        this.buttonId = buttonId;
        this.speed = speed;
        this.motors = motors;
        this.spin = spin;
        this.duration = duration;
    }

    public String getButtonId() {
        return buttonId;
    }
    public int getSpeed(){
        return speed;
    }
    public int getMotors(){
        return motors;
    }
    public boolean getSpin(){
        return spin;
    }
    public long getDuration() {
        return duration;
    }

}
