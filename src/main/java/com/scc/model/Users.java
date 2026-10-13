package com.scc.model;


public record Users(int id, String username, String role, String status) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s", id, username, role, status);
    }
}
