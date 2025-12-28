package net.kaoruxun.ncepucore.inspect;

public enum InspectAction {
    PLACE("放置"),
    BREAK("破坏"),
    CONTAINER_OPEN("打开"),
    CONTAINER_CHANGE("容器变更");

    public final String display;

    InspectAction(String display) {
        this.display = display;
    }
}


